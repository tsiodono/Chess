package dataaccess.sql;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.ChessMove;
import dataaccess.AlreadyTakenException;
import dataaccess.NotFoundException;
import model.GameData;

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

    @Test
    void updatePersistsMoves() throws Exception {
        var created = games.create("Practice game");
        var game = created.game();

        game.makeMove(new ChessMove(
                new ChessPosition(2, 5),
                new ChessPosition(4, 5),
                null));

        games.update(new GameData(
                created.gameID(),
                created.whiteUsername(),
                created.blackUsername(),
                created.gameName(),
                game));

        var loaded = new SqlGameDAO().find(created.gameID()).orElseThrow();

        assertNull(loaded.game().getBoard().getPiece(new ChessPosition(2, 5)));

        var e4 = loaded.game().getBoard().getPiece(new ChessPosition(4, 5));
        assertNotNull(e4);
        assertEquals(ChessPiece.PieceType.PAWN, e4.getPieceType());
        assertEquals(ChessGame.TeamColor.BLACK, loaded.game().getTeamTurn());
    }

    @Test
    void updateMissingGameThrowsNotFound() {
        var game = new GameData(99999, null, null, "Missing", new ChessGame());

        assertThrows(NotFoundException.class, () -> games.update(game));
    }

    @Test
    void claimWhiteSeatPersists() throws Exception {
        var created = games.create("Practice game");

        var claimed = games.claimSeat(
                created.gameID(), ChessGame.TeamColor.WHITE, "ana");

        var loaded = new SqlGameDAO().find(created.gameID()).orElseThrow();

        assertEquals("ana", claimed.whiteUsername());
        assertEquals("ana", loaded.whiteUsername());
    }

    @Test
    void claimBlackSeatPersists() throws Exception {
        var created = games.create("Practice game");

        var claimed = games.claimSeat(
                created.gameID(), ChessGame.TeamColor.BLACK, "bob");

        assertEquals("bob", claimed.blackUsername());
        assertEquals(
                "bob",
                new SqlGameDAO().find(created.gameID()).orElseThrow().blackUsername());
    }

    @Test
    void claimOccupiedOrMissingSeatThrows() throws Exception {
        var created = games.create("Practice game");
        games.claimSeat(created.gameID(), ChessGame.TeamColor.WHITE, "ana");

        assertThrows(
                AlreadyTakenException.class,
                () -> games.claimSeat(
                        created.gameID(), ChessGame.TeamColor.WHITE, "bob"));

        assertThrows(
                NotFoundException.class,
                () -> games.claimSeat(
                        99999, ChessGame.TeamColor.WHITE, "bob"));
    }
}