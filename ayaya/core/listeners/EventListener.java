package ayaya.core.listeners;

import ayaya.core.BotData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

/**
 * Class extending the ListenerAdapter to listen to events.
 */
public class EventListener extends ListenerAdapter {

    private static final String LINK = "https://discord.gg/";
    //private static final String GREETING =
    //        "Welcome %s to the Aya's Office! For any help related with me, go to the #support channel. Hope you enjoy your stay here!";
    //private static final String FAREWELL = "Goodbye %s, we will miss you.";

    //private String server;
    //private String greetings_farewells;
    private long messagesCounter;

    public EventListener() {
        messagesCounter = 0;
    }

    @Override
    public void onReady(ReadyEvent event) {

        JDA jda = event.getJDA();
        AudioManager audioManager;
        for (Guild g : jda.getGuilds()) {
            audioManager = g.getAudioManager();
            if (audioManager.isConnected())
                audioManager.closeAudioConnection();
        }
        OffsetDateTime start_time = OffsetDateTime.now(ZoneId.of("GMT"));
        Objects.requireNonNull(jda.getTextChannelById(BotData.getConsoleID()))
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

        String alternatePrefix = String.format("<@%s>", BotData.getId());
        String content = event.getMessage().getContentRaw();
        messagesCounter++;
        User user = event.getAuthor();
        MessageChannel channel = event.getChannel();
        OffsetDateTime time = OffsetDateTime.now(ZoneId.of("GMT"));
        if (!content.isBlank()
                && !user.isBot())
        {
            if (content.toLowerCase().startsWith(BotData.getPrefix())) {
                channel.sendMessage("Starting in the 30th of April 2022, you will have to use my mention as prefix when using my commands like this:\n"
                        + alternatePrefix + " help\n"
                        + "Without it, I won't be able to see the content of the messages.").queue();
            }
            else if (channel instanceof PrivateChannel
                        && !content.toLowerCase().startsWith(alternatePrefix)) {
                if (content.contains(LINK)) {
                    channel.sendMessage("If you want to invite me to a server you need to use one of my invite links!\n"
                            + "Write `" + BotData.getPrefix() + "invite` to see a list of the available invite links.").queue();
                    return;
                }
                System.out.printf(
                        "Warning: %s#%s sent me a direct message at %02d/%02d/%d at %02d:%02d:%02d.\n\nContent:\n%s\n",
                        user.getName(), user.getDiscriminator(), time.getDayOfMonth(), time.getMonth().getValue(), time.getYear(),
                        time.getHour(), time.getMinute(), time.getSecond(), content
                );
                Objects.requireNonNull(event.getJDA().getTextChannelById(BotData.getConsoleID())).sendMessage(
                        String.format(":warning: %s#%s sent me a direct message at `%02d/%02d/%d` at `%02d:%02d:%02d`.\n\n**Content:**\n```css\n%s```",
                                user.getName(), user.getDiscriminator(), time.getDayOfMonth(), time.getMonth().getValue(), time.getYear(),
                                time.getHour(), time.getMinute(), time.getSecond(), content)
                ).queue();
            }
        }

    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        OffsetDateTime time = OffsetDateTime.now(ZoneId.of("GMT"));
        Guild guild = event.getGuild();
        guild.retrieveOwner(true).queue(owner -> {
            Objects.requireNonNull(event.getJDA().getTextChannelById(BotData.getConsoleID())).sendMessage(
                    "I just joined the server " + guild.getName() + " `" + guild.getId() + "`, owned by `"
                            + owner.getUser().getName() + "#"
                            + owner.getUser().getDiscriminator() + "` `" + owner.getId() + "` at `"
                            + time.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")) + "`."
            ).allowedMentions(Collections.emptyList()).queue();
        }, e -> {
            Objects.requireNonNull(event.getJDA().getTextChannelById(BotData.getConsoleID())).sendMessage(
                    "I just joined the server " + guild.getName() + " `" + guild.getId() + "`, owned by " +
                            "an unknown person at `"
                            + time.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")) + "`."
            ).allowedMentions(Collections.emptyList()).queue();
        });

    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {

        OffsetDateTime time = OffsetDateTime.now(ZoneId.of("GMT"));
        Guild guild = event.getGuild();
        guild.retrieveOwner(true).queue(owner -> {
            Objects.requireNonNull(event.getJDA().getTextChannelById(BotData.getConsoleID())).sendMessage(
                    "I just left the server " + guild.getName() + " `" + guild.getId() + "`, owned by `"
                            + owner.getUser().getName() + "#"
                            + owner.getUser().getDiscriminator() + "` `" + owner.getId() + "` at `"
                            + time.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")) + "`."
            ).allowedMentions(Collections.emptyList()).queue();
        }, e -> {
            Objects.requireNonNull(event.getJDA().getTextChannelById(BotData.getConsoleID())).sendMessage(
                    "I just left the server " + guild.getName() + " `" + guild.getId() + "`, " +
                            "at `"
                            + time.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")) + "`."
            ).allowedMentions(Collections.emptyList()).queue();
        });

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

    /**
     * Returns the counter of total messages received.
     *
     * @return messages counter
     */
    public long getMessagesCounter() {
        return messagesCounter;
    }

}