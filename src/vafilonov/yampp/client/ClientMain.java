package vafilonov.yampp.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class ClientMain {

    private static boolean exit = false;

    public static void main(String[] args) {

        InetAddress address = InetAddress.getLoopbackAddress();
        int port = 40000;
        PrintWriter writer;
        try (Socket socket = new Socket(address, port)) {
            writer = new PrintWriter(socket.getOutputStream(),true);
            writer.println("Client message");
            Scanner reader = new Scanner(socket.getInputStream());
            String ans = reader.nextLine();
            System.out.println("I got: " + ans);
            System.out.println("Socket is connected: " + socket.isClosed());

        } catch (IOException ioex) {
            System.out.println("Socket IOex");
            ioex.printStackTrace();
        }
        System.out.println("Socket end");
        boolean t = true;
        if (t)
            return;



        final Scanner userInput = new Scanner(System.in);

        System.out.println("Server manager terminal startup.");

        while (!exit) {
            System.out.print("Enter command: ");
            String command = userInput.nextLine().trim();

            switch (command) {
                case "startup":

                    break;
                case "exit":    //  intentional lack of break
                    exit = true;
                    break;
                case "shutdown":

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
}
