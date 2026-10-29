package dataaccess.sql;

import dataaccess.AlreadyTakenException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.NotFoundException;
import model.AuthData;

import java.sql.SQLException;
import java.util.Optional;

public final class SqlAuthDAO implements AuthDAO {
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    @Override
    public void create(AuthData auth) throws DataAccessException {
        String sql = """
                INSERT INTO auths (auth_token, username)
                VALUES (?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                throw new AlreadyTakenException("Token already exists");
            }
            throw new DataAccessException("Unable to create auth token", ex);
        }
    }

    @Override
    public Optional<AuthData> find(String authToken)
            throws DataAccessException {
        String sql = """
                SELECT auth_token, username
                FROM auths
                WHERE auth_token = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, authToken);

            try (var results = statement.executeQuery()) {
                if (!results.next()) {
                    return Optional.empty();
                }

                var auth = new AuthData(
                        results.getString("auth_token"),
                        results.getString("username"));

                return Optional.of(auth);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to find auth token", ex);
        }
    }

    @Override
    public void delete(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auths WHERE auth_token = ?";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, authToken);

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("Token does not exist");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to delete auth token", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auths";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to clear auth tokens", ex);
        }
    }
}