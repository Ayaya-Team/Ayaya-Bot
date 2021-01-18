package ayaya.core.music;

import ayaya.core.exceptions.music.NoAudioMatchingException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class PlayHandler implements AudioLoadResultHandler {

    private static final String HTTPS = "https://";
    private static final String SOUNDCLOUD_SEARCH = "scsearch:";
    private static final String YOUTUBE_SEARCH = "ytsearch:";

    private String url;
    private TextChannel channel;
    private GuildMusicManager guildMusicManager;

    PlayHandler(String url, TextChannel channel, GuildMusicManager guildMusicManager) {

        this.url = url;
        this.channel = channel;
        this.guildMusicManager = guildMusicManager;

    }

    @Override
    public void trackLoaded(AudioTrack track) {

        String trackTitle = track.getInfo().title;
        if (trackTitle == null || trackTitle.isEmpty())
            trackTitle = "Unknown";

        boolean queueOnly = guildMusicManager.getScheduler().getTrackAmount() != 0;
        if (guildMusicManager.getScheduler().playTrack(track)) {
            if (queueOnly)
                channel.sendMessage("`" + trackTitle + "` was added to the queue.").queue();
            else
                channel.sendMessage("Now playing `" + trackTitle + "`.").queue();
        } else {
            channel.sendMessage("I couldn't queue this track because the queue is too full.").queue();
        }

    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

        FriendlyException fe = null;
        List<AudioTrack> tracks = playlist.getTracks();
        if (playlist.isSearchResult()) {
            trackLoaded(playlist.getTracks().get(0));
        } else {
            boolean noErrors = true;
            AudioTrack firstTrack;
            String trackTitle;
            int i = 0;
            boolean queueOnly = guildMusicManager.getScheduler().getTrackAmount() != 0;
            do {
                firstTrack = tracks.get(i);
                try {
                    if (guildMusicManager.getScheduler().playTrack(firstTrack)) {
                        channel.sendMessage(
                                "I couldn't queue any the tracks of `" + playlist.getName()
                                        + "` because the queue is already full.").queue();
                        return;
                    }
                } catch (FriendlyException e) {
                    noErrors = false;
                    firstTrack = null;
                    fe = e;
                }
                i++;
            } while (firstTrack == null && i < tracks.size());
            if (firstTrack == null) {
                if (fe == null) {
                    channel.sendMessage(
                            "You found an empty playlist, there are no tracks to play."
                    ).queue();
                    return;
                } else {
                    channel.sendMessage(
                            "None of the tracks of this playlist could be queued due to errors." +
                                    " Please try other url."
                    ).queue();
                    System.err.println(fe.getMessage());
                    fe.printStackTrace();
                }
            }
            String playingTrackTitle = guildMusicManager.getPlayer().getPlayingTrack().getInfo().title;
            if (playingTrackTitle == null || playingTrackTitle.isEmpty())
                playingTrackTitle = "Undefined";
            for (; i < tracks.size(); i++) {
                AudioTrack track = tracks.get(i);
                if (track != firstTrack)
                    try {
                        if (guildMusicManager.getScheduler().queue(track)) {
                            channel.sendMessage(
                                    "I couldn't queue all the tracks of `" + playlist.getName()
                                            + "` because the queue is now full."
                            ).queue();
                            if (!queueOnly)
                                channel.sendMessage("Now playing `" + playingTrackTitle + "`.").queue();
                            return;
                        }
                    } catch (FriendlyException e) {
                        noErrors = false;
                    }
            }
            if (noErrors)
                channel.sendMessage(
                        "Finished queueing all the tracks from `" + playlist.getName() + "`."
                ).queue();
            else
                channel.sendMessage("I couldn't queue all the tracks of `" + playlist.getName()
                        + "` because there were errors loading some of them.").queue();
            if (!queueOnly)
                channel.sendMessage("Now playing `" + playingTrackTitle + "`.").queue();
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