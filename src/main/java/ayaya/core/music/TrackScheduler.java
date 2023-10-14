package ayaya.core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class is the track scheduler. It contains the queue of tracks,
 * the audio player and the channel to send update messages.
 */
public class TrackScheduler extends AudioEventAdapter {

    private static final int QUEUE_CAPACITY = 30;
    private static final int INITIAL_AUDIO_VOLUME = 50;

    private final AudioPlayer player;
    private final int limit;
    private List<AudioTrack> queue;
    private volatile boolean repeat;
    private TextChannel channel;

    /**
     * Constructor of the track scheduler.
     * 
     * @param player  The audio player this scheduler uses
     * @param channel The channel for started track messages
     */
    TrackScheduler(AudioPlayer player, TextChannel channel) {

        this.player = player;
        this.player.setVolume(INITIAL_AUDIO_VOLUME);
        this.limit = QUEUE_CAPACITY;
        queue = Collections.synchronizedList(new LinkedList<>());
        repeat = false;
        this.channel = channel;

    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {

        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (repeat)
                queue.add(track.makeClone());
            this.startFirst();
            AudioTrack next = this.getCurrentTrack();
            if (next != null) {
                String playingTrackTitle = next.getInfo().title;
                playingTrackTitle =
                        (playingTrackTitle == null || playingTrackTitle.isEmpty()) ? "Undefined" : playingTrackTitle;
                channel.sendMessage("Now playing `" + playingTrackTitle + "`.").queue();
            }
        }

    }

    /**
     * Tells wether the queue is in repeat mode or not
     * 
     * @return true if repeating, false on the contrary
     */
    boolean isRepeat() {
        return repeat;
    }

    /**
     * Toggles the repeat mode and returns the new value.
     * 
     * @return true if repeating, false on the contrary
     */
    boolean repeat() {
        return repeat = !repeat;
    }

    /**
     * Pauses the player.
     */
    void pause() {
        player.setPaused(true);
    }

    /**
     * Resumes the player.
     */
    void unpause() {
        player.setPaused(false);
    }

    /**
     * Tells if the player is paused or not
     * 
     * @return true if paused, false on the contrary
     */
    boolean isPaused() {
        return player.isPaused();
    }

    /**
     * Returns the current volume of the player.
     * 
     * @return volume
     */
    int getVolume() {
        return player.getVolume();
    }

    /**
     * Changes the volume of the player.
     * 
     * @param volume the new volume
     */
    void setVolume(int volume) {
        player.setVolume(volume);
    }

    /**
     * Retrieves the channel to which the messages of started tracks are being sent.
     * 
     * @return channel
     */
    TextChannel getChannel() {
        return channel;
    }

    /**
     * Changes the channel of started track messages.
     * 
     * @param channel
     */
    void setChannel(TextChannel channel) {
        this.channel = channel;
    }

    /**
     * Gets the track being played. Can be null.
     * 
     * @return track
     */
    AudioTrack getCurrentTrack() {
        return player.getPlayingTrack();
    }

    /**
     * Attempts to fast-forward the track for the given amount of seconds.
     * With a negative amount, the fast-forward is inverted.
     * 
     * @param seconds the amount of seconds to fast-forward/fast-backward
     * @return true if the track is seekable, false on the contrary
     */
    boolean seek(long seconds) {
        AudioTrack track = this.getCurrentTrack();
        if (!track.isSeekable()) return false;
        long currentTime = track.getPosition();
        long timeToSeek = Math.max(0, Math.min(currentTime + seconds * 1000, track.getDuration()));
        if (timeToSeek >= track.getDuration())
            this.skip();
        else
            track.setPosition(timeToSeek);
        return true;
    }

    /**
     * Attempts to start the first track in the queue.
     * 
     * @return true if the track was started, false if there was a problem or the queue was empty
     */
    boolean startFirst() {
        return !queue.isEmpty() && player.startTrack(this.removeFirstTrack(), true);
    }

    /**
     * Attempts to skip the current track to play the next one.
     * 
     * @return true or false
     */
    boolean skip() {
        return this.skipTrack(true);
    }

