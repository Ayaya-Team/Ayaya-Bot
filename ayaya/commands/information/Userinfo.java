package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.Utils;
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

    private static final String STAFF = "<:staff_badge:770616558769930251>";
    private static final String PARTNER = "<:partner_badge:770616558690369547>";
    private static final String BUG_HUNTER_LEVEL_1 = "<:bug_hunter_badge:770616558942552080>";
    private static final String BUG_HUNTER_LEVEL_2 = "<:golden_bug_hunter_badge:770661542756483134>";
    private static final String EARLY_SUPPORTER = "<:early_supporter_badge:770616558644232203>";
    private static final String HYPESQUAD = "<:hypesquad_badge:770653617127948300>";

    private static final String HYPESQUAD_BRAVERY = "<:hs_bravery_badge:770616559017132062>";
    private static final String HYPESQUAD_BRILLIANCE = "<:hs_brilliance_badge:770616559059599370>";
    private static final String HYPESQUAD_BALANCE = "<:hs_balance_badge:770616559034957834>";

    private static final String VERIFIED_BOT_DEVELOPER =
            "<:verified_bot_developer_badge:770616558941896724>";

    public Userinfo() {

        this.name = "userinfo";
        this.help = "When you want to know more about a user, trigger this command.";
        this.arguments = "{prefix}userinfo <mention, name/nickname or id>";
        this.category = INFORMATION.asCategory();
        this.isGuildOnly = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String content = event.getArgs();
        User mentionedUser;
        Member member;
        Guild guild = event.getGuild();
        Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(content);
        Matcher idFinder;
        if (mentionFinder.find()) {
            idFinder = ANY_ID.matcher(mentionFinder.group());
            idFinder.find();
            guild.retrieveMemberById(idFinder.group(), true).queue(m -> showUserInfo(event, m),
                    t -> event.replyError("I'm sorry, but I can't find anyone with that mention."));
        } else if (!content.isEmpty()) {
            guild.retrieveMembersByPrefix(content, 1).onSuccess(l -> {
                if (l.isEmpty())
                    guild.retrieveMemberById(content, true).queue(
                            m -> {
                                if (m != null)
                                    showUserInfo(event, m);
                            },
                            t -> event.replyError("I'm sorry, but I can't find anyone with that name/nickname or id.")
                    );
                else
                    showUserInfo(event, l.get(0));
            });
        } else {
            showUserInfo(event, event.getMember(), event.getAuthor());
        }

    }

    /**
     * Displays the information of a member.
     *
     * @param event  the event triggered for this command
     * @param member the member to display information from
     */
    private void showUserInfo(CommandEvent event, Member member) {
        showUserInfo(event, member, member.getUser());
    }

    /**
     * Displays the information of a member.
     *
     * @param event  the event triggered for this command
     * @param member the member to display information from
     * @param user   the user object associated with the member
     */
    private void showUserInfo(CommandEvent event, Member member, User user) {

        String description = "**Mention**: " + user.getAsMention()
                + "\n**User ID**: " + user.getId();
        if (!user.getFlags().isEmpty())
            description += "\n**Badges**: " + printBadges(user);
        OffsetDateTime joinTime = member.getTimeJoined();
        String joinWeekDay = joinTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        OffsetDateTime creationTime = user.getTimeCreated();
        String createWeekDay = creationTime.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.getDefault());
        String roles;
        StringBuilder roleList = new StringBuilder();
        for (int i = 0; i < member.getRoles().size(); i++) {
            if (i == 0) roleList.append("`").append(member.getRoles().get(i).getName()).append("`");
            else roleList.append(", `").append(member.getRoles().get(i).getName()).append("`");
        }
        if (roleList.length() > FIELD_LIMIT)
            roles = "It wasn't possible to list the roles of this user due to their amount being huge.";
        else roles = roleList.toString();
        EmbedBuilder userinfoEmbed = new EmbedBuilder()
                .setAuthor(user.getName() + "#" + user.getDiscriminator(), null)
                .setDescription(description);
        if (member.getNickname() != null) userinfoEmbed.addField("Nickname", member.getNickname(), false);
        else userinfoEmbed.addField("Nickname", "None", false);
        userinfoEmbed.addField("Joined on",
                String.format("%s, %s %s of %02d at %02d:%02d:%02d",
                        joinWeekDay, joinTime.getMonth()
                                .getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        Utils.getDayWithSuffix(joinTime.getDayOfMonth()), joinTime.getYear(),
                        joinTime.getHour(), joinTime.getMinute(), joinTime.getSecond()),
                false);
        userinfoEmbed.addField("Created on",
                String.format("%s, %s %s of %02d at %02d:%02d:%02d",
                        createWeekDay, creationTime.getMonth()
                                .getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        Utils.getDayWithSuffix(creationTime.getDayOfMonth()), creationTime.getYear(),
                        creationTime.getHour(), creationTime.getMinute(), creationTime.getSecond()),
                false);
        if (!roles.isEmpty())
            userinfoEmbed.addField("Roles", roles, false);
        String avatar = user.getEffectiveAvatarUrl();
        if (!avatar.contains("?size="))
            avatar = avatar + "?size=2048";
        userinfoEmbed.setThumbnail(avatar)
                .setColor(member.getColor())
                .setFooter("Requested by " + event.getAuthor().getName(), null);
        event.reply(userinfoEmbed.build());

    }

    /**
     * Prints a string with the badges of a user.
     *
     * @param user the user
     * @return string of badges
     */
    private String printBadges(User user) {
        StringBuilder s = new StringBuilder();
        for (User.UserFlag flag : user.getFlags()) {
            switch (flag) {
                case STAFF:
                    s.append(STAFF);
                    break;
                case PARTNER:
                    s.append(PARTNER);
                    break;
                case BUG_HUNTER_LEVEL_1:
                    s.append(BUG_HUNTER_LEVEL_1);
                    break;
                case BUG_HUNTER_LEVEL_2:
                    s.append(BUG_HUNTER_LEVEL_2);
                    break;
                case EARLY_SUPPORTER:
                    s.append(EARLY_SUPPORTER);
                    break;
                case HYPESQUAD:
                    s.append(HYPESQUAD);
                    break;
                case HYPESQUAD_BRAVERY:
                    s.append(HYPESQUAD_BRAVERY);
                    break;
                case HYPESQUAD_BRILLIANCE:
                    s.append(HYPESQUAD_BRILLIANCE);
                    break;
                case HYPESQUAD_BALANCE:
                    s.append(HYPESQUAD_BALANCE);
                    break;
                case VERIFIED_DEVELOPER:
                    s.append(VERIFIED_BOT_DEVELOPER);
                    break;
                default:
            }
            s.append(' ');
        }
        return s.toString().trim();
    }

}