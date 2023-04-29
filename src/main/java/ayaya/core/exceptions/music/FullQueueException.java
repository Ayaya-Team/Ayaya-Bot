package ayaya.core.exceptions.music;

/**
 * Exception thrown when the music queue is completely full.
 */
public class FullQueueException extends RuntimeException {
    public FullQueueException() {super();}

    public FullQueueException(String s) {super(s);}
}