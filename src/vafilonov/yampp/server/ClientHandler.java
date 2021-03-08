package vafilonov.yampp.server;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    public ClientHandler(Socket clientSocket) {
        client = clientSocket;
    }
    private Socket client;

    @Override
    public void run() {
        try (Socket socket = client) {
            while () {

            }
        } catch (IOException ioEx) {

        }
    }
}
