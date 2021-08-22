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
    private boolean queueOnly;

    PlayHandler(
            String url, TextChannel channel, GuildMusicManager guildMusicManager, AudioPlayerManager audioPlayerManager,
            boolean queueOnly
    ) {
        this.searchMode = "";
        this.url = url;
        this.channel = channel;
        this.guildMusicManager = guildMusicManager;
        this.audioPlayerManager = audioPlayerManager;
        this.queueOnly = queueOnly;
    }

    PlayHandler(
            String searchMode, String url, TextChannel channel,
            GuildMusicManager guildMusicManager, AudioPlayerManager audioPlayerManager, boolean queueOnly
    ) {

        this.searchMode = searchMode;
        this.url = url;
        this.channel = channel;
        this.guildMusicManager = guildMusicManager;
        this.audioPlayerManager = audioPlayerManager;
        this.queueOnly = queueOnly;

    }

    @Override
    public void trackLoaded(AudioTrack track) {

        TrackScheduler trackScheduler = guildMusicManager.getScheduler();
        if (trackScheduler.queue(track)) {
            AudioTrack current = trackScheduler.getCurrentTrack();
            String addedTrackTitle = track.getInfo().title;
            addedTrackTitle = (addedTrackTitle == null || addedTrackTitle.isEmpty()) ? "Unknown" : addedTrackTitle;
            String s = "`" + addedTrackTitle + "` was added to the queue.";
            if (!queueOnly) {
                if (current == null) {
                    trackScheduler.startFirst();
                    String nextTrackTitle = trackScheduler.getCurrentTrack().getInfo().title;
                    nextTrackTitle = (nextTrackTitle == null || nextTrackTitle.isEmpty()) ? "Unknown" : nextTrackTitle;
                    s += "\nNow playing `" + nextTrackTitle + "`.";
                }
                else if (trackScheduler.isPaused()) {
                    trackScheduler.unpause();
                    String currentTrackTitle = trackScheduler.getCurrentTrack().getInfo().title;
                    currentTrackTitle = (currentTrackTitle == null || currentTrackTitle.isEmpty()) ? "Unknown" : currentTrackTitle;
                    s += "\nResumed playing `" + currentTrackTitle + "`.";
                }
            }
            channel.sendMessage(s).queue();
        }
        else
            channel.sendMessage("I couldn't queue this track because the queue is too full.").queue();

    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

        if (playlist.isSearchResult()) {
            List<AudioTrack> tracks = playlist.getTracks();
            if (tracks.isEmpty())
                this.searchWithNoAudioMatches();
            else {
                this.trackLoaded(tracks.get(0));
            }
        }
        else {
            this.queuePlaylist(playlist);
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
                    new PlayHandler(SOUNDCLOUD_SEARCH, url, channel, guildMusicManager, audioPlayerManager, queueOnly));

    }

    private void queuePlaylist(AudioPlaylist playlist) {

        List<AudioTrack> tracks = playlist.getTracks();
        String playlistName = playlist.getName();
        playlistName = (playlistName == null || playlistName.isEmpty()) ? "Unknown" : playlistName;
        if (tracks.isEmpty())
            channel.sendMessage("You found an empty playlist, there's nothing to queue.").queue();
        else {
            TrackScheduler trackScheduler = guildMusicManager.getScheduler();
            AudioTrack first = tracks.get(0);
            int amountQueued = 0;
            for (AudioTrack track: tracks) {
                if (trackScheduler.queue(track))
                    amountQueued++;
                else if (amountQueued == 0) {
                    channel.sendMessage(
                            "I couldn't queue any tracks from the playlist "
                                    + playlistName
                                    + " because the queue is full."
                    ).queue();
                    return;
                }
                else {
                    this.start(trackScheduler, first, playlist, false);
                    return;
                }
            }
            this.start(trackScheduler, first, playlist, true);
        }

    }

    private void start(TrackScheduler trackScheduler, AudioTrack firstPlaylistTrack,
                       AudioPlaylist playlist, boolean success) {

        String playlistName = playlist.getName();
        playlistName = (playlistName == null || playlistName.isEmpty()) ? "Unknown" : playlistName;
        AudioTrack current = trackScheduler.getCurrentTrack();
        String trackTitle;
        String s = (success ? "Finished queueing all the tracks from `" + playlistName + "`." :
                "I couldn't queue all tracks from the playlist "
                        + playlistName
                        + " because the queue is now full.");
        if (current == null) {
            if (queueOnly) {
                trackTitle = firstPlaylistTrack.getInfo().title;
                trackTitle = (trackTitle == null || trackTitle.isEmpty()) ? "Unknown" : trackTitle;
                s += "\nThe first track to play is `" + trackTitle + "`.";
            }
            else {
                trackScheduler.startFirst();
                trackTitle = trackScheduler.getCurrentTrack().getInfo().title;
                trackTitle = (trackTitle == null || trackTitle.isEmpty()) ? "Unknown" : trackTitle;
                s += "\nNow playing `" + trackTitle + "`.";
            }
        }
        else {
            trackTitle = trackScheduler.getCurrentTrack().getInfo().title;
            trackTitle = (trackTitle == null || trackTitle.isEmpty()) ? "Unknown" : trackTitle;
            if (queueOnly)
                s += "\nThe first track to play is `" + trackTitle + "`.";
            else if (trackScheduler.isPaused()) {
                trackScheduler.unpause();
                s += "\nResumed playing `" + trackTitle + "`.";
            }
        }
        channel.sendMessage(s).queue();

    }

    private void searchWithNoAudioMatches() {

        if (searchMode.equals(YOUTUBE_SEARCH))
            audioPlayerManager.loadItemOrdered(guildMusicManager, SOUNDCLOUD_SEARCH + url,
                    new PlayHandler(SOUNDCLOUD_SEARCH, url, channel, guildMusicManager, audioPlayerManager, queueOnly));
        else if (searchMode.equals(SOUNDCLOUD_SEARCH))
            channel.sendMessage(
                    "The search for the provided query returned no results."
                            + "Try providing a url or try a different search query."
            ).queue();

    }

}