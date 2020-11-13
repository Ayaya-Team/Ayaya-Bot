package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import ayaya.core.enums.Commands;
import ayaya.core.listeners.CommandListener;
import ayaya.core.listeners.EventListener;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Class of the stats command.
 */
public class Stats extends Command {

    private String version;

    public Stats() {

        this.name = "stats";
        this.help = "Do you like numbers? Then this might be a good command for you.";
        this.arguments = "{prefix}stats";
        this.aliases = new String[]{"statistics"};
        this.category = CommandCategories.INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        fetchData();
        String javaVersion = System.getProperty("java.version");
        LocalDateTime uptime = getUptime(event);
        EventListener eListener = null;
        CommandListener cListener = null;
        for (Object o: event.getJDA().getRegisteredListeners()) {
            if (o instanceof EventListener) {
                eListener = (EventListener) o;
                break;
            }
        }
        com.jagrosh.jdautilities.command.CommandListener listener = event.getClient().getListener();
        if (listener instanceof CommandListener)
            cListener = (CommandListener) listener;
        EmbedBuilder stats_embed = new EmbedBuilder()
                .setAuthor("Statistics for this session:", null, event.getSelfUser().getAvatarUrl())
                .addField(
                        "Versions",
                        String.format(
                                "Ayaya: %s\nJava: %s\nJDA: %s\nJDA Utilities: %s\nLavaplayer: %s",
                                version, javaVersion, JDAInfo.VERSION, JDAUtilitiesInfo.VERSION, PlayerLibrary.VERSION
                        ),
                        false
                )
                .addField(
                        "General Information",
                        String.format(
                                "Total Servers: **%d**\nTotal Text Channels: **%d**\nTotal Voice Channels: **%d**\n" +
                                        "Uptime: %s",
                                event.getJDA().getGuilds().size(),
                                event.getJDA().getTextChannels().size(), event.getJDA().getVoiceChannels().size(),
                                "**" + String.valueOf(uptime.getDayOfYear() - 1) + "** days, **" + uptime.getHour() +
                                        "** hours, **" + uptime.getMinute() + "** minutes and **" + uptime.getSecond()
                                        + "** seconds"
                        ),
                        false
                );
        if (eListener != null && cListener != null) {
            stats_embed
                    .addField(
                            "Commands Usage",
                            String.format(
                                    "Messages Received: **%d**\nCommands Loaded: **%d**\nCommands Executed: **%d**\n"+
                                            "Help Requests: **%d**",
                                    eListener.getMessagesCounter(), event.getClient().getCommands().size(),
                                    cListener.getCommandsCounter(), event.getClient().getCommandUses(
                                            Commands.HELP.getName()
                                    )
                            ),
                            false
                    );
        } else {
            stats_embed
                    .addField(
                            "Commands Usage",
                            String.format(
                                    "Commands Loaded: %d\nHelp Requests: %d",
                                    event.getClient().getCommands().size(),
                                    event.getClient().getCommandUses(
                                            Commands.HELP.getName()
                                    )
                            ),
                            false
                    );
        }
        stats_embed.setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getAvatarUrl());
        try {
            stats_embed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            stats_embed.setColor(Color.decode("#155FA0"));
        }
        event.reply(stats_embed.build());
    }

    /**
     * Fetches the required data from the database to execute this command.
     */
    private void fetchData() {

        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            version = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'version';", 10)
                    .getString("value");
        } catch (SQLException e) {
            System.out.println("A problem occurred while trying to get necessary information for the " + this.name + " command! Aborting the read process...");
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
     * Fetches the total uptime of the bot.
     *
     * @param event the event tha triggered this command
     * @return uptime
     */
    private LocalDateTime getUptime(CommandEvent event) {
        LocalDateTime start_time = event.getClient().getStartTime().toLocalDateTime();
        LocalDateTime current_time = OffsetDateTime.now().toLocalDateTime();
        return current_time.minusDays(start_time.getDayOfYear() - 1).minusHours(start_time.getHour())
                .minusMinutes(start_time.getMinute()).minusSeconds(start_time.getSecond());
    }

}