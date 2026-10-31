package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dataaccess.AlreadyTakenException;
import dataaccess.NotFoundException;
import java.sql.Connection;

public final class SqlGameDAO implements GameDAO {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    @Override
    public GameData create(String gameName) throws DataAccessException {
        String sql = """
                INSERT INTO games
                    (white_username, black_username, game_name, game_json)
                VALUES (?, ?, ?, ?)
                """;
        var game = new ChessGame();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setNull(1, java.sql.Types.VARCHAR);
            statement.setNull(2, java.sql.Types.VARCHAR);
            statement.setString(3, gameName);
            statement.setString(4, GSON.toJson(game));
            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException(
                            "Creating game did not return an ID");
                }

                return new GameData(
                        keys.getInt(1), null, null, gameName, game);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to create game", ex);
        }
    }

    @Override
    public Optional<GameData> find(int gameID) throws DataAccessException {
        String sql = """
                SELECT game_id, white_username, black_username,
                       game_name, game_json
                FROM games
                WHERE game_id = ?
                """;

        try (var connection = DatabaseManager.getConnection()) {
            return findGame(connection, gameID);
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to find game", ex);
        }
    }

    @Override
    public List<GameData> list() throws DataAccessException {
        String sql = """
                SELECT game_id, white_username, black_username,
                       game_name, game_json
                FROM games
                ORDER BY game_id
                """;
        List<GameData> games = new ArrayList<>();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);
             var results = statement.executeQuery()) {
            while (results.next()) {
                games.add(readGame(results));
            }
            return games;
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to list games", ex);
        }
    }

    @Override
    public void update(GameData game) throws DataAccessException {
        String sql = """
            UPDATE games
            SET white_username = ?, black_username = ?,
                game_name = ?, game_json = ?
            WHERE game_id = ?
            """;

        int changedRows;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, GSON.toJson(game.game()));
            statement.setInt(5, game.gameID());
            changedRows = statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to update game", ex);
        }

        // MySQL may report zero changed rows when the stored values were already
        // identical, so check whether the game exists before calling it missing.
        if (changedRows == 0 && find(game.gameID()).isEmpty()) {
            throw new NotFoundException("Game does not exist");
        }
    }

    private Optional<GameData> findGame(Connection connection, int gameID)
            throws SQLException, DataAccessException {
        String sql = """
            SELECT game_id, white_username, black_username,
                   game_name, game_json
            FROM games
            WHERE game_id = ?
            """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, gameID);

            try (var results = statement.executeQuery()) {
                if (!results.next()) {
                    return Optional.empty();
                }
                return Optional.of(readGame(results));
            }
        }
    }

    @Override
    public GameData claimSeat(
            int gameID, ChessGame.TeamColor color, String username)
            throws DataAccessException {
        if (color == null || username == null) {
            throw new DataAccessException("Color and username are required");
        }

        // These column names come from the enum, not from user-provided SQL.
        String column = color == ChessGame.TeamColor.WHITE
                ? "white_username"
                : "black_username";

        String updateSql = "UPDATE games SET " + column
                + " = ? WHERE game_id = ? AND " + column + " IS NULL";

        try (var connection = DatabaseManager.getConnection()) {
            try (var statement = connection.prepareStatement(updateSql)) {
                statement.setString(1, username);
                statement.setInt(2, gameID);

                if (statement.executeUpdate() == 0) {
                    if (findGame(connection, gameID).isEmpty()) {
                        throw new NotFoundException("Game does not exist");
                    }
                    throw new AlreadyTakenException("Player seat already taken");
                }
            }

            return findGame(connection, gameID)
                    .orElseThrow(() -> new NotFoundException("Game does not exist"));
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to claim player seat", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to clear games", ex);
        }
    }

    private GameData readGame(ResultSet results)
            throws SQLException, DataAccessException {
        try {
            ChessGame game = GSON.fromJson(
                    results.getString("game_json"), ChessGame.class);

            if (game == null) {
                throw new DataAccessException(
                        "Stored game data could not be read");
            }

            return new GameData(
                    results.getInt("game_id"),
                    results.getString("white_username"),
                    results.getString("black_username"),
                    results.getString("game_name"),
                    game);
        } catch (JsonParseException ex) {
            throw new DataAccessException(
                    "Stored game data is invalid", ex);
        }
    }
}