package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.TimeUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.regex.Matcher;

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
        User mentioned_user;
        Member member;
        Guild guild = event.getGuild();
        Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(content);
        Matcher idFinder;
        if (mentionFinder.find()) {
            idFinder = ANY_ID.matcher(mentionFinder.group());
            idFinder.find();
            guild.retrieveMemberById(idFinder.group(), true).queue(m -> showUserInfo(event, m));
        } else if (!content.isEmpty()) {
            guild.retrieveMembersByPrefix(content, 1).onSuccess(l -> {
                if (l.isEmpty())
                    guild.retrieveMemberById(content, true).queue(m -> {
                        if (m != null) showUserInfo(event, m);
                    }, t -> {});
                else
                    showUserInfo(event, l.get(0));
            });
        } else {
            showUserInfo(event, event.getMember(), event.getAuthor());
        }

    }

    private void showUserInfo(CommandEvent event, Member member) {
        showUserInfo(event, member, member.getUser());
    }

    private void showUserInfo(CommandEvent event, Member member, User user) {

        EmbedBuilder userinfo_embed = new EmbedBuilder();
        OffsetDateTime joinTime = member.getTimeJoined();
        String join_week_day = joinTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        OffsetDateTime creationTime = user.getTimeCreated();
        String create_week_day = creationTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String roles;
        StringBuilder role_list = new StringBuilder();
        for (int i = 0; i < member.getRoles().size(); i++) {
            if (i == 0) role_list.append("`").append(member.getRoles().get(i).getName()).append("`");
            else role_list.append(", `").append(member.getRoles().get(i).getName()).append("`");
        }
        if (role_list.length() > FIELD_LIMIT)
            roles = "It wasn't possible to list the roles of this user due to their amount being huge.";
        else roles = role_list.toString();
        userinfo_embed.setAuthor(user.getName() + "#" + user.getDiscriminator(),
                null);
        userinfo_embed.setDescription(
                "**Mention**: " + user.getAsMention() + "\n**User ID**: " + user.getId()
        );
        if (member.getNickname() != null) userinfo_embed.addField("Nickname", member.getNickname(), false);
        else userinfo_embed.addField("Nickname", "None", false);
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
        userinfo_embed.setThumbnail(user.getAvatarUrl());
        userinfo_embed.setColor(member.getColor());
        userinfo_embed.setFooter("Requested by " + event.getAuthor().getName(), null);
        event.reply(userinfo_embed.build());

    }

}