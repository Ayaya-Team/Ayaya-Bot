package ayaya.commands.utilities;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import ayaya.core.utils.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class Time extends Command {

    private static final String LIST_CALL = "TIMEZONES";

    public Time() {

        this.name = "time";
        this.help = "Use this command to check the current time for any timezone.";
        this.arguments = "{prefix}time <timezone>\n{prefix}time timezones - for the list of timezones";
        this.category = CommandCategories.UTILITIES.asCategory();

    }

    @Override
    protected void executeInstructions(CommandEvent event) {
        String timezone = event.getArgs().toUpperCase();
        ZonedDateTime time;
        if (timezone.isEmpty()) {
            time = ZonedDateTime.now(ZoneId.systemDefault());
            event.reply(
                    String.format("The current time of my host is %s %s of %d, **%02d:%02d:%02d**",
                            time.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            Utils.getDayWithSuffix(time.getDayOfMonth()), time.getYear(), time.getHour(),
                            time.getMinute(), time.getSecond())
            );
        } else if (timezone.equals(LIST_CALL)) {
            event.reply(
                    "**Timezones:**\n\n" + Utils.TIMEZONES +
                            "\nYou can also check the time in UTC and for any deviations of any timezone, like:\n" +
                            "`" + event.getClient().getPrefix() + "time <timezone>+<amount in hours>` or\n" +
                            "`" + event.getClient().getPrefix() + "time <timezone>-<amount in hours>`\n" +
                            "Remember to not type the `<>` in your commands.\n" +
                            "For more information on timezones you can check this link:" +
                            " <https://en.wikipedia.org/wiki/List_of_UTC_time_offsets>"
            );
        } else {
            String args[];
            try {
                if (timezone.contains("+")) {
                    args = timezone.split("\\+");
                    time = ZonedDateTime.now(ZoneId.of(args[0], Utils.ZONE_IDS));
                    try {
                        time = time.plusHours(Long.parseLong(args[1]));
                    } catch (NumberFormatException e) {
                        event.reply(
                                ":x: The number inserted along with the timezone is either invalid, " +
                                        "not an integer or too big."
                        );
                        return;
                    }
                } else if (timezone.contains("-")) {
                    args = timezone.split("-");
                    time = ZonedDateTime.now(ZoneId.of(args[0], Utils.ZONE_IDS));
                    time = time.minusHours(Long.parseLong(args[1]));
                } else time = ZonedDateTime.now(ZoneId.of(timezone, Utils.ZONE_IDS));
                event.reply(
                        String.format("The current time for **%s** is %s %s of %d, **%02d:%02d:%02d**",
                                timezone, time.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                Utils.getDayWithSuffix(time.getDayOfMonth()), time.getYear(), time.getHour(),
                                time.getMinute(), time.getSecond())
                );
            } catch (DateTimeException e) {
                event.reply(":x: There is no such timezone in my list.");
            }
        }
    }

}