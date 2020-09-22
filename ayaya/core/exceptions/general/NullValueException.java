package ayaya.core.exceptions.general;

/**
 * Exception to be thrown upon the occurrence of a NullPointerException
 * to make the exception handler able to catch it.
 */
public class NullValueException extends RuntimeException {
    public NullValueException() {super();}

    public NullValueException(String s) {super(s);}
}