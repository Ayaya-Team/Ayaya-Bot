package ayaya.core.exceptions.music;

/**
 * When there is no video/music matching the query or the url does not contain any audio.
 */
public class NoAudioMatchingException extends RuntimeException {
    public NoAudioMatchingException() {super();}

    public NoAudioMatchingException(String s) {super(s);}
}