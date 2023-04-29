package ayaya.core.exceptions.general;

public class UnsupportedTypeException extends RuntimeException {
    public UnsupportedTypeException() {
        super("This type isn't supported by the SQLController. Please use the standard JDBC instead");
    }
}