package vafilonov.yampp.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;

class ClientHandler implements Runnable {

    public ClientHandler(Socket clientSocket) {
        client = clientSocket;
    }
    private final Socket client;

    @Override
    public void run() {


        try (Socket socket = client) {
            while (true) {
                break;
                // does nothing
            }
        } catch (IOException ioEx) {

        }
    }
}
