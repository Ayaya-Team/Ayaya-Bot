package ayaya.core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;

public class PlayHandler implements AudioLoadResultHandler {

    private static final String URL_PREFIX = "https://";
    private static final String SOUNDCLOUD_SEARCH = "scsearch:";
    private static final String YOUTUBE_SEARCH = "ytsearch:";

    private String url;
    private TextChannel channel;
    private TrackScheduler scheduler;

    PlayHandler(String url, TextChannel channel, TrackScheduler scheduler) {

        this.url = url;
        this.channel = channel;
        this.scheduler = scheduler;

    }

    @Override
    public void trackLoaded(AudioTrack track) {

        String track_title = track.getInfo().title;
        if (track_title == null || track_title.isEmpty())
            track_title = "Undefined";
        boolean queueOnly = scheduler.getTrackAmount() != 0;
        scheduler.playTrack(track);
        if (queueOnly)
            channel.sendMessage("`" + track_title + "` was added to the queue.").queue();
        else
            channel.sendMessage("Now playing `" + track_title + "`.").queue();

    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

        if (url.startsWith(URL_PREFIX)) {
            
        } else {

        }

    }

    @Override
    public void noMatches() {

    }

    @Override
    public void loadFailed(FriendlyException exception) {

    }

}