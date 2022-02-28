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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;

/**
 * Class of the unmute command.
 */
public class Unmute extends ModCommand {

    private Map<CommandEvent, ModActionData> cmdData;
    private ReentrantLock lock;

    public Unmute() {

        this.name = "unmute";
        this.help = "This is the command you can use to unmute someone who is currently muted.";
        this.arguments = "{prefix}unmute <@user or name>" +
                "\n\nYou can mention more than one person or put more than one name/nickname/id in your command." +
                " Altough, separate all names/nicknames/ids and separate them from the mentions with a comma." +
                " The mentions don't need to be separated between themselves.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MANAGE_ROLES, Permission.MESSAGE_WRITE};
        this.userPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.cooldownTime = 5;
        cmdData = new HashMap<>(10);
        lock = new ReentrantLock();

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
            ParallelThreadHandler<Member, List<Member>> threadHandler =
                    new ParallelThreadHandler<>();
            threadHandler.setFinalCallback(this::onFinish);
            threadHandler.setCommandEvent(event);
            ModActionData data = new ModActionData();
            while (mentionFinder.find()) {
                idFinder = ANY_ID.matcher(mentionFinder.group());
                idFinder.find();
                threadHandler.executeRestAction(
                        guild.retrieveMemberById(idFinder.group()),
                        m -> unmute(author, authorHighestPosition, event.getSelfMember(),
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
                                            m -> unmute(author, authorHighestPosition, event.getSelfMember(),
                                                    highestPosition, m, guild, muteRole, data, threadHandler),
                                            t -> {
                                                data.putNotFound();
                                                threadHandler.onExecutionFinish();
                                            }
                                    );
                                } else
                                    unmute(author, authorHighestPosition, event.getSelfMember(),
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
            cmdData.put(event, data);
            threadHandler.submittedAllThreads();
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
     * @param data                  the action data
     * @param threadHandler         the thread handler in use
     */
    private void unmute(Member author, int authorHighestPosition, Member self, int highestPosition,
                        Member member, Guild guild, Role muteRole, ModActionData data,
                        ParallelThreadHandler<Member, List<Member>> threadHandler) {
        boolean abort = true;
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
                    abort = false;
                    break;
                }
            }
            if (abort) {
                data.putRedundantAction();
                threadHandler.onExecutionFinish();
            } else {
                guild.removeRoleFromMember(member, muteRole)
                        .reason("Unmute requested by " + author.getEffectiveName() + ".")
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
        lock.lock();
        ModActionData data = cmdData.remove(event);
        lock.unlock();
        String answer;
        switch (data.getSuccesses()) {
            case 0:
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was unmuting them.");
                else if (data.getLackingPerms())
                    event.replyError(
                            "Due to lack of permissions I couldn't unmute some of the people you mentioned." +
                                    " You aren't able to unmute yourself or to unmute people who are already unmuted."
                    );
                else if (data.getRedundantAction())
                    event.replyWarning("One or more users are already unmuted.");
                else if (data.getNotFound())
                    event.replyWarning("One or more users mentioned couldn't be found.");
                else
                    event.replyError("Couldn't unmute one or more of the mentioned users" +
                            " due to an error in the Discord API.");
                break;
            case 1:
                answer = "<:KawaiiThumbup:361601400079253515> 1 member was unmuted." +
                        " When you want to unmute them, use the unmute command.";
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was unmuting them.");
                else if (data.getLackingPerms())
                    answer += " Couldn't unmute all the people mentioned due to lack of permissions.";
                else if (data.getRedundantAction())
                    answer += " One or more users are already unmuted.";
                else if (data.getNotFound())
                    answer += " One or more users mentioned couldn't be found.";
                else if (data.hasException())
                    answer += " Couldn't unmute one or more of the mentioned users" +
                            " due to an error in the Discord API.";
                event.reply(answer);
                break;
            default:
                answer = "<:KawaiiThumbup:361601400079253515> "
                        + data.getSuccesses() + " members were unmuted. " +
                        "When you want to unmute them, use the unmute command.";
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was unmuting them.");
                else if (data.getLackingPerms())
                    answer += " Couldn't unmute all the people mentioned due to lack of permissions.";
                else if (data.getRedundantAction())
                    answer += " One or more users are already unmuted.";
                else if (data.getNotFound())
                    answer += " One or more users mentioned couldn't be found.";
                else if (data.hasException())
                    answer += " Couldn't unmute one or more of the mentioned users" +
                            " due to an error in the Discord API.";
                event.reply(answer);
        }
    }

}