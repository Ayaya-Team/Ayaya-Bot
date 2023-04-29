package ayaya.commands.moderator;

import ayaya.commands.ModCommand;
import ayaya.core.enums.CommandCategories;
import ayaya.core.utils.ModActionData;
import ayaya.core.utils.ParallelThreadHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * Class of the mute command.
 */
public class Mute extends ModCommand {

    private Map<CommandEvent, ModActionData> cmdData;

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
        this.botPerms = new Permission[]{Permission.MANAGE_ROLES, Permission.MESSAGE_WRITE};
        this.userPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.cooldownTime = 5;
        cmdData = new ConcurrentHashMap<>(10);

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
                    " Please rename the role that isn't mean to be the real mute role or delete it.");
        }
        String message = event.getArgs();
        Guild guild = event.getGuild();
        Member author = event.getMember();
        Role muteRole = roles.get(0);
        int muteRolePosition = muteRole.getPosition();
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
            ParallelThreadHandler<Member, List<Member>> threadHandler =
                    new ParallelThreadHandler<>();
            threadHandler.setFinalCallback(this::onFinish);
            threadHandler.setCommandEvent(event);
            ModActionData data = new ModActionData();
            cmdData.put(event, data);
            while (mentionFinder.find()) {
                idFinder = ANY_ID.matcher(mentionFinder.group());
                idFinder.find();
                threadHandler.executeRestAction(
                        guild.retrieveMemberById(idFinder.group()),
                        m -> mute(author, authorHighestPosition, event.getSelfMember(),
                                highestPosition, m, guild, muteRole, data, threadHandler),
                        e -> {
                            data.putNotFound();
                            threadHandler.onExecutionFinish();
                        }
                );
            }
            String[] input = message.split(",");
            for (String s : input) {
                s = s.trim();
                mentionFinder = USER_MENTION.matcher(s);
                if (!mentionFinder.find()) {
                    final String arg = s;
                    threadHandler.executeTask(
                            guild.retrieveMembersByPrefix(s, 1),
                            l -> {
                                if (l.isEmpty()) {
                                    guild.retrieveMemberById(arg, true).queue(
                                            m -> mute(author, authorHighestPosition, event.getSelfMember(),
                                                    highestPosition, m, guild, muteRole, data, threadHandler),
                                            e -> {
                                                data.putNotFound();
                                                threadHandler.onExecutionFinish();
                                            }
                                    );
                                } else
                                    mute(author, authorHighestPosition, event.getSelfMember(),
                                            highestPosition, l.get(0), guild, muteRole, data, threadHandler);
                            },
                            e -> {
                                e.printStackTrace();
                                data.putException();
                                threadHandler.onExecutionFinish();
                            }
                    );
                }
            }
            threadHandler.submittedAllThreads();
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to mute? You didn't tell me yet.");
        }

    }

    /**
     * Performs a mute action.
     *
     * @param author                the author of the triggered command
     * @param authorHighestPosition the position of the top role of the command's author
     * @param self                  the member representing this bot
     * @param highestPosition       the top role position of this bot
     * @param member                the member to be muted
     * @param guild                 the guild where this command was triggered
     * @param muteRole              the mute role to be used
     * @param data                  the action data
     * @param threadHandler         the thread handler in use
     */
    private synchronized void mute(
            Member author, int authorHighestPosition, Member self, int highestPosition,
            Member member, Guild guild, Role muteRole, ModActionData data,
            ParallelThreadHandler<Member, List<Member>> threadHandler
    ) {
        boolean abort = false;
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
                    data.putRedundantAction();
                    abort = true;
                    break;
                }
            }
            if (abort) {
                threadHandler.onExecutionFinish();
            } else {
                guild.addRoleToMember(member, muteRole)
                        .reason("Mute requested by " + author.getEffectiveName() + ".")
                        .queue(
                                v -> {
                                    data.incrementSuccesses();
                                    threadHandler.onExecutionFinish();
                                },
                                e -> {
                                    if (e instanceof ErrorResponseException &&
                                            ((ErrorResponseException) e).getErrorResponse() ==
                                                    ErrorResponse.UNKNOWN_MEMBER) {
                                        data.putLeftGuild();
                                    } else
                                        data.putException();
                                    threadHandler.onExecutionFinish();
                                }
                        );
            }
        } else {
            data.putLackingPerms();
            threadHandler.onExecutionFinish();
        }
    }

    @Override
    protected void onFinish(CommandEvent event) {
        ModActionData data = cmdData.remove(event);
        String answer;
        switch (data.getSuccesses()) {
            case 0:
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was muting them.");
                else if (data.getLackingPerms())
                    event.replyError(
                            "Due to lack of permissions I couldn't mute some of the people you mentioned." +
                                    " You aren't able to mute yourself or to mute people who are already muted."
                    );
                else if (data.getRedundantAction())
                    event.replyWarning("One or more users are already muted.");
                else if (data.getNotFound())
                    event.replyWarning("One or more users mentioned couldn't be found.");
                else
                    event.replyError("Couldn't mute one or more of the mentioned users" +
                            " due to an error in the Discord API.");
                break;
            case 1:
                answer = "<:KawaiiThumbup:361601400079253515> 1 member was muted." +
                        " When you want to unmute them, use the unmute command.";
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was muting them.");
                else if (data.getLackingPerms())
                    answer += " Couldn't mute all the people mentioned due to lack of permissions.";
                else if (data.getRedundantAction())
                    answer += " One or more users are already muted.";
                else if (data.getNotFound())
                    answer += " One or more users mentioned couldn't be found.";
                else if (data.hasException())
                    answer += " Couldn't mute one or more of the mentioned users" +
                            " due to an error in the Discord API.";
                event.reply(answer);
                break;
            default:
                answer = "<:KawaiiThumbup:361601400079253515> "
                        + data.getSuccesses() + " members were muted. " +
                        "When you want to unmute them, use the unmute command.";
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was muting them.");
                else if (data.getLackingPerms())
                    answer += " Couldn't mute all the people mentioned due to lack of permissions.";
                else if (data.getRedundantAction())
                    answer += " One or more users are already muted.";
                else if (data.getNotFound())
                    answer += " One or more users mentioned couldn't be found.";
                else if (data.hasException())
                    answer += " Couldn't mute one or more of the mentioned users" +
                            " due to an error in the Discord API.";
                event.reply(answer);
        }
    }

}