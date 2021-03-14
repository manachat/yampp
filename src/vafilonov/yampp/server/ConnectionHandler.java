package vafilonov.yampp.server;

import vafilonov.yampp.util.ByteAccumulator;
import vafilonov.yampp.util.Constants;
import vafilonov.yampp.util.Constants.MessageType;
import vafilonov.yampp.server.userData.User;

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


            while (executor.isShutdown() || executor.isTerminated()) {
                selector.select();
                for (SelectionKey k : selector.selectedKeys()) {
                    if (k.isReadable()) {
                        handleNetMessage(buf);
                    } else if (k.isWritable()) {

                    }

                }

            }

        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }


    private String readMessageFromChannel(ByteBuffer buf, SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        int read = channel.read(buf);
        final ByteAccumulator accumulator = new ByteAccumulator(read);

        while (read != -1) {
            buf.flip();
            accumulator.append(buf.array(), buf.remaining());
            buf.clear();
            read = channel.read(buf);
        }

        return new String(accumulator.array(), 0, accumulator.getSize(), StandardCharsets.UTF_8);
    }


    private void handleNetMessage(ByteBuffer buf, SelectionKey key) throws IOException {

        String message = readMessageFromChannel(buf, key);
        String[] tokens = message.split("\0");


        if (!logged) {
            logged = handleAuthentication(Constants.resolveType(tokens[0]), tokens[1]);
        } else {
            // TODO retrieve message, save to archive and if possible notify handling thread
        }
    }


    /**
     * Handles login/signup. Checks availability of operation and sends reply.
     * @param type
     * @param body
     * @return
     * @throws IOException
     */
    private boolean handleAuthentication(MessageType type, String body) throws IOException {

        if (type == MessageType.SIGN_UP) {
            try {
                User created = User.createUser(body);
                sessionUser = created;
            } catch (IllegalArgumentException argumentException) {
                return false;
            }
        } else if (type == MessageType.LOGIN) {
            try {

            }
        } else {

        }
        //TODO тут везде жопа, исправитб
        return true;
    }
}