    private boolean skipTrack(boolean allowRepeat) {
        AudioTrack track = this.getCurrentTrack();
        if (track == null)
            return !queue.isEmpty() && player.startTrack(this.removeFirstTrack(), false);
        if (repeat && allowRepeat)
            queue.add(track.makeClone());
        return player.startTrack(this.removeFirstTrack(), false);
    }

    /**
     * Attempts to go back to the previous track.
     * 
     * @return true or false
     */
    boolean previousTrack() {
        AudioTrack track = this.getCurrentTrack();
        if (track == null)
            return !queue.isEmpty() && player.startTrack(this.removeLastTrack(), false);
        return player.startTrack(this.removeLastTrack(), false);
    }

    /**
     * Clears the queue and stops the player.
     */
    void stopAndClear() {
        if (player.getPlayingTrack() != null)
            player.startTrack(null, false);
        queue.clear();
    }

    /**private AudioTrack getFirstTrack() {
        if (queue.isEmpty())
            return null;
        return queue.get(0);
    }*/

    private AudioTrack removeFirstTrack() {
        if (queue.isEmpty())
            return null;
        return queue.remove(0);
    }

    private AudioTrack removeLastTrack() {
        if (queue.isEmpty())
            return null;
        return queue.remove(queue.size() - 1);
    }

    /**
     * Returns the total amount of tracks, including the one being played.
     * 
     * @return amount
     */
    int getTrackAmount() {
        return queue.size() + (this.getCurrentTrack() == null ? 0 : 1);
    }

    /**
     * Returns an iterator for the current tracks in the queue.
     * 
     * @return iterator
     */
    Iterator<AudioTrack> getTrackIterator() {
        return queue.iterator();
    }

    /**
     * Attempts to queue a track.
     * 
     * @param track the track to add to the queue
     * @return true if the track was queued, false on the contrary
     */
    boolean queue(AudioTrack track) {
        return track != null && queue.size() < limit && queue.add(track);
    }

    /**
     * Removes a track at a given index.
     * 
     * @param index the index of the track to remove
     * @return the removed track
     */
    AudioTrack dequeue(int index) {
        if (index >= queue.size() + 1)
            return null;
        else if (index == 0) {
            AudioTrack result = this.getCurrentTrack();
            this.skipTrack(false);
            return result;
        }
        else
            return queue.remove(this.getCurrentTrack() != null ? index - 1 : index);
    }

    /**
     * Attempts to move a track from index i to index j.
     * 
     * @param i the index of the track to move
     * @param j the position to move it to
     * @return true of the track was moved, false on the contrary
     */
    boolean move(int i, int j) {

        if (this.getCurrentTrack() != null) {
            i--;
            j--;
        }

        if (i < 0 || i >= queue.size() || j < 0 || j >= queue.size())
            return false;
        else if (i == j)
            return true;
        else {
            synchronized (this) {
                if (j == queue.size() - 1) {
                    AudioTrack track = this.dequeue(i + 1);
                    return queue.add(track);
                }
                else {
                    ListIterator<AudioTrack> it = queue.listIterator(i);
                    AudioTrack track = it.next();
                    it.remove();
                    if (i < j)
                        while (it.hasNext() && it.nextIndex() < j)
                            it.next();
                    else
                        while (it.hasPrevious() && it.previousIndex() >= j)
                            it.previous();
                    it.add(track);
                    return true;
                }
            }
        }

    }

    /**
     * Shuffles the queue.
     */
    void shuffle() {

        int currentQueueSize = queue.size();
        List<Integer> indexes = new ArrayList<>();

        while (indexes.size() < currentQueueSize) {
            int x = ThreadLocalRandom.current().nextInt(currentQueueSize);
            if (!indexes.contains(x))
                indexes.add(x);
        }

        List<AudioTrack> newQueue = Collections.synchronizedList(new LinkedList<>());
        synchronized (this) {
            while (!indexes.isEmpty())
                newQueue.add(queue.get(indexes.remove(0)));

            queue = newQueue;
        }

    }

}