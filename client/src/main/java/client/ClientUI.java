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
}