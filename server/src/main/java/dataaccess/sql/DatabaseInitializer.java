package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

import java.sql.SQLException;

public final class DatabaseInitializer {

        private static final String[] CREATE_TABLE_STATEMENTS = {
                """
                CREATE TABLE IF NOT EXISTS user (
                username VARCHAR(255) NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                PRIMARY KEY (username)
                ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin
                """,
                """
                CREATE TABLE IF NOT EXISTS auths (
                auth_token VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL,
                PRIMARY KEY(auth_token)
                ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin
                """,
                """
                CREATE TABLE IF NOT EXISTS games (
                game_id INT NOT NULL AUTO_INCREMENT,
                white_username VARCHAR(255),
                black_username VARCHAR(255),
                game_name VARCHAR(255) NOT NULL,
                game_json TEXT NOT NULL,
                PRIMARY KEY (game_id)
                ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin
                """
        };

        private DatabaseInitializer() {

        }

        public static void initialize() throws DataAccessException {
            DatabaseManager.createDatabase();
            try (var connection = DatabaseManager.getConnection()) {
                for (String sql : CREATE_TABLE_STATEMENTS) {
                    try (var statement = connection.prepareStatement(sql)) {
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException ex) {
                throw new DataAccessException("Unable to create database tables", ex);
            }
        }
}