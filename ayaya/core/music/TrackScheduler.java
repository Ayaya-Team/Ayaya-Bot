package ayaya.core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.apache.commons.collections4.list.CursorableLinkedList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {

    private static final int QUEUE_CAPACITY = 31;
    private static final int INITIAL_VOLUME = 50;

    private final AudioPlayer player;
    private CursorableLinkedList<AudioTrack> queue;
    private final int limit;
    private boolean repeat;

    /**
     * @param player The audio player this scheduler uses
     */
    TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.player.setVolume(INITIAL_VOLUME);
        queue = new CursorableLinkedList<>();
        this.limit = QUEUE_CAPACITY;
        repeat = false;
    }

    /**
     * Gets the amount of tracks in the queue.
     *
     * @return <code>int</code>
     */
    synchronized int getTrackAmount() {
        return queue.size();
    }

    /**
     * Tells wether the music is currently stopped or not.
     *
     * @return <code>boolean</code> if the player is stopped or no track is playing at the moment
     */
    synchronized boolean musicPaused() {
        return player.isPaused() || noMusicPlaying();
    }

    /**
     * Tells wether there is or there isn't a music currently playing or paused.
     *
     * @return true if the situation above applies, false on the contrary
     */
    synchronized boolean noMusicPlaying() {
        return player.getPlayingTrack() == null;
    }

    /**
     * Add the next track to queue.
     *
     * @param track The track to queue.
     * @return true if the track was added, false otherwise
     */
    synchronized boolean queue(AudioTrack track) {
        return track != null && queue.size() < limit && queue.addLast(track);
    }

    /**
     * Remove the next track from the queue.
     *
     * @param index The number of the track to remove from the queue
     * @return the removed track
     */
    synchronized AudioTrack dequeue(int index) {
        if (index >= queue.size())
            return null;
        return queue.remove(index);
    }

    synchronized boolean move(int i, int j) {
        if (i < 1 || i >= queue.size() || j < 1 || j >= queue.size())
            return false;
        if (i == j)
            return true;
        if (j == queue.size() - 1) {
            AudioTrack track = dequeue(i);
            return queue.addLast(track);
        }
        if (i < j) {
            CursorableLinkedList.Cursor<AudioTrack> c = queue.cursor(i);
            AudioTrack track = c.next();
            c.remove();
            while (c.hasNext() && c.nextIndex() <= j + 1) c.next();
            c.add(track);
            c.close();
            return true;
        } else {
            CursorableLinkedList.Cursor<AudioTrack> c = queue.cursor(i);
            AudioTrack track = c.next();
            c.remove();
            while (c.hasPrevious() && c.previousIndex() > j) c.previous();
            c.add(track);
            c.close();
            return true;
        }
    }

    synchronized void shuffle() {
        CursorableLinkedList<AudioTrack> newQueue = new CursorableLinkedList<>();
        List<AudioTrack> array = new ArrayList<>(queue.size());
        array.addAll(queue);

        newQueue.addFirst(array.remove(0));
        Random rng = new Random();
        while (!array.isEmpty())
            newQueue.addLast(array.remove(rng.nextInt(array.size())));

        queue = newQueue;
    }

    /**
     * Add the next track to queue or play it right away if nothing is in the queue.
     * If the queue had already something and the player was stopped, plays the element at the head of the queue and
     * adds the new track to the tail of the queue.
     *
     * @param track The track to play or add to queue.
     * @return true if the track was queued, false otherwise
     */
    synchronized boolean playTrack(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        boolean r;
        if ((r = queue(track)) && player.startTrack(getCurrentTrack(), true)) {
            player.setPaused(false);
        }
        return r;
    }

    /**
     * Returns the track that is currently playing.
     *
     * @return <code>AudioTrack</code>
     */
    synchronized AudioTrack getCurrentTrack() {
        return player.getPlayingTrack();
    }

    private synchronized AudioTrack getNextTrack() {
        if (queue.isEmpty()) return null;
        return queue.getFirst();
    }

    /**
     * Returns the track iterator of the queue.
     *
     * @return <code>Iterator</code>
     */
    Iterator<AudioTrack> getTrackIterator() {
        return queue.iterator();
    }

    /**
     * Toggles the repeat mode on or off for the queue.
     *
     * @return the new repeat boolean value
     */
    synchronized boolean repeat() {
        return repeat = !repeat;
    }

    /**
     * Checks if the repeat mode is on.
     *
     * @return <code>boolean</code>
     */
    synchronized boolean isRepeating() {return repeat;}

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack(false);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     *
     * @return <code>boolean</code>
     */
    synchronized boolean nextTrack(boolean ignoreRepeat) {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrack track = dequeue(0);
        if (isRepeating() && !ignoreRepeat)
            queue(track.makeClone());
        return player.startTrack(getNextTrack(), false);
    }

    /**
     * Stops the player and prunes the queue.
     */
    synchronized void stopAndClear() {
        queue.clear();
        player.startTrack(null, false);
    }

    /**
     * Sets the volume of the player.
     *
     * @param volume the new volume
     */
    synchronized void setVolume(int volume) {
        if (volume > 0 && volume < 101) player.setVolume(volume);
    }

    /**
     * Returns the volume of the current player.
     *
     * @return integer from 1 to 100
     */
    int getVolume() {
        return player.getVolume();
    }

}