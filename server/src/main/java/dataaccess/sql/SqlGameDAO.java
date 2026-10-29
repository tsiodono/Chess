package dataaccess.sql;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.List;
import java.util.Optional;

public final class SqlGameDAO implements GameDAO {

    @Override
    public GameData create(String gameName) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<GameData> find(int gameID) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<GameData> list() throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(GameData game) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public GameData claimSeat(int gameID, ChessGame.TeamColor color,
                              String username) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void clear() throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}