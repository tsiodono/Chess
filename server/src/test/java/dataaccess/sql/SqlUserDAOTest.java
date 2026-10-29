package dataaccess.sql;

import dataaccess.AlreadyTakenException;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlUserDAOTest {
    private final SqlUserDAO users = new SqlUserDAO();

    @BeforeEach
    void setUp() throws Exception {
        DatabaseInitializer.initialize();
        users.clear();
    }

    @Test
    void createAndFindUser() throws Exception {
        // This value represents a password hash. Hashing happens in UserService.
        var user = new UserData("ana", "example-hash", "ana@example.com");

        users.create(user);

        assertEquals(user, users.find("ana").orElseThrow());
    }

    @Test
    void findMissingUserReturnsEmpty() throws Exception {
        assertTrue(users.find("missing").isEmpty());
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        var original = new UserData("ana", "example-hash", "ana@example.com");
        users.create(original);

        assertThrows(
                AlreadyTakenException.class,
                () -> users.create(
                        new UserData("ana", "other-hash", "other@example.com")));

        assertEquals(original, users.find("ana").orElseThrow());
    }

    @Test
    void clearRemovesUsers() throws Exception {
        users.create(new UserData("ana", "example-hash", "ana@example.com"));

        users.clear();

        assertTrue(users.find("ana").isEmpty());
    }
}