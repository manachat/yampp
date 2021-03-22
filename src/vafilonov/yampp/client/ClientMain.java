package vafilonov.yampp.client;


import vafilonov.yampp.util.Constants;

import java.util.Scanner;

public class ClientMain {

    private static boolean exit = false;

    private static Constants.ClientState state = Constants.ClientState.INITIAL;


    private static OutputService output;

    public static void main(String[] args) {
        final Scanner userInput = new Scanner(System.in);
        output = new OutputService();
        Thread outservice = new Thread(output);
        outservice.setDaemon(true);
        outservice.start();

        output.serviceMessage("Welcome to yampp.\nPrint /help to see list of commands.");
        output.nextLine();

        while (!exit) {
            System.out.print("Enter command: ");
            String input = userInput.nextLine().trim();

            switch (state) {
                case INITIAL:
                    handleInitial(input);
                    break;
                case LOGGEDIN:
                    handleLoggedIn(input);
                    break;
                case DIALOG:
                    handleDialog(input);
                    break;
            }
        }

        System.out.println("Goodbye.");

    }

    private static void handleInitial(String input) {
        if (input.charAt(0) == '/') {
            String[] words = input.split("\\s");
            if (words[0].equals("/login")) {

            } else if (words[0].equals("/signup")) {

            } else if (words[0].equals("/help")) {
                output.serviceMessage("/login [username] - login\n" +
                        "/signup [username - sign up]\n" +
                        "/help - this message");
            } else {
                output.serviceMessage("Unknown command. Print /help to see list of commands.");
            }
        } else {
            output.serviceMessage("Unknown command. Print /help to see list of commands.");
        }
    }

    private static void handleLoggedIn(String input) {

    }

    private static void handleDialog(String input) {

    }
}
