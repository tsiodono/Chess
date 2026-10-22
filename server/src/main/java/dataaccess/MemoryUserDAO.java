package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public synchronized Optional<UserData> find(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public synchronized void create(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new AlreadyTakenException("Username already exists");
        }
        users.put(user.username(), user);
    }

    @Override
    public synchronized void clear() {
        users.clear();
    }
}
