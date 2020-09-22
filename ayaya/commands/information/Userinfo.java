package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.TimeUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the userinfo command.
 */
public class Userinfo extends Command {

    private static final int FIELD_LIMIT = 1024;

    public Userinfo() {

        this.name = "userinfo";
        this.help = "When you want to know more about a user, trigger this command.";
        this.arguments = "{prefix}userinfo <@user or name>";
        this.category = INFORMATION.asCategory();
        this.isGuildOnly = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String content = event.getArgs();
        EmbedBuilder userinfo_embed = new EmbedBuilder();
        User user = event.getAuthor();
        User mentioned_user;
        Member member;
        Guild guild = event.getGuild();
        if (event.getMessage().getMentionedMembers().size() > 0) {
            mentioned_user = event.getMessage().getMentionedMembers().get(0).getUser();
            member = event.getMessage().getMentionedMembers().get(0);
        } else if (!content.isEmpty()) {
            List<Member> members = guild.getMembersByEffectiveName(content, false);
            if (members.isEmpty()) members = guild.getMembersByName(content, false);
            if (members.isEmpty()) {
                event.replyError("I'm sorry, but I can't find anyone with that name or mention.");
                return;
            }
            member = members.get(0);
            mentioned_user = member.getUser();
        } else {
            mentioned_user = user;
            member = event.getMember();
        }
        String status = member.getOnlineStatus().name();
        status = status.substring(0, 1) + status.substring(1).toLowerCase().replace('_', ' ');
        OffsetDateTime joinTime = member.getTimeJoined();
        String join_week_day = joinTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        OffsetDateTime creationTime = mentioned_user.getTimeCreated();
        String create_week_day = creationTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        StringBuilder activity = new StringBuilder();
        List<Activity> activities = member.getActivities();
        for (Activity a : activities) {
            switch (a.getType()) {
                case CUSTOM_STATUS:
                    activity.append("**Custom Status:** ");
                    Activity.Emoji emoji = a.getEmoji();
                    if (emoji != null && !emoji.isEmote())
                        activity.append(emoji.getAsMention()).append(' ');
                    activity.append(a.getName());
                    break;
                case WATCHING:
                    activity.append("**Watching:** ");
                    if (a.isRich())
                        activity.append(Objects.requireNonNull(a.asRichPresence()).getState());
                    else
                        activity.append(a.getName());
                    break;
                case LISTENING:
                    activity.append("**Listening:** ");
                    if (a.isRich())
                        activity.append(Objects.requireNonNull(a.asRichPresence()).getDetails())
                                .append(" by ").append(Objects.requireNonNull(a.asRichPresence()).getState());
                    else
                        activity.append(a.getName());
                    break;
                case STREAMING:
                    activity.append("**Streaming: **");
                    if (a.isRich())
                        activity.append(Objects.requireNonNull(a.asRichPresence()).getDetails());
                    else
                        activity.append(a.getName());
                    break;
                case DEFAULT:
                    activity.append("**Playing: **").append(a.getName());
                    break;
                default:
                    activity.append("**Other: **").append(a.getName());
            }
            activity.append('\n');

        }
        String roles;
        StringBuilder role_list = new StringBuilder();
        for (int i = 0; i < member.getRoles().size(); i++) {
            if (i == 0) role_list.append("`").append(member.getRoles().get(i).getName()).append("`");
            else role_list.append(", `").append(member.getRoles().get(i).getName()).append("`");
        }
        if (role_list.length() > FIELD_LIMIT)
            roles = "It wasn't possible to list the roles of this user due to their amount being huge.";
        else roles = role_list.toString();
        userinfo_embed.setAuthor(mentioned_user.getName() + "#" + mentioned_user.getDiscriminator(),
                null);
        userinfo_embed.setDescription(
                "**Mention**: " + mentioned_user.getAsMention() + "\n**User ID**: " + mentioned_user.getId()
        );
        if (member.getNickname() != null) userinfo_embed.addField("Nickname", member.getNickname(), true);
        else userinfo_embed.addField("Nickname", "None", true);
        userinfo_embed.addField("Status", status, true);
        if (activities.isEmpty()) {
            userinfo_embed.addField("Activity", "None", false);
        } else {
            userinfo_embed.addField("Activity", activity.toString(), false);
        }
        userinfo_embed.addField("Joined on",
                String.format("%s, %s %s of %02d at %02d:%02d:%02d",
                        join_week_day, joinTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        TimeUtils.getDayWithSuffix(joinTime.getDayOfMonth()), joinTime.getYear(), joinTime.getHour(),
                        joinTime.getMinute(), joinTime.getSecond()),
                false);
        userinfo_embed.addField("Created on",
                String.format("%s, %s %s of %02d at %02d:%02d:%02d",
                        create_week_day, creationTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        TimeUtils.getDayWithSuffix(creationTime.getDayOfMonth()), creationTime.getYear(),
                        creationTime.getHour(), creationTime.getMinute(), creationTime.getSecond()),
                false);
        if (!roles.isEmpty())
            userinfo_embed.addField("Roles", roles, false);
        userinfo_embed.setThumbnail(mentioned_user.getAvatarUrl());
        userinfo_embed.setColor(member.getColor());
        userinfo_embed.setFooter("Requested by " + event.getAuthor().getName(), null);
        event.reply(userinfo_embed.build());

    }

}