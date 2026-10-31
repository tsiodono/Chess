package server;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import dataaccess.sql.DatabaseInitializer;
import dataaccess.sql.SqlAuthDAO;
import dataaccess.sql.SqlGameDAO;
import dataaccess.sql.SqlUserDAO;
import service.ClearService;
import service.GameService;
import service.ServiceException;
import service.UserService;
import io.javalin.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        this(new SqlUserDAO(), new SqlAuthDAO(), new SqlGameDAO());

        try {
            DatabaseInitializer.initialize();
        } catch (DataAccessException ex) {
            throw new IllegalStateException(
                    "Unable to initialize the database", ex);
        }
    }
    Server(UserDAO users, AuthDAO auths, GameDAO games) {
        JsonUtil json = new JsonUtil();

        ClearService clearService = new ClearService(users, auths, games);
        UserService userService = new UserService(users, auths);
        GameService gameService = new GameService(auths, games);

        ApiHandler handler = new ApiHandler(clearService, userService, gameService, json);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", handler::clear);
        javalin.post("/user", handler::register);
        javalin.post("/session", handler::login);
        javalin.delete("/session", handler::logout);
        javalin.get("/game", handler::listGames);
        javalin.post("/game", handler::createGame);
        javalin.put("/game", handler::joinGame);

        javalin.exception(ServiceException.class,(ex, ctx) -> json.error(ctx, ex.statusCode(), ex.getMessage()));

        javalin.exception(Exception.class, (ex, ctx) -> json.error(ctx, 500, "internal server error"));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
