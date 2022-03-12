package ayaya.core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.api.entities.TextChannel;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {

    /**
     * Audio player for the guild.
     */
    private final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    private final TrackScheduler scheduler;

    /**
     * Creates a player and a track scheduler.
     * @param playerManager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager playerManager, TextChannel channel) {
        player = playerManager.createPlayer();
        scheduler = new TrackScheduler(player, channel);
        player.addListener(scheduler);
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    /**
     * @return audio player
     */
    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * @return track scheduler
     */
    public TrackScheduler getScheduler() {
        return scheduler;
    }

}