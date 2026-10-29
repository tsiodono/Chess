package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

import java.util.Optional;

public final class SqlAuthDAO implements AuthDAO {

    @Override
    public void create(AuthData auth) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<AuthData> find(String authToken)
            throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(String authToken) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void clear() throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}