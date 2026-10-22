package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new LinkedHashMap<>();
    private int nextID = 1;

    @Override
    public synchronized GameData create(String name) {
        GameData game = new GameData(nextID++, null, null, name, new ChessGame());
        games.put(game.gameID(), game);
        return game;
    }

    @Override
    public synchronized Optional<GameData> find(int id) {
        return Optional.ofNullable(games.get(id));
    }

    @Override
    public synchronized List<GameData> list() {
        return new ArrayList<>(games.values());
    }

    @Override
    public synchronized void update(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new NotFoundException("Game does not exist");
        }
        games.put(game.gameID(), game);
    }

    @Override
    public synchronized GameData claimSeat(int id, ChessGame.TeamColor color, String username)
            throws DataAccessException {
        GameData old = games.get(id);
        if (old == null) {
            throw new NotFoundException("Game does not exist");
        }
        if (color == null || username == null) {
            throw new DataAccessException("Color and username are required");
        }
        boolean white = color == ChessGame.TeamColor.WHITE;
        String occupant = white ? old.whiteUsername() : old.blackUsername();
        if (occupant != null) {
            throw new AlreadyTakenException("Player seat already taken");
        }
        GameData updated = white ? old.withWhite(username) : old.withBlack(username);
        games.put(id, updated);
        return updated;
    }

    @Override
    public synchronized void clear() {
        games.clear();
        nextID = 1;
    }
}
