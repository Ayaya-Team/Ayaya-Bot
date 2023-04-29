package ayaya.core.exceptions.db;

/**
 * Exception thrown when attempting to use an operation that isn't supported.
 */
public class DBOperationNotSupportedException extends RuntimeException {
    public DBOperationNotSupportedException()
    {super("This operation is not supported.");}
}