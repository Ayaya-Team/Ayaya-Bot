package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.List;
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
        this.aliases = new String[]{"guildinfo"};
        this.isGuildOnly = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        Guild guild = event.getGuild();
        OffsetDateTime creationTime = guild.getTimeCreated();
        String creationWeekDay = creationTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
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
        String community = guild.getFeatures().contains("COMMUNITY") ? "Yes" : "No";
        String emotes;
        StringBuilder emoteList = new StringBuilder();
        List<Emote> emoteArray = guild.getEmotes();
        for (Emote emote : emoteArray) {
            String s = emote.getAsMention();
            if (emoteList.length() + s.length() > FIELD_LIMIT)
                break;
            emoteList.append(s).append(' ');
        }
        emotes = emoteList.toString().trim();
        String roles;
        StringBuilder roleList = new StringBuilder();
        List<Role> roleArray = guild.getRoles();
        for (Role role : roleArray) {
            String s = role.getName();
            if (roleList.length() + s.length() + 4 > FIELD_LIMIT)
                break;
            if (roleList.length() == 0) roleList.append('`').append(s).append('`');
            else roleList.append(", `").append(s).append('`');
        }
        roles = roleList.toString();
        guild.retrieveMetaData().queue(md -> guild.retrieveOwner(true).queue(owner -> {
            int textChannelCount = guild.getTextChannels().size();
            int voiceChannelCount = guild.getVoiceChannels().size();
            int totalMembersAmount = md.getApproximateMembers();
            int totalMembersOnline = md.getApproximatePresences();
            long percentageOnline = Math.round((double) totalMembersOnline / (double) totalMembersAmount * 100);
            EmbedBuilder serverinfoEmbed = new EmbedBuilder()
                    .setTitle(guild.getName());
            serverinfoEmbed.addField("Owner", owner.getAsMention(), true)
                    .addField("Region", guild.getRegion().getName(), true)
                    .addField("Verification Level", vlevel, true)
                    .addField(
                            "Channels", "Text Channels: " + textChannelCount +
                                    "\nVoice Channels: " + voiceChannelCount +
                                    //"\nTotal: " + String.valueOf(textChannelCount + voiceChannelCount) +
                                    "\nCategories: " + String.valueOf(guild.getCategories().size()),
                            true
                    )
                    .addField(
                            "Members", "Total: " + totalMembersAmount
                                    + "\nOnline: " + totalMembersOnline + " (" + percentageOnline + "%)",
                            true
                    )
                    .addField("Is Community", community, true)
                    .addField("Created on",
                            String.format("%s, %s %s of %02d at %02d:%02d:%02d", creationWeekDay,
                                    creationTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                    Utils.getDayWithSuffix(creationTime.getDayOfMonth()), creationTime.getYear(),
                                    creationTime.getHour(), creationTime.getMinute(), creationTime.getSecond()),
                            false);
            if (!emotes.isEmpty())
                serverinfoEmbed.addField(String.format("Emojis (%d)", emoteArray.size()), emotes, false);
            if (!roles.isEmpty())
                serverinfoEmbed.addField(String.format("Roles (%d)", roleArray.size()), roles, false);
            serverinfoEmbed.setFooter(String.format(
                    "Requested by %s     Server ID: %s",
                    event.getAuthor().getName(), guild.getId()
            ), null).setThumbnail(guild.getIconUrl());
            try {
                serverinfoEmbed.setColor(event.getSelfMember().getColor());
            } catch (IllegalStateException | NullPointerException e) {
                serverinfoEmbed.setColor(Color.decode("#155FA0"));
            }
            event.reply(serverinfoEmbed.build());
        }));

    }

}