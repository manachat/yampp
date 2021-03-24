package vafilonov.yampp.client;

import vafilonov.yampp.util.BasicConnectionHandler;
import vafilonov.yampp.util.Constants;
import vafilonov.yampp.util.Constants.ClientState;
import vafilonov.yampp.util.TimedMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

class NetworkHandler extends BasicConnectionHandler {


    private final InetAddress address;
    private final int port;
    private final OutputService output;
    private Selector networkSelector;
    private SelectionKey channelKey;
    private ClientState state = ClientState.INITIAL;
    private String currentDialog;
    private String currentUser;

    private volatile boolean shutdown = false;
    private final BlockingQueue<TypedMessage> clientMessages = new ArrayBlockingQueue<>(2);
    private final List<String> userCache = new ArrayList<>();

    public NetworkHandler(InetAddress address, int port, OutputService output) {
        this.address = address;
        this.port = port;
        this.output = output;
    }

    @Override
    public void run() {
        try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(address, port));
             Selector selector = Selector.open()) {
            networkSelector = selector;
            channel.configureBlocking(false);
            channelKey = channel.register(selector, SelectionKey.OP_READ);
            ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);

            while(!shutdown) {
                selector.select();

                Set<SelectionKey> selectedSet = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedSet.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    if (key.isReadable()) {
                        handleServerMessage(buf, key);
                    } else if (key.isWritable()) {
                        handleClientMessage(key);
                        key.interestOps(OP_READ);
                    } else {
                        throw new IllegalStateException("State error");
                    }
                    iter.remove();


                }
                /*
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isReadable()) {
                        System.err.println("NetworkHandler::readable_key");
                        handleServerMessage(buf, key);
                    } else if (key.isWritable()) {
                        System.err.println("NetworkHandler::writable_key");
                        handleClientMessage(key);
                        key.interestOps(OP_READ);
                    } else {
                        throw new IllegalStateException("State error");
                    }
                }

                 */

            }


        } catch(InterruptedException interEx) {
            output.serviceMessage("Interrupted exception");
            interEx.printStackTrace();
        } catch(ClosedSelectorException closeEx) {
            output.serviceMessage("Connection to server closed.");
        } catch (IOException ioEx) {
            output.serviceMessage("Network handler IO Exception.");
            ioEx.printStackTrace();
        } finally {
            shutdown = true;
        }
    }

    private void handleServerMessage(ByteBuffer buf, SelectionKey key) throws IOException {
        String message = readMessageFromNetChannel(buf, key);
        if (message == null) {
            throw new IOException("zero read");
        }

        switch (state) {
            case INITIAL:
                processServerInitial(message);
                break;
            case LOGIN_TRANSIT:
                processServerLoginTransit(message);
                break;
            case LOGGED_IN:
                processServerLogged(message);
                break;
            case DIALOG_TRANSIT:
                processServerDialogTransit(message);
                break;
            case DIALOG:
                processServerDialog(message);
                break;
        }

    }

    private void processServerInitial(String message) {
        //throw new IllegalStateException("Illegal server initial state");    //  server does not send in initial
    }

    private void processServerLoginTransit(String message) {
        String[] tokens = message.split(Constants.TOKEN_SEPARATOR);

        if (tokens[0].equals(Constants.ERROR_TYPE)) {
            synchronized (this) {
                state = ClientState.INITIAL;
            }
            output.serviceMessage(tokens[1]);

        } else if (tokens[0].equals(Constants.ECHO_TYPE)) {
            synchronized (this) {
                state = ClientState.LOGGED_IN;
            }
            output.serviceMessage(tokens[1]);

        } else {
            //throw new IllegalStateException("Illegal server login transit state");
        }

    }

    /**
     * Ignore messages from other users outside dialog
     * (in ideal world it should be stored in session)
     * @param message discarded
     */
    private void processServerLogged(String message) {
        String[] tokens = message.split(Constants.TOKEN_SEPARATOR);
        if (tokens[0].equals(Constants.MESSAGE_TYPE)) {
            // discard
            // TODO save to session records
        } else {
            //throw new IllegalStateException("Illegal server logged state.");
        }

    }

    private void processServerDialogTransit(String message) {
        String[] tokens = message.split(Constants.TOKEN_SEPARATOR);
        if (tokens[0].equals(Constants.ECHO_TYPE)) {
            synchronized (this) {
                state = ClientState.DIALOG;
            }

        } else if (tokens[0].equals(Constants.ERROR_TYPE)) {
            synchronized (this) {
                state = ClientState.LOGGED_IN;
            }
            output.serviceMessage(tokens[1]);

        } else if (tokens[0].equals(Constants.MESSAGE_TYPE)) {
            // discard
            // TODO save
        } else {
            //throw new IllegalStateException("Illegal server dialog transit state.");
        }
    }

    private void processServerDialog(String message) {
        String[] tokens = message.split(Constants.TOKEN_SEPARATOR);
        if (tokens[0].equals(Constants.MESSAGE_TYPE)) {
            output.submitMessage(new TimedMessage(tokens[1] + ": " + tokens[3], ZonedDateTime.parse(tokens[4])));
        } else if (tokens[0].equals(Constants.ECHO_TYPE)) {
            output.submitMessage(new TimedMessage(tokens[1] + ": " + tokens[3], ZonedDateTime.parse(tokens[4])));
        } else {
            throw new IllegalStateException("Illegal server dialog state.");
        }
    }

    private void handleClientMessage(SelectionKey key) throws InterruptedException, IOException {
        TypedMessage msg = clientMessages.take();

        switch (state) {

            case INITIAL:
                processClientInitial(msg);
                break;
            case LOGIN_TRANSIT:
                processClientLoginTransit(msg);
                break;
            case LOGGED_IN:
                processClientLogged(msg);
                break;
            case DIALOG_TRANSIT:
                processClientDialogTransit(msg);
                break;
            case DIALOG:
                processClientDialog(msg);
                break;

        }

        key.interestOps(SelectionKey.OP_READ);
    }

    private void processClientInitial(TypedMessage msg) throws IOException {

        if (msg.type == Constants.MessageType.SIGN_UP || msg.type == Constants.MessageType.LOGIN) {
            synchronized (this) {
                state = ClientState.LOGIN_TRANSIT;
            }

            sendMessageThroughNetChannel(msg.message, channelKey);

        } else {
            throw new IllegalStateException("Illegal client initial state.");
        }
    }

    private void processClientLoginTransit(TypedMessage msg) {
        throw new IllegalStateException("Illegal client login transit state.");
    }

    private void processClientLogged(TypedMessage msg) throws IOException {
        if (msg.type == Constants.MessageType.INTERNAL) {
            synchronized (this) {
                state = ClientState.DIALOG;
            }
            currentDialog = msg.message.split(Constants.TOKEN_SEPARATOR)[2];

        } else if (msg.type == Constants.MessageType.MESSAGE) {
            synchronized (this) {
                state = ClientState.DIALOG_TRANSIT;
            }
            currentDialog = msg.message.split(Constants.TOKEN_SEPARATOR)[2];
            sendMessageThroughNetChannel(msg.message, channelKey);

        } else {
            throw new IllegalStateException("Illegal client logged srtate.");
        }
    }

    private void processClientDialogTransit(TypedMessage msg) {
        throw  new IllegalStateException("Illegal client dialog transit state.");
    }

    private void processClientDialog(TypedMessage msg) throws IOException {
        if (msg.type == Constants.MessageType.MESSAGE) {
            sendMessageThroughNetChannel(msg.message, channelKey);
        } else if (msg.type == Constants.MessageType.INTERNAL) {
            synchronized (this) {
                state = ClientState.LOGGED_IN;
            }

        } else {
            throw new IllegalStateException("Illegal client dialog state.");
        }
    }

    public void sendMessage(Constants.MessageType type, String message) throws InterruptedException {
        clientMessages.put(new TypedMessage(type, message));
        channelKey.interestOps(OP_WRITE);
        networkSelector.wakeup();

    }

    public synchronized ClientState getState() {
        return state;
    }

    public void shutdown() throws IOException {
        shutdown = true;

        if (networkSelector != null) {
            networkSelector.close();
        }



    }

    public boolean isShutdown() {
        return shutdown;
    }

    public boolean lookupUser(String username) {
        return userCache.contains(username);
    }

    public String getCurrentDialog() {
        return currentDialog;
    }

    private static class TypedMessage {
        private final Constants.MessageType type;
        private final String message;

        private TypedMessage(Constants.MessageType type, String message) {
            this.type = type;
            this.message = message;
        }
    }
}
