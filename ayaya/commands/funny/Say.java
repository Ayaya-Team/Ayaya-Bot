package ayaya.commands.funny;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the say command.
 */
public class Say extends Command {

    private static final int LIMIT = 2000;
    private static final List<Message.MentionType> ALLOWED_MENTIONS = Arrays.asList(
            Message.MentionType.CHANNEL, Message.MentionType.EMOTE
    );

    public Say() {

        this.name = "say";
        this.help = "Want me to say something for you? No problem, use this command then!\n" +
                "However you can't use this to tag anyone or roles.";
        this.arguments = "{prefix}say <message>";
        this.category = FUNNY.asCategory();
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        if (message.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> What do you want me to say? I can't guess it!");
            return;
        }
        RestAction.setDefaultFailure(ErrorResponseException.ignore(EnumSet.of(ErrorResponse.UNKNOWN_MESSAGE)));
        if (!event.getMessage().getMentions(
                    Message.MentionType.USER, Message.MentionType.HERE, Message.MentionType.EVERYONE
            ).isEmpty()
                && event.getChannelType().isGuild() && event.getGuild() != null
                && event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
            event.getMessage().delete().queue();
        message = event.getAuthor().getAsMention() + ": " + message;
        if (message.length() > LIMIT) {
            event.replyError("The input message must have less than 1900 characters in it.");
            return;
        }
        event.getChannel().sendMessage(message).allowedMentions(ALLOWED_MENTIONS).queue();

    }

}