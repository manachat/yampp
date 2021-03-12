package vafilonov.yampp.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;


class ConnectionHandler implements Runnable {



    public ConnectionHandler(SocketChannel clientSocket) {
        client = clientSocket;
    }
    private final SocketChannel client;
    @Override
    public void run() {

        try (client) {
            Selector selector = Selector.open();
            client.configureBlocking(false);

            client.register(selector, SelectionKey.OP_READ);
            ByteBuffer buf = ByteBuffer.allocate(256);


            selector.select();

            for (SelectionKey t : selector.selectedKeys()) {
                if (t.isReadable()) {
                    client.read(buf);
                    System.out.println("Server recieved: " + new String(buf.array()));
                    buf.clear();
                    buf.put("Server answer".getBytes(StandardCharsets.UTF_8));
                    buf.flip();
                    client.write(buf);
                    buf.clear();
                }
            }


            while (true) {
                break;
                // does nothing
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }
}
