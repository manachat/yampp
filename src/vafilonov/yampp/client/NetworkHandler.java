package vafilonov.yampp.client;

import vafilonov.yampp.util.BasicConnectionHandler;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;

class NetworkHandler extends BasicConnectionHandler {


    private final InetAddress address;
    private final int port;

    public NetworkHandler(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        try (SocketChannel channel = SocketChannel.open()) {

        }
    }
}
