package vafilonov.yampp.server;

import vafilonov.yampp.util.BasicConnectionHandler;
import vafilonov.yampp.util.Constants;
import vafilonov.yampp.util.Constants.MessageType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;


class ConnectionHandler extends BasicConnectionHandler {


    public ConnectionHandler(SocketChannel clientSocket, Supermanager manager) {
        networkChannel = clientSocket;
        this.manager = manager;
    }

    private final SocketChannel networkChannel;

    private boolean logged = false;
    private final Supermanager manager;
    private int sessionId;

    @Override
    public void run() {

        try (networkChannel; Selector selector = Selector.open()) {

            if (manager.isShutdown()) {
                return;
            }

            networkChannel.configureBlocking(false);
            final SelectionKey networkKey =  networkChannel.register(selector, SelectionKey.OP_READ);
            ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);


            while (!manager.isShutdown()) {
                selector.select();
                for (SelectionKey k : selector.selectedKeys()) {
                    if (k.isReadable()) {
                        ZonedDateTime utcArrival =  ZonedDateTime.now(ZoneId.of("UTC"));
                        handleNetMessage(buf, k, utcArrival);
                    } else if (k.isWritable()) {

                    }

                }

            }

        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }





    private void handleNetMessage(ByteBuffer buf, final SelectionKey key, final ZonedDateTime timestamp) throws IOException {

        String message = readMessageFromNetChannel(buf, key);
        String[] tokens = message.split("\0");


        if (!logged) {
            String reply = handleAuthentication(Constants.resolveType(tokens[0]), tokens[1]);
            sendMessageThroughNetChannel(reply, key);
        } else {
            // TODO retrieve message, save to archive and if possible notify handling thread
        }
    }


    /**
     * Handles login/signup. Checks availability of operation and sends reply.
     * @param type
     * @param body
     * @return reply message
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private String handleAuthentication(MessageType type, String body) throws IOException {

        if (type == MessageType.SIGN_UP) {
            int userId = manager.createUser(body);
            String reply;
            if (userId == -1) {
                reply = Constants.ERROR_TYPE + "\0Authentication failed. User \"" + body + "\" already exists";
                logged = false;
            } else if (userId == -10) {
                reply = Constants.ERROR_TYPE + "\0Authentication failed. User limit reached.";
                logged = false;
            } else {
                reply = Constants.SIGN_UP_TYPE + "\0Sign up successful";
                sessionId = userId;
                logged = true;
            }

            return reply;
        } else if (type == MessageType.LOGIN) {

            try {

            }
        } else {
            throw new IllegalArgumentException("Invalid authentication message type.");
        }
        //TODO тут везде жопа, исправитб
        return true;
    }


}
