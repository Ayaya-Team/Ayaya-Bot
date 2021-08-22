package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.BotData;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the bigtext command.
 */
public class Bigtext extends Command {

    private static final int LIMIT = 2000;
    private static final List<Message.MentionType> ALLOWED_MENTIONS = Arrays.asList(
            Message.MentionType.CHANNEL, Message.MentionType.EMOTE
    );

    public Bigtext() {

        this.name = "bigtext";
        this.help = "Want me to say something for you? No problem, I can make it more visible too!";
        this.arguments = "{prefix}bigtext <message>";
        this.aliases = new String[]{"emojitext", "regionaltext"};
        this.category = FUNNY.asCategory();
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs().trim();
        if (message.isEmpty())
            event.reply("<:AyaWhat:362990028915474432> You want a big text of what? The air?");
        else {
            RestAction.setDefaultFailure(ErrorResponseException.ignore(EnumSet.of(ErrorResponse.UNKNOWN_MESSAGE)));
            if (!event.getMessage().getMentions(
                        Message.MentionType.USER, Message.MentionType.HERE, Message.MentionType.EVERYONE
                ).isEmpty()
                    && event.getChannelType().isGuild() && event.getGuild() != null &&
                    event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
                event.getMessage().delete().queue();
            String big_text = event.getAuthor().getAsMention() + ": " + getBigText(message);
            if (big_text.length() > LIMIT) {
                event.replyError("The input message is too long. Remember that 1 emote is more than 1 character.");
                return;
            }
            event.getChannel().sendMessage(big_text).allowedMentions(ALLOWED_MENTIONS).queue();
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
        String c;
        SQLController jdbc = new SQLController();
        int i = 0;
        try {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            ResultSet result;
            do {
                c = String.valueOf(args.charAt(i));
                result = jdbc.sqlSelect("SELECT * FROM emojis WHERE emoji_name='" + c.toLowerCase() + "';", 5);
                big_text.append(result.next() ? result.getString("emoji") : c)
                        .append(" ");
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