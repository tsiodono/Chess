package service;

import dataaccess.AlreadyTakenException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.NotFoundException;
import dataaccess.UserDAO;
import model.AuthData;
import model.LoginRequest;
import model.LoginResult;
import model.LogoutRequest;
import model.RegisterRequest;
import model.RegisterResult;
import model.UserData;
import java.util.UUID;

public final class UserService {
    private final UserDAO users;
    private final AuthDAO auths;

    public UserService(UserDAO users, AuthDAO auths) {
        this.users = users;
        this.auths = auths;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        if (request == null || blank(request.username()) || blank(request.password()) || blank(request.email())) {
            throw new ServiceException(400, "bad request");
        }
        try {
            users.create(new UserData(request.username(), request.password(), request.email()));
        } catch (AlreadyTakenException ex) {
            throw new ServiceException(403, "already taken");
        }
        String token = createAuth(request.username());
        return new RegisterResult(request.username(), token);
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request == null || blank(request.username()) || blank(request.password())) {
            throw new ServiceException(400, "bad request");
        }
        UserData user = users.find(request.username())
                .orElseThrow(() -> new ServiceException(401, "unauthorized"));
        if (!user.password().equals(request.password())) {
            throw new ServiceException(401, "unauthorized");
        }
        return new LoginResult(user.username(), createAuth(user.username()));
    }

    public void logout(LogoutRequest request) throws DataAccessException {
        if (request == null || blank(request.authToken())) {
            throw new ServiceException(401, "unauthorized");
        }
        try {
            auths.delete(request.authToken());
        } catch (NotFoundException ex) {
            throw new ServiceException(401, "unauthorized");
        }
    }

    private String createAuth(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        auths.create(new AuthData(token, username));
        return token;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
