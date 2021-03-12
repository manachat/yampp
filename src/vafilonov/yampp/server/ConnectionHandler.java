package vafilonov.yampp.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;


class ConnectionHandler implements Runnable {

    public ConnectionHandler(SocketChannel clientSocket, ExecutorService executor) {
        client = clientSocket;
        this.executor = executor;

    }


    private boolean logged = false;
    private final SocketChannel client;
    private final ExecutorService executor;

    private static final int BUFFER_SIZE = 1024;

    @Override
    public void run() {

        try (client) {

            if (executor.isShutdown() || executor.isTerminated()) {
                return;
            }
            Selector selector = Selector.open();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);

            while (executor.isShutdown() || executor.isTerminated()) {
                selector.select();
                for (SelectionKey t : selector.selectedKeys()) {
                    if (t.isReadable()) {
                        client.read(buf);
                        System.out.println("Server recieved: " + new String(buf.array()));
                        buf.clear();
                        buf.put("Pong answer".getBytes(StandardCharsets.UTF_8));
                        buf.flip();
                        client.write(buf);
                        buf.clear();
                    }
                }


            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }
}
