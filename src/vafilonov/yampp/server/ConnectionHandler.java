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
    private int sessionId = 0;                  //  default "loopback" session

    @Override
    public void run() {

        try (networkChannel; Selector selector = Selector.open()) {

            if (manager.isShutdown()) {
                return;
            }

            networkChannel.configureBlocking(false);
            final SelectionKey networkKey = networkChannel.register(selector, SelectionKey.OP_READ);
            ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);

            while (!manager.isShutdown()) {
                selector.select();
                for (SelectionKey k : selector.selectedKeys()) {
                    if (k.isReadable()) {
                        ZonedDateTime utcArrival =  ZonedDateTime.now(ZoneId.of("UTC"));
                        handleNetMessage(buf, k, utcArrival);
                    } else if (k.isWritable()) {
                        handleNotification();
                        k.interestOps(SelectionKey.OP_READ);    //  reset for read
                    } else {
                        // not possible

                    }
                }

            }

        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } finally {
            manager.unregisterUserThread(sessionId);
        }
    }


    private void handleNotification() {

    }


    /**
     * Process message incoming from net
     * @param buf  byte buffer
     * @param key  selection key of net channel
     * @param timestamp message time
     * @throws IOException Exception in I/O operation of channel
     * @see BasicConnectionHandler
     */
    private void handleNetMessage(ByteBuffer buf, final SelectionKey key, final ZonedDateTime timestamp) throws IOException {

        String message = readMessageFromNetChannel(buf, key);
        String[] tokens = message.split(Constants.TOKEN_SEPARATOR);

        if (!logged) {
            // Invalid argument here signals about logic errors in messages
            String reply = handleAuthentication(Constants.resolveType(tokens[0]), tokens[1]);
            sendMessageThroughNetChannel(reply, key);
            if (logged) {
                manager.registerUserThread(tokens[1], key);
            }
        } else {
            // TODO retrieve message, save to archive and if possible notify handling thread
            MessageType type = Constants.resolveType(tokens[0]);
            String reply = handleClientMessage(type, tokens[1], tokens[2]);
            manager.submitMessage(sessionId, tokens[1], tokens[2], timestamp);
            sendMessageThroughNetChannel(reply, key, timestamp);
        }
    }

    /**
     * Handles text messages from client.
     * @param type message type
     * @param user destination user
     * @param body message contents
     * @return reply message
     */
    private String handleClientMessage(MessageType type, String user, String body) {
        String echoReply;

        if (type == MessageType.SIGNAL) {
            // TODO когда-нибудь пойму что это значит и сделаю
            throw new IllegalArgumentException("Is not yet implemented. ");
        } else if (type == MessageType.MESSAGE) {
            echoReply = Constants.ECHO_TYPE + Constants.TOKEN_SEPARATOR + user + Constants.TOKEN_SEPARATOR + body;

        } else {
            throw new IllegalArgumentException("Invalid message type");
        }
        return echoReply;
    }


    /**
     * Handles login/signup. Checks availability of operation and constructs reply.
     * In case operation is valid, initializes {@link #sessionId} and sets {@link #logged} to true
     * @param type  Type of incoming message
     * @param body  Body of message
     * @return reply message
     * @throws IllegalArgumentException incorrect message type
     */
    private String handleAuthentication(MessageType type, String body) {

        String reply;
        int userId;

        if (type == MessageType.SIGN_UP) {      //  sign up user
            userId = manager.createUser(body);

            if (userId == -1) {
                reply = Constants.ERROR_TYPE + Constants.TOKEN_SEPARATOR +"Authentication failed. User \"" +
                        body + "\" already exists";
                logged = false;
            } else if (userId == -10) {
                reply = Constants.ERROR_TYPE + Constants.TOKEN_SEPARATOR + "Authentication failed. User limit reached.";
                logged = false;
            } else {
                reply = Constants.SIGN_UP_TYPE + Constants.TOKEN_SEPARATOR + "Sign up successful";
                sessionId = userId;
                logged = true;
            }

        } else if (type == MessageType.LOGIN) { //  login user
            userId = manager.getUserId(body);

            if (userId == -1) {
                reply = Constants.ERROR_TYPE + Constants.TOKEN_SEPARATOR + "Authentication failed. User \"" +
                        body + "\" does not exist.";
                logged = false;
            } else {
                reply = Constants.LOGIN_TYPE + Constants.TOKEN_SEPARATOR + "Login successful.";
                logged = true;
            }

        } else {                                //  error
            throw new IllegalArgumentException("Invalid authentication message type.");
        }

        return reply;
    }


}
