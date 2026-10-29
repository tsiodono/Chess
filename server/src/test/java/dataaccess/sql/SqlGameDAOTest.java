package dataaccess.sql;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlGameDAOTest {
    private final SqlGameDAO games = new SqlGameDAO();

    @BeforeEach
    void setUp() throws Exception {
        DatabaseInitializer.initialize();
        games.clear();
    }

    @Test
    void createAndFindGamePreservesInitialState() throws Exception {
        var created = games.create("Practice game");

        var saved = games.find(created.gameID()).orElseThrow();

        assertTrue(created.gameID() > 0);
        assertEquals("Practice game", saved.gameName());
        assertNull(saved.whiteUsername());
        assertNull(saved.blackUsername());
        assertEquals(ChessGame.TeamColor.WHITE, saved.game().getTeamTurn());

        var a1 = saved.game().getBoard().getPiece(new ChessPosition(1, 1));
        assertNotNull(a1);
        assertEquals(ChessGame.TeamColor.WHITE, a1.getTeamColor());
        assertEquals(ChessPiece.PieceType.ROOK, a1.getPieceType());
    }

    @Test
    void findMissingGameReturnsEmpty() throws Exception {
        assertTrue(games.find(99999).isEmpty());
    }

    @Test
    void listReturnsCreatedGames() throws Exception {
        var first = games.create("First");
        var second = games.create("Second");

        var savedGames = games.list();

        assertEquals(2, savedGames.size());
        assertEquals(first.gameID(), savedGames.get(0).gameID());
        assertEquals(second.gameID(), savedGames.get(1).gameID());
        assertEquals("First", savedGames.get(0).gameName());
        assertEquals("Second", savedGames.get(1).gameName());
    }

    @Test
    void clearRemovesAllGames() throws Exception {
        games.create("First");
        games.create("Second");

        games.clear();

        assertTrue(games.list().isEmpty());
    }
}