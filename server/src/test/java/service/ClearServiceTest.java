package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.LoginRequest;
import model.RegisterRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static service.ServiceAssertions.assertStatus;

class ClearServiceTest {
    @Test
    void clearRemovesEveryTableAndInvalidatesOldCredentials() throws DataAccessException {
        MemoryUserDAO users = new MemoryUserDAO();
        MemoryAuthDAO auths = new MemoryAuthDAO();
        MemoryGameDAO games = new MemoryGameDAO();
        UserService userService = new UserService(users, auths);
        GameService gameService = new GameService(auths, games);
        String token = userService.register(new RegisterRequest("ana", "pw", "email")).authToken();
        String otherToken = userService.login(new LoginRequest("ana", "pw")).authToken();
        int id = gameService.createGame(new CreateGameRequest("Game", token)).gameID();
        gameService.joinGame(new JoinGameRequest(id, "BLACK", token));

        new ClearService(users, auths, games).clear();

        assertTrue(users.find("ana").isEmpty());
        assertTrue(auths.find(token).isEmpty());
        assertTrue(auths.find(otherToken).isEmpty());
        assertTrue(games.find(id).isEmpty());
        assertTrue(games.list().isEmpty());
        assertStatus(401, () -> userService.login(new LoginRequest("ana", "pw")));
        assertEquals("ana", userService.register(new RegisterRequest("ana", "pw", "email")).username());
    }

    @Test
    void clearCanBeRepeatedOnEmptyStorage() throws DataAccessException {
        MemoryUserDAO users = new MemoryUserDAO();
        MemoryAuthDAO auths = new MemoryAuthDAO();
        MemoryGameDAO games = new MemoryGameDAO();
        ClearService service = new ClearService(users, auths, games);
        service.clear();
        service.clear();
        assertTrue(games.list().isEmpty());
        assertTrue(users.find("missing").isEmpty());
        assertTrue(auths.find("missing").isEmpty());
    }
}
