package vafilonov.yampp.server;

import vafilonov.yampp.server.userData.DataManager;
import vafilonov.yampp.util.BasicConnectionHandler;
import vafilonov.yampp.util.Constants;
import vafilonov.yampp.util.Constants.MessageType;
import vafilonov.yampp.util.TimedMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;


class ConnectionHandler extends BasicConnectionHandler {


    public ConnectionHandler(SocketChannel clientSocket, DataManager manager) {
        networkChannel = clientSocket;
        this.manager = manager;
    }

    private final SocketChannel networkChannel;

    private boolean logged = false;
    private final DataManager manager;
    private int sessionId = 0;                  //  default "loopback" session
    private String username;

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
                int keynum = selector.select(40000);   //  wait 40s until connection check
                System.err.println("Selected");
                if (!networkChannel.isConnected()) {
                    System.err.println("timed out");
                    break;
                }

                for (SelectionKey k : selector.selectedKeys()) {
                    if (k.isReadable()) {
                        ZonedDateTime utcArrival = ZonedDateTime.now(ZoneId.of("UTC"));
                        System.err.println("selected readable");
                        handleNetMessage(buf, k, utcArrival);

                    } else if (k.isWritable()) {
                        handleNotification(k);

                    } else {
                        // not possible
                        throw new IllegalStateException("State error");
                    }
                }


            }

        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } finally {
            System.err.println("ConHandler finally");
            manager.unregisterUserThread(username);
        }
    }


    private void handleNotification(SelectionKey key) throws IOException {
        TimedMessage ret = manager.retrieveMessage(sessionId);
        while (ret != null) {
            String reply = Constants.MESSAGE_TYPE + Constants.TOKEN_SEPARATOR + username + Constants.TOKEN_SEPARATOR +
                    ret.getMessage();
            sendMessageThroughNetChannel(reply, key, ret.getTime());
            ret = manager.retrieveMessage(sessionId);
        }
        key.interestOps(SelectionKey.OP_READ);
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
        MessageType type = Constants.resolveType(tokens[0]);
        if (type == MessageType.ALIVE) {    //  discard alive message
            return;
        }

        if (!logged) {
            System.err.println("!logged");
            // Invalid argument here signals about logic errors in messages
            String reply = handleAuthentication(Constants.resolveType(tokens[0]), tokens[1]);
            if (logged) {
                if (!manager.registerUserThread(username, key)) {
                    logged = false;
                    reply = Constants.ERROR_TYPE + Constants.TOKEN_SEPARATOR + "Authentication failed. User \"" +
                            tokens[1] + "\" already online.";
                }
            }
            sendMessageThroughNetChannel(reply, key);
        } else {
            // TODO retrieve message, save to archive and if possible notify handling thread
            String reply = handleClientMessage(type, tokens);
            if (type == MessageType.MESSAGE) {
                if (tokens[3].isEmpty()) {
                    /*
                    When client sends MSG with empty body it means he asks server to ask for user existence
                    ECHO reply - yes, ERR reply - no
                    reply body is empty
                     */
                    if (manager.getUserId(tokens[2]) > 0) {
                        reply = Constants.ECHO_TYPE + Constants.TOKEN_SEPARATOR;

                    } else {
                        reply = Constants.ERROR_TYPE + Constants.TOKEN_SEPARATOR +
                                "User \"" + tokens[2] + "\" does not exist";

                    }

                } else if (!manager.submitMessage(sessionId, tokens[1], tokens[2], timestamp)) {
                    reply = Constants.ERROR_TYPE + Constants.TOKEN_SEPARATOR +
                            "User \"" + tokens[2] + "\" does not exist";

                }
            }
            sendMessageThroughNetChannel(reply, key, timestamp);    // TODO должен ли он посылать disconnected?
        }
    }

    /**
     * Handles text messages from client.
     * @param type message type
     * @param tokens message tokens
     * @return reply message
     */
    private String handleClientMessage(MessageType type,  String[] tokens) {
        String echoReply;

        if (type == MessageType.SIGNAL) {
            // TODO когда-нибудь пойму что это значит и сделаю
            echoReply = Constants.ECHO_TYPE + Constants.TOKEN_SEPARATOR + tokens[1] + " disconnected.";
            logged = false;
            username = null;
            sessionId = 0;
            //throw new IllegalArgumentException("Is not yet implemented. ");
        } else if (type == MessageType.MESSAGE) {
            echoReply = Constants.ECHO_TYPE + Constants.TOKEN_SEPARATOR + tokens[2] + Constants.TOKEN_SEPARATOR + tokens[3];

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
        System.err.println("Authentication handle");

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
                reply = Constants.ECHO_TYPE + Constants.TOKEN_SEPARATOR + "Sign up successful";
                sessionId = userId;
                username = body;
                logged = true;
            }

        } else if (type == MessageType.LOGIN) { //  login user
            userId = manager.getUserId(body);

            if (userId == -1) {
                reply = Constants.ERROR_TYPE + Constants.TOKEN_SEPARATOR + "Authentication failed. User \"" +
                        body + "\" does not exist.";
                logged = false;
                System.err.println("Login refused");
            } else {
                reply = Constants.ECHO_TYPE + Constants.TOKEN_SEPARATOR + "Login successful.";
                sessionId = userId;
                username = body;
                logged = true;
            }

        } else {                                //  error
            throw new IllegalArgumentException("Invalid authentication message type.");
        }

        return reply;
    }


}
