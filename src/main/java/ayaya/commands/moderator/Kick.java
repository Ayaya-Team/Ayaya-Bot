package ayaya.commands.moderator;

import ayaya.commands.ModCommand;
import ayaya.core.Emotes;
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
 * Class of the kick command.
 */
public class Kick extends ModCommand {

    private Map<CommandEvent, ModActionData> cmdData;

    public Kick() {

        this.name = "kick";
        this.help = "Is someone doing bad things in your server without permission? Then why not kicking them?";
        this.arguments = "{prefix}kick <mention, name/nickname or id>" +
                "\n\nYou can mention more than one person or put more than one name/nickname/id in your command." +
                " Altough, separate all names/nicknames/ids and separate them from the mentions with a comma." +
                " The mentions don't need to be separated between themselves.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.KICK_MEMBERS, Permission.MESSAGE_WRITE};
        this.userPerms = new Permission[]{Permission.KICK_MEMBERS};
        this.cooldownTime = 5;
        cmdData = new ConcurrentHashMap<>(10);

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
                threadHandler.executeRestAction(
                        guild.retrieveMemberById(idFinder.group()),
                        m -> kick(author, event.getSelfMember(), m, guild, data, threadHandler),
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
                                            m -> kick(author, event.getSelfMember(), m, guild, data, threadHandler),
                                            t -> {
                                                data.putNotFound();
                                                threadHandler.onExecutionFinish();
                                            });
                                } else
                                    kick(author, event.getSelfMember(), l.get(0), guild, data, threadHandler);
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
            event.reply(Emotes.CONFUSED_EMOTE + " Who do you want me to kick? You didn't tell me yet.");
        }
    }

    /**
     * Performs a kick action.
     *
     * @param author the author of the triggered command
     * @param self   the member representing this bot
     * @param member the member to be kicked
     * @param guild  the guild where the command was triggered
     */
    private void kick(Member author, Member self, Member member, Guild guild, ModActionData data,
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
            guild.kick(member,"Kick requested by " + author.getEffectiveName() + ".")
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

    @Override
    protected void onFinish(CommandEvent event) {
        ModActionData data = cmdData.remove(event);
        String answer;
        switch (data.getSuccesses()) {
            case 0:
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was kicking them.");
                else if (data.getLackingPerms())
                    event.replyError(
                            "Due to lack of permissions I couldn't kick some of the people you mentioned." +
                                    " You aren't able to kick yourself or to kick people who aren't in the server."
                    );
                else if (data.getNotFound())
                    event.replyWarning("One or more users mentioned couldn't be found.");
                else
                    event.replyError("Couldn't kick one or more of the mentioned users" +
                            " due to an error in the Discord API.");
                break;
            case 1:
                answer = Emotes.OK_EMOTE + " 1 member was kicked." +
                        " No more actions are needed now.";
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was kicking them.");
                else if (data.getLackingPerms())
                    answer += " Couldn't kick all the people mentioned due to lack of permissions.";
                else if (data.getNotFound())
                    answer += " One or more users mentioned couldn't be found.";
                else if (data.hasException())
                    answer += " Couldn't kick one or more of the mentioned users" +
                            " due to an error in the Discord API.";
                event.reply(answer);
                break;
            default:
                answer = Emotes.OK_EMOTE + " "
                        + data.getSuccesses() + " members were kicked. No more actions are needed now.";
                if (data.getLeftGuild())
                    event.replyWarning("One or more users left the guild while I was kicking them.");
                else if (data.getLackingPerms())
                    answer += " Couldn't kick all the people mentioned due to lack of permissions.";
                else if (data.getNotFound())
                    answer += " One or more users mentioned couldn't be found.";
                else if (data.hasException())
                    answer += " Couldn't kick one or more of the mentioned users" +
                            " due to an error in the Discord API.";
                event.reply(answer);
        }
    }
}