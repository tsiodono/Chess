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

    private boolean handlePrelogin(String command) {
        switch (command) {
            case "login" -> login();
            case "register" -> register();
            case "" -> { }
            default -> out.println("Unknown command. Type 'help' to see available commands.");
        }
        return true;
    }

    private boolean handlePostlogin(String command) {
        switch (command) {
            case "logout" -> logout();
            case "create", "create game" -> createGame();
            case "list", "list games" -> listGames();
            case "play", "play game" -> playGame();
            case "observe", "observe game" -> observeGame();
            case "" -> { }
            default -> out.println("Unknown command. Type 'help' to see available commands.");
        }
        return true;
    }
    private void login() {
        String username = prompt("Username: ");
        String password = prompt("Password: ");
        if (username == null || password == null) return;
        session = server.login(username, password);
        games = List.of();
        out.println("Welcome back, " + session.username() + ".");
    }

    private void register() {
        String username = prompt("Username: ");
        String password = prompt("Password: ");
        String email = prompt("Email: ");
        if (username == null || password == null || email == null) return;
        session = server.register(username, password, email);
        games = List.of();
        out.println("Account created. Welcome, " + session.username() + ".");
    }

    private void logout() {
        server.logout(session.authToken());
        session = null;
        games = List.of();
        out.println("You have logged out.");
    }

    private void createGame() {
        String name = prompt("Game name: ");
        if (name == null) return;
        server.createGame(session.authToken(), name);
        games = List.of();
        out.println("Game created. Use 'list' to see it and choose a side.");
    }

    private void listGames() {
        games = new ArrayList<>(server.listGames(session.authToken()));
        if (games.isEmpty()) {
            out.println("There are no games yet. Use 'create' to start one.");
            return;
        }
        out.println("Games:");
        for (int index = 0; index < games.size(); index++) {
            GameInfo game = games.get(index);
            out.printf("%d. %s — White: %s | Black: %s%n", index + 1, game.gameName(),
                    playerName(game.whiteUsername()), playerName(game.blackUsername()));
        }
    }

    private void playGame() {
        GameInfo game = chooseGame();
        if (game == null) return;
        String colorInput = prompt("Choose a color (white or black): ");
        if (colorInput == null) return;
        ChessGame.TeamColor color = parseColor(colorInput);
        if (color == null) {
            out.println("Please enter 'white' or 'black'.");
            return;
        }
        server.joinGame(session.authToken(), game.gameID(), color);
        out.println("Joined " + game.gameName() + " as " + color.name().toLowerCase() + ".");
        BoardPrinter.drawInitialBoard(color, out);
        out.println("Gameplay commands will be available in a later phase.");
    }

    private void observeGame() {
        GameInfo game = chooseGame();
        if (game == null) return;
        out.println("Observing " + game.gameName() + " from White's perspective.");
        BoardPrinter.drawInitialBoard(ChessGame.TeamColor.WHITE, out);
        out.println("Live observation will be available in a later phase.");
    }

    private GameInfo chooseGame() {
        if (games.isEmpty()) listGames();
        if (games.isEmpty()) return null;
        String selection = prompt("Game number: ");
        if (selection == null) return null;
        try {
            int number = Integer.parseInt(selection.trim());
            if (number < 1 || number > games.size()) {
                out.println("Choose a number from the game list.");
                return null;
            }
            return games.get(number - 1);
        } catch (NumberFormatException ex) {
            out.println("Enter the game's number from the list.");
            return null;
        }
    }

    private void showHelp() {
        if (session == null) {
            out.println("Commands: help, login, register, quit");
        } else {
            out.println("Commands: help, create, list, play, observe, logout, quit");
            out.println("Play joins a game as White or Black. Observe shows its board from White's side.");
        }
    }

    private String prompt(String label) {
        out.print(label);
        return readLine();
    }

    private String readLine() {
        return input.hasNextLine() ? input.nextLine() : null;
    }

    private static String playerName(String username) {
        return username == null || username.isBlank() ? "Open" : username;
    }

    private static ChessGame.TeamColor parseColor(String value) {
        return switch (value.trim().toLowerCase()) {
            case "white" -> ChessGame.TeamColor.WHITE;
            case "black" -> ChessGame.TeamColor.BLACK;
            default -> null;
        };
    }
}