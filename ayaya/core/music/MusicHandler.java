package ayaya.core.music;

import ayaya.core.exceptions.music.FullQueueException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class of the music system handler.
 */
public class MusicHandler {

    private static final String URL_PREFIX = "https://";
    private static final String SOUNDCLOUD_SEARCH = "scsearch:";
    private static final String YOUTUBE_SEARCH = "ytsearch:";

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final ReentrantLock lock;

    public MusicHandler() {
        playerManager = new DefaultAudioPlayerManager();
        musicManagers = new HashMap<>();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        lock = new ReentrantLock();
    }

    /**
     * Returns the player manager.
     *
     * @return player manager
     */
    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Returns the guild music manager of a guild.
     *
     * @param guild the guild
     * @return guild music manager
     */
    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {

        long guildId = guild.getIdLong();
        lock.lock();
        GuildMusicManager musicManager = musicManagers.get(guildId);
        lock.unlock();

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            lock.lock();
            musicManagers.put(guildId, musicManager);
            lock.unlock();
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;

    }

    /**
     * Joins the specified voice channel of a specified guild.
     *
     * @param guild   the guild
     * @param channel the channel to join
     */
    public void join(Guild guild, VoiceChannel channel) throws InsufficientPermissionException {
        guild.getAudioManager().openAudioConnection(channel);
    }

    /**
     * Leaves the specified channel of the specified guild.
     *
     * @param guild   the guild
     * @param channel the channel to leave
     */
    public void leave(Guild guild, TextChannel channel) {
        if (getGuildAudioPlayer(guild).scheduler.musicStopped()) {
            channel.sendMessage("Disconnecting from voice channel.").queue();
            guild.getAudioManager().closeAudioConnection();
        } else {
            channel.sendMessage("There's still music being played right now.").queue();
        }
    }

    /**
     * Adds a new music or a new playlist to the queue.
     *
     * @param channel  the channel where the command was sent
     * @param trackUrl the url of the track/playlist to queue
     */
    public void queue(final TextChannel channel, final String trackUrl) {

        Guild guild = channel.getGuild();
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (!trackUrl.isEmpty())
            playerManager.loadItemOrdered(guild, trackUrl, new AudioLoadResultHandler() {

                @Override
                public void trackLoaded(AudioTrack track) {

                    String track_title = track.getInfo().title;
                    if (track_title == null || track_title.isEmpty())
                        track_title = "Undefined";
                    queueTrack(musicManager, track);
                    channel.sendMessage("`" + track_title + "` was added to the queue.").queue();

                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {

                    FriendlyException fe = null;
                    List<AudioTrack> tracks = playlist.getTracks();
                    if (trackUrl.startsWith(URL_PREFIX)) {
                        boolean noErrors = true;
                        AudioTrack firstTrack;
                        int i = 0;
                        int queued = 0;
                        for (; i < tracks.size(); i++) {
                            AudioTrack track = tracks.get(i);
                            try {
                                queueTrack(musicManager, track);
                                queued++;
                            } catch (FullQueueException e) {
                                channel.sendMessage(
                                        "Couldn't queue all the tracks of `" + playlist.getName()
                                                + "` because it was too big."
                                ).queue();
                                return;
                            } catch (FriendlyException e) {
                                noErrors = false;
                                fe = e;
                            }
                        }
                        if (queued == 0) {
                            channel.sendMessage(
                                    "None of the tracks of this playlist could be queued due to errors." +
                                            " Please try other url."
                            ).queue();
                            throw fe;
                        } else if (noErrors)
                            channel.sendMessage(
                                    "Queued all the tracks from `" + playlist.getName() + "`."
                            ).queue();
                        else
                            channel.sendMessage(
                                    "Couldn't queue all the tracks of `" + playlist.getName()
                                    + "` because there were errors loading some of them."
                            ).queue();
                    } else {
                        boolean success = false;
                        for (AudioTrack track : tracks) {
                            try {
                                trackLoaded(track);
                                success = true;
                                break;
                            } catch (FriendlyException e) {
                                fe = e;
                            }
                        }
                        if (!success) {
                            if (trackUrl.startsWith(SOUNDCLOUD_SEARCH)) {
                                channel.sendMessage("I couldn't queue this track due to an error. " +
                                        "Try providing an url or try a different search query.").queue();
                                throw fe;
                            }
                            else if (trackUrl.startsWith(YOUTUBE_SEARCH)) {
                                String query = SOUNDCLOUD_SEARCH + trackUrl.replace(YOUTUBE_SEARCH, "");
                                queue(channel, query);
                            } else {
                                queue(channel, YOUTUBE_SEARCH + trackUrl);
                            }
                        }
                    }

                }

                @Override
                public void noMatches() {
                    if (trackUrl.startsWith(URL_PREFIX) || trackUrl.startsWith(SOUNDCLOUD_SEARCH))
                        channel.sendMessage("No audio found for that query or url.").queue();
                    else if (trackUrl.startsWith(YOUTUBE_SEARCH)) {
                        String query = SOUNDCLOUD_SEARCH + trackUrl.replace(YOUTUBE_SEARCH, "");
                        queue(channel, query);
                    } else {
                        queue(channel, YOUTUBE_SEARCH + trackUrl);
                    }
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    if (trackUrl.startsWith(SOUNDCLOUD_SEARCH)) {
                        channel.sendMessage(
                                "I couldn't queue this track due to an error. " +
                                        "Try providing an url or try a different search query." +
                                        "\nIf the problem persists, " +
                                        "it's likely that my ip has got banned by youtube and soundcloud."
                        ).queue();
                        throw exception;
                    }
                    else if (trackUrl.startsWith(URL_PREFIX)) {
                        channel.sendMessage(
                                "I couldn't queue this track/playlist due to an error. " +
                                        "Please try other url."
                        ).queue();
                        throw exception;
                    }
                    else if (trackUrl.startsWith(YOUTUBE_SEARCH)) {
                        String query = SOUNDCLOUD_SEARCH + trackUrl.replace(YOUTUBE_SEARCH, "");
                        queue(channel, query);
                    } else {
                        queue(channel, YOUTUBE_SEARCH + trackUrl);
                    }
                }

            });

    }

    /**
     * Queues an audio track.
     *
     * @param musicManager the music manager to manage the track
     * @param track        the track to be queued
     */
    private void queueTrack(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.queue(track);
    }

    /**
     * Dequeues a certain track, given it's number.
     *
     * @param channel     the channel where the command was executed
     * @param trackNumber the number of the track in the queue
     * @return removed track
     */
    public AudioTrack dequeue(final TextChannel channel, int trackNumber) {

        Guild guild = channel.getGuild();
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        AudioTrack removedTrack;
        if (trackNumber == 0) {
            if (isRepeating(guild)) {
                skip(guild);
                trackNumber = musicManager.scheduler.amountOfTracksInQueue() - 1;
                removedTrack = musicManager.scheduler.dequeue(trackNumber);
            } else {
                removedTrack = musicManager.scheduler.getCurrentTrack();
                skip(guild);
            }
        } else
            removedTrack = musicManager.scheduler.dequeue(trackNumber);
        return removedTrack;

    }

    /**
     * Queues and starts playing a track. If the url is of a playlist then the first track of the playlist is played.
     *
     * @param channel  the channel where the command was sent
     * @param trackUrl the url of the track/playlist
     */
    public void play(final TextChannel channel, final String trackUrl) {

        Guild guild = channel.getGuild();
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (!trackUrl.isEmpty()) {
            playerManager.loadItemOrdered(guild, trackUrl, new AudioLoadResultHandler() {

                @Override
                public void trackLoaded(AudioTrack track) {

                    String track_title = track.getInfo().title;
                    if (track_title == null || track_title.isEmpty())
                        track_title = "Undefined";
                    boolean queueOnly = !musicManager.scheduler.getTracks().isEmpty();
                    playTrack(musicManager, track);
                    if (queueOnly) channel.sendMessage("`" + track_title + "` was added to the queue.").queue();
                    else {
                        channel.sendMessage("Now playing `" + track_title + "`.").queue();
                    }

                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {

                    FriendlyException fe = null;
                    List<AudioTrack> tracks = playlist.getTracks();
                    if (trackUrl.startsWith(URL_PREFIX)) {
                        boolean noErrors = true;
                        AudioTrack firstTrack;
                        String track_title;
                        int i = 0;
                        boolean queueOnly = !musicManager.scheduler.getTracks().isEmpty();
                        do {
                            firstTrack = tracks.get(i);
                            try {
                                playTrack(musicManager, firstTrack);
                            } catch (FullQueueException e) {
                                channel.sendMessage(
                                        "Couldn't queue any the tracks of `" + playlist.getName()
                                                + "` because the queue is already full.").queue();
                                return;
                            } catch (FriendlyException e) {
                                noErrors = false;
                                firstTrack = null;
                                fe = e;
                            }
                            i++;
                        } while (firstTrack == null && i < tracks.size());
                        if (firstTrack == null) {
                            channel.sendMessage(
                                    "None of the tracks of this playlist could be queued due to errors." +
                                            " Please try other url."
                            ).queue();
                            throw fe;
                        }
                        String playing_track_title = musicManager.player.getPlayingTrack().getInfo().title;
                        if (playing_track_title == null || playing_track_title.isEmpty())
                            playing_track_title = "Undefined";
                        for (; i < tracks.size(); i++) {
                            AudioTrack track = tracks.get(i);
                            if (track != firstTrack)
                                try {
                                    queueTrack(musicManager, track);
                                } catch (FullQueueException e) {
                                    channel.sendMessage(
                                            "Couldn't queue all the tracks of `" + playlist.getName()
                                                    + "` because the queue is now full."
                                    ).queue();
                                    if (!queueOnly)
                                        channel.sendMessage("Now playing `" + playing_track_title + "`.").queue();
                                    return;
                                } catch (FriendlyException e) {
                                    noErrors = false;
                                }
                        }
                        if (noErrors)
                            channel.sendMessage(
                                    "Finished queueing all the tracks from `" + playlist.getName() + "`."
                            ).queue();
                        else
                            channel.sendMessage("Couldn't queue all the tracks of `" + playlist.getName()
                                    + "` because there were errors loading some of them.").queue();
                        if (!queueOnly) channel.sendMessage("Now playing `" + playing_track_title + "`.").queue();
                    } else {
                        boolean success = false;
                        for (AudioTrack track : tracks) {
                            try {
                                trackLoaded(track);
                                success = true;
                                break;
                            } catch (FriendlyException e) {
                                fe = e;
                            }
                        }
                        if (!success) {
                            if (trackUrl.startsWith(SOUNDCLOUD_SEARCH)) {
                                channel.sendMessage("I couldn't queue this track due to an error. " +
                                        "Try providing an url or try a different search query.").queue();
                                throw fe;
                            }
                            else if (trackUrl.startsWith(YOUTUBE_SEARCH)) {
                                String query = SOUNDCLOUD_SEARCH + trackUrl.replace(YOUTUBE_SEARCH, "");
                                play(channel, query);
                            } else {
                                play(channel, YOUTUBE_SEARCH + trackUrl);
                            }
                        }
                    }

                }

                @Override
                public void noMatches() {
                    if (trackUrl.startsWith(URL_PREFIX) || trackUrl.startsWith(SOUNDCLOUD_SEARCH))
                        channel.sendMessage("I didn't find anything for that query or url.").queue();
                    else if (trackUrl.startsWith(YOUTUBE_SEARCH)) {
                        String query = SOUNDCLOUD_SEARCH + trackUrl.replace(YOUTUBE_SEARCH, "");
                        play(channel, query);
                    } else {
                        play(channel, YOUTUBE_SEARCH + trackUrl);
                    }
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    if (trackUrl.startsWith(SOUNDCLOUD_SEARCH)) {
                        channel.sendMessage(
                                "I couldn't queue this track due to an error. " +
                                        "Try providing an url or try a different search query." +
                                        "\nIf the problem persists, " +
                                        "it's likely that my ip has got banned by youtube and soundcloud."
                        ).queue();
                        throw exception;
                    }
                    else if (trackUrl.startsWith(URL_PREFIX)) {
                        channel.sendMessage(
                                "I couldn't queue this track/playlist due to an error. " +
                                        "Please try other url."
                        ).queue();
                        throw exception;
                    }
                    else if (trackUrl.startsWith(YOUTUBE_SEARCH)) {
                        String query = SOUNDCLOUD_SEARCH + trackUrl.replace(YOUTUBE_SEARCH, "");
                        queue(channel, query);
                    } else {
                        queue(channel, YOUTUBE_SEARCH + trackUrl);
                    }
                }

            });
        }
        else {
            if (musicManager.scheduler.amountOfTracksInQueue() > 0) {
                if (!musicManager.scheduler.musicStopped()) {
                    channel.sendMessage(
                            "Already playing `" + musicManager.scheduler.getCurrentTrack().getInfo().title + "`."
                    ).queue();
                } else {
                    playTrack(musicManager, null);
                    AudioTrack track = musicManager.scheduler.getCurrentTrack();
                    String playing_track_title = track.getInfo().title;
                    if (playing_track_title == null || playing_track_title.isEmpty())
                        playing_track_title = "Undefined";
                    channel.sendMessage(
                            "Now playing `" + playing_track_title + "`."
                    ).queue();
                }
            }
            else channel.sendMessage("There are no tracks to play right now.").queue();
        }
    }

    /**
     * Plays an audio track.
     *
     * @param musicManager the music manager that manages the tracks
     * @param track        the track to be played
     */
    private void playTrack(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.playTrack(track);
    }

    /**
     * Returns a list of the tracks in the queue, including the one currently playing.
     *
     * @param guild the guild where the command was sent
     * @return track list
     */
    public List<AudioTrack> trackList(Guild guild) {
        return getGuildAudioPlayer(guild).scheduler.getTracks();
    }

    /**
     * Enables or disables the repeat mode of the queue.
     *
     * @param guild the guild where the command was sent
     */
    public void repeat(Guild guild) {
        getGuildAudioPlayer(guild).scheduler.repeat();
    }

    /**
     * Checks if the repeat mode is enabled or not.
     *
     * @param guild the guild where the command was sent
     * @return true if the queue is in repeat mode, false if not
     */
    public boolean isRepeating(Guild guild) {
        return getGuildAudioPlayer(guild).scheduler.isRepeating();
    }

    /**
     * Pauses the music that is currently playing.
     *
     * @param channel the channel where the command was sent.
     */
    public void pause(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        if (musicManager.scheduler.getTracks().size() == 0) {
            channel.sendMessage("There is no track in the queue to pause.").queue();
            return;
        }
        if (!musicManager.player.isPaused()) {
            musicManager.player.setPaused(true);
            channel.sendMessage("The music was paused.").queue();
        }
        else channel.sendMessage("The music is already paused.").queue();
    }

    /**
     * Checks wether the music currently playing is paused or not.
     *
     * @param guild the guild where the command was sent.
     * @return true if the music is paused, false if not
     */
    public boolean musicStopped(Guild guild) {
        return getGuildAudioPlayer(guild).scheduler.musicStopped();
    }

    /**
     * Resumes the currently playing music.
     *
     * @param channel the channel where the command was sent
     */
    public void resume(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        if (musicManager.player.isPaused()) {
            musicManager.player.setPaused(false);
            channel.sendMessage("The music was resumed.").queue();
        }
        else if (musicManager.scheduler.noMusicPlaying())
            channel.sendMessage("There is no music playing.").queue();
        else
            channel.sendMessage("The music is already playing.").queue();
    }

    /**
     * Skips the current music.
     *
     * @param guild the guild where the command was sent
     * @return true if a new track started playing, false if not
     */
    public boolean skip(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        AudioTrack current = musicManager.scheduler.getCurrentTrack();
        return musicManager.scheduler.nextTrack(current);
    }

    /**
     * Clears the queue.
     *
     * @param guild the guild where the command was sent
     */
    public void stop(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.scheduler.stopAllTracks();
    }

    /**
     * Seeks x seconds ahead or back (depending on the signal of the amount requested).
     *
     * @param guild   the guild associated with the player
     * @param seconds the amount of seconds
     * @return true if the operation was successful, false on the contrary.
     */
    public boolean seek(Guild guild, long seconds) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        AudioTrack track = musicManager.player.getPlayingTrack();
        if (!track.isSeekable()) return false;
        long currentTime = track.getPosition();
        track.setPosition(Math.max(0, Math.min(currentTime + seconds * 1000, track.getDuration())));
        return true;
    }

    /**
     * Sets the volume of the player.
     *
     * @param guild  the guild of the player
     * @return player volume
     */
    public int getVolume(Guild guild) {
        return getGuildAudioPlayer(guild).scheduler.getVolume();
    }

    /**
     * Sets the volume of the player.
     *
     * @param guild  the guild of the player
     * @param volume the volume to set
     */
    public void setVolume(Guild guild, int volume) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.scheduler.setVolume(volume);
    }

}