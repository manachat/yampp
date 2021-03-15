package vafilonov.yampp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

class ServerManager implements Runnable {

    public ServerManager(int port, int nThreads) {
        PORT = port;
        N_THREADS = nThreads;
        executor = Executors.newFixedThreadPool(N_THREADS);
        supermanager = new Supermanager();
    }

    private final int PORT;
    private final int N_THREADS;

    private volatile boolean isShutdown = false;
    private final ExecutorService executor;
    private ServerSocketChannel server = null;
    private final Supermanager supermanager;

    @Override
    public void run() {
        final Thread executionThread = Thread.currentThread();
        try (ServerSocketChannel listener = ServerSocketChannel.open()) {
            server = listener;
            listener.bind(new InetSocketAddress("localhost", PORT));
            listener.configureBlocking(true);

            while (!isShutdown && !executionThread.isInterrupted()) {
                final SocketChannel client = listener.accept();

                try {
                    executor.submit(new ConnectionHandler(client, supermanager));

                } catch (RejectedExecutionException ejectedEx) {
                    client.close();
                }
            }

        } catch (SocketException socketEx) {
            System.out.println("Server manager: socket closed via interruption.");

        } catch (ClosedChannelException closed) {
            System.out.println("Server manager: manager channel closed");
        } catch (IOException ioEx) {
            System.out.println("Server manager: socket I/O exception.");
            ioEx.printStackTrace();
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
        supermanager.shutdown();
        System.out.println("Server manager: await executor termination, 10s left...");

        try {
            boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);
            System.out.println("Server manager:termination successful\n\tTasks left: " + !finished);

        } catch (InterruptedException consumed) {
            System.err.println("Manager shutdown: error awaiting pool termination");

        } finally {
            if (server != null && server.isOpen()) {
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
