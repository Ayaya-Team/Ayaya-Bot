package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.Emotes;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the say command.
 */
public class Say extends Command {

    private static final int LIMIT = 2000;
    private static final List<Message.MentionType> ALLOWED_MENTIONS = Arrays.asList(
            Message.MentionType.CHANNEL, Message.MentionType.EMOTE
    );
    private static final Pattern TEXT = Pattern.compile(":");
    private static final Pattern EMOTE = Pattern.compile("(([\\S&&[^:]]){2,}:)+?");
    private static final Pattern SNOWFLAKE = Pattern.compile("(([\\p{Digit}])+>){1}?");

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
            event.reply(Emotes.CONFUSED_EMOTE + " What do you want me to say? I can't guess it!");
            return;
        }

        String newMessage = message;
        Matcher matcher = TEXT.matcher(message);
        JDA jda = event.getJDA();
        List<String> parsedEmotes = new LinkedList<>();
        boolean notInGuild = !event.getChannelType().isGuild();
        while (matcher.find()) {
            matcher.usePattern(EMOTE);
            if (matcher.find()) {
                String groupString = matcher.group();
                if (parsedEmotes.isEmpty() || !parsedEmotes.contains(groupString)) {
                    int start = matcher.start();
                    if ((start > 1 && message.charAt(start - 2) == '<')
                            || (start > 2 && (message.charAt(start - 2) == 'a' && message.charAt(start - 3) == '<'))) {
                        matcher.usePattern(SNOWFLAKE);
                        if (matcher.find()) {
                            parsedEmotes.add(groupString);
                            matcher.usePattern(TEXT);
                            continue;
                        }
                    }

                    String emoteName = groupString.substring(0, groupString.length() - 1);
                    List<Emote> emotes;

                    if (notInGuild
                            || (emotes = event.getGuild().getEmotesByName(emoteName, false)).isEmpty()) {
                        emotes = jda.getEmotesByName(emoteName, false);
                    }

                    if (!emotes.isEmpty()) {
                        newMessage = newMessage.replace(":" + groupString, emotes.get(0).getAsMention());
                    }

                    parsedEmotes.add(groupString);
                }
            }
            matcher.usePattern(TEXT);
        }

        RestAction.setDefaultFailure(ErrorResponseException.ignore(EnumSet.of(ErrorResponse.UNKNOWN_MESSAGE)));
        if (event.getMessage().getMentions(
                Message.MentionType.USER, Message.MentionType.HERE, Message.MentionType.EVERYONE,
                Message.MentionType.ROLE
            ).isEmpty()
                && event.getChannelType().isGuild()
                && event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
            event.getMessage().delete().queue();
        newMessage = event.getAuthor().getAsMention() + ": " + newMessage;
        message = event.getAuthor().getAsMention() + ": " + message;
        if (newMessage.length() <= LIMIT)
            event.getChannel().sendMessage(newMessage).allowedMentions(ALLOWED_MENTIONS).queue();
        else if (message.length() <= LIMIT)
            event.getChannel().sendMessage(message).allowedMentions(ALLOWED_MENTIONS).queue();
        else
            event.replyError(String.format("The input message must have less than %d characters in it.", LIMIT));

    }

}