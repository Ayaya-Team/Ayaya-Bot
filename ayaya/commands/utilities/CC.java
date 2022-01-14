package ayaya.commands.utilities;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import ayaya.core.exceptions.http.HttpNullResponseException;
import ayaya.core.exceptions.http.HttpResponseFailedException;
import ayaya.core.exceptions.http.MissingHeaderInfoException;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;

import static ayaya.core.utils.HTTP.getJSONObject;

/**
 * Class of the cc command.
 */
public class CC extends Command {

    public CC() {

        this.name = "cc";
        this.help = "Having trouble with currencies? Don't worry, I can help you! <:AyaSmile:331115374739324930>";
        this.arguments = "{prefix}cc <prefix of the currency to convert from> " +
                "<prefix of the currency to convert to> <amount>\n\n"
                + "For the currencies list do `{prefix}cc currencies`.";
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
                if (args[0].equals("currencies")) {
                    EmbedBuilder currencies = new EmbedBuilder()
                            .setTitle("List of currencies available")
                            .setDescription("aud, bgn, brl, cad, chf, cny, czk, dkk, eur, gbp, hkd, hrk, huf, idr, ils, inr, isk, jpy, krw, mxn, myr, nok, nzd, php, pln, ron, rub, sek, sgd, thb, try, usd, zar")
                            .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getAvatarUrl());
                    try {
                        currencies.setColor(event.getGuild().getSelfMember().getColor());
                    } catch (IllegalStateException | NullPointerException e) {
                        currencies.setColor(Color.decode("#155FA0"));
                    }
                    event.reply(currencies.build());
                } else {
                    event.reply("To use this command do `" + prefix +
                            "cc <currency to convert from> <currency to convert to> <amount to convert>`.\n" +
                            "To see the currencies available do `" + prefix + "cc currencies`.");
                }
                break;
            default:
                event.reply(convert(event));
        }


    }

    /**
     * Processes a currency convertion request and returns the answer string.
     *
     * @param event the event that triggered this command
     * @return answer
     */
    private String convert(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        String unit1 = args[0].replace("<", "").replace(">", "");
        String unit2 = args[1].replace("<", "").replace(">", "");
        if (unit1.equals(unit2)) {
            return "Converting " + unit1 + " to " + unit2 + "? That doesn't make sense!";
        }
        String prefix = event.getClient().getPrefix();
        JSONObject ratings = null;
        try {
            ratings = getJSONObject("https://frankfurter.app/latest?from=" + unit1.toUpperCase());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException | MissingHeaderInfoException e) {
            e.printStackTrace();
            return "I'm sorry but there was an error with the response from the currency api. Please try again later.";
        } catch (HttpResponseFailedException e) {
            return "I'm sorry but the connection with the currency api failed. Please try again later.";
        } catch (HttpNullResponseException e) {
            return "I'm sorry but the currency api failed to respond. Please try again later.";
        }
        if (ratings == null || !ratings.getJSONObject("rates").has(unit2.toUpperCase())) {
            return ":x: Unknown currency. Type `" + prefix + this.name + " currencies` to see the list of currencies currently available.\n" +
                    "Also make sure you put the right arguments when executing this command. Check it's usage with `" +
                    prefix + "help " + this.name + "`.\n" +
                    "If none of the above is your case then the api used by this command may be currently down.";
        }
        double rating = ratings.getJSONObject("rates").getDouble(unit2.toUpperCase());
        double result;
        try {
            result = Double.parseDouble(args[2]) * rating;
        } catch (NumberFormatException e) {
            return ":x: The amount introduced for the conversion isn't a valid number.";
        }
        result = Math.round(result*100.0)/100.0;
        return args[2] + " "  + unit1 + " = " + String.valueOf(result) + " "  + unit2;
    }

}