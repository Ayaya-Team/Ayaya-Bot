package ayaya.core.exceptions.http;

/**
 * Exception thrown when some of the header information is missing.
 */
public class MissingHeaderInfoException extends Throwable {
    public MissingHeaderInfoException() {super();}

    public MissingHeaderInfoException(String s) {super(s);}
}