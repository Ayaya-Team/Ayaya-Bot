package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Random;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * @author John Grosh (jagrosh) Modified by Aya Komichi#7541
 * Class of the ping command.
 */
public class Ping extends Command {

    private String console;
    private String ping_quote = "";

    public Ping() {

        this.name = "ping";
        this.help = "A command to check how fast my response is! Don't expect me to be a rocket thou.";
        this.arguments = "{prefix}ping";
        this.category = INFORMATION.asCategory();
        this.isGuildOnly = false;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        event.reply("Pinging...", (m) -> {

            Random var = new Random();
            int number = var.nextInt(8) + 1;
            fetchData(number);
            long ping = event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS)
                    & 0b01111111111111111111111111111111;
            long websocket_ping = event.getJDA().getGatewayPing();
            m.editMessage(ping_quote + " **" + ping + "ms** `Websocket: " + websocket_ping + "ms`")
                    .queue();
            String create_Hour, create_Minute, create_Second;
            int create_day = event.getMessage().getTimeCreated().getDayOfMonth();
            int create_month = event.getMessage().getTimeCreated().getMonthValue();
            int create_year = event.getMessage().getTimeCreated().getYear();
            int create_hour = event.getMessage().getTimeCreated().getHour();
            int create_minute = event.getMessage().getTimeCreated().getMinute();
            int create_second = event.getMessage().getTimeCreated().getSecond();
            if (create_hour < 10) create_Hour = "0" + create_hour;
            else create_Hour = String.valueOf(create_hour);
            if (create_minute < 10) create_Minute = "0" + create_minute;
            else create_Minute = String.valueOf(create_minute);
            if (create_second < 10) create_Second = "0" + create_second;
            else create_Second = String.valueOf(create_second);
            try {
                Objects.requireNonNull(
                        event.getJDA().getTextChannelById(console)).sendMessage(":warning: `"
                        + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator()
                        + "` checked my ping in the channel `"
                        + event.getChannel().getName() + "` in the server `" + event.getGuild().getName() + "` on `"
                        + create_day + "/" + create_month + "/" + create_year + "` at `"
                        + create_Hour + ":" + create_Minute + ":" + create_Second
                        + "`. The result was **" + ping + "ms** with a websocket ping of **" + websocket_ping
                        + "ms**.").queue();
            } catch (NullPointerException | IllegalStateException e) {}
        });

    }

    /**
     * Fetches the ping response from the database with the given id.
     *
     * @param number the id
     */
    private void fetchData(int number) {

        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            console = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'console';", 5)
                    .getString("value");
            ping_quote = jdbc.sqlSelect("SELECT * FROM 'ping quotes' WHERE id = " + number + ";", 5)
                    .getString("quote");
        } catch (SQLException e) {
            System.out.println("A problem occurred while trying to get necessary information for the ping command! Aborting the read process...");
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

}