package vafilonov.yampp.server;

import vafilonov.yampp.Constants;
import vafilonov.yampp.server.userData.User;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;


class ConnectionHandler implements Runnable {

    public ConnectionHandler(SocketChannel clientSocket, ExecutorService executor) {
        client = clientSocket;
        this.executor = executor;

    }


    private boolean logged = false;
    private final SocketChannel client;
    private final ExecutorService executor;
    private User sessionUser;

    private static final int BUFFER_SIZE = 1024;

    @Override
    public void run() {

        try (client; Selector selector = Selector.open()) {

            if (executor.isShutdown() || executor.isTerminated()) {
                return;
            }

            client.configureBlocking(false);
            final SelectionKey networkKey =  client.register(selector, SelectionKey.OP_READ);
            ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);

            Pipe pipa = Pipe.open();
            Pipe.SourceChannel src =  pipa.source();
            src.configureBlocking(false);
            final SelectionKey pipeKey = src.register(selector, SelectionKey.OP_READ);
            selector.close();

            while (executor.isShutdown() || executor.isTerminated()) {
                selector.select();
                for (SelectionKey k : selector.selectedKeys()) {
                    if (k == networkKey) {
                        handleNetMessage(buf);
                    }
                    if (k == pipeKey) {

                    }
                }

            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    private void registerPipe(Dialog dialog) {

    }

    private void handleNetMessage(ByteBuffer buf) throws IOException {
        client.read(buf);
        byte[] chType = new byte[3];
        buf.get(chType, 0, 3);
        String type = new String(chType);
        byte[] chBody = new byte[buf.remaining()];
        buf.get(chBody);
        String body = new String(chBody).trim();
        buf.clear();


        if (!logged) {
            logged = handleLogin(type, body);
        } else {
            client.read(buf);
            System.out.println("Server recieved: " + new String(buf.array()));
            buf.clear();
            buf.put("Pong answer".getBytes(StandardCharsets.UTF_8));
            buf.flip();
            client.write(buf);
            buf.clear();
        }
    }

    private boolean handleLogin(String type, String body) throws IOException {


        if (type.equals(Constants.SIGN_UP_TYPE)) {
            try {
                User created = User.createUser(body);
                sessionUser = created;
            } catch (IllegalArgumentException argumentException) {
                return false;
            }
        } else if (type.equals(Constants.LOGIN_TYPE)) {
            try {

            }
        } else {

        }
    }
}
