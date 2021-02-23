package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;

/**
 * Class of the quote command.
 */
public class Quote extends Command {

    public Quote() {

        this.name = "quote";
        this.help = "With this command you can get any quote from anywhere, but make sure I have the required perms to get it also.";
        this.arguments = "`{prefix}quote <channel id> <message id>` or `{prefix}quote <channel id> <message content>`\n\n"
                + "The channel id is only needed when trying to quote messages from a different channel than the one where this command is used." +
                " The message content also can only be part of the content of a message, but you can put the whole content for a more accurate search.";
        this.category = CommandCategories.FUNNY.asCategory();
        this.isGuildOnly = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();

        if (message.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You did not specify any arguments for this command." +
                    " Remember to check `" + event.getClient().getPrefix() + "help quote` in case you need more" +
                    " help with this command.");
            return;
        }

        Matcher matcher = ID.matcher(message);
        String channelID = "", messageID = "";

        if (matcher.find()) {
            if (matcher.end() == message.length() - 1)
                messageID = matcher.group();
            else if (matcher.end() < message.length() - 1) {
                channelID = matcher.group();
                if (matcher.find()) {
                    messageID = matcher.group();
                }
            }
        }

        Guild guild = event.getGuild();
        TextChannel channel;
        Message quote;
        User quoteAuthor;
        OffsetDateTime creation_time;

        if (channelID.isBlank())
            channel = event.getTextChannel();
        else
            channel = event.getGuild().getTextChannelById(channelID);

        if (messageID.isEmpty() && channel != null) {
            if (!event.getSelfMember().hasPermission(channel, Permission.MESSAGE_HISTORY)) {
                event.replyError(
                        "I do not have permission to see the message history in that channel."
                );
                return;
            }
            quote = getMessageByContent(
                    channel, message.substring(channelID.length()).trim(), event.getMessage().getId()
            );
        } else if (channel != null) {
            if (!event.getSelfMember().hasPermission(channel, Permission.MESSAGE_HISTORY)) {
                event.replyError(
                        "I do not have permission to see the message history in that channel."
                );
                return;
            }
            quote = getMessageByID(channel, messageID);
        } else {
            event.replyError("Couldn't find that channel, make sure you inserted it's correct id.");
            return;
        }

        if (quote == null) {
            event.replyError("Couldn't find that message, make sure you inserted the correct ids and/or there is a message with the content you typed.");
            return;
        }

        quoteAuthor = quote.getAuthor();
        creation_time = quote.getTimeCreated();
        EmbedBuilder quote_embed = new EmbedBuilder()
                .setAuthor("Message sent by "
                        + quoteAuthor.getName() + " on "
                        + channel.getName() + " at "
                        + guild.getName() + ":", null, quoteAuthor.getAvatarUrl())
                .setDescription(quote.getContentRaw())
                .setFooter("Created on "
                        + creation_time.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " "
                        + creation_time.getDayOfMonth() + "/"
                        + creation_time.getMonthValue() + "/"
                        + creation_time.getYear() + " at "
                        + String.format("%02d:%02d:%02d",
                        creation_time.getHour(), creation_time.getMinute(), creation_time.getSecond()), null);

        quote_embed.setColor(Color.decode("#155FA0"));
        List<Message.Attachment> attachments = quote.getAttachments();
        if (!attachments.isEmpty()) quote_embed.setImage(attachments.get(0).getUrl());
        event.reply(quote_embed.build());

    }

    /**
     * Checks if the given string contains a long.
     *
     * @param test the test string
     * @return true if the string contains a number, false on the contrary
     */
    private boolean isLong(String test) {
        Scanner scanner = new Scanner(test);
        boolean answer = scanner.hasNextLong();
        scanner.close();
        return answer;
    }

    /**
     * Fetches a message by it's id.
     *
     * @param channel the channel to check at
     * @param id      the id of the message
     * @return the message
     */
    private Message getMessageByID(TextChannel channel, String id) {
        Message quote = null;
        for (Message m : channel.getIterableHistory()) {
            if (m.getId().equals(id)) {
                quote = m;
                break;
            }
        }
        return quote;
    }

    /**
     * Fetches a message that contains the given content
     *
     * @param channel    the channel to check at
     * @param content    the content that the message must contain
     * @param idToIgnore the id of the message to ignore,
     *                   ideally this should be the message that triggered this command
     * @return the message
     */
    private Message getMessageByContent(TextChannel channel, String content, String idToIgnore) {
        Message quote = null;
        for (Message m : channel.getIterableHistory()) {
            if (!m.getId().equals(idToIgnore) && m.getContentRaw().contains(content)) {
                quote = m;
                break;
            }
        }
        return quote;
    }

}