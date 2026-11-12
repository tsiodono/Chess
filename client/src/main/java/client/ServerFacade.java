package client;

import chess.ChessGame;
import com.google.gson.Gson;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private static final Gson GSON = new Gson();
    private final HttpClient client;
    private final String baseUrl;

    public ServerFacade(int port) {
        this("localhost", port);
    }
}
