package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;

import java.sql.*;
import java.util.EnumSet;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the bigtext command.
 */
public class Bigtext extends Command {

    private static final int LIMIT = 2000;

    public Bigtext() {

        this.name = "bigtext";
        this.help = "Want me to say something for you? No problem, I can make it more visible too!";
        this.arguments = "{prefix}bigtext <message>";
        this.aliases = new String[]{"emojitext", "regionaltext"};
        this.category = FUNNY.asCategory();
        this.isGuildOnly = false;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs().trim();
        if (message.isEmpty())
            event.reply("<:AyaWhat:362990028915474432> You want a big text of what? The air?");
        else {
            RestAction.setDefaultFailure(ErrorResponseException.ignore(EnumSet.of(ErrorResponse.UNKNOWN_MESSAGE)));
            if (event.getChannelType().isGuild() && event.getGuild() != null &&
                    event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
                event.getMessage().delete().queue();
            String big_text = "From " + event.getAuthor().getAsTag()
                    + " (" + event.getAuthor().getId() + "): " + getBigText(message);
            if (big_text.length() > LIMIT) {
                event.replyError("The input message is too long. Remember that 1 emote is more than 1 character.");
                return;
            }
            event.reply(big_text);
        }

    }

    /**
     * Fetches the emotes that correspond to all the given characters and returns a string built from the emotes.
     *
     * @param args the args to convert
     * @return emoji text
     */
    private String getBigText(String args) {
        StringBuilder big_text = new StringBuilder();
        char c;
        SQLController jdbc = new SQLController();
        int i = 0;
        try {
            jdbc.open("jdbc:sqlite:data.db");
            ResultSet result;
            do {
                c = args.charAt(i);
                result = jdbc.sqlSelect("SELECT * FROM `emojis` WHERE `emoji name` LIKE '" + c + "';", 5);
                if (result.next()) {
                    big_text.append(result.getString("emoji"));
                } else {
                    big_text.append(c);
                }
                big_text.append(" ");
                i++;
            } while (i < args.length());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                jdbc.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return big_text.toString().trim();
    }

}