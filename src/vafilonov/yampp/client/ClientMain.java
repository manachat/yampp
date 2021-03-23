package vafilonov.yampp.client;


import vafilonov.yampp.util.Constants;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMain {

    private boolean exit = false;

    private Constants.ClientState state = Constants.ClientState.INITIAL;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private NetworkHandler networkHandler;
    private String currentUser;

    static final int PORT = 40000;


    private OutputService output;

    private void start() throws InterruptedException {
        final Scanner userInput = new Scanner(System.in);
        output = new OutputService();
        Thread outservice = new Thread(output);
        outservice.setDaemon(true);
        outservice.start();
        networkHandler = new NetworkHandler(InetAddress.getLoopbackAddress(), PORT, output);
        new Thread(networkHandler).start();
        Thread.sleep(500);

        output.serviceMessage("Welcome to yampp.\nPrint /help to see list of commands.");
        output.nextLine();
        try {
            while (!exit) {
                System.out.print("Enter command: ");
                String input = userInput.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

                switch (networkHandler.getState()) {
                    case INITIAL:
                        handleInitial(input);
                        break;
                    case LOGIN_TRANSIT:
                        handleTransit();
                        break;
                    case LOGGED_IN:
                        handleLoggedIn(input);
                        break;
                    case DIALOG_TRANSIT:
                        handleDialogTransit();
                        break;
                    case DIALOG:
                        handleDialog(input);
                        break;
                }
            }
        } catch (InterruptedException interex) {
            output.serviceMessage("Interrupted.");
            interex.printStackTrace();
        } finally {
            networkHandler.shutdown();
        }

        System.out.println("Goodbye.");
    }

    private void handleInitial(String input) throws InterruptedException {
        if (input.charAt(0) == '/') {
            String[] words = input.split(" ");

            if (words[0].equals("/login")) {
                if (words.length != 2) {
                    output.serviceMessage("Invalid command format.");

                } else {
                    currentUser = words[1];
                    output.serviceMessage("Logging in...");
                    submitMessage(Constants.MessageType.LOGIN,
                            Constants.LOGIN_TYPE + Constants.TOKEN_SEPARATOR + words[1]);
                    Thread.sleep(100);

                }

            } else if (words[0].equals("/signup")) {
                if (words.length != 2) {
                    output.serviceMessage("Invalid command format.");

                } else {
                    currentUser = words[1];
                    output.serviceMessage("Signing up...");
                    submitMessage(Constants.MessageType.SIGN_UP,
                            Constants.SIGN_UP_TYPE + Constants.TOKEN_SEPARATOR + words[1]);
                    Thread.sleep(100);

                }

            } else if (words[0].equals("/exit")) {
                exit = true;
            } else if (words[0].equals("/help")) {
                output.serviceMessage("/login [username] - login\n" +
                        "/signup [username] - sign up\n" +
                        "/exit - exit\n" +
                        "/help - this message");

            } else {
                output.serviceMessage("Unknown command. Print /help to see list of commands.");

            }
        } else {
            output.serviceMessage("Unknown command. Print /help to see list of commands.");

        }
    }

    private void handleTransit() throws InterruptedException {
        output.serviceMessage("Waiting for server respond...");
        Thread.sleep(100);
    }

    private void handleLoggedIn(String input) throws InterruptedException {
        if (input.charAt(0) == '/') {
            String[] words = input.split(" ");

            if (words[0].equals("/dialog")) {
                if (words.length != 2) {
                    output.serviceMessage("Invalid command format.");

                } else {
                    if (networkHandler.lookupUser(words[1])) {
                        submitMessage(Constants.MessageType.INTERNAL,
                                Constants.INTERNAL_TYPE + Constants.TOKEN_SEPARATOR + words[1]);
                    } else {
                        submitMessage(Constants.MessageType.MESSAGE,
                                Constants.MESSAGE_TYPE + Constants.TOKEN_SEPARATOR + currentUser
                                        + Constants.TOKEN_SEPARATOR + words[1] + Constants.TOKEN_SEPARATOR);
                    }
                    Thread.sleep(100);
                }

            } else if (words[0].equals("/logout")) {
                // TODO implement real logout
                exit = true;

            } else if (words[0].equals("/help")) {
                output.serviceMessage("/dialog [username] - go to user dialog\n" +
                        "/logout - log out\n" +
                        "/help - this message");

            } else {
                output.serviceMessage("Unknown command. Print /help to see list of commands.");

            }
        } else {
            output.serviceMessage("Unknown command. Print /help to see list of commands.");

        }
    }

    private void handleDialogTransit() throws InterruptedException {
        output.serviceMessage("Getting dialog info...");
        Thread.sleep(100);
    }

    private void handleDialog(String input) throws InterruptedException {
        if (input.charAt(0) == '/') {
            String[] words = input.split(" ", 2);

            if (words[0].equals("/message")) {
                if (words.length != 2) {
                    output.serviceMessage("Invalid command format.");

                } else {
                    submitMessage(Constants.MessageType.MESSAGE, Constants.MESSAGE_TYPE + Constants.TOKEN_SEPARATOR +
                            currentUser + Constants.TOKEN_SEPARATOR + networkHandler.getCurrentDialog() +
                            Constants.TOKEN_SEPARATOR + words[1]);
                    Thread.sleep(10);

                }

            } else if (words[0].equals("/back")) {
                submitMessage(Constants.MessageType.INTERNAL, Constants.INTERNAL_TYPE);

            } else if (words[0].equals("/help")) {
                output.serviceMessage("/message [message] - send message\n" +
                        "/back - go to dialog choice\n" +
                        "/help - this message");

            } else {
                output.serviceMessage("Unknown command. Print /help to see list of commands.");

            }
        } else {
            output.serviceMessage("Unknown command. Print /help to see list of commands.");

        }
    }

    private void submitMessage(Constants.MessageType type, String message) throws InterruptedException {
        if (networkHandler.isShutdown()) {
            System.out.println("Critical error occured.");
            exit = true;
            return;
        }
        networkHandler.sendMessage(type, message);
    }


    public static void main(String[] args) throws Exception {
        new ClientMain().start();
    }
}
