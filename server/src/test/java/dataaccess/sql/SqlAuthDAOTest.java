package dataaccess.sql;

import dataaccess.AlreadyTakenException;
import dataaccess.DatabaseManager;
import dataaccess.NotFoundException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlAuthDAOTest {
    private final SqlAuthDAO auths = new SqlAuthDAO();

    @BeforeEach
    void setUp() throws Exception {
        DatabaseInitializer.initialize();
        auths.clear();
    }

    @Test
    void createAndFindAuth() throws Exception {
        var auth = new AuthData("token-1", "ana");

        auths.create(auth);

        assertEquals(auth, auths.find("token-1").orElseThrow());
    }

    @Test
    void findMissingTokenReturnsEmpty() throws Exception {
        assertTrue(auths.find("missing").isEmpty());
    }

    @Test
    void duplicateTokenIsRejected() throws Exception {
        var original = new AuthData("token-1", "ana");
        auths.create(original);

        assertThrows(
                AlreadyTakenException.class,
                () -> auths.create(new AuthData("token-1", "bob")));

        assertEquals(original, auths.find("token-1").orElseThrow());
    }

    @Test
    void deleteRemovesToken() throws Exception {
        auths.create(new AuthData("token-1", "ana"));

        auths.delete("token-1");

        assertTrue(auths.find("token-1").isEmpty());
    }

    @Test
    void deleteMissingTokenThrowsNotFound() throws Exception {
        assertThrows(NotFoundException.class, () -> auths.delete("missing"));
    }

    @Test
    void clearRemovesAllTokens() throws Exception {
        auths.create(new AuthData("token-1", "ana"));
        auths.create(new AuthData("token-2", "ana"));

        auths.clear();

        assertTrue(auths.find("token-1").isEmpty());
        assertTrue(auths.find("token-2").isEmpty());
    }
}