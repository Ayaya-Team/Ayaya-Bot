package ayaya.core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

import static ayaya.core.music.MusicHandler.*;

public class PlayHandler implements AudioLoadResultHandler {

    private String searchMode;
    private String url;
    private TextChannel channel;
    private GuildMusicManager guildMusicManager;
    private AudioPlayerManager audioPlayerManager;

    PlayHandler(
            String url, TextChannel channel, GuildMusicManager guildMusicManager, AudioPlayerManager audioPlayerManager
    ) {
        this.searchMode = "";
        this.url = url;
        this.channel = channel;
        this.guildMusicManager = guildMusicManager;
        this.audioPlayerManager = audioPlayerManager;
    }

    PlayHandler(
            String searchMode, String url, TextChannel channel,
            GuildMusicManager guildMusicManager, AudioPlayerManager audioPlayerManager
    ) {

        this.searchMode = searchMode;
        this.url = url;
        this.channel = channel;
        this.guildMusicManager = guildMusicManager;
        this.audioPlayerManager = audioPlayerManager;

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
                searchWithNoAudioMatches();
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
            searchWithNoAudioMatches();
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if (url.startsWith(HTTPS)) {
            channel.sendMessage("I couldn't queue this track due to an exception."
                    + "Try another url or try a search query.").queue();
            exception.printStackTrace();
        } else if (searchMode.equals(SOUNDCLOUD_SEARCH))
            channel.sendMessage(
                    "The search for the provided query failed. "
                            + "Try providing a url or try a different search query."
                            + "\nIf the problem persists, "
                            + "it's likely that youtube and soundcloud are unavailable for me at the moment."
            ).queue();
        else
            audioPlayerManager.loadItemOrdered(guildMusicManager, SOUNDCLOUD_SEARCH + url,
                    new PlayHandler(SOUNDCLOUD_SEARCH, url, channel, guildMusicManager, audioPlayerManager));
    }

    private void searchWithNoAudioMatches() {
        if (searchMode.equals(YOUTUBE_SEARCH))
            audioPlayerManager.loadItemOrdered(guildMusicManager, SOUNDCLOUD_SEARCH + url,
                    new PlayHandler(SOUNDCLOUD_SEARCH, url, channel, guildMusicManager, audioPlayerManager));
        else if (searchMode.equals(SOUNDCLOUD_SEARCH))
            channel.sendMessage(
                    "The search for the provided query returned no results."
                            + "Try providing a url or try a different search query."
            ).queue();
    }

}