package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.io.Serializable;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Class of the premium command.
 */
public class Premium extends Command {

    private static final String PATTERN = "d-MM-yyyy";
    private static final String INFINITE = "f";

    public Premium() {

        this.name = "premium";
        this.help = "A command to check how many days of Premium you have left.";
        this.arguments = "{prefix}premium";
        this.category = CommandCategories.INFORMATION.asCategory();
        this.isGuildOnly = false;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String id = event.getAuthor().getId();
        if (id.equals(event.getClient().getOwnerId())) {
            event.reply("All owners and co-owners are premium by default.");
            return;
        }
        for (String co_owner: event.getClient().getCoOwnerIds())
            if (id.equals(co_owner)) {
                event.reply("All owners and co-owners are premium by default.");
                return;
            }
        long days_left = isPremium(event);
        switch ((int) days_left) {
            case -2:
                break;
            case -1:
                event.reply("You are premium forever.");
                break;
            case 0:
                event.reply("You aren't premium.");
                break;
            default:
                event.reply("You are premium for another " + days_left + " days.");
        }

    }

    /**
     * Checks wether the author of a CommandEvent is premium.
     *
     * @param event the CommandEvent triggered
     * @return true if the author is premium, false if not
     */
    private long isPremium(CommandEvent event) {
        String id = event.getAuthor().getId();
        long days_left = 0;
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            ResultSet resultSet = jdbc
                    .sqlSelect("SELECT * FROM patreon_whitelist WHERE user_id = " + id + ";", 5);
            if (resultSet.next()) {
                String result = resultSet.getString("expiration_date");
                if (result.equals(INFINITE))
                    days_left = -1;
                else {
                    LocalDate date = LocalDate.parse(result, DateTimeFormatter.ofPattern(PATTERN));
                    LocalDate now = LocalDate.now();
                    int compare = date.compareTo(now);
                    if (compare < 0) {
                        Serializable[] o = {id};
                        jdbc.sqlInsertUpdateOrDelete(
                                "DELETE FROM patreon_whitelist WHERE user_id = ?;", o, 5
                        );
                        days_left = 0;
                    } else if (compare == 0) {
                        days_left = 1;
                    } else {
                        days_left = ChronoUnit.DAYS.between(now, date) + 1;
                    }
                }
            }
        } catch (SQLException e) {
            event.replyError("There was a problem while checking wether you are or aren't a premium. If this error persists, try again later.");
            e.printStackTrace();
            days_left = -2;
        } finally {
            try {
                jdbc.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return days_left;
    }


}