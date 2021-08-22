package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the channelinfo command.
 */
public class Channelinfo extends Command {

    public Channelinfo() {

        this.name = "channelinfo";
        this.help = "When you want to know more about a channel in a server, trigger this command.";
        this.arguments = "{prefix}channelinfo <#channel or name>\n\nTo get the info of the channel you're in run: {prefix}channelinfo";
        this.category = INFORMATION.asCategory();
        this.isGuildOnly = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String name = event.getArgs();
        EmbedBuilder channelinfo_embed = new EmbedBuilder();
        List<net.dv8tion.jda.api.entities.Category> categories = event.getGuild().getCategories();
        GuildChannel channel;
        if (event.getMessage().getMentionedChannels().size() > 0)
            channel = event.getMessage().getMentionedChannels().get(0);
        else if (!name.isEmpty()) {
            List<TextChannel> textChannels = event.getGuild().getTextChannelsByName(name, false);
            List<VoiceChannel> voiceChannels = event.getGuild().getVoiceChannelsByName(name, false);
            List<StoreChannel> storeChannels = event.getGuild().getStoreChannelsByName(name, false);
            if (!textChannels.isEmpty()) channel = textChannels.get(0);
            else if (!voiceChannels.isEmpty()) channel = voiceChannels.get(0);
            else if (!storeChannels.isEmpty()) channel = storeChannels.get(0);
            else {
                event.replyError("I can't find a channel with that name. Make sure you wrote it correctly.");
                return;
            }
        } else channel = event.getTextChannel();
        String type = channel.getType().name();
        type = type.substring(0, 1) + type.substring(1).toLowerCase();
        String categoryName = null;
        for (net.dv8tion.jda.api.entities.Category category: categories) {
            for (GuildChannel c: category.getChannels()) {
                if (channel.equals(c)) {
                    categoryName = category.getName();
                    break;
                }
            }
        }
        if (categoryName == null) {
            categoryName = "None";
        }
        OffsetDateTime creationTime = channel.getTimeCreated();
        String creation_week_day = creationTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        if (channel instanceof TextChannel) {
            TextChannel textChannel = (TextChannel) channel;
            String nsfw = "No";
            if (textChannel.isNSFW()) nsfw = "Yes";
            String topic = textChannel.getTopic();
            channelinfo_embed.setTitle("#" + channel.getName());
            if (topic != null)
                channelinfo_embed.setDescription(topic);
            channelinfo_embed.addField("Type", type, true)
                    .addField("Mention", textChannel.getAsMention(), true)
                    .addField("Category", categoryName, true)
                    .addField("NSFW", nsfw, true);
        } else if (channel instanceof VoiceChannel) {
            VoiceChannel voiceChannel = (VoiceChannel) channel;
            int bitrate = voiceChannel.getBitrate() / 1000;
            channelinfo_embed.setTitle(channel.getName())
                    .addField("Type", type, true)
                    .addField("Bitrate", bitrate + " kbps", true)
                    .addField("User Limit", String.valueOf(voiceChannel.getUserLimit()), true)
                    .addField("Category", categoryName, true);
        } else {
            channelinfo_embed.setTitle(channel.getName())
                    .addField("Type", type, true)
                    .addField("Category", categoryName, true);
        }
        channelinfo_embed.addField("Created on",
                String.format("%s, %s %s of %02d at %02d:%02d:%02d",
                        creation_week_day, creationTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        Utils.getDayWithSuffix(creationTime.getDayOfMonth()), creationTime.getYear(),
                        creationTime.getHour(), creationTime.getMinute(), creationTime.getSecond()),
                false);
        channelinfo_embed.setFooter(
                String.format("Requested by %s     Channel ID: %s", event.getAuthor().getName(), channel.getId()),
                null
        );
        try {
            channelinfo_embed.setColor(event.getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            channelinfo_embed.setColor(Color.decode("#155FA0"));
        }
        event.reply(channelinfo_embed.build());

    }

}