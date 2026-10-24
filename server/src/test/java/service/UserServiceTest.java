package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.LoginRequest;
import model.LoginResult;
import model.LogoutRequest;
import model.RegisterRequest;
import model.RegisterResult;
import model.UserData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static service.ServiceAssertions.assertStatus;

class UserServiceTest {
    private final MemoryUserDAO users = new MemoryUserDAO();
    private final MemoryAuthDAO auths = new MemoryAuthDAO();
    private final UserService service = new UserService(users, auths);

    @Test
    void registerStoresUserAndCreatesAuth() throws DataAccessException {
        RegisterResult result = registerAna();
        assertEquals("ana", result.username());
        assertFalse(result.authToken().isBlank());
        assertEquals(new UserData("ana", "pw", "ana@example.com"), users.find("ana").orElseThrow());
        assertEquals(new AuthData(result.authToken(), "ana"), auths.find(result.authToken()).orElseThrow());
    }

    @Test
    void registerRejectsDuplicateWithoutOverwritingUser() throws DataAccessException {
        RegisterResult original = registerAna();
        assertStatus(403, () -> service.register(new RegisterRequest("ana", "changed", "new@example.com")));
        assertEquals("pw", users.find("ana").orElseThrow().password());
        assertTrue(auths.find(original.authToken()).isPresent());
    }

    @Test
    void registerRejectsMissingOrBlankFields() {
        assertStatus(400, () -> service.register(null));
        for (String invalid : new String[]{null, "", " "}) {
            assertStatus(400, () -> service.register(new RegisterRequest(invalid, "pw", "email")));
            assertStatus(400, () -> service.register(new RegisterRequest("ana", invalid, "email")));
            assertStatus(400, () -> service.register(new RegisterRequest("ana", "pw", invalid)));
        }
        assertTrue(users.find("ana").isEmpty());
    }

    @Test
    void loginCreatesIndependentTokens() throws DataAccessException {
        RegisterResult registered = registerAna();
        LoginResult first = service.login(new LoginRequest("ana", "pw"));
        LoginResult second = service.login(new LoginRequest("ana", "pw"));
        assertEquals("ana", first.username());
        assertNotEquals(registered.authToken(), first.authToken());
        assertNotEquals(first.authToken(), second.authToken());
        assertEquals("ana", auths.find(first.authToken()).orElseThrow().username());
        assertTrue(auths.find(second.authToken()).isPresent());
        assertTrue(auths.find(registered.authToken()).isPresent());
    }

    @Test
    void loginRejectsWrongPasswordAndUnknownUser() throws DataAccessException {
        registerAna();
        assertStatus(401, () -> service.login(new LoginRequest("ana", "wrong")));
        assertStatus(401, () -> service.login(new LoginRequest("missing", "pw")));
    }

    @Test
    void loginRejectsMalformedRequests() {
        assertStatus(400, () -> service.login(null));
        for (String invalid : new String[]{null, "", " "}) {
            assertStatus(400, () -> service.login(new LoginRequest(invalid, "pw")));
            assertStatus(400, () -> service.login(new LoginRequest("ana", invalid)));
        }
    }

    @Test
    void logoutRevokesOnlyTheSpecifiedToken() throws DataAccessException {
        RegisterResult registered = registerAna();
        LoginResult other = service.login(new LoginRequest("ana", "pw"));
        service.logout(new LogoutRequest(registered.authToken()));
        assertTrue(auths.find(registered.authToken()).isEmpty());
        assertTrue(auths.find(other.authToken()).isPresent());
        assertTrue(users.find("ana").isPresent());
    }

    @Test
    void logoutRejectsUnknownAndPreviouslyDeletedTokens() throws DataAccessException {
        RegisterResult registered = registerAna();
        service.logout(new LogoutRequest(registered.authToken()));
        assertStatus(401, () -> service.logout(new LogoutRequest(registered.authToken())));
        assertStatus(401, () -> service.logout(new LogoutRequest("missing")));
    }

    @Test
    void logoutRejectsMissingTokens() {
        assertStatus(401, () -> service.logout(null));
        for (String invalid : new String[]{null, "", " "}) {
            assertStatus(401, () -> service.logout(new LogoutRequest(invalid)));
        }
    }

    private RegisterResult registerAna() throws DataAccessException {
        return service.register(new RegisterRequest("ana", "pw", "ana@example.com"));
    }
}
