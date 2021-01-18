package ayaya.core.music;

import ayaya.core.exceptions.music.NoAudioMatchingException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;

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

    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

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