package ayaya.commands.moderator;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Class of the unmute command.
 */
public class Unmute extends Command {

    private Member member;
    private int amountUnmuted;
    private boolean apiError;
    private boolean lackingPerms;
    private boolean alreadyUnmuted;

    public Unmute() {

        this.name = "unmute";
        this.help = "This is the command you can use to unmute someone who is currently muted.";
        this.arguments = "{prefix}unmute <@user or name>" +
                "\n\nYou can mention more than one person or put more than one name/nickname/id in your command." +
                " Altough, separate all mentions/names/nicknames/ids with a comma.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.userPerms = new Permission[]{Permission.MANAGE_ROLES};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        amountUnmuted = 0;
        apiError = false;
        lackingPerms = false;
        alreadyUnmuted = false;
        boolean notFound = false;
        List<Role> roles = event.getGuild().getRolesByName("muted", false);
        if (roles.isEmpty()) {
            event.replyError("There isn't a role set to mute/unmute people. " +
                    "The role searched for this is a role named `muted`. You can create it manually." +
                    "\nRemember that for this role to be 100% effective it must have the send message permission " +
                    "explicitly denied on all text channels of a server.");
            return;
        }
        if (roles.size() > 1) {
            event.replyError("There is more than one role with the name `muted`." +
                    " Please rename the role that isn't mean to be the real mute role.");
        }
        String message = event.getArgs();
        Guild guild = event.getGuild();
        Member author = event.getMember();
        Role muteRole = roles.get(0);
        int muteRolePosition = muteRole.getPosition();
        StringBuilder name_list = new StringBuilder();
        final int authorHighestPosition;
        if (!author.getRoles().isEmpty())
            authorHighestPosition = author.getRoles().get(0).getPosition();
        else
            authorHighestPosition = 0;
        final int highestPosition;
        if (!event.getSelfMember().getRoles().isEmpty())
            highestPosition = event.getSelfMember().getRoles().get(0).getPosition();
        else
            highestPosition = 0;
        if (highestPosition <= muteRolePosition) {
            event.replyError(
                    "The mute rule is too high in the hierarchy for me to assign it." +
                            " Please move it below my highest role or move my highest role up."
            );
            return;
        }
        if (!message.isEmpty()) {
            String[] input = message.split(",");
            Matcher mentionFinder, idFinder;
            Member idMember = null;
            for (String s: input) {
                member = null;
                s = s.trim();
                mentionFinder = USER_MENTION.matcher(s);
                idFinder = ID.matcher(s);
                if (mentionFinder.find() && idFinder.find()) {
                    guild.retrieveMemberById(idFinder.group(), true)
                            .queue(m -> member = m, e -> apiError = true);
                } else {
                    final Matcher finalIdFinder = idFinder;
                    final String arg = s;
                    guild.retrieveMembersByPrefix(s, 1).onSuccess(l -> {
                        if (l.isEmpty() && finalIdFinder.find())
                            guild.retrieveMemberById(arg, true)
                                    .queue(m -> member = m, e -> apiError = true);
                        else
                            member = l.get(0);
                    }).onError(e -> apiError = true);
                }
                if (member != null)
                    unmute(author, authorHighestPosition,
                            event.getSelfMember(), highestPosition,
                            member, guild, muteRole);
                else
                    notFound = true;
            }
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to mute? You didn't tell me yet.");
            return;
        }
        String answer;
        switch (amountUnmuted) {
            case 0:
                if (apiError)
                    event.replyError(
                            "There was an issue with the Discord API or my Internet connection" +
                                    " so I could not finish your request."
                    );
                else if (lackingPerms)
                    event.replyError(
                            "Due to lack of permissions I couldn't unmute any of the people you mentioned." +
                                    " You aren't able to mute yourself or to mute people who are already muted."
                    );
                else if (alreadyUnmuted)
                    event.replyWarning("The user or users mentioned aren't muted.");
                else
                    event.replyError("I'm sorry, but I can't find anyone with that name, mention or id.");
                break;
            case 1:
                answer = "<:KawaiiThumbup:361601400079253515> 1 member was unmuted.";
                if (apiError)
                    answer += " Couldn't mute all the people mentioned due to" +
                            " an issue with the Discord API or my Internet connection";
                else if (lackingPerms)
                    answer += " Couldn't mute all the people mentioned due to lack of permissions.";
                else if (alreadyUnmuted)
                    answer += " Couldn't mute all the people mentioned" +
                            " due to some of them being already muted.";
                else if (notFound)
                    answer += " Couldn't mute all the people mentioned" +
                            " because I did not find any of them.";
                event.reply(answer);
                break;
            default:
                answer = "<:KawaiiThumbup:361601400079253515> "
                        + amountUnmuted + " members were muted.";
                if (apiError)
                    answer += " Couldn't mute all the people mentioned due to" +
                            " an issue with the Discord API or my Internet connection";
                else if (lackingPerms)
                    answer += " Couldn't mute all the people mentioned due to lack of permissions.";
                else if (alreadyUnmuted)
                    answer += " Couldn't mute all the people mentioned" +
                            " due to some of them being already muted.";
                else if (notFound)
                    answer += " Couldn't mute all the people mentioned" +
                            " because I did not find any of them.";
                event.reply(answer);
        }

    }

    private void unmute(Member author, int authorHighestPosition, Member self, int highestPosition,
                      Member member, Guild guild, Role muteRole)
    {
        List<Role> memberRoles;
        int memberHighestPosition = 0;
        memberRoles = member.getRoles();
        if (!memberRoles.isEmpty())
            memberHighestPosition = memberRoles.get(0).getPosition();
        if (
                !member.getId().equals(guild.getOwnerId())
                        && (author.getId().equals(guild.getOwnerId())
                        || memberHighestPosition < authorHighestPosition)
                        && memberHighestPosition < highestPosition
                        && !member.equals(author)
                        && !member.equals(self)
        ) {
            for (Role role : memberRoles) {
                if (role.equals(muteRole)) {
                    alreadyUnmuted = true;
                    break;
                }
            }
            guild.removeRoleFromMember(member, muteRole)
                    .reason("Unmute requested by " + author.getEffectiveName() + ".")
                    .queue(s -> {}, e -> apiError = true);
            amountUnmuted++;
        } else lackingPerms = true;
    }

}