package ayaya.core.exceptions.http;

/**
 * Exception thrown when an http response has a null body.
 */
public class HttpNullResponseException extends Throwable {
    public HttpNullResponseException() {super();}

    public HttpNullResponseException(String s) {super(s);}
}