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
    public ServerFacade(String host, int port) {
        if (host == null || host.isBlank() || port < 0 || port > 65535) {
            throw new IllegalArgumentException("A valid server host and port are required.");
        }
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        this.baseUrl = "http://" + host + ":" + port;
    }
}
