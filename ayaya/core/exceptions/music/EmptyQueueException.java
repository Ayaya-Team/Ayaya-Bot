package ayaya.core.exceptions.music;

/**
 * Exception thrown when trying to pause the music while the queue is empty.
 */
public class EmptyQueueException extends RuntimeException {

    public EmptyQueueException() {
        super();
    }

    public EmptyQueueException(String s) {
        super(s);
    }

}