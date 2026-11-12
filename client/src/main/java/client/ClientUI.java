package client;

import chess.ChessGame;
import model.AuthData;
import model.GameInfo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Console interface for account and pregame actions. */
public final class ClientUI {
    private final ServerFacade server;
    private final Scanner input;
    private final PrintStream out;
    private AuthData session;
    private List<GameInfo> games = List.of();

    public ClientUI(ServerFacade server, Scanner input, PrintStream out) {
        this.server = server;
        this.input = input;
        this.out = out;
    }

    public void run() {
        out.println("Welcome to Chess. Type 'help' to see available commands.");
        boolean running = true;
        while (running) {
            out.print(session == null ? "[signed out] > " : "[" + session.username() + "] > ");
            String line = readLine();
            if (line == null) {
                out.println("Goodbye.");
                break;
            }
            String command = line.trim().toLowerCase();
            try {
                if (command.equals("quit") || command.equals("exit")) {
                    running = false;
                    out.println("Goodbye.");
                } else if (command.equals("help")) {
                    showHelp();
                } else if (session == null) {
                    running = handlePrelogin(command);
                } else {
                    running = handlePostlogin(command);
                }
            } catch (ServerFacade.ApiException ex) {
                out.println(ex.getMessage());
            }
        }
    }
}