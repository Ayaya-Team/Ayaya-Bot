package ayaya.commands.utilities;

import ayaya.commands.Command;
import ayaya.core.BotData;
import ayaya.core.enums.CommandCategories;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static ayaya.core.enums.ConvertionArrays.*;

/**
 * Class of the convert command.
 */
public class Convert extends Command {

    private List<String> length_array = LENGHTH_UNITS.toList();
    private List<String> speed_array = SPEED_UNITS.toList();
    private List<String> weight_array = WEIGHT_UNITS.toList();
    private List<String> temperature_array = TEMPERATURE_UNITS.toList();
    private List<String> pressure_array = PRESSURE_UNITS.toList();
    private List<String> information_array = INFORMATION_UNITS.toList();

    public Convert() {

        this.name = "convert";
        this.help = "Having trouble with units? Don't worry, I can help you! <:AyaSmile:331115374739324930>";
        this.arguments = "{prefix}convert <prefix of the unit to convert from> " +
                "<prefix of the unit to convert to> <amount>\n\n" +
                "For the units list do `{prefix}convert units`.";
        this.category = CommandCategories.UTILITIES.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {
        String[] args = event.getArgs().replace(",", ".").split(" ");
        String prefix = event.getClient().getPrefix();
        switch (args.length) {
            case 1:
            case 2:
                if (args[0].equals("units")) {
                    EmbedBuilder units = new EmbedBuilder()
                            .setTitle("List of units available")
                            .addField("Length:", "km, hm, dam, m, dm, cm, mm, mi, ft", false)
                            .addField("Speed:", "km/h, m/s, mph", false)
                            .addField("Weight:", "kg, g, lbs, oz", false)
                            .addField("Temperature:", "k, c, f", false)
                            .addField("Pressure:", "at, atm, bar, mmhg, pa, psi", false)
                            .addField("Information:", "bit, kbit, mbit, gbit, tbit, pbit, ebit, zbit, ybit, byte, kbyte, mbyte, gbyte, tbyte, pbyte, ebyte, zbyte, ybyte", false)
                            .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getAvatarUrl());
                    try {
                        units.setColor(event.getGuild().getSelfMember().getColor());
                    } catch (NullPointerException e) {
                        units.setColor(Color.decode("#155FA0"));
                    }
                    event.reply(units.build());
                } else {
                    event.reply("To use this command do `" + prefix +
                            "convert <unit to convert from> <unit to convert to> <amount to convert>`.\n" +
                            "To see the units available do `" + prefix + "convert units`.");
                }
                break;
            default:
                event.reply(convert(event));
        }
    }

    /**
     * Processes a unit convertion request.
     *
     * @param event the event that triggered this command and returns the answer string
     * @return answer
     */
    private String convert(CommandEvent event) {
        String message = event.getArgs();
        String[] args = message.split(" ");
        String unit1 = args[0].replace("<", "").replace(">", "");
        String unit2 = args[1].replace("<", "").replace(">", "");
        if (unit1.equals(unit2)) {
            return "Converting " + unit1 + " to " + unit2 + "? That doesn't make sense!";
        }
        String prefix = event.getClient().getPrefix();
        double amount;
        double result;
        if (!(length_array.contains(unit1) && length_array.contains(unit2)) &&
                !(speed_array.contains(unit1) && speed_array.contains(unit2)) &&
                !(weight_array.contains(unit1) && weight_array.contains(unit2)) &&
                !(temperature_array.contains(unit1) && temperature_array.contains(unit2)) &&
                !(pressure_array.contains(unit1) && pressure_array.contains(unit2)) &&
                !(information_array.contains(unit1) && information_array.contains(unit2))) {
            return ":x: Unknown unit. Type `" + prefix + this.name + " units` to see the list of units currently available.\n" +
                    "Also make sure you put the right arguments when executing this command and don't mix units of different types.\n Check it's usage with `" +
                    prefix + "help " + this.name + "`.";
        } else if (temperature_array.contains(unit1) && temperature_array.contains(unit2)) {
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                return ":x: The amount introduced for the conversion isn't a valid number.";
            }
            result = convertTemperature(unit1, unit2, amount);
        } else {
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                return ":x: The amount introduced for the conversion isn't a valid number.";
            }
            result = amount * getConvertRating(unit1, unit2);
            result = Math.round(result*1000.0)/1000.0;
        }
        return args[2] + " " + unit1 + " = " + String.valueOf(result) + " "  + unit2;
    }

    /**
     * Converts temperature units.
     *
     * @param unit1  unit to convert from
     * @param unit2  unit to convert to
     * @param amount the amount to convert
     * @return result
     */
    private double convertTemperature(String unit1, String unit2, double amount) {
        switch (unit1) {
            case "c":
                if (unit2.equals("f"))
                    return amount * 1.8 + 32;
                else if (unit2.equals("k"))
                    return amount + 274.15;
            case "f":
                if (unit2.equals("c"))
                    return (amount - 32) / 1.8;
                else if (unit2.equals("k"))
                    return amount / (5 / 9) - 459.67;
            case "k":
                if (unit2.equals("f"))
                    return (amount + 459.67) * (5 / 9);
                else if (unit2.equals("c"))
                    return amount - 274.15;
        }
        return 0;
    }

    /**
     * Fetches the database for the convertion rating between 2 units.
     * This is executed for all kinds of units except temperature ones.
     *
     * @param unit1 unit to convert from
     * @param unit2 unit to convert to
     * @return rating
     */
    private double getConvertRating(String unit1, String unit2) {
        double rating = 0;
        SQLController jdbc = new SQLController();
        try {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            Serializable[] o = new Serializable[]{unit1, unit2};
            ResultSet rs = jdbc.sqlSelect("SELECT * FROM converter WHERE unit1 = ?"
                    + " AND unit2 = ?;", o, 5);
            rating = rs.next() ? rs.getDouble("rating") : 0;
        } catch (SQLException e) {
            System.out.println("A problem occurred while trying to get the convert rating.");
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
        return rating;
    }

}