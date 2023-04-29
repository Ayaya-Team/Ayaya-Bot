package ayaya.commands.utilities;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import ayaya.core.exceptions.http.HttpNullResponseException;
import ayaya.core.exceptions.http.HttpResponseFailedException;
import ayaya.core.exceptions.http.MissingHeaderInfoException;
import ayaya.core.utils.HTTP;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckColor extends Command {

    protected static final Pattern CODE_TYPE = Pattern.compile("(?:([^\\p{Blank}\\p{Digit}#])+([\\p{Blank}])?)");
    private static final Pattern HEX = Pattern.compile("(?:[\\p{Digit}a-f]{3}?){1,2}");
    private static final Pattern RGB = Pattern.compile("([\\p{Digit}]{1,3})");
    //private static final Pattern INT = Pattern.compile("(?:[\\p{Digit}]{1,8}?)");

    private static final String URL = "http://thecolorapi.com/id?%s=%s&format=json";
    private static final String IMAGE_URL = "https://dummyimage.com/300x300/%s.png&text=%%20";

    private static final int MAX_RGB_VAL = 255;

    public CheckColor() {
        this.name = "color";
        this.help = "This command allows you to check for a color and its name through its code.";
        this.arguments = "{prefix}color hex <color code>\n" +
                "{prefix}color rgb <color code>";
        this.category = CommandCategories.UTILITIES.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};
    }

    @Override
    protected void executeInstructions(CommandEvent event) {
        String args = event.getArgs().toLowerCase();
        if (args.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You did not specify a color code type neither a color code.");
            return;
        }
        String codeType;
        Matcher matcher = CODE_TYPE.matcher(args);
        if (matcher.find()) codeType = matcher.group().trim();
        else {
            event.reply(
                    "<:AyaWhat:362990028915474432> You either did not specify a color code or a color code type."
            );
            return;
        }
        JSONObject json;
        try {
            switch (codeType) {
                case "hex":
                    String hex = "";
                    matcher = HEX.matcher(args);
                    if (matcher.find()) hex = matcher.group();
                    json = HTTP.getJSONObject(String.format(URL, codeType, hex));
                    break;
                case "rgb":
                    int rgb[] = new int[3];
                    matcher = RGB.matcher(args);
                    for (int i = 0; i < 3; i++) {
                        if (matcher.find()) {
                            rgb[i] = Integer.parseInt(matcher.group());
                            if (rgb[i] > MAX_RGB_VAL) {
                                event.replyError("rgb values aren't bigger than " + MAX_RGB_VAL + ".");
                                return;
                            }
                        }
                        else {
                            event.replyError("I need 3 numbers for the rgb code.");
                            return;
                        }
                    }
                    json = HTTP.getJSONObject(
                            String.format(URL, codeType, String.format("%d,%d,%d", rgb[0], rgb[1], rgb[2]))
                    );
                    break;
                default:
                    event.replyError("I only work with rgb or hex color codes.");
                    return;
            }
        } catch (NumberFormatException e) {
            event.replyError("The code you inserted isn't valid.");
            return;
        } catch (IOException | MissingHeaderInfoException e) {
            event.reply(
                    "There was a problem communicating with the color api. If this keeps occurring, try again later."
            );
            e.printStackTrace();
            return;
        } catch (HttpResponseFailedException e) {
            event.reply("I'm sorry but the connection with the color api failed. Please try again later.");
            return;
        } catch (HttpNullResponseException e) {
            event.reply("I'm sorry but the color api failed to respond. Please try again later.");
            return;
        }
        try {
            String hex = json.getJSONObject("hex").getString("value");
            JSONObject rgb = json.getJSONObject("rgb");
            EmbedBuilder colorEmbed = new EmbedBuilder();
            colorEmbed.setTitle(json.getJSONObject("name").getString("value"));
            colorEmbed.setDescription(
                    String.format(
                            "%s\n%s",
                            hex,
                           rgb.getString("value")
                    )
            );
            colorEmbed.setImage(String.format(IMAGE_URL, hex.toLowerCase().replace("#", "")));
            if (hex.equals("#FFFFFF")) hex = "#FFFFFE";
            colorEmbed.setColor(Color.decode(hex));
            event.reply(colorEmbed.build());
        } catch (JSONException e) {
            event.replyError(
                    "The code you inserted isn't valid.\n" +
                            "If this isn't your case then there might have been" +
                            " an error with the response from the color api.\n" +
                            "If this keeps occurring then try again later."
            );
        }
    }

}