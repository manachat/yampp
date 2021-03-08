package vafilonov.yampp.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class ServerManager implements Runnable {

    static final int PORT = 10000;


    @Override
    public void run() {
        ServerSocket listener = null;
        try (listener = new ServerSocket(PORT)) {

        }

    }
}
