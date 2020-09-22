package ayaya.core.exceptions.db;

/**
 * Exception thrown when the jdbc isn't connected to any database,
 * while trying to perform operations that require a connection.
 */
public class DBNotConnectedException extends RuntimeException {
    public DBNotConnectedException()
    {super("There is no database connected to this controller.");}
}