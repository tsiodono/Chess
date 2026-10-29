package dataaccess.sql;

import dataaccess.DatabaseManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DbInitializerTest {

    @Test
    void initializeCreatesTablesAndCanRunAgain() throws Exception {
        DatabaseInitializer.initialize();
        DatabaseInitializer.initialize();

        try (var connection = DatabaseManager.getConnection()) {
            var metadata = connection.getMetaData();

            for (String table : new String[]{"users", "auths", "games"}) {
                try (var results = metadata.getTables(
                        connection.getCatalog(),
                        null,
                        table,
                        new String[]{"TABLE"})) {

                    assertTrue(
                            results.next(),
                            "Expected table to exist: " + table);
                }
            }
        }
    }
}