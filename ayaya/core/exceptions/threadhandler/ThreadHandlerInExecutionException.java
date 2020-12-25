package ayaya.core.exceptions.threadhandler;

public class ThreadHandlerInExecutionException extends RuntimeException {
    public ThreadHandlerInExecutionException() {
        super();
    }

    public ThreadHandlerInExecutionException(String s) {
        super(s);
    }
}