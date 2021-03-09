package vafilonov.yampp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

class ServerManager implements Runnable {

    public ServerManager(int port, int nThreads) {
        PORT = port;
        N_THREADS = nThreads;
        executor = Executors.newFixedThreadPool(N_THREADS);
    }

    private final int PORT;
    private final int N_THREADS;

    private volatile boolean isShutdown = false;
    private final ExecutorService executor;
    private ServerSocket server = null;

    @Override
    public void run() {
        final Thread executionThread = Thread.currentThread();
        try (ServerSocket listener = new ServerSocket(PORT);) {
            server = listener;

            while (!isShutdown && !executionThread.isInterrupted()) {
                final Socket client = listener.accept();

                try {
                    executor.submit(new ClientHandler(client));

                } catch (RejectedExecutionException ejectedEx) {
                    client.close();

                }
            }

        } catch (SocketException socketEx) {
            System.out.println("Server manager: socket closed via interruption.");

        } catch (IOException ioEx) {
            System.out.println("Server manager: socket I/O exception.");

        } finally {
            if (!isShutdown) {
                isShutdown = true;
                executor.shutdown();
            }
            System.out.println("Server manager: server shuts down");
        }

    }

    /**
     * Attempts to shutdown server
     * @throws IOException in case of I/O error on socket closing
     */
    public synchronized void shutdown() throws IOException {
        if (isShutdown) {
            return;
        }
        isShutdown = true;

        executor.shutdown();
        System.out.println("Server manager: await termination, 10s left...");

        try {
            boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);
            System.out.println("Server manager:termination successful\n\tTasks left: " + !finished);

        } catch (InterruptedException consumed) {
            System.err.println("Manager shutdown: error awaiting pool termination");

        } finally {
            if (server != null && !server.isClosed()) {
                server.close();
            }
        }
    }

    /**
     * Checks shutdown state
     * @return shutdown state
     */
    public synchronized boolean isShutdown() {   // synchronized?
        return isShutdown;
    }

}
