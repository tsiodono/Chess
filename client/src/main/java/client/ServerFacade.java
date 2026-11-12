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

    public void clear() {
        send("DELETE", "/db", null, null, Void.class);
    }

    private <T> T send(String method, String path, Object body, String authToken, Class<T> responseType) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json");
        if (authToken != null) {
            request.header("authorization", authToken);
        }
        if (body != null) {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(GSON.toJson(body)));
        } else {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        }

        try {
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw apiError(response.statusCode());
            }
            if (responseType == Void.class) {
                return null;
            }
            try {
                return GSON.fromJson(response.body(), responseType);
            } catch (JsonParseException | IllegalStateException ex) {
                throw new ApiException("The server returned an unreadable response.");
            }
        } catch (IOException ex) {
            throw new ApiException("Could not reach the Chess server. Make sure it is running and try again.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException("The request was interrupted. Please try again.");
        }

        private ApiException apiError(int statusCode) {
            return switch (statusCode) {
                case 400 -> new ApiException("That request could not be completed. Check the information and try again.");
                case 401 -> new ApiException("Your session is not valid. Please log in again.");
                case 403 -> new ApiException("That name or player spot is already in use.");
                case 500 -> new ApiException("The Chess server encountered a problem. Please try again later.");
                default -> new ApiException("The Chess server could not complete that request.");
            };
        }

        /** User-safe error message for a failed server request. */
        public static final class ApiException extends RuntimeException {
            public ApiException(String message) {
                super(message);
            }
        }
}

