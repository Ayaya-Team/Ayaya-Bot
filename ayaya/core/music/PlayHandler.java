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
        trackTitle = (trackTitle == null || trackTitle.isEmpty()) ? "Unknown" : trackTitle;

        boolean queueOnly = guildMusicManager.getScheduler().getTrackAmount() != 0;
        if (guildMusicManager.getScheduler().playTrack(track)) {
            channel.sendMessage(
                    queueOnly ? "`" + trackTitle + "` was added to the queue." : "Now playing `" + trackTitle + "`."
            ).queue();
        } else {
            channel.sendMessage("I couldn't queue this track because the queue is too full.").queue();
        }

    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

        List<AudioTrack> tracks = playlist.getTracks();
        if (playlist.isSearchResult()) {
            if (tracks.isEmpty())
                throw new NoAudioMatchingException("The provided query did not return any results.");
            else
                trackLoaded(tracks.get(0));
        } else {
            String playlistName = playlist.getName();
            playlistName = (playlistName == null || playlistName.isEmpty()) ? "Unknown" : playlistName;

            AudioTrack firstTrack = null;
            boolean queueOnly = guildMusicManager.getScheduler().getTrackAmount() != 0;

            for (AudioTrack track: tracks) {
                if (!guildMusicManager.getScheduler().playTrack(track)) {
                    if (firstTrack == null)
                        channel.sendMessage(
                                "I couldn't queue any tracks from the playlist "
                                        + playlistName
                                        + " because the queue is full."
                        ).queue();

                    else {
                        String trackTitle = firstTrack.getInfo().title;
                        trackTitle = (trackTitle == null || trackTitle.isEmpty()) ? "Unknown" : trackTitle;
                        channel.sendMessage(
                                "I couldn't queue all tracks from the playlist "
                                        + playlistName
                                        + " because the queue is now full."
                                        + (queueOnly ? "" : "\nNow playing `" + trackTitle + "`.")
                        ).queue();
                    }
                    return;
                }

                if (firstTrack == null)
                    firstTrack = track;
            }

            if (firstTrack == null)
                channel.sendMessage("You found an empty playlist, there's nothing to queue.").queue();
            else {
                String trackTitle = firstTrack.getInfo().title;
                trackTitle = (trackTitle == null || trackTitle.isEmpty()) ? "Unknown" : trackTitle;
                channel.sendMessage(
                        "Finished queueing all the tracks from `" + playlistName + "`."
                                + (queueOnly ? "" : "\nNow playing `" + trackTitle + "`.")
                ).queue();
            }
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