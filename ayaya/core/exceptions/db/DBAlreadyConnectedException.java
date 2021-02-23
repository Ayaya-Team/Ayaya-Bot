package ayaya.core.exceptions.db;

/**
 * Exception thrown when it's attempted to create a database and there's already an opened connection.
 */
public class DBAlreadyConnectedException extends RuntimeException {
    public DBAlreadyConnectedException()
    {super("This controller is already connected to a database. Close it's connection first.");}
}