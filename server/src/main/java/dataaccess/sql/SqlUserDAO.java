package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import java.util.Optional;

public final class SqlUserDAO implements UserDAO {
    @Override
    public void create(UserData user) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<UserData> find(String username) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void clear() throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
