package ayaya.core.exceptions.threadhandler;

/**
 * Exception thrown when a ParallelThreadHandler object attempts to run threads
 * without having all information needed.
 */
public class ThreadHandlerNotReadyException extends RuntimeException {
    public ThreadHandlerNotReadyException() {
        super();
    }

    public ThreadHandlerNotReadyException(String s) {
        super(s);
    }
}