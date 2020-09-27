package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.TimeUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the serverinfo command.
 */
public class Serverinfo extends Command {

    private static final int FIELD_LIMIT = 1024;
    private static final String NONE = "Unrestricted";
    private static final String LOW = "Verified Email";
    private static final String MEDIUM = "Registered for 5+ minutes";
    private static final String HIGH = "Member for 10+ minutes";
    private static final String VERY_HIGH = "Verified Phone";
    private static final String UNKNOWN = "Unknown";

    public Serverinfo() {

        this.name = "serverinfo";
        this.help = "When you want to know more about a server, trigger this command inside that server.";
        this.arguments = "{prefix}serverinfo";
        this.category = INFORMATION.asCategory();
        this.isGuildOnly = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        Guild guild = event.getGuild();
        OffsetDateTime creationTime = guild.getTimeCreated();
        String creation_week_day = creationTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        int user_count = 0;
        int bot_count = 0;
        String vlevel;
        switch (guild.getVerificationLevel()) {
            case NONE:
                vlevel = NONE;
                break;
            case LOW:
                vlevel = LOW;
                break;
            case MEDIUM:
                vlevel = MEDIUM;
                break;
            case HIGH:
                vlevel = HIGH;
                break;
            case VERY_HIGH:
                vlevel = VERY_HIGH;
                break;
            default:
                vlevel = UNKNOWN;
        }
        String roles;
        StringBuilder role_list = new StringBuilder();
        for (int i = 0; i < guild.getRoles().size(); i++) {
            if (i == 0) role_list.append("`").append(guild.getRoles().get(i).getName()).append("`");
            else role_list.append(", `").append(guild.getRoles().get(i).getName()).append("`");
        }
        if (role_list.length() > FIELD_LIMIT)
            roles = "It wasn't possible to list the roles of this server due to their amount being huge.";
        else roles = role_list.toString();
        guild.retrieveMetaData().queue(md -> guild.retrieveOwner(true).queue(owner -> {
            EmbedBuilder serverinfo_embed = new EmbedBuilder()
                    .setTitle(guild.getName());
            int textChannelCount = guild.getTextChannels().size(), voiceChannelCount = guild.getVoiceChannels().size();
            serverinfo_embed.addField("Owner", owner.getAsMention(), true)
                    .addField("Region", guild.getRegion().getName(), true)
                    .addField("Verification Level", vlevel, true)
                    .addField("Categories", String.valueOf(guild.getCategories().size()), true)
                    .addField(
                            "Channels", "Text Channels: " + textChannelCount +
                                    "\nVoice Channels: " + voiceChannelCount +
                                    "\nTotal: " + String.valueOf(textChannelCount + voiceChannelCount), true
                    )
                    .addField(
                            "Members", "Total: " + md.getApproximateMembers()
                                    + "\nOnline: " + md.getApproximatePresences(), true
                    )
                    .addField("Server ID", guild.getId(), true)
                    .addField("Emojis", String.valueOf(guild.getEmotes().size()), true)
                    .addField("Created on",
                            String.format("%s, %s %s of %02d at %02d:%02d:%02d", creation_week_day,
                                    creationTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                    TimeUtils.getDayWithSuffix(creationTime.getDayOfMonth()), creationTime.getYear(),
                                    creationTime.getHour(), creationTime.getMinute(), creationTime.getSecond()),
                            false);
            if (!roles.isEmpty())
                serverinfo_embed.addField("Roles", roles, false);
            serverinfo_embed.setFooter("Requested by " + event.getAuthor().getName(), null)
                    .setThumbnail(guild.getIconUrl());
            try {
                serverinfo_embed.setColor(event.getSelfMember().getColor());
            } catch (IllegalStateException | NullPointerException e) {
                serverinfo_embed.setColor(Color.decode("#155FA0"));
            }
            event.reply(serverinfo_embed.build());
        }));

    }

}