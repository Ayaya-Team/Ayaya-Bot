package ayaya.core.listeners;

import ayaya.core.music.MusicHandler;
import ayaya.core.utils.CustomThreadFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ayaya.core.Ayaya.THREAD_AMOUNT_PER_CORE;
import static ayaya.core.Ayaya.disconnectVoice;

/**
 * Class extending the ListenerAdapter to listen only to voice events events.
 */
public class VoiceEventListener extends ListenerAdapter {

    private static final int MAX_MULTIPLIER = 10;

    private ScheduledThreadPoolExecutor voiceTimeoutManager;
    private Map<String, ScheduledFuture<?>> scheduledTimeouts;
    private MusicHandler musicHandler;

    public VoiceEventListener(MusicHandler musicHandler) {

        int corePoolSize = THREAD_AMOUNT_PER_CORE;
        voiceTimeoutManager = new ScheduledThreadPoolExecutor(
                corePoolSize, new CustomThreadFactory("voice-timeout-thread"));
        voiceTimeoutManager.setMaximumPoolSize(THREAD_AMOUNT_PER_CORE * MAX_MULTIPLIER);
        scheduledTimeouts = new ConcurrentHashMap<>(corePoolSize);
        this.musicHandler = musicHandler;

    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        VoiceChannel channel = event.getChannelJoined();
        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()
                && audioManager.getConnectedChannel().getId().equals(channel.getId())) {
            if (channel.getMembers().size() >= 2) {
                cancelTimer(guild.getId());
                if (channel.getMembers().size() == 2
                        && !event.getMember().getUser().equals(event.getJDA().getSelfUser()))
                    musicHandler.unpauseQueue(guild);
            }
            else
                scheduleTimer(guild);
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        VoiceChannel channel = event.getChannelLeft();
        AudioManager audioManager = event.getGuild().getAudioManager();
        if (audioManager.isConnected()
                && audioManager.getConnectedChannel().getId().equals(channel.getId())
                && channel.getMembers().size() < 2
                && !event.getMember().getUser().equals(event.getJDA().getSelfUser()))
            scheduleTimer(event.getGuild());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        VoiceChannel channel = event.getChannelJoined();
        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()
                && audioManager.getConnectedChannel().getId().equals(channel.getId())) {
                if (channel.getMembers().size() < 2
                        && event.getMember().getUser().equals(event.getJDA().getSelfUser()))
                    scheduleTimer(guild);
                else {
                    cancelTimer(guild.getId());
                    if (channel.getMembers().size() == 2
                        && !event.getMember().getUser().equals(event.getJDA().getSelfUser()))
                    musicHandler.unpauseQueue(guild);
                }
        }
    }

    /**
     * Schedules a voice channel timeout for a given server.
     *
     * @param guild the server
     */
    private void scheduleTimer(Guild guild) {
        scheduledTimeouts.put(guild.getId(),
                voiceTimeoutManager.schedule(() -> voiceTimeoutLeave(guild), 1, TimeUnit.MINUTES));
    }

    /**
     * Cancels the voice channel timeout for a given server.
     *
     * @param id the server id
     * @throws NullPointerException in case the server id specified isn't on the HashMap
     */
    private void cancelTimer(String id) throws NullPointerException {
        ScheduledFuture<?> timer = scheduledTimeouts.get(id);
        if (timer != null) {
            timer.cancel(false);
            scheduledTimeouts.remove(id);
        }
    }

    /**
     * Once the timeout reaches the end, closes the audio connection with the voice channel of the specified server.
     *
     * @param guild the server
     */
    private void voiceTimeoutLeave(Guild guild) {
        disconnectVoice(guild);
        scheduledTimeouts.remove(guild.getId());
    }

}