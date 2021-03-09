package vafilonov.yampp.server;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static boolean exit = false;
    private static boolean startup = false;

    private static ServerManager manager = null;

    public static void main(String[] args) {

        final Scanner userInput = new Scanner(System.in);

        System.out.println("Server manager terminal startup.");

        while (!exit) {
            System.out.print("Enter command: ");
            String command = userInput.nextLine().trim();

            switch (command) {
                case "startup":
                    startupServer();
                    break;
                case "exit":    //  intentional lack of break
                    exit = true;
                case "shutdown":
                    shutdownServer();
                    break;
                case "help":
                    System.out.println("List of available commands:\n" +
                            "\tstartup - start the server\n" +
                            "\tshutdown - shutdown server\n" +
                            "\texit - shutdown & exit\n");
                    break;
                default:
                    System.out.println("Unknown command. Type \"help\" to see list of available commands.");
                    break;
            }
        }

        System.out.println("Goodbye.");


    }

    private static void startupServer() {
        if (!startup) {
            System.out.println("Starting up server...");
            final int port = 10000;
            final int nthreads = 10;
            manager = new ServerManager(port, nthreads);
            startup = true;
            System.out.println("Server started up.");
        } else {
            System.out.println("Server already started.");
        }
    }

    private static void shutdownServer() {
        if (startup && !manager.isShutdown()) {
            try {
                manager.shutdown();
            } catch (IOException consumed) {
                System.out.println("Server manager: server socket I/O exception.");
            }
        } else {
            System.out.println("Server is not running.");
        }
    }
}
