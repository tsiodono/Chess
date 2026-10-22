package dataaccess;

/** A requested update or deletion refers to data that does not exist. */
public final class NotFoundException extends DataAccessException {
    public NotFoundException(String message) {
        super(message);
    }
}
