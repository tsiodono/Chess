package server;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.ListGamesRequest;
import model.LoginRequest;
import model.LogoutRequest;
import model.RegisterRequest;
import service.ClearService;
import service.GameService;
import service.UserService;
import java.util.Map;

/** Translates the seven HTTP endpoints into service requests. */
final class ApiHandler {
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    private final JsonUtil json;

    ApiHandler(ClearService clearService, UserService userService, GameService gameService, JsonUtil json) {
        this.clearService = clearService;
        this.userService = userService;
        this.gameService = gameService;
        this.json = json;
    }

    void clear(Context ctx) throws DataAccessException {
        clearService.clear();
        json.write(ctx, Map.of());
    }

    void register(Context ctx) throws DataAccessException {
        json.write(ctx, userService.register(json.parse(ctx, RegisterRequest.class)));
    }

    void login(Context ctx) throws DataAccessException {
        json.write(ctx, userService.login(json.parse(ctx, LoginRequest.class)));
    }

    void logout(Context ctx) throws DataAccessException {
        userService.logout(new LogoutRequest(ctx.header("authorization")));
        json.write(ctx, Map.of());
    }

    void listGames(Context ctx) throws DataAccessException {
        json.write(ctx, gameService.listGames(new ListGamesRequest(ctx.header("authorization"))));
    }

    void createGame(Context ctx) throws DataAccessException {
        CreateGameRequest body = json.parse(ctx, CreateGameRequest.class);
        CreateGameRequest request = new CreateGameRequest(body.gameName(), ctx.header("authorization"));
        json.write(ctx, gameService.createGame(request));
    }

    void joinGame(Context ctx) throws DataAccessException {
        JoinGameRequest body = json.parse(ctx, JoinGameRequest.class);
        JoinGameRequest request = new JoinGameRequest(body.gameID(), body.playerColor(), ctx.header("authorization"));
        gameService.joinGame(request);
        json.write(ctx, Map.of());
    }
}
