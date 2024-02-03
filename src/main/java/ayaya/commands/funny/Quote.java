package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.Emotes;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Class of the quote command.
 */
public class Quote extends Command {

    private static final int MESSAGE_LIMIT = 200;

    public Quote() {

        this.name = "quote";
        this.help = "With this command you can get any quote from anywhere," +
                " but make sure I have the required perms to get it as well." +
                " This command searches through the last **200 messages** from the desired channel.";
        this.arguments = "`{prefix}quote <channel id> <message id>` or `{prefix}quote <channel id> <message content>`\n\n"
                + "The channel id is only needed when trying to quote messages from a different channel "
                + "that isn't the one where this command is used."
                + "\nThe message content can be part of the content to quote or the whole content, "
                + "but the more content in the search, the more accurate it will be.";
        this.category = CommandCategories.FUNNY.asCategory();
        this.isGuildOnly = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();

        if (message.isEmpty()) {
            event.reply(Emotes.CONFUSED_EMOTE + " You did not specify any arguments for this command." +
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
                    channel, message.substring(channelID.length()).trim(),
                    event.getMessage().getId(), event.getClient().getPrefix()
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
     * Fetches a message by it's id.
     *
     * @param channel the channel to check at
     * @param id      the id of the message
     * @return the message
     */
    private Message getMessageByID(TextChannel channel, String id) {
        Message quote = null;
        int count = 0;
        for (Message m : channel.getIterableHistory()) {
            if (m.getId().equals(id)) {
                quote = m;
                break;
            }
            if (++count == MESSAGE_LIMIT)
                break;
        }
        return quote;
    }

    /**
     * Fetches a message that contains the given content
     *
     * @param channel        the channel to check at
     * @param content        the content that the message must contain
     * @param idToIgnore     the id of the message to ignore,
     *                       ideally this should be the message that triggered this command
     * @param prefixToIgnore the prefix of the message content to ingnore,
     *                       ideally this should be the prefix of the bot
     * @return the message
     */
    private Message getMessageByContent(TextChannel channel, String content, String idToIgnore, String prefixToIgnore)
    {
        Message quote = null;
        int count = 0;
        for (Message m : channel.getIterableHistory()) {
            String messageContent = m.getContentRaw();
            if (!m.getId().equals(idToIgnore)
                    && !messageContent.startsWith(prefixToIgnore)
                    && messageContent.contains(content))
            {
                quote = m;
                break;
            }
            if (++count == MESSAGE_LIMIT)
                break;
        }
        return quote;
    }

}