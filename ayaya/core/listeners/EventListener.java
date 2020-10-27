package ayaya.core.listeners;

import ayaya.core.utils.SQLController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class extending the ListenerAdapter to listen to the different events.
 */
public class EventListener extends ListenerAdapter {

    private static final String LINK = "https://discord.gg/";
    private static final String GREETING =
            "Welcome %s to the Aya's Office! For any help related with me, go to the #support channel. Hope you enjoy your stay here!";
    private static final String FAREWELL = "Goodbye %s, we will miss you.";
    private static final int POOL_AMOUNT = 500;
    private static final int AMOUNT_TO_ADD = 50;

    private String prefix;
    private String console;
    private String server;
    private String greetings_farewells;
    private int messagesCounter;
    private ScheduledThreadPoolExecutor voiceTimeoutManager;
    private Map<String, ScheduledFuture<?>> scheduledTimeouts;
    private int amount;

    public EventListener() {

        fetchSettings();
        messagesCounter = 0;
        amount = POOL_AMOUNT;
        voiceTimeoutManager = new ScheduledThreadPoolExecutor(amount);
        scheduledTimeouts = new HashMap<>(amount);

    }

    @Override
    public void onReady(ReadyEvent event) {

        JDA jda = event.getJDA();
        AudioManager audioManager;
        for (Guild g : jda.getGuilds()) {
            g.retrieveOwner(true);
            audioManager = g.getAudioManager();
            if (audioManager.isConnected())
                audioManager.closeAudioConnection();
        }
        OffsetDateTime start_time = OffsetDateTime.now(ZoneId.of("GMT"));
        Objects.requireNonNull(jda.getTextChannelById(console))
                .sendMessage(
                        String.format(
                                "I awoke on `%02d/%02d/%d` at `%02d:%02d:%02d` and I'm now working in %d guilds.",
                                start_time.getDayOfMonth(), start_time.getMonth().getValue(), start_time.getYear(),
                                start_time.getHour(), start_time.getMinute(), start_time.getSecond(),
                                jda.getGuilds().size())
                ).queue();

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String content = event.getMessage().getContentRaw();
        messagesCounter++;
        User user = event.getAuthor();
        Member member = event.getMember();
        MessageChannel channel = event.getChannel();
        boolean bot = user.isBot();
        OffsetDateTime time = OffsetDateTime.now(ZoneId.of("GMT"));
        if (!content.isBlank() && channel instanceof PrivateChannel && !user.isBot() && !content.toLowerCase().startsWith(prefix)) {
            if (content.contains(LINK)) {
                channel.sendMessage("If you want to invite me to a server you need to use one of my invite links!\n"
                        + "Write `" + prefix + "invite` to see a list of the available invite links.").queue();
                return;
            }
            System.out.printf(
                    "Warning: %s sent me a direct message at %02d/%02d/%d at %02d:%02d:%02d.\n\nContent:\n%s\n",
                    user.getName(), time.getDayOfMonth(), time.getMonth().getValue(), time.getYear(),
                    time.getHour(), time.getMinute(), time.getSecond(), content
            );
            Objects.requireNonNull(event.getJDA().getTextChannelById(console)).sendMessage(
                    String.format(":warning: %s sent me a direct message at `%02d/%02d/%d` at `%02d:%02d:%02d`.\n\n**Content:**\n```css\n%s```",
                            user.getName(), time.getDayOfMonth(), time.getMonth().getValue(), time.getYear(),
                            time.getHour(), time.getMinute(), time.getSecond(), content)
            ).queue();
        }

    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        OffsetDateTime time = OffsetDateTime.now(ZoneId.of("GMT"));
        Guild guild = event.getGuild();
        guild.retrieveOwner(true).queue(owner -> {
            Objects.requireNonNull(event.getJDA().getTextChannelById(console)).sendMessage(
                    "I just joined the server " + guild.getName() + " `" + guild.getId() + "`, owned by `"
                            + owner.getUser().getName() + "#"
                            + owner.getUser().getDiscriminator() + "` `" + owner.getId() + "` at `"
                            + time.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")) + "`."
            ).queue();
        });

    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {

        OffsetDateTime time = OffsetDateTime.now(ZoneId.of("GMT"));
        Guild guild = event.getGuild();
        User owner = Objects.requireNonNull(guild.getOwner()).getUser();
        Objects.requireNonNull(event.getJDA().getTextChannelById(console)).sendMessage(
                "I just left the server " + guild.getName() + " `" + guild.getId() + "`, owned by `"
                        + owner.getName() + "#" + owner.getDiscriminator() + "` `" + owner.getId() + "` at `"
                        + time.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")) + "`."
        ).queue();

    }

	// This code should be enabled when the Guild Member intent is enabled again
    /*
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        if (event.getGuild().getId().equals(server)) {
            Member member = event.getMember();
            Objects.requireNonNull(event.getJDA().getTextChannelById(greetings_farewells))
                    .sendMessage(String.format(GREETING, member.getAsMention())).queue();
        }

    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {

        if (event.getGuild().getId().equals(server)) {
            Member member = event.getMember();
            Objects.requireNonNull(event.getJDA().getTextChannelById(greetings_farewells))
                    .sendMessage(String.format(FAREWELL, Objects.requireNonNull(member).getEffectiveName())).queue();
        }

    }
    */

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        VoiceChannel channel = event.getChannelJoined();
        Guild guild = event.getGuild();
        if (Objects.requireNonNull(channel).getMembers().size() == 2
                && !event.getMember().getUser().equals(event.getJDA().getSelfUser()))
            stopTimer(guild.getId());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        VoiceChannel channel = event.getChannelLeft();
        if (Objects.requireNonNull(channel).getMembers().size() < 2
                && !event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            if (scheduledTimeouts.size() == amount) {
                amount += AMOUNT_TO_ADD;
                voiceTimeoutManager.setCorePoolSize(amount);
            }
            scheduleTimer(event.getGuild());
        }
    }

    /**
     * Retrieves the information needed from the database to handle the events.
     */
    private void fetchSettings() {

        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            prefix = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'prefix';", 5)
                    .getString("value");
            server = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'server';", 5)
                    .getString("value");
            console = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'console';", 5)
                    .getString("value");
            greetings_farewells =
                    jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'greetings/farewells';", 5)
                            .getString("value");
        } catch (SQLException e) {
            System.out.println("A problem occurred while trying to get necessary information to analyze this message!" +
                    " Skipping the proccess...");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                jdbc.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }

    }

    /**
     * Returns the counter of total messages received.
     *
     * @return messages counter
     */
    public int getMessagesCounter() {
        return messagesCounter;
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
    private void stopTimer(String id) throws NullPointerException {
        ScheduledFuture<?> timer = scheduledTimeouts.get(id);
        if (timer != null)
            timer.cancel(true);
    }

    /**
     * Once the timeout reaches the end, closes the audio connection with the voice channel of the specified server.
     *
     * @param guild the server
     */
    private void voiceTimeoutLeave(Guild guild) {
        guild.getAudioManager().closeAudioConnection();
    }

}