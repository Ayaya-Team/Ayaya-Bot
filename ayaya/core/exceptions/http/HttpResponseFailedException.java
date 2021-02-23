package ayaya.core.exceptions.http;

/**
 * Exception thrown for a failed http request.
 */
public class HttpResponseFailedException extends Throwable {
    public HttpResponseFailedException() {super();}

    public HttpResponseFailedException(String s) {super(s);}
}