package dataaccess;

/** A unique username, token, or player seat is already occupied. */
public final class AlreadyTakenException extends DataAccessException {
    public AlreadyTakenException(String message) {
        super(message);
    }
}
