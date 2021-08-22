package ayaya.commands.utilities;

import ayaya.commands.Command;
import ayaya.core.BotData;
import ayaya.core.enums.CommandCategories;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class of the claimkey command.
 */
public class ClaimKey extends Command {

    public ClaimKey() {

        this.name = "claimkey";
        this.help = "If you have pledged 2 dollars or more on my patreon page, " +
                "ask my developer for your patreon key and use this command to claim it in order to be whitelisted.";
        this.arguments = "{prefix}claimkey <key>";
        this.category = CommandCategories.UTILITIES.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String key = event.getArgs();
        if (key.isEmpty()) {
            event.replyError("You did not specify the key you wanted to claim.");
            return;
        }
        int code = claimKey(event, key);
        switch (code) {
            case 1:
                event.replySuccess("Key claimed with success! Thank you for your donation.");
                break;
            case 0:
                event.replyError("Such key does not exist on my database or it was already claimed.");
            default:
        }

    }

    /**
     * Attempts to process a claim request of a key for an user.
     *
     * - If the key requested isn't found, returns 0;
     * - If the key is found but a problem occurrs while assigning it, returns -1;
     * - If there's success in the whole process, returns 1.
     *
     * @param event the event that triggered this command
     * @param key   the key to claim
     * @return int
     */
    private int claimKey(CommandEvent event, String key) {
        if (!keyExists(event, key)) return 0;
        if (assignKey(event, event.getAuthor().getId(), key)) return 1;
        return -1;
    }

    /**
     * Checks if the given key exists in the database,
     *
     * @param event the event that triggered this command.
     * @param key   the key to check for
     * @return true if the key exists, false if not
     */
    private boolean keyExists(CommandEvent event, String key) {
        boolean premium = false;
        SQLController jdbc = new SQLController();
        try {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            ResultSet result = jdbc.sqlSelect("SELECT * FROM patreon_keys WHERE key = '" + key + "';", 5);
            premium = result.next();
        } catch (SQLException e) {
            event.replyError("There was a problem while checking wether you are or aren't a premium. If this error persists, try again later.");
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
        return premium;
    }

    /**
     * In case the key exists in the database, assigns a premium period to the user.
     * If the key is assigned without problems returns true, else returns false.
     *
     * @param event the event that triggered this command
     * @param id    the id of the user that is claiming the key
     * @param key   the key to be claimed
     * @return true or false
     */
    private boolean assignKey(CommandEvent event, String id, String key) {
        boolean fine = false;
        SQLController jdbc = new SQLController();
        assign: try {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            int duration = jdbc.sqlSelectNext("SELECT * FROM patreon_keys WHERE key = '" + key + "';", 5)
                    .getInt("duration");
            String date = getPremiumExpirationDate(event);
            if (date != null) {
                if (date.equals(INFINITE)) {
                    event.reply("You don't need to claim this key, because you're already premium forever.");
                    break assign;
                }
                else
                    date = addExtraDurationToDate(date, duration);
            }
            else date = convertDurationtoDateString(duration);
            Serializable[] o = new Serializable[]{id, date};
            jdbc.sqlInsertUpdateOrDelete(
                    "INSERT OR REPLACE INTO patreon_whitelist(user_id, expiration_date) VALUES(?, ?);",
                    o, 5
            );
            o = new Serializable[]{key};
            jdbc.sqlInsertUpdateOrDelete("DELETE FROM patreon_keys WHERE key = '?';", o, 5);
            fine = true;
        } catch (SQLException e) {
            event.replyError("There was a problem while assigning you the premium key. If this error persists, try again later.");
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
        return fine;
    }

    /**
     * Converts the given duration in the key into a final date string
     * for the end of the premium period.
     *
     * @param duration the duration of the key
     * @return date string
     */
    private String convertDurationtoDateString(int duration) {
        if (duration > 0) {
            OffsetDateTime dateTime = OffsetDateTime.now().plusMonths(duration);
            return dateTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        } else
            return INFINITE;
    }

    /**
     * Adds the given duration in the key to a date string.
     *
     * @param date     the original date string
     * @param duration the duration of the key
     * @return new date string
     */
    private String addExtraDurationToDate(String date, int duration) {
        if (duration > 0) {
            LocalDate newDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_PATTERN));
            return newDate.plusMonths(duration).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        } else
            return INFINITE;
    }

}