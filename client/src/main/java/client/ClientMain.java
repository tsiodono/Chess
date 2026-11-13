package client;

import java.util.Scanner;

public final class ClientMain {
    private static final int DEFAULT_PORT = 8080;

    private ClientMain() { }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 1) {
            System.out.println("Usage: ClientMain [server-port]");
            return;
        }

        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                System.out.println("Please provide a valid server port between 1 and 65535.");
                return;
            }
        }

        ServerFacade server = new ServerFacade(port);
        ClientUI ui = new ClientUI(server, new Scanner(System.in), System.out);
        ui.run();
    }
}