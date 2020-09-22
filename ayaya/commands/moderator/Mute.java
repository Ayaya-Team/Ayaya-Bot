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
 * Class of the mute command.
 */
public class Mute extends Command {

    private Member member;
    private int amountMuted;
    private boolean apiError;
    private boolean lackingPerms;
    private boolean alreadyMuted;

    public Mute() {

        this.name = "mute";
        this.help = "For anyone in the server who's misbehaving and deserves it," +
                " you can use this command to mute them." +
                " Make sure you have set a mute role with the name `muted` before using this command.";
        this.arguments = "{prefix}mute <@user, name/nickname or id>" +
                "\n\nYou can mention more than one person or put more than one name/nickname/id in your command." +
                " Altough, separate all mentions/names/nicknames/ids with a comma.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.userPerms = new Permission[]{Permission.MANAGE_ROLES};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        amountMuted = 0;
        apiError = false;
        lackingPerms = false;
        alreadyMuted = false;
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
                    " Please rename the role that isn't mean to be the real mute role or delete it.");
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
            authorHighestPosition = -1;
        final int highestPosition;
        if (!event.getSelfMember().getRoles().isEmpty())
            highestPosition = event.getSelfMember().getRoles().get(0).getPosition();
        else
            highestPosition = -1;
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
            for (String value : input) {
                member = null;
                String s = value.trim();
                mentionFinder = USER_MENTION.matcher(s);
                idFinder = ID.matcher(s);
                if (mentionFinder.find() && idFinder.find())
                    guild.retrieveMemberById(idFinder.group(), true)
                            .queue(m -> member = m, e -> apiError = true);
                else {
                    final Matcher finalIdFinder = idFinder;
                    final String arg = s.trim();
                    guild.retrieveMembersByPrefix(s.trim(), 1).onSuccess(l -> {
                        if (l.isEmpty() && finalIdFinder.find())
                            guild.retrieveMemberById(arg, true)
                                    .queue(m -> member = m, e -> apiError = true);
                        else
                            member = l.get(0);
                    }).onError(e -> apiError = true);
                }
                if (member != null)
                    mute(author, authorHighestPosition,
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
        switch (amountMuted) {
            case 0:
                if (apiError)
                    event.replyError(
                            "There was an issue with the Discord API or my Internet connection" +
                                    " so I could not finish your request."
                    );
                else if (lackingPerms)
                    event.replyError(
                            "Due to lack of permissions I couldn't mute any of the people you mentioned." +
                        " You aren't able to mute yourself or to mute people who are already muted."
                    );
                else if (alreadyMuted)
                    event.replyWarning("The user or users mentioned are already muted.");
                else
                    event.replyError("I'm sorry, but I can't find anyone with that name, mention or id.");
                break;
            case 1:
                answer = "<:KawaiiThumbup:361601400079253515> 1 member was muted." +
                        " When you want to unmute them, use the unmute command.";
                if (apiError)
                    answer += " Couldn't mute all the people mentioned due to" +
                            " an issue with the Discord API or my Internet connection";
                else if (lackingPerms)
                    answer += " Couldn't mute all the people mentioned due to lack of permissions.";
                else if (alreadyMuted)
                    answer += " Couldn't mute all the people mentioned" +
                            " due to some of them being already muted.";
                else if (notFound)
                    answer += " Couldn't mute all the people mentioned" +
                            " because I did not find any of them.";
                event.reply(answer);
                break;
            default:
                answer = "<:KawaiiThumbup:361601400079253515> "
                        + amountMuted + " members were muted. " +
                        "When you want to unmute them, use the unmute command.";
                if (apiError)
                    answer += " Couldn't mute all the people mentioned due to" +
                            " an issue with the Discord API or my Internet connection";
                else if (lackingPerms)
                    answer += " Couldn't mute all the people mentioned due to lack of permissions.";
                else if (alreadyMuted)
                    answer += " Couldn't mute all the people mentioned" +
                            " due to some of them being already muted.";
                else if (notFound)
                    answer += " Couldn't mute all the people mentioned" +
                            " because I did not find any of them.";
                event.reply(answer);
        }

    }

    private void mute(Member author, int authorHighestPosition, Member self, int highestPosition,
            Member member, Guild guild, Role muteRole)
    {
        List<Role> memberRoles;
        int memberHighestPosition = -1;
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
                    alreadyMuted = true;
                    break;
                }
            }
            guild.addRoleToMember(member, muteRole)
                    .reason("Mute requested by " + author.getEffectiveName() + ".")
                    .queue(s -> {}, e -> apiError = true);
            amountMuted++;
        } else {
            lackingPerms = true;
        }
    }

}