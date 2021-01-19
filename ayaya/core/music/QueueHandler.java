package ayaya.core.music;

import ayaya.core.exceptions.music.NoAudioMatchingException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class QueueHandler implements AudioLoadResultHandler {

    private static final String HTTPS = "https://";
    private static final String SOUNDCLOUD_SEARCH = "scsearch:";
    private static final String YOUTUBE_SEARCH = "ytsearch:";

    private String url;
    private TextChannel channel;
    private GuildMusicManager guildMusicManager;

    QueueHandler(String url, TextChannel channel, GuildMusicManager guildMusicManager) {

        this.url = url;
        this.channel = channel;
        this.guildMusicManager = guildMusicManager;

    }

    @Override
    public void trackLoaded(AudioTrack track) {

        String trackTitle = track.getInfo().title;
        if (trackTitle == null || trackTitle.isEmpty())
            trackTitle = "Undefined";
        if (guildMusicManager.getScheduler().queue(track))
            channel.sendMessage("`" + trackTitle + "` was added to the queue.").queue();
        else
            channel.sendMessage("I couldn't queue this track because the queue is too full.").queue();

    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        FriendlyException fe = null;
        List<AudioTrack> tracks = playlist.getTracks();

        if (tracks.size() == 0)
            channel.sendMessage("You found an empty playlist, there's nothing to queue.").queue();
        else if (playlist.isSearchResult()) {
            trackLoaded(playlist.getTracks().get(0));
        } else {

        }

    }

    @Override
    public void noMatches() {
        if (url.startsWith(HTTPS)) {
            channel.sendMessage("I didn't find anything for that url.").queue();
        } else {
            throw new NoAudioMatchingException("The provided query did not return any results.");
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if (url.startsWith(HTTPS)) {
            channel.sendMessage("I couldn't queue this track due to an exception."
                    + "Try another url or try a search query.").queue();
            exception.printStackTrace();
        } else
            throw exception;
    }

}