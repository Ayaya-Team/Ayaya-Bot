package ayaya.core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;

public class PlayHandler implements AudioLoadResultHandler {

    private TextChannel channel;
    private TrackScheduler scheduler;

    PlayHandler(TextChannel channel, TrackScheduler scheduler) {

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

    }

    @Override
    public void noMatches() {

    }

    @Override
    public void loadFailed(FriendlyException exception) {

    }

}