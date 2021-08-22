package ayaya.core.listeners;

import ayaya.commands.information.Stats;
import ayaya.core.BotData;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Class overriding the default CommandListener to listen to all the commands.
 */
public class CommandListener implements com.jagrosh.jdautilities.command.CommandListener {

    private long commandsCounter = 0;

    @Override
    public void onCompletedCommand(CommandEvent event, Command command) {
        if (!(command instanceof Stats) && !(command.isOwnerCommand()))
            commandsCounter++;
    }

    @Override
    public void onTerminatedCommand(CommandEvent event, Command command) {
    }

    @Override
    public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
        int ids_amount = 2;
        String[] ids = new String[ids_amount];
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
        String warning = ":warning: <@!" + BotData.getOwners().get(0)
                + "> Something is wrong in the command `" + command.getName() + "`! Please, check this error and fix it!\nFull command: `"
                + event.getMessage().getContentRaw().replace(event.getClient().getPrefix(), "") + "`\nAuthor: "
                + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " `"
                + event.getAuthor().getId() + "`";
        String stackTrace = writer.toString();
        String moment = "`" + final_day + "/" + final_month + "/" + final_year + " " + final_hour + ":" + final_minute + ":" + final_second + "`";
        TextChannel consoleChannel = event.getJDA().getTextChannelById(BotData.getConsoleID());
        if (consoleChannel != null) {
            consoleChannel.sendMessage(warning).queue();
            if (stackTrace.length() > 2000) {
                try {
                    ArrayList<String> stackTracePieces = CommandEvent.splitMessage(stackTrace);
                    for (String p : stackTracePieces) {
                        consoleChannel.sendMessage("```" + p + "```").queue();
                    }
                } catch (IllegalArgumentException e) {}
            } else
                consoleChannel.sendMessage("```" + stackTrace + "```").queue();
            consoleChannel.sendMessage(moment).queue();
        }
        throw throwable instanceof RuntimeException ? (RuntimeException) throwable : new RuntimeException(throwable);
    }

    /**
     * Returns the counter of total commands received.
     *
     * @return commands counter
     */
    public long getCommandsCounter() {
        return commandsCounter;
    }

}