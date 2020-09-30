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
 * Class of the unmute command.
 */
public class Unmute extends Command {

    public Unmute() {

        this.name = "unmute";
        this.help = "This is the command you can use to unmute someone who is currently muted.";
        this.arguments = "{prefix}unmute <@user or name>" +
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
            Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(message);
            Matcher idFinder;
            while (mentionFinder.find()) {
                idFinder = ANY_ID.matcher(mentionFinder.group());
                idFinder.find();
                guild.retrieveMemberById(idFinder.group()).queue(m -> {
                    if (m != null)
                        unmute(author, authorHighestPosition, event.getSelfMember(),
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
                                    unmute(author, authorHighestPosition, event.getSelfMember(),
                                            highestPosition, m, guild, muteRole);
                            }, t -> {});
                        } else
                            unmute(author, authorHighestPosition, event.getSelfMember(),
                                    highestPosition, l.get(0), guild, muteRole);
                    }).onError(t -> {});
                }
            }
            event.replySuccess("I attempted to unmute all the members mentioned.");
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to mute? You didn't tell me yet.");
        }
    }

    /**
     * Performs a unmute action.
     *
     * @param author                the author of the triggered command
     * @param authorHighestPosition the position of the top role of the command's author
     * @param self                  the member representing this bot
     * @param highestPosition       the top role position of this bot
     * @param member                the member to be unmuted
     * @param guild                 the guild where this command was triggered
     * @param muteRole              the mute role to be used
     */
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
                    break;
                }
            }
            guild.removeRoleFromMember(member, muteRole)
                    .reason("Unmute requested by " + author.getEffectiveName() + ".")
                    .queue(s -> {}, t -> {});
        }
    }

}