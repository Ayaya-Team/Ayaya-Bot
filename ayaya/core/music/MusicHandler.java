package ayaya.core.music;

import ayaya.core.enums.TrustedHosts;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, String> channelIDs;

    public MusicHandler() {
        player = new DefaultAudioPlayerManager();
        musicManagers = new ConcurrentHashMap<>();
        channelIDs = new ConcurrentHashMap<>();
        AudioSourceManagers.registerRemoteSources(player);
        AudioSourceManagers.registerLocalSource(player);
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
     * Returns the music manager of a server.
     *
     * @param guild the server
     * @return GuildMusicManager
     */
    private GuildMusicManager getGuildMusicManager(Guild guild) {

        String guildId = guild.getId();
        musicManagers.putIfAbsent(guildId, new GuildMusicManager(player, guild.getTextChannelById(channelIDs.get(guildId))));

        GuildMusicManager musicManager = musicManagers.get(guildId);
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;

    }

    /**
     * Removes a server music manager from the map.
     *
     * @param guild the server of the music manager
     */
    private void removeGuildMusicManager(Guild guild) {

        GuildMusicManager musicManager = musicManagers.remove(guild.getId());
        musicManager.getPlayer().destroy();

    }

    /**
     * Method to connect to a voice channel in a server.
     *
     * @param guild        the server
     * @param voiceChannel the voice channel to connect to
     * @param textChannel  the text channel to send messages when new tracks start
     * @return true if the connection was open,
     * false if it was already open in that server.
     */
    public boolean connect(Guild guild, VoiceChannel voiceChannel, TextChannel textChannel) {
        return this.connect(guild, voiceChannel, textChannel, false);
    }

    /**
     * Method to connect to a voice channel in a server.
     *
     * @param guild        the server
     * @param voiceChannel the voice channel to connect to
     * @param textChannel  the text channel to send messages when new tracks start
     * @param move         a boolean telling wether we want to move voice channel
     * @return true if the connection was open,
     * false if it was already open in that server.
     */
    public boolean connect(Guild guild, VoiceChannel voiceChannel, TextChannel textChannel, boolean move) {
        if (!move && guild.getAudioManager().isConnected()) return false;
        guild.getAudioManager().openAudioConnection(voiceChannel);
        channelIDs.put(guild.getId(), textChannel.getId());
        return true;
    }

    /**
     * Method to disconnect from any voice channel in a server.
     *
     * @param guild the server
     * @return true if the disconnection was successful,
     * false if there wasn't any connection open in that server or the music was still playing.
     */
    public boolean disconnect(Guild guild) {
        GuildMusicManager musicManager = getGuildMusicManager(guild);
        TrackScheduler trackScheduler = musicManager.getScheduler();
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected() && (trackScheduler.getCurrentTrack() == null
                || trackScheduler.isPaused() || trackScheduler.getTrackAmount() == 0)) {
            audioManager.closeAudioConnection();
            removeGuildMusicManager(guild);
            channelIDs.remove(guild.getId());
            return true;
        }
        return false;
    }

    /**
     * Method to force a disconnection in case a timeout needs to be enforced.
     *
     * @param guild the server
     */
    public void forceDisconnect(Guild guild) {
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()) {
            audioManager.closeAudioConnection();
            removeGuildMusicManager(guild);
        }
    }

    /**
     * Method to check if the queue of a server has repeat mode enabled or not.
     *
     * @param guild the server
     * @return true if repeating, false if not
     */
    public boolean queueIsRepeating(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().isRepeat();
    }

    /**
     * Method to enable or disable the repeat mode of a server
     *
     * @param guild the server
     * @return true if the queue is now repeating, false if not
     */
    public boolean repeatQueue(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().repeat();
    }

    /**
     * Method to check if a queue of a server is paused.
     *
     * @param guild the server
     * @return true if the queue is paused, false if not
     */
    public boolean queueIsPaused(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().isPaused();
    }

    /**
     * Method to pause a queue of a server.
     *
     * @param guild the server
     * @return true if the queue was successfully paused, false if not
     */
    public boolean pauseQueue(final Guild guild) {
        TrackScheduler trackScheduler = this.getGuildMusicManager(guild).getScheduler();
        if (trackScheduler.getCurrentTrack() != null && !trackScheduler.isPaused()) {
            trackScheduler.pause();
            return true;
        }
        return false;
    }

    /**
     * Method to unpause a queue of a server.
     *
     * @param guild the server
     * @return true if the queue was successfully unpaused, false if not
     */
    public boolean unpauseQueue(final Guild guild) {
        TrackScheduler trackScheduler = this.getGuildMusicManager(guild).getScheduler();
        if (trackScheduler.getCurrentTrack() == null || !trackScheduler.isPaused()) {
            return false;
        }
        trackScheduler.unpause();
        return true;
    }

    /**
     * Method to retrieve the music volume in a server.
     *
     * @param guild the server
     * @return music volume
     */
    public int getMusicVolume(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().getVolume();
    }

    /**
     * Method to set the new music volume for a server.
     *
     * @param guild  the server
     * @param volume the new volume
     */
    public void setMusicVolume(final Guild guild, final int volume) {
        this.getGuildMusicManager(guild).getScheduler().setVolume(volume);
    }

    /**
     * Method to retrieve the music being played in a server.
     *
     * @param guild the server
     * @return AudioTrack
     */
    public AudioTrack getCurrentMusic(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().getCurrentTrack();
    }

    /**
     * Method to seek in the current music of a server.
     *
     * @param guild   the server
     * @param seconds the amount in seconds to seek
     * @return true if the operatio succeeded, false if not
     */
    public boolean seekInMusic(final Guild guild, final long seconds) {
        return this.getGuildMusicManager(guild).getScheduler().seek(seconds);
    }

    /**
     * Method to queue and/or play a music from a given url/query.
     *
     * @param channel  channel where to play the music
     * @param trackUrl the url/query of the music
     * @param play     flag stating wether or not to start playing the music
     */
    public void queueAndPlay(final TextChannel channel, final String trackUrl, final boolean play) {

        GuildMusicManager guildMusicManager = this.getGuildMusicManager(channel.getGuild());
        TrackScheduler trackScheduler = guildMusicManager.getScheduler();

        if (!trackUrl.isEmpty()) {
            if (trackUrl.startsWith(HTTP)) {
                channel.sendMessage("For security reasons, http urls aren't allowed. Try a https url instead.").queue();
                return;
            } else if (trackUrl.startsWith(HTTPS)) {
                try {
                    URL url = new URL(trackUrl);
                    if (TrustedHosts.hostnameTrusted(url.getHost()))
                        player.loadItemOrdered(guildMusicManager, trackUrl,
                                play ? new PlayHandler(trackUrl, channel, guildMusicManager, player, false)
                                        : new PlayHandler(trackUrl, channel, guildMusicManager, player, true));
                    else
                        channel.sendMessage("The url points to a non trusted host."
                                + " I only accept youtube, soundcloud, vimeo or twitch urls.").queue();
                } catch (MalformedURLException e) {
                    channel.sendMessage("The provided url isn't valid. Please try another url.").queue();
                }
            } else {
                player.loadItemOrdered(guildMusicManager, YOUTUBE_SEARCH + trackUrl,
                        play ? new PlayHandler(YOUTUBE_SEARCH, trackUrl, channel, guildMusicManager, player, false)
                                : new PlayHandler(YOUTUBE_SEARCH, trackUrl, channel, guildMusicManager, player, true));
            }
            if (play && trackScheduler.getCurrentTrack() != null) {
                trackScheduler.unpause();
            }
        }
        else if (play && trackScheduler.getTrackAmount() > 0) {
            AudioTrack track = trackScheduler.getCurrentTrack();
            if (track != null && trackScheduler.isPaused()) {
                trackScheduler.unpause();
                channel.sendMessage("Resumed playing `" + track.getInfo().title + "`.").queue();
            }
            else if (trackScheduler.startFirst()) {
                String playingTrackTitle = trackScheduler.getCurrentTrack().getInfo().title;
                playingTrackTitle =
                        (playingTrackTitle == null || playingTrackTitle.isEmpty()) ? "Undefined" : playingTrackTitle;
                channel.sendMessage("Now playing `" + playingTrackTitle + "`.").queue();
            }
            else
                channel.sendMessage("Already playing `" + trackScheduler.getCurrentTrack().getInfo().title + "`.")
                        .queue();
        }
        else channel.sendMessage("There are no tracks to play right now.").queue();

    }

    /**
     * Method to skip the current music in a server.
     *
     * @param guild the server
     * @return true if the music was skipped, false if not
     */
    public boolean skipMusic(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().skip();
    }

    /**
     * Method to go back to the previous music in a server.
     *
     * @param guild the server
     * @return true if the operation succeeded, false if not
     */
    public boolean previousMusic(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().previousTrack();
    }

    /**
     * Method to stop the music and clear the queue in a server.
     *
     * @param guild the server
     * @return true if the operations succeeded, false if not
     */
    public boolean stopMusic(final Guild guild) {
        TrackScheduler scheduler = this.getGuildMusicManager(guild).getScheduler();
        if (scheduler.getCurrentTrack() != null || scheduler.getTrackAmount() != 0) {
            scheduler.stopAndClear();
            return true;
        } else
            return false;
    }

    /**
     * Method to get the amount of musics in the queue of a server.
     *
     * @param guild the server
     * @return amount of musics
     */
    public int getMusicAmount(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().getTrackAmount();
    }

    /**
     * Method to retrieve the iterator of musics for a given server.
     *
     * @param guild the server
     * @return Iterator<AudioTrack>
     */
    public Iterator<AudioTrack> getMusicIterator(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().getTrackIterator();
    }

    /**
     * Method to remove a music from a server queue.
     *
     * @param guild the server
     * @param index the index of the music to remove
     * @return the removed track
     */
    public AudioTrack dequeueMusic(final Guild guild, final int index) {
        return this.getGuildMusicManager(guild).getScheduler().dequeue(index);
    }

    /**
     * Method to move a music of a given index to another in a server's queue.
     *
     * @param guild the server
     * @param i     the index of the music
     * @param j     the index to move to
     * @return true if the operation succeeded, false if not
     */
    public boolean moveMusic(final Guild guild, final int i, final int j) {
        return this.getGuildMusicManager(guild).getScheduler().move(i, j);
    }

    /**
     * Method to shuffle a server's queue.
     *
     * @param guild the server
     */
    public void queueShuffle(final Guild guild) {
        this.getGuildMusicManager(guild).getScheduler().shuffle();
    }

    //TODO
    public boolean move(final Guild guild, final VoiceChannel currentVoiceChannel, final VoiceChannel nextVoiceChannel)
    {
        TrackScheduler scheduler = this.getGuildMusicManager(guild).getScheduler();
        TextChannel textChannel = guild.getTextChannelById(channelIDs.get(guild.getId()));
        scheduler.pause();
        this.disconnect(guild);
        if (this.connect(guild, nextVoiceChannel, textChannel, true)) {
            scheduler.unpause();
            return true;
        } else
            scheduler.unpause();
        return false;
    }

}