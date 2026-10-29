package dataaccess.sql;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import model.UserData;

import java.sql.SQLException;
import java.util.Optional;

public final class SqlUserDAO implements UserDAO {
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    @Override
    public void create(UserData user) throws DataAccessException {
        String sql = """
                INSERT INTO users (username, password_hash, email)
                VALUES (?, ?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.email());
            statement.executeUpdate();
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                throw new AlreadyTakenException("Username already exists");
            }
            throw new DataAccessException("Unable to create user", ex);
        }
    }

    @Override
    public Optional<UserData> find(String username)
            throws DataAccessException {
        String sql = """
                SELECT username, password_hash, email
                FROM users
                WHERE username = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (var results = statement.executeQuery()) {
                if (!results.next()) {
                    return Optional.empty();
                }

                var user = new UserData(
                        results.getString("username"),
                        results.getString("password_hash"),
                        results.getString("email"));

                return Optional.of(user);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to find user", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM users";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to clear users", ex);
        }
    }
}