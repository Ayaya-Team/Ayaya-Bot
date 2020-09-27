package ayaya.commands.moderator;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.*;

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
                " The mentions don't need to be separated between themselves.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.userPerms = new Permission[]{Permission.MANAGE_ROLES};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

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
            Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(message);
            Matcher idFinder;
            while (mentionFinder.find()) {
                idFinder = ANY_ID.matcher(mentionFinder.group());
                idFinder.find();
                guild.retrieveMemberById(idFinder.group()).queue(m -> {
                    if (m != null)
                        mute(author, authorHighestPosition, event.getSelfMember(),
                                highestPosition, m, guild, muteRole);
                }, t -> {});
            }
            String[] input = message.split(",");
            for (String s : input) {
                s = s.trim();
                mentionFinder = USER_MENTION.matcher(s);
                if (!mentionFinder.find()) {
                    final String arg = s;
                    guild.retrieveMembersByPrefix(s, 1).onSuccess(l -> {
                        if (l.isEmpty()) {
                            guild.retrieveMemberById(arg, true).queue(m -> {
                                if (m != null)
                                    mute(author, authorHighestPosition, event.getSelfMember(),
                                            highestPosition, m, guild, muteRole);
                            }, t -> {});
                        } else
                            mute(author, authorHighestPosition, event.getSelfMember(),
                                    highestPosition, l.get(0), guild, muteRole);
                    }).onError(t -> {});
                }
            }
            event.replySuccess("I attempted to mute all the members mentioned.");
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to mute? You didn't tell me yet.");
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
                    break;
                }
            }
            guild.addRoleToMember(member, muteRole)
                    .reason("Mute requested by " + author.getEffectiveName() + ".")
                    .queue(s -> {}, t -> {});
        }
    }

}