package service;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import model.CreateGameRequest;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static service.ServiceAssertions.assertStatus;

class GameServiceTest {
    private final MemoryAuthDAO auths = new MemoryAuthDAO();
    private final MemoryGameDAO games = new MemoryGameDAO();
    private final GameService service = new GameService(auths, games);

    @BeforeEach
    void authorizeUsers() throws DataAccessException {
        auths.create(new AuthData("t1", "ana"));
        auths.create(new AuthData("t2", "ben"));
    }

    @Test
    void listGamesReturnsEmptyList() throws DataAccessException {
        assertEquals(List.of(), service.listGames(new ListGamesRequest("t1")).games());
    }

    @Test
    void listGamesReturnsAllNamesAndPlayers() throws DataAccessException {
        int first = create("First");
        int second = create("Second");
        service.joinGame(new JoinGameRequest(first, "WHITE", "t1"));
        service.joinGame(new JoinGameRequest(second, "BLACK", "t2"));
        var result = service.listGames(new ListGamesRequest("t1")).games();
        assertEquals(2, result.size());
        assertEquals("First", result.get(0).gameName());
        assertEquals("ana", result.get(0).whiteUsername());
        assertNull(result.get(0).blackUsername());
        assertEquals("ben", result.get(1).blackUsername());
    }

    @Test
    void listGamesRejectsInvalidAuth() {
        assertStatus(401, () -> service.listGames(null));
        for (String token : new String[]{null, "", " ", "unknown"}) {
            assertStatus(401, () -> service.listGames(new ListGamesRequest(token)));
        }
    }

    @Test
    void createGameStoresInitialBoardAndUniqueId() throws DataAccessException {
        int first = create("Finals");
        int second = create("Practice");
        assertTrue(first > 0);
        assertNotEquals(first, second);
        GameData saved = games.find(first).orElseThrow();
        assertEquals("Finals", saved.gameName());
        assertNull(saved.whiteUsername());
        assertNull(saved.blackUsername());
        assertEquals(ChessGame.TeamColor.WHITE, saved.game().getTeamTurn());
        assertEquals(ChessPiece.PieceType.KING,
                saved.game().getBoard().getPiece(new ChessPosition(1, 5)).getPieceType());
    }

    @Test
    void createGameRejectsMissingOrBlankNames() {
        for (String name : new String[]{null, "", " "}) {
            assertStatus(400, () -> service.createGame(new CreateGameRequest(name, "t1")));
        }
        assertTrue(games.list().isEmpty());
    }

    @Test
    void createGameRejectsInvalidAuth() {
        assertStatus(401, () -> service.createGame(null));
        for (String token : new String[]{null, "", "unknown"}) {
            assertStatus(401, () -> service.createGame(new CreateGameRequest("Game", token)));
        }
        assertTrue(games.list().isEmpty());
    }

    @Test
    void joinGameAsWhitePreservesBlackSeat() throws DataAccessException {
        int id = create("Game");
        service.joinGame(new JoinGameRequest(id, "WHITE", "t1"));
        assertEquals("ana", games.find(id).orElseThrow().whiteUsername());
        assertNull(games.find(id).orElseThrow().blackUsername());
    }

    @Test
    void joinGameAsBlackPreservesWhiteSeat() throws DataAccessException {
        int id = create("Game");
        service.joinGame(new JoinGameRequest(id, "WHITE", "t1"));
        service.joinGame(new JoinGameRequest(id, "BLACK", "t2"));
        assertEquals("ana", games.find(id).orElseThrow().whiteUsername());
        assertEquals("ben", games.find(id).orElseThrow().blackUsername());
    }

    @Test
    void oneUserMayOccupyBothDifferentColors() throws DataAccessException {
        int id = create("Self play");
        service.joinGame(new JoinGameRequest(id, "WHITE", "t1"));
        service.joinGame(new JoinGameRequest(id, "BLACK", "t1"));
        assertEquals("ana", games.find(id).orElseThrow().whiteUsername());
        assertEquals("ana", games.find(id).orElseThrow().blackUsername());
    }

    @Test
    void joinGameRejectsOccupiedColor() throws DataAccessException {
        int id = create("Game");
        service.joinGame(new JoinGameRequest(id, "WHITE", "t1"));
        assertStatus(403, () -> service.joinGame(new JoinGameRequest(id, "WHITE", "t2")));
        assertStatus(403, () -> service.joinGame(new JoinGameRequest(id, "WHITE", "t1")));
        assertEquals("ana", games.find(id).orElseThrow().whiteUsername());
        assertNull(games.find(id).orElseThrow().blackUsername());
    }

    @Test
    void joinGameRejectsInvalidColorAndId() throws DataAccessException {
        int id = create("Game");
        for (String color : new String[]{null, "", "GREEN", "white"}) {
            assertStatus(400, () -> service.joinGame(new JoinGameRequest(id, color, "t1")));
        }
        for (Integer invalid : new Integer[]{null, 0, -1, 999}) {
            assertStatus(400, () -> service.joinGame(new JoinGameRequest(invalid, "WHITE", "t1")));
        }
        assertNull(games.find(id).orElseThrow().whiteUsername());
    }

    @Test
    void joinGameRejectsInvalidAuth() throws DataAccessException {
        int id = create("Game");
        assertStatus(401, () -> service.joinGame(null));
        for (String token : new String[]{null, "", "unknown"}) {
            assertStatus(401, () -> service.joinGame(new JoinGameRequest(id, "WHITE", token)));
        }
        assertNull(games.find(id).orElseThrow().whiteUsername());
    }

    private int create(String name) throws DataAccessException {
        return service.createGame(new CreateGameRequest(name, "t1")).gameID();
    }
}
