package ayaya.core.listeners;

import ayaya.commands.information.Stats;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Class overriding the default CommandListener to listen to all the commands.
 */
public class CommandListener implements com.jagrosh.jdautilities.command.CommandListener {

    private String prefix = "";
    private String console = "";
    private String owner = "";
    private int commandsCounter = 0;

    @Override
    public void onCompletedCommand(CommandEvent event, Command command) {
        if (!(command instanceof Stats))
            commandsCounter++;
    }

    @Override
    public void onTerminatedCommand(CommandEvent event, Command command) {
    }

    @Override
    public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
        int ids_amount = 2;
        String[] ids = new String[ids_amount];
        getData();
        event.getChannel()
                .sendMessage(":x: An error has occurred and has been reported to my developer. S-Sorry for this...")
                .queue();
        int day = event.getMessage().getTimeCreated().getDayOfMonth();
        int month = event.getMessage().getTimeCreated().getMonth().getValue();
        int year = event.getMessage().getTimeCreated().getYear();
        int hour = event.getMessage().getTimeCreated().getHour();
        int minute = event.getMessage().getTimeCreated().getMinute();
        int second = event.getMessage().getTimeCreated().getSecond();
        int guilds = event.getJDA().getGuilds().size();
        int users = event.getJDA().getUsers().size();
        String final_day, final_month, final_year, final_hour, final_minute, final_second;
        if (day < 10) final_day = "0" + String.valueOf(day);
        else final_day = String.valueOf(day);
        if (month < 10) final_month = "0" + String.valueOf(month);
        else final_month = String.valueOf(month);
        if (year < 10) final_year = "0" + String.valueOf(year);
        else final_year = String.valueOf(year);
        if (hour < 10) final_hour = "0" + String.valueOf(hour);
        else final_hour = String.valueOf(hour);
        if (minute < 10) final_minute = "0" + String.valueOf(minute);
        else final_minute = String.valueOf(minute);
        if (second < 10) final_second = "0" + String.valueOf(second);
        else final_second = String.valueOf(second);
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        String warning = ":warning: <@!" + owner
                + "> Something is wrong in the command `" + command.getName() + "`! Please, check this error and fix it!\nFull command: `"
                + event.getMessage().getContentRaw().replace(prefix, "") + "`\nAuthor: "
                + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " `"
                + event.getAuthor().getId() + "`";
        String stackTrace = writer.toString();
        String moment = "`" + final_day + "/" + final_month + "/" + final_year + " " + final_hour + ":" + final_minute + ":" + final_second + "`";
        Objects.requireNonNull(event.getJDA().getTextChannelById(console)).sendMessage(warning).queue();
        if (stackTrace.length() > 2000) {
            try {
                ArrayList<String> stackTracePieces = CommandEvent.splitMessage(stackTrace);
                for (String p : stackTracePieces) {
                    Objects.requireNonNull(
                            event.getJDA().getTextChannelById(console)
                    ).sendMessage("```" + p + "```").queue();
                }
            } catch (IllegalArgumentException e) {}
        } else Objects.requireNonNull(
                event.getJDA().getTextChannelById(console)
        ).sendMessage("```" + stackTrace + "```").queue();
        Objects.requireNonNull(event.getJDA().getTextChannelById(console)).sendMessage(moment).queue();
        throw throwable instanceof RuntimeException ? (RuntimeException) throwable : new RuntimeException(throwable);
    }

    /**
     * Retrieves the necessary information about the bot's current prefix,
     * the channel used as console and the id of the bot's owner.
     */
    private void getData() {
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            prefix = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'prefix';", 5)
                    .getString("value");
            console = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'console';", 5)
                    .getString("value");
            owner = jdbc.sqlSelect("SELECT * FROM owners WHERE person LIKE 'owner';", 5)
                    .getString("discord_id");
        } catch (SQLException e) {
            System.out.println("A problem occurred while trying to get necessary information for the error handler! Unable to report the error to the discord console...");
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
     * Returns the counter of total commands received.
     *
     * @return commands counter
     */
    public int getCommandsCounter() {
        return commandsCounter;
    }

}