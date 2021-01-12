package ayaya.core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.HashMap;
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
     * Returns the music manager of a guild.
     *
     * @param guild the guild
     * @return guild music manager
     */
    public GuildMusicManager getGuildMusicManager(Guild guild) {

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
        if (guild.getAudioManager().isConnected()) {
            guild.getAudioManager().closeAudioConnection();
            return true;
        }
        return false;
    }

    public void play(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        if (!trackUrl.isEmpty())
            playerManager.loadItemOrdered(musicManager, trackUrl, new PlayHandler(channel, musicManager.getScheduler()));
    }

    public void queue(final Guild guild, final String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(guild);
    }

}