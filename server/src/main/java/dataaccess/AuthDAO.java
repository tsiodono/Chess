package dataaccess;

import model.AuthData;
import java.util.Optional;

public interface AuthDAO {
    Optional<AuthData> find(String authToken) throws DataAccessException;
    void create(AuthData auth) throws DataAccessException;
    void delete(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}
