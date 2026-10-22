package dataaccess;

import model.UserData;
import java.util.Optional;

public interface UserDAO {
    Optional<UserData> find(String username) throws DataAccessException;
    void create(UserData user) throws DataAccessException;
    void clear() throws DataAccessException;
}
