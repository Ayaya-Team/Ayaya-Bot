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
 * Class of the ban command.
 */
public class Ban extends ModCommand {

    private Map<CommandEvent, ModActionData> cmdData;
    private ReentrantLock lock;

    public Ban() {

        this.name = "ban";
        this.help = "Someone being specially annoying in your server? Then let's ban that person!";
        this.arguments = "{prefix}ban <mention, name/nickname or id>" +
                "\n\nYou can mention more than one person or put more than one name/nickname/id in your command." +
                " Altough, separate all names/nicknames/ids and separate them from the mentions with a comma." +
                " The mentions don't need to be separated between themselves.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.BAN_MEMBERS};
        this.userPerms = new Permission[]{Permission.BAN_MEMBERS};
        this.cooldownTime = 5;
        cmdData = new HashMap<>(10);
        lock = new ReentrantLock();

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        Guild guild = event.getGuild();
        Member author = event.getMember();
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
                threadHandler.addRestAction(
                        guild.retrieveMemberById(idFinder.group()),
                        m -> ban(author, event.getSelfMember(), m, guild, data, threadHandler),
                        e -> {
                            data.putNotFound();
                            threadHandler.onExecutionFinish();
                        }
                );
            }
            String[] input = message.split(",");
            for (String s: input) {
                s = s.trim();
                mentionFinder = USER_MENTION.matcher(s);
                if (!mentionFinder.find()) {
                    final String arg = s;
                    threadHandler.addTask(
                            guild.retrieveMembersByPrefix(s, 1),
                            l -> {
                                if (l.isEmpty()) {
                                    guild.retrieveMemberById(arg, true).queue(
                                            m -> ban(author, event.getSelfMember(), m, guild, data, threadHandler),
                                            t -> ban(author, arg, guild, data, threadHandler)
                                    );
                                } else
                                    ban(author, event.getSelfMember(), l.get(0), guild, data, threadHandler);
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
            threadHandler.run();
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to ban? You didn't tell me yet.");
        }

    }

    /**
     * Performs a ban action.
     *
     * @param author        the author of the triggered command
     * @param self          the member representing this bot
     * @param member        the member to be banned
     * @param guild         the guild where the command was executed
     * @param data          the action data
     * @param threadHandler the thread handler in use
     */
    private synchronized void ban(Member author, Member self, Member member, Guild guild, ModActionData data,
                                  ParallelThreadHandler<Member, List<Member>> threadHandler) {
        int authorHighestPosition = -1;
        if (!author.getRoles().isEmpty())
            authorHighestPosition = author.getRoles().get(0).getPosition();
        int highestPosition = -1;
        if (!self.getRoles().isEmpty())
            highestPosition = self.getRoles().get(0).getPosition();
        List<Role> roles;
        int memberHighestPosition = -1;
        roles = member.getRoles();
        if (!roles.isEmpty())
            memberHighestPosition = roles.get(0).getPosition();
        if (
                !member.getId().equals(guild.getOwnerId())
                        && (author.getId().equals(guild.getOwnerId())
                        || memberHighestPosition < authorHighestPosition)
                        && memberHighestPosition < highestPosition
                        && !member.equals(author)
                        && !member.equals(self)
        ) {
            guild.ban(member, 0, "Ban requested by " + author.getEffectiveName() + ".")
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
        } else {
            data.putLackingPerms();
            threadHandler.onExecutionFinish();
        }
    }

    /**
     * Performs a ban action.
     *
     * @param author        the author of the triggered command
     * @param id            the id of the user to be banned
     * @param guild         the guild where the command was triggered
     * @param data          the action data
     * @param threadHandler the thread handler in use
     */
    private synchronized void ban(Member author, String id, Guild guild, ModActionData data,
                                  ParallelThreadHandler<Member, List<Member>> threadHandler) {
        try {
            guild.ban(id, 0, "Ban requested by " + author.getEffectiveName() + ".")
                    .queue(
                            v -> {
                                data.incrementSuccesses();
                                threadHandler.onExecutionFinish();
                            },
                            e -> {
                                if (e instanceof ErrorResponseException &&
                                        ((ErrorResponseException) e).getErrorResponse() ==
                                                ErrorResponse.UNKNOWN_MEMBER) {
                                    data.putNotFound();
                                } else
                                    data.putException();
                                threadHandler.onExecutionFinish();
                            }
                    );
        } catch (NumberFormatException e) {
            data.putNotFound();
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
                    event.replyWarning("One or more users left the guild while I was banning them.");
                else if (data.getLackingPerms())
                    event.replyError(
                            "Due to lack of permissions I couldn't ban some of the people you mentioned." +
                                    " You aren't able to ban yourself or to ban people who are already banned."
                    );
                else if (data.getNotFound())
                    event.replyWarning("One or more users mentioned couldn't be found.");
                else
                    event.replyError("Couldn't ban one or more of the mentioned users" +
                            " due to an error in the Discord API.");
                break;
            case 1:
                answer = "<:KawaiiThumbup:361601400079253515> 1 member was banned." +
                        " Now you don't have to worry about that person anymore.";
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was banning them.");
                else if (data.getLackingPerms())
                    answer += " Couldn't ban all the people mentioned due to lack of permissions.";
                else if (data.getNotFound())
                    answer += " One or more users mentioned couldn't be found.";
                else if (data.hasException())
                    answer += " Couldn't ban one or more of the mentioned users" +
                            " due to an error in the Discord API.";
                event.reply(answer);
                break;
            default:
                answer = "<:KawaiiThumbup:361601400079253515> "
                        + data.getSuccesses() + " members were banned. Now you don't have to worry about them anymore.";
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was banning them.");
                else if (data.getLackingPerms())
                    answer += " Couldn't ban all the people mentioned due to lack of permissions.";
                else if (data.getNotFound())
                    answer += " One or more users mentioned couldn't be found.";
                else if (data.hasException())
                    answer += " Couldn't ban one or more of the mentioned users" +
                            " due to an error in the Discord API.";
                event.reply(answer);
        }
    }

}