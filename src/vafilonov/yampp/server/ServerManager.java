package vafilonov.yampp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerManager implements Runnable {



    public ServerManager(int port, int nThreads) {
        PORT = port;
        N_THREADS = nThreads;
        executor = Executors.newFixedThreadPool(N_THREADS);
    }

    private final int PORT;
    private final int N_THREADS;

    private Thread executionThread = null;
    private volatile boolean isShutdown = false;
    private final ExecutorService executor;

    @Override
    public void run() {
        executionThread = Thread.currentThread();

        try (ServerSocket listener = new ServerSocket(PORT);) {
            while (!isShutdown && !executionThread.isInterrupted()) {
                final Socket client = listener.accept();
                executor.execute(new ClientHandler(client));
            }
        } catch (IOException ioEx) {

        }

    }

    public void shutdown() {
        isShutdown = true;
        executor.shutdown();
        if (executionThread != null) {
            executionThread.interrupt();
        }
    }
}
