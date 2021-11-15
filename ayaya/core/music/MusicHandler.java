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

    public MusicHandler() {
        player = new DefaultAudioPlayerManager();
        musicManagers = new ConcurrentHashMap<>();
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
     * Returns the music manager of a guild.
     *
     * @param guild the guild
     * @return guild music manager
     */
    private GuildMusicManager getGuildMusicManager(Guild guild) {

        String guildId = guild.getId();
        musicManagers.putIfAbsent(guildId, new GuildMusicManager(player));

        GuildMusicManager musicManager = musicManagers.get(guildId);
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;

    }

    private void removeGuildMusicManager(Guild guild) {

        GuildMusicManager musicManager = musicManagers.remove(guild.getId());
        musicManager.getPlayer().destroy();

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
            return true;
        }
        return false;
    }

    public boolean queueIsRepeating(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().isRepeat();
    }

    public boolean repeatQueue(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().repeat();
    }

    public boolean queueIsPaused(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().isPaused();
    }

    public boolean pauseQueue(final Guild guild) {
        TrackScheduler trackScheduler = this.getGuildMusicManager(guild).getScheduler();
        if (trackScheduler.getCurrentTrack() != null && !trackScheduler.isPaused()) {
            trackScheduler.pause();
            return true;
        }
        return false;
    }

    public boolean unpauseQueue(final Guild guild) {
        TrackScheduler trackScheduler = this.getGuildMusicManager(guild).getScheduler();
        if (trackScheduler.getCurrentTrack() == null || !trackScheduler.isPaused()) {
            return false;
        }
        trackScheduler.unpause();
        return true;
    }

    public int getMusicVolume(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().getVolume();
    }

    public void setMusicVolume(final Guild guild, final int volume) {
        this.getGuildMusicManager(guild).getScheduler().setVolume(volume);
    }

    public AudioTrack getCurrentMusic(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().getCurrentTrack();
    }

    public boolean seekInMusic(final Guild guild, final long seconds) {
        return this.getGuildMusicManager(guild).getScheduler().seek(seconds);
    }

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

    public boolean skipMusic(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().skip();
    }

    public boolean previousMusic(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().previousTrack();
    }

    public boolean stopMusic(final Guild guild) {
        TrackScheduler scheduler = this.getGuildMusicManager(guild).getScheduler();
        if (scheduler.getCurrentTrack() != null || scheduler.getTrackAmount() != 0) {
            scheduler.stopAndClear();
            return true;
        } else
            return false;
    }

    public int getMusicAmount(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().getTrackAmount();
    }

    public Iterator<AudioTrack> getMusicIterator(final Guild guild) {
        return this.getGuildMusicManager(guild).getScheduler().getTrackIterator();
    }

    public AudioTrack dequeueMusic(final Guild guild, final int index) {
        return this.getGuildMusicManager(guild).getScheduler().dequeue(index);
    }

    public boolean moveMusic(final Guild guild, final int i, final int j) {
        return this.getGuildMusicManager(guild).getScheduler().move(i, j);
    }

    public void queueShuffle(final Guild guild) {
        this.getGuildMusicManager(guild).getScheduler().shuffle();
    }

    public boolean move(final Guild guild, final VoiceChannel currentVoiceChannel, final VoiceChannel nextVoiceChannel)
    {
        TrackScheduler scheduler = this.getGuildMusicManager(guild).getScheduler();
        scheduler.pause();
        this.disconnect(guild);
        if (this.connect(guild, nextVoiceChannel)) {
            scheduler.unpause();
            return true;
        } else if (this.connect(guild, currentVoiceChannel))
            scheduler.unpause();
        else
            scheduler.stopAndClear();
        return false;
    }

}