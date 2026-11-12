package client;

import chess.ChessGame;
import model.AuthData;
import model.GameInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    void registerCreatesAndLogsInUser() {
        AuthData auth = facade.register("player1", "secret", "p1@example.com");
        assertEquals("player1", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        facade.register("player1", "secret", "p1@example.com");
        ServerFacade.ApiException error = assertThrows(ServerFacade.ApiException.class,
                () -> facade.register("player1", "other", "other@example.com"));
        assertEquals("That name or player spot is already in use.", error.getMessage());
    }

    @Test
    void loginAcceptsValidCredentials() {
        facade.register("player1", "secret", "p1@example.com");
        AuthData auth = facade.login("player1", "secret");
        assertEquals("player1", auth.username());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void loginRejectsInvalidCredentials() {
        facade.register("player1", "secret", "p1@example.com");
        ServerFacade.ApiException error = assertThrows(ServerFacade.ApiException.class,
                () -> facade.login("player1", "wrong"));
        assertEquals("Username or password is incorrect.", error.getMessage());
    }

    @Test
    void logoutInvalidatesSession() {
        AuthData auth = facade.register("player1", "secret", "p1@example.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
        assertThrows(ServerFacade.ApiException.class, () -> facade.listGames(auth.authToken()));
    }

    @Test
    void logoutRejectsUnknownToken() {
        ServerFacade.ApiException error = assertThrows(ServerFacade.ApiException.class,
                () -> facade.logout("not-a-token"));
        assertEquals("Your session is not valid. Please log in again.", error.getMessage());
    }

    @Test
    void listGamesReturnsGamesWithPlayers() {
        AuthData auth = facade.register("player1", "secret", "p1@example.com");
        int gameId = facade.createGame(auth.authToken(), "Friday match");
        facade.joinGame(auth.authToken(), gameId, ChessGame.TeamColor.WHITE);

        List<GameInfo> games = facade.listGames(auth.authToken());
        assertEquals(1, games.size());
        assertEquals(gameId, games.get(0).gameID());
        assertEquals("Friday match", games.get(0).gameName());
        assertEquals("player1", games.get(0).whiteUsername());
    }

    @Test
    void listGamesRejectsMissingSession() {
        ServerFacade.ApiException error = assertThrows(ServerFacade.ApiException.class,
                () -> facade.listGames(""));
        assertEquals("Your session is not valid. Please log in again.", error.getMessage());
    }

    @Test
    void createGameReturnsInternalIdForClientMapping() {
        AuthData auth = facade.register("player1", "secret", "p1@example.com");
        assertTrue(facade.createGame(auth.authToken(), "Friday match") > 0);
    }

    @Test
    void createGameRejectsBlankName() {
        AuthData auth = facade.register("player1", "secret", "p1@example.com");
        ServerFacade.ApiException error = assertThrows(ServerFacade.ApiException.class,
                () -> facade.createGame(auth.authToken(), " "));
        assertEquals("That request could not be completed. Check the information and try again.",
                error.getMessage());
    }

    @Test
    void createGameRejectsMissingSession() {
        assertThrows(ServerFacade.ApiException.class, () -> facade.createGame("", "Friday match"));
    }

}
