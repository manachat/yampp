package vafilonov.yampp.server;

public class Main {
    public static void main(String[] args) {

        final int port = 10000;
        final int nthreads = 10;
        ServerManager manager = new ServerManager(port, nthreads);

        Thread managerThread = new Thread(manager);

        managerThread.start();
    }
}
