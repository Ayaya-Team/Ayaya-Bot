package ayaya.commands.moderator;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Class of the mute command.
 */
public class Mute extends Command {

    public Mute() {

        this.name = "mute";
        this.help = "For anyone in the server who's misbehaving and deserves it," +
                " you can use this command to mute them." +
                " Make sure you have set a mute role with the name `muted` before using this command.";
        this.arguments = "{prefix}mute <@user, name/nickname or id>" +
                "\n\nYou can mention more than one person or put more than one name/nickname/id in your command." +
                " Altough, separate all names/nicknames/ids and separate them from the mentions with a comma." +
                " The mentions don't need to be separated between themselves."
                + "\nIn servers with over 250 accounts connected this command is more reliable with user mentions.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.userPerms = new Permission[]{Permission.MANAGE_ROLES};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        List<Role> roles = event.getGuild().getRolesByName("muted", false);
        if (roles.isEmpty()) {
            event.replyError("There isn't a role set to mute/unmute people. " +
                    "The role searched for this is a role named `muted`. You can create it manually." +
                    "\nRemember that for this role to be 100% effective it must have the send message permission " +
                    "explicitly denied on all text channels of a server.");
            return;
        }
        if (roles.size() > 1) {
            event.replyError("There is more than one role with the name `muted`. Please rename the role that isn't mean to be the real mute role.");
        }
        String message = event.getArgs();
        List<Member> members = new LinkedList<>();
        Guild guild = event.getGuild();
        Member author = event.getMember();
        members.addAll(event.getMessage().getMentionedMembers(guild));
        boolean notFound = false;
        if (!message.isEmpty()) {
            String[] names = message.split(",");
            List<Member> list;
            Matcher mentionFinder, idFinder;
            Member idMember = null;
            for (String s: names) {
                s = s.trim();
                mentionFinder = USER_MENTION.matcher(s);
                idFinder = ID.matcher(s);
                if (mentionFinder.find()) {
                    continue;
                }
                list = event.getGuild().getMembersByEffectiveName(s, false);
                if (list.isEmpty()) event.getGuild().getMembersByName(s, false);
                if (!list.isEmpty()) members.add(list.get(0));
                else if (idFinder.find()) {
                    try {
                        idMember = event.getGuild().getMemberById(s);
                    } catch (NumberFormatException e) {
                        notFound = true;
                    }
                }
                if (idMember != null) members.add(idMember);
            }
            if (members.isEmpty()) {
                event.replyError("I'm sorry, but I can't find anyone with that name, mention or id.");
                return;
            }
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to mute? You didn't tell me yet.");
            return;
        }
        Role muteRole = roles.get(0);
        int muteRolePosition = muteRole.getPosition();
        StringBuilder name_list = new StringBuilder();
        int authorHighestPosition = -1;
        if (!author.getRoles().isEmpty())
            authorHighestPosition = author.getRoles().get(0).getPosition();
        int highestPosition = -1;
        if (!event.getSelfMember().getRoles().isEmpty())
            highestPosition = event.getSelfMember().getRoles().get(0).getPosition();
        if (highestPosition <= muteRolePosition) {
            event.replyError(
                    "The mute rule is too high in the hierarchy for me to assign it." +
                    " Please move it below my highest role or move my highest role up."
            );
            return;
        }
        boolean lackingPerms = false;
        boolean alreadyMuted = false;
        List<Role> memberRoles;
        int amountMuted = 0;
        int memberHighestPosition;
        for (Member member : members) {
            memberHighestPosition = -1;
            memberRoles = member.getRoles();
            if (!memberRoles.isEmpty())
                memberHighestPosition = memberRoles.get(0).getPosition();
            if (
                    !member.getId().equals(guild.getOwnerId())
                            && (author.getId().equals(guild.getOwnerId())
                            || memberHighestPosition < authorHighestPosition)
                            && memberHighestPosition < highestPosition
                            && !member.equals(author)
                            && !member.equals(event.getSelfMember())
            ) {
                for (Role role : memberRoles) {
                    if (role.equals(muteRole)) {
                        alreadyMuted = true;
                        break;
                    }
                }
                guild.addRoleToMember(member, muteRole)
                        .reason("Mute requested by " + author.getEffectiveName() + ".").queue();
                amountMuted++;
            } else lackingPerms = true;
        }
        String answer;
        switch (amountMuted) {
            case 0:
                if (lackingPerms)
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
                if (lackingPerms)
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
                if (lackingPerms)
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

}