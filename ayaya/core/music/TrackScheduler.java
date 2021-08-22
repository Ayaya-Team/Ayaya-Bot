package ayaya.core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.*;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {

    private static final int QUEUE_CAPACITY = 30;
    private static final int INITIAL_AUDIO_VOLUME = 50;

    private final AudioPlayer player;
    private final int limit;
    private List<AudioTrack> queue;
    private volatile boolean repeat;

    /**
     * @param player The audio player this scheduler uses
     */
    TrackScheduler(AudioPlayer player) {

        this.player = player;
        this.player.setVolume(INITIAL_AUDIO_VOLUME);
        this.limit = QUEUE_CAPACITY;
        queue = Collections.synchronizedList(new LinkedList<>());
        repeat = false;

    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {

        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (repeat)
                queue.add(track.makeClone());
            this.startFirst();
        }

    }

    boolean isRepeat() {
        return repeat;
    }

    boolean repeat() {
        return repeat = !repeat;
    }

    void pause() {
        player.setPaused(true);
    }

    void unpause() {
        player.setPaused(false);
    }

    boolean isPaused() {
        return player.isPaused();
    }

    int getVolume() {
        return player.getVolume();
    }

    void setVolume(int volume) {
        player.setVolume(volume);
    }

    AudioTrack getCurrentTrack() {
        return player.getPlayingTrack();
    }

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

    boolean startFirst() {
        return !queue.isEmpty() && player.startTrack(this.removeFirstTrack(), true);
    }

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

    boolean previousTrack() {
        AudioTrack track = this.getCurrentTrack();
        if (track == null)
            return !queue.isEmpty() && player.startTrack(this.removeLastTrack(), false);
        return player.startTrack(this.removeLastTrack(), false);
    }

    void stopAndClear() {
        if (player.getPlayingTrack() != null)
            player.startTrack(null, false);
        queue.clear();
    }

    private AudioTrack getFirstTrack() {
        if (queue.isEmpty())
            return null;
        return queue.get(0);
    }

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

    int getTrackAmount() {
        return queue.size() + (this.getCurrentTrack() == null ? 0 : 1);
    }

    Iterator<AudioTrack> getTrackIterator() {
        return queue.iterator();
    }

    boolean queue(AudioTrack track) {
        return track != null && queue.size() < limit && queue.add(track);
    }

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

    void shuffle() {

        int currentQueueSize = queue.size();
        List<Integer> indexes = new ArrayList<>();
        Random rng = new Random();

        while (indexes.size() < currentQueueSize) {
            int x = rng.nextInt(currentQueueSize);
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