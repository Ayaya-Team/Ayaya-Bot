package ayaya.core.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ayaya.core.Ayaya.INITIAL_AMOUNT;

/**
 * Class extending the ListenerAdapter to listen only to voice events events.
 */
public class VoiceEventListener extends ListenerAdapter {

    private static final float GROWTH_RATE = 1.5f;
    private static final float SHRINK_RATE = 1/3;
    private static final float SHRINK_LEFTOVER = 2/3;

    private ScheduledThreadPoolExecutor voiceTimeoutManager;
    private Map<String, ScheduledFuture<?>> scheduledTimeouts;
    private int amount;

    public VoiceEventListener() {

        amount = INITIAL_AMOUNT;
        voiceTimeoutManager = new ScheduledThreadPoolExecutor(amount);
        scheduledTimeouts = new HashMap<>(amount);

    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        VoiceChannel channel = event.getChannelJoined();
        Guild guild = event.getGuild();
        if (channel.getMembers().size() >= 2
                && !event.getMember().getUser().equals(event.getJDA().getSelfUser()))
            cancelTimer(guild.getId());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        VoiceChannel channel = event.getChannelLeft();
        if (channel.getMembers().size() < 2
                && !event.getMember().getUser().equals(event.getJDA().getSelfUser()))
            scheduleTimer(event.getGuild());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        VoiceChannel channel = event.getChannelLeft();
        if (channel.getMembers().size() < 2
                && !event.getMember().getUser().equals(event.getJDA().getSelfUser()))
            scheduleTimer(event.getGuild());
    }

    /**
     * Schedules a voice channel timeout for a given server.
     *
     * @param guild the server
     */
    private void scheduleTimer(Guild guild) {
        scheduledTimeouts.put(guild.getId(),
                voiceTimeoutManager.schedule(() -> voiceTimeoutLeave(guild), 1, TimeUnit.MINUTES));
        synchronized (this) {
            if (scheduledTimeouts.size() == amount) {
                amount *= GROWTH_RATE;
                voiceTimeoutManager.setCorePoolSize(amount);
            }
        }
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
            synchronized (this) {
                int shrinkThreshold = (int)(SHRINK_LEFTOVER * (float)amount);
                if (amount != INITIAL_AMOUNT && scheduledTimeouts.size() == shrinkThreshold) {
                    amount = Math.max(INITIAL_AMOUNT, shrinkThreshold);
                    voiceTimeoutManager.setCorePoolSize(amount);
                }
            }
        }
    }

    /**
     * Once the timeout reaches the end, closes the audio connection with the voice channel of the specified server.
     *
     * @param guild the server
     */
    private void voiceTimeoutLeave(Guild guild) {
        guild.getAudioManager().closeAudioConnection();
        synchronized (this) {
            int shrinkThreshold = (int)(SHRINK_LEFTOVER * (float)amount);
            if (amount != INITIAL_AMOUNT && scheduledTimeouts.size() == shrinkThreshold) {
                amount = Math.max(INITIAL_AMOUNT, shrinkThreshold);
                voiceTimeoutManager.setCorePoolSize(amount);
            }
        }
    }

}