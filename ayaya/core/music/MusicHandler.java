package ayaya.core.music;

import ayaya.core.enums.TrustedHosts;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class of the music system handler.
 */
public class MusicHandler {

    private static final String HTTP = "http://";
    static final String HTTPS = "https://";
    static final String SOUNDCLOUD_SEARCH = "scsearch:";
    static final String YOUTUBE_SEARCH = "ytsearch:";

    private final AudioPlayerManager player;
    private final Map<String, GuildMusicManager> musicManagers;
    private final ReentrantLock lock;

    public MusicHandler() {
        player = new DefaultAudioPlayerManager();
        musicManagers = new HashMap<>();
        AudioSourceManagers.registerRemoteSources(player);
        AudioSourceManagers.registerLocalSource(player);
        lock = new ReentrantLock();
    }

    /**
     * Returns the player manager.
     *
     * @return player manager
     */
    public AudioPlayerManager getPlayer() {
        return player;
    }

    /**
     * Returns the music manager of a guild.
     *
     * @param guild the guild
     * @return guild music manager
     */
    private GuildMusicManager getGuildMusicManager(Guild guild) {

        String guildId = guild.getId();
        lock.lock();
        GuildMusicManager musicManager = musicManagers.get(guildId);
        lock.unlock();

        if (musicManager == null) {
            musicManager = new GuildMusicManager(player);
            lock.lock();
            musicManagers.put(guildId, musicManager);
            lock.unlock();
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;

    }

    /**
     * Method to connect to a voice channel in a server.
     *
     * @param guild   the server
     * @param channel the voice channel to connect to
     * @return true if the connection was open,
     * false if it was already open in that server.
     */
    public boolean connect(Guild guild, VoiceChannel channel) {
        if (guild.getAudioManager().isConnected()) return false;
        guild.getAudioManager().openAudioConnection(channel);
        return true;
    }

    /**
     * Method to disconnect from any voice channel in a server.
     *
     * @param guild the server
     * @return true if the disconnection was successful,
     * false if there wasn't any connection open in that server.
     */
    public boolean disconnect(Guild guild) {
        if (guild.getAudioManager().isConnected() && getGuildMusicManager(guild).getScheduler().musicPaused()) {
            guild.getAudioManager().closeAudioConnection();
            return true;
        }
        return false;
    }

    public void play(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        if (!trackUrl.isEmpty()) {
            if (trackUrl.startsWith(HTTP))
                channel.sendMessage("For security reasons, http urls aren't allowed. Try a https url instead.").queue();
            else {
                if (trackUrl.startsWith(HTTPS))
                    try {
                        URL url = new URL(trackUrl);
                        try {
                            TrustedHosts.valueOf(url.getHost());
                            player.loadItemOrdered(musicManager, trackUrl,
                                    new PlayHandler(trackUrl, channel, musicManager, player));
                        } catch (IllegalArgumentException e) {
                            channel.sendMessage("The url points to a non trusted host."
                                    + " I only accept youtube and soundcloud urls").queue();
                        }
                    } catch (MalformedURLException e) {
                        channel.sendMessage("The provided url isn't valid. Please try another url.").queue();
                    }
                else {
                    player.loadItemOrdered(musicManager, YOUTUBE_SEARCH + trackUrl,
                            new PlayHandler(YOUTUBE_SEARCH, trackUrl, channel, musicManager, player));
                }
            }
        } else {
            TrackScheduler trackScheduler = musicManager.getScheduler();
            if (trackScheduler.getTrackAmount() > 0) {
                if (!trackScheduler.musicPaused()) {
                    channel.sendMessage(
                            "Already playing `" + trackScheduler.getCurrentTrack().getInfo().title + "`."
                    ).queue();
                } else {
                    AudioTrack track;
                    if ((track = trackScheduler.getCurrentTrack()) != null)
                        resume(channel);
                    else
                        musicManager.getPlayer().playTrack((track = trackScheduler.getNextTrack()));
                    String playingTrackTitle = track.getInfo().title;
                    playingTrackTitle =
                            (playingTrackTitle == null || playingTrackTitle.isEmpty()) ? "Undefined" : playingTrackTitle;
                    channel.sendMessage(
                            "Now playing `" + playingTrackTitle + "`."
                    ).queue();
                }
            }
            else channel.sendMessage("There are no tracks to play right now.").queue();
        }
    }

    public void queue(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        if (!trackUrl.isEmpty()) {
            if (trackUrl.startsWith(HTTP))
                channel.sendMessage("For security reasons, http urls aren't allowed. Try a https url instead.").queue();
            else if (trackUrl.startsWith(HTTPS))
                try {
                    URL url = new URL(trackUrl);
                    try {
                        TrustedHosts.valueOf(url.getHost());
                        player.loadItemOrdered(musicManager, trackUrl,
                                new QueueHandler(trackUrl, channel, musicManager, player));
                    } catch (IllegalArgumentException e) {
                        channel.sendMessage("The url points to a non trusted host."
                                + " I only accept youtube and soundcloud urls").queue();
                    }
                } catch (MalformedURLException e) {
                    channel.sendMessage("The provided url isn't valid. Please try another url.").queue();
                }
            else {
                player.loadItemOrdered(musicManager, YOUTUBE_SEARCH + trackUrl,
                        new QueueHandler(YOUTUBE_SEARCH, trackUrl, channel, musicManager, player));
            }
        } else
            channel.sendMessage(
                    "<:AyaWhat:362990028915474432> You didn't say anything about the track you wanted to queue."
            ).queue();
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
        TrackScheduler scheduler = getGuildMusicManager(guild).getScheduler();
        AudioTrack removedTrack;
        if (trackNumber == 0) {
            removedTrack = scheduler.getCurrentTrack();
            if (isRepeating(guild))
                scheduler.nextTrack(true);
            else
                skip(guild);
        } else
            removedTrack = scheduler.dequeue(trackNumber);
        return removedTrack;

    }

    public Iterator<AudioTrack> getTrackIterator(final Guild guild) {
        return getGuildMusicManager(guild).getScheduler().getTrackIterator();
    }

    public int getTrackAmount(final Guild guild) {
        return getGuildMusicManager(guild).getScheduler().getTrackAmount();
    }

    /**
     * Pauses the music that is currently playing.
     *
     * @param channel the channel where the command was sent.
     * @return if the music has been paused or not
     */
    public boolean pause(TextChannel channel) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        if (musicManager.getScheduler().getTrackAmount() != 0 && !musicManager.getPlayer().isPaused()) {
            musicManager.getPlayer().setPaused(true);
            return true;
        }
        return false;
    }

    /**
     * Checks wether the music currently playing is paused or not.
     *
     * @param guild the guild where the command was sent.
     * @return true if the music is paused, false if not
     */
    public boolean musicPaused(Guild guild) {
        return getGuildMusicManager(guild).getScheduler().musicPaused();
    }

    /**
     * Resumes the currently playing music.
     *
     * @param channel the channel where the command was sent
     * @return if the music was resumed or not
     */
    public boolean resume(TextChannel channel) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        if (musicManager.getPlayer().isPaused()) {
            musicManager.getPlayer().setPaused(false);
            return true;
        }
        return false;
    }

    /**
     * Enables or disables the repeat mode of the queue.
     *
     * @param guild the guild where the command was sent
     * @return the new repeat boolean value
     */
    public boolean repeat(Guild guild) {
        return getGuildMusicManager(guild).getScheduler().repeat();
    }

    /**
     * Checks if the repeat mode is enabled or not.
     *
     * @param guild the guild where the command was sent
     * @return true if the queue is in repeat mode, false if not
     */
    public boolean isRepeating(Guild guild) {
        return getGuildMusicManager(guild).getScheduler().isRepeating();
    }

    public boolean noMusicPlaying(Guild guild) {
        return getGuildMusicManager(guild).getScheduler().noMusicPlaying();
    }

    public AudioTrack getCurrentTrack(Guild guild) {
        return getGuildMusicManager(guild).getScheduler().getCurrentTrack();
    }

    public boolean stopMusic(Guild guild) {
        TrackScheduler scheduler = getGuildMusicManager(guild).getScheduler();
        if (!scheduler.noMusicPlaying() || scheduler.getTrackAmount() != 0) {
            scheduler.stopAndClear();
            return true;
        } else
            return false;
    }

    /**
     * Skips the current music.
     *
     * @param guild the guild where the command was sent
     * @return true if a new track started playing, false if not
     */
    public boolean skip(Guild guild) {
        return getGuildMusicManager(guild).getScheduler().nextTrack(false);
    }

    /**
     * Sets the volume of the player.
     *
     * @param guild  the guild of the player
     * @return player volume
     */
    public int getVolume(Guild guild) {
        return getGuildMusicManager(guild).getScheduler().getVolume();
    }

    /**
     * Sets the volume of the player.
     *
     * @param guild  the guild of the player
     * @param volume the volume to set
     */
    public void setVolume(Guild guild, int volume) {
        getGuildMusicManager(guild).getScheduler().setVolume(volume);
    }

    /**
     * Seeks x seconds ahead or back (depending on the signal of the amount requested).
     *
     * @param guild   the guild associated with the player
     * @param seconds the amount of seconds
     * @return true if the operation was successful, false on the contrary.
     */
    public boolean seek(Guild guild, long seconds) {
        GuildMusicManager musicManager = getGuildMusicManager(guild);
        AudioTrack track = musicManager.getPlayer().getPlayingTrack();
        if (!track.isSeekable()) return false;
        long currentTime = track.getPosition();
        track.setPosition(Math.max(0, Math.min(currentTime + seconds * 1000, track.getDuration())));
        return true;
    }

}