package ayaya.core.music;

import ayaya.core.exceptions.music.NoAudioMatchingException;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
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

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final String SOUNDCLOUD_SEARCH = "scsearch:";
    private static final String YOUTUBE_SEARCH = "ytsearch:";

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
    public GuildMusicManager getGuildMusicManager(Guild guild) {

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
        if (guild.getAudioManager().isConnected()) {
            guild.getAudioManager().closeAudioConnection();
            return true;
        }
        return false;
    }

    public void play(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        if (!trackUrl.isEmpty()) {
            if (trackUrl.startsWith(HTTP)) {
                channel.sendMessage("For security reasons, http urls aren't allowed. Try an https url instead.").queue();
            } else if (trackUrl.startsWith(HTTPS))
                player.loadItemOrdered(musicManager, trackUrl, new PlayHandler(trackUrl, channel, musicManager));
            else {
                playFromQuery(channel, trackUrl, musicManager);
            }
        }
    }

    private void playFromQuery(final TextChannel channel, final String trackUrl, final GuildMusicManager musicManager) {
        try {
            if (!trackUrl.startsWith(YOUTUBE_SEARCH) && !trackUrl.startsWith(SOUNDCLOUD_SEARCH))
                player.loadItemOrdered(musicManager, YOUTUBE_SEARCH + trackUrl,
                    new PlayHandler(trackUrl, channel, musicManager));
            else
                player.loadItemOrdered(musicManager, trackUrl, new PlayHandler(trackUrl, channel, musicManager));
        } catch (FriendlyException e) {
            e.printStackTrace();
            if (trackUrl.startsWith(SOUNDCLOUD_SEARCH))
                channel.sendMessage(
                        "The search for the provided query failed."
                                + "Try providing an url or try a different search query."
                                + "\nIf the problem persists, "
                                + "it's likely that my ip has got temporarily banned by youtube and soundcloud."
                ).queue();
            else
                playFromQuery(channel, SOUNDCLOUD_SEARCH + trackUrl, musicManager);
        } catch (NoAudioMatchingException e) {
            if (trackUrl.startsWith(SOUNDCLOUD_SEARCH))
                channel.sendMessage(
                        "The search for the provided query returned no results."
                                + "Try providing an url or try a different search query."
                ).queue();
            else
                playFromQuery(channel, SOUNDCLOUD_SEARCH + trackUrl, musicManager);
        }
    }

    public void queue(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        if (!trackUrl.isEmpty()) {
            if (trackUrl.startsWith(HTTP)) {
                channel.sendMessage("For security reasons, http urls aren't allowed. Try an https url instead.").queue();
            } else if (trackUrl.startsWith(HTTPS))
                player.loadItemOrdered(musicManager, trackUrl, new QueueHandler(trackUrl, channel, musicManager));
            else {
                queueFromQuery(channel, trackUrl, musicManager);
            }
        }
    }

    private void queueFromQuery(final TextChannel channel, final String trackUrl, final GuildMusicManager musicManager) {
        try {
            if (!trackUrl.startsWith(YOUTUBE_SEARCH) && !trackUrl.startsWith(SOUNDCLOUD_SEARCH))
                player.loadItemOrdered(musicManager, YOUTUBE_SEARCH + trackUrl,
                        new PlayHandler(trackUrl, channel, musicManager));
            else
                player.loadItemOrdered(musicManager, trackUrl, new QueueHandler(trackUrl, channel, musicManager));
        } catch (FriendlyException e) {
            e.printStackTrace();
            if (trackUrl.startsWith(SOUNDCLOUD_SEARCH))
                channel.sendMessage(
                        "The search for the provided query failed."
                                + "Try providing an url or try a different search query."
                                + "\nIf the problem persists, "
                                + "it's likely that my ip has got temporarily banned by youtube and soundcloud."
                ).queue();
            else
                queueFromQuery(channel, SOUNDCLOUD_SEARCH + trackUrl, musicManager);
        } catch (NoAudioMatchingException e) {
            if (trackUrl.startsWith(SOUNDCLOUD_SEARCH))
                channel.sendMessage(
                        "The search for the provided query returned no results."
                                + "Try providing an url or try a different search query."
                ).queue();
            else
                queueFromQuery(channel, SOUNDCLOUD_SEARCH + trackUrl, musicManager);
        }
    }

}