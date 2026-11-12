package client;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import model.AuthData;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.GameInfo;
import model.JoinGameRequest;
import model.ListGamesResult;
import model.LoginRequest;
import model.LoginResult;
import model.LogoutRequest;
import model.RegisterRequest;
import model.RegisterResult;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.io.IOException;
import java.net.URI;

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

    public AuthData register(String username, String password, String email) {
        RegisterResult result = send("POST", "/user", new RegisterRequest(username, password, email),
                null, RegisterResult.class);
        return new AuthData(result.authToken(), result.username());
    }

    public AuthData login(String username, String password) {
        try {
            LoginResult result = send("POST", "/session", new LoginRequest(username, password),
                    null, LoginResult.class);
            return new AuthData(result.authToken(), result.username());
        } catch (ApiException ex) {
            if (ex.getMessage().equals("Your session is not valid. Please log in again.")) {
                throw new ApiException("Username or password is incorrect.");
            }
            throw ex;
        }
    }

    public void logout(String authToken) {
        send("DELETE", "/session", new LogoutRequest(authToken), authToken, Void.class);
    }

    public List<GameInfo> listGames(String authToken) {
        ListGamesResult result = send("GET", "/game", null, authToken, ListGamesResult.class);
        return result.games() == null ? List.of() : List.copyOf(result.games());
    }

    public int createGame(String authToken, String gameName) {
        CreateGameResult result = send("POST", "/game", new CreateGameRequest(gameName, authToken),
                authToken, CreateGameResult.class);
        return result.gameID();
    }

    public void joinGame(String authToken, int gameId, ChessGame.TeamColor color) {
        String requestedColor = color == null ? null : color.name();
        send("PUT", "/game", new JoinGameRequest(gameId, requestedColor, authToken),
                authToken, Void.class);
    }

