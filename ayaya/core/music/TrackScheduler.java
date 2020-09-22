package ayaya.core.music;

import ayaya.core.exceptions.music.FullQueueException;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class schedules tracks for the audio player. It contains the playTrack of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {

    private static final int QUEUE_CAPACITY = 30;
    private static final int INITIAL_VOLUME = 50;

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final List<AudioTrack> tracks;
    private final ReentrantLock lock;
    private boolean repeat;

    /**
     * @param player The audio player this scheduler uses
     */
    TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        tracks = new ArrayList<>(QUEUE_CAPACITY + 1);
        player.setVolume(INITIAL_VOLUME);
        lock = new ReentrantLock();
        repeat = false;
    }

    /**
     * Add the next track to queue.
     *
     * @param track The track to queue.
     */
    protected void queue(AudioTrack track) {
        if (!queue.offer(track) || tracks.size() == QUEUE_CAPACITY)
            throw new FullQueueException();
        tracks.add(track);
    }

    /**
     * Remove the next track from the queue.
     *
     * @param trackNumber The number of the track to remove from the queue
     * @return the removed track
     */
    AudioTrack dequeue(int trackNumber) {
        AudioTrack track = tracks.get(trackNumber);
        if (track != null) {
            queue.remove(track);
            tracks.remove(trackNumber);
        }
        return track;
    }

    /**
     * Gets the amount of tracks in the queue.
     *
     * @return <code>int</code>
     */
    int amountOfTracksInQueue() {
        return tracks.size();
    }

    /**
     * Tells wether the music is currently stopped or not.
     *
     * @return <code>boolean</code> if the player is stopped or not track is playing at the moment
     */
    public boolean musicStopped() {
        return player.isPaused() || player.getPlayingTrack() == null;
    }

    /**
     * Tells wether there is or there isn't a music currently playing or paused.
     *
     * @return true if the situation above applies, false on the contrary
     */
    public boolean noMusicPlaying() {
        return player.getPlayingTrack() == null;
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue. If the queue had already something and
     * the player was stopped, plays the element at the head of the queue and adds the new track to the tail of the
     * queue.
     *
     * @param track The track to play or add to queue.
     */
    void playTrack(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (track == null) {
            player.startTrack(queue.poll(), true);
        } else if (player.getPlayingTrack() == null) {
            if (!player.startTrack(queue.poll(), true)) {
                player.startTrack(track, true);
                tracks.add(track);
            } else {
                queue(track);
            }
        } else queue(track);
        player.setPaused(false);
    }

    /**
     * Returns the track that is currently playing.
     *
     * @return <code>AudioTrack</code>
     */
    public AudioTrack getCurrentTrack() {
        if (tracks.isEmpty()) return null;
        return tracks.get(0);
    }

    /**
     * Returns the list of tracks in the queue. This list also contains the track that is currently playing.
     *
     * @return <code>List</code>
     */
    public List<AudioTrack> getTracks() {
        return tracks;
    }

    /**
     * Toggles the repeat mode on or off for the queue.
     */
    void repeat() {
        lock.lock();
        repeat = !repeat;
        lock.unlock();
    }

    /**
     * Checks if the repeat mode is on.
     *
     * @return <code>boolean</code>
     */
    boolean isRepeating() {return repeat;}

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack(track);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     *
     * @param track the track that is playing to requeue if needed
     * @return <code>boolean</code>
     */
    boolean nextTrack(AudioTrack track) {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        if (tracks.size() > 0)
            tracks.remove(0);
        if (this.isRepeating())
            queue(track.makeClone());
        return player.startTrack(queue.poll(), false);
    }

    /**
     * Stops the player and prunes the queue.
     */
    void stopAllTracks() {
        queue.clear();
        tracks.clear();
        player.startTrack(null, false);
    }

    /**
     * Sets the volume of the player.
     *
     * @param volume the new volume
     */
    void setVolume(int volume) {
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