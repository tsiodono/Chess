package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> auths = new HashMap<>();

    @Override
    public synchronized Optional<AuthData> find(String token) {
        return Optional.ofNullable(auths.get(token));
    }

    @Override
    public synchronized void create(AuthData auth) throws DataAccessException {
        if (auths.containsKey(auth.authToken())) {
            throw new AlreadyTakenException("Token already exists");
        }
        auths.put(auth.authToken(), auth);
    }

    @Override
    public synchronized void delete(String token) throws DataAccessException {
        if (auths.remove(token) == null) {
            throw new NotFoundException("Token does not exist");
        }
    }

    @Override
    public synchronized void clear() {
        auths.clear();
    }
}
