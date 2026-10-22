package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.List;
import java.util.Optional;

public interface GameDAO {
    GameData create(String gameName) throws DataAccessException;
    Optional<GameData> find(int gameID) throws DataAccessException;
    List<GameData> list() throws DataAccessException;
    void update(GameData game) throws DataAccessException;
    GameData claimSeat(int gameID, ChessGame.TeamColor color, String username) throws DataAccessException;
    void clear() throws DataAccessException;
}
