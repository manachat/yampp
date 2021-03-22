package vafilonov.yampp.client;

import vafilonov.yampp.util.BasicConnectionHandler;
import vafilonov.yampp.util.Constants;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

class NetworkHandler extends BasicConnectionHandler {


    private final InetAddress address;
    private final int port;
    private final OutputService output;
    private Selector networkSelector;


    private volatile boolean shutdown = false;

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
            SelectionKey registerKey = channel.register(selector, SelectionKey.OP_READ);
            ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);s

            while(!shutdown) {
                int keyNum = selector.select(60000);     //  send alive message every 60s
                if (keyNum == 0) {
                    sendMessageThroughNetChannel(Constants.ALIVE_TYPE, registerKey);
                } else {
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (key.isReadable()) {

                        } else if (key.isWritable()) {

                        } else {
                            throw new IllegalStateException("State error");
                        }
                    }
                }
            }


        } catch(ClosedSelectorException closeEx) {
            output.serviceMessage("Connection to server closed.");
        } catch (IOException ioEx) {
            output.serviceMessage("Network handler IO Exception.");
        }
    }

    private void handleServerMessage() {

    }

    public void sendMessage(String message) {

    }

    public void shutdown() throws IOException {
        if (networkSelector != null && networkSelector.isOpen()) {
            networkSelector.close();
        }

    }

    public boolean isShutdown() {
        return shutdown;
    }
}
