package ayaya.commands.moderator;

import ayaya.commands.ModCommand;
import ayaya.core.enums.CommandCategories;
import ayaya.core.utils.ParallelThreadHandler;
import ayaya.core.utils.PruneActionData;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;

/**
 * Class of the prune command.
 */
public class Prune extends ModCommand {

    private static final String BOT_PRUNE = "bots";
    private static final String USER_PRUNE = "users";
    private static final String CONTENT_PRUNE = "content";

    private Map<CommandEvent, PruneActionData> cmdData;
    private ReentrantLock lock;

    public Prune() {

        this.name = "prune";
        this.help = "Is the channel too messy? I can clean it for you! <:AyaSmile:331115374739324930>";
        this.arguments = "{prefix}prune <amount of messages>\n\n" +
                "To get the complete help list of this command, run it without any arguments.";
        this.aliases = new String[]{"purge"};
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_MANAGE, Permission.MESSAGE_WRITE};
        this.userPerms = new Permission[]{Permission.MESSAGE_MANAGE};
        this.cooldownTime = 5;
        cmdData = new HashMap<>(10);
        lock = new ReentrantLock();

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        Guild guild = event.getGuild();
        String input = event.getArgs();
        String args[] = input.split("--");
        String amountString = "";
        boolean pruneBots = false;

        Matcher matcher, mentionFinder, idFinder;
        ParallelThreadHandler<Member, List<Member>> threadHandler =
                new ParallelThreadHandler<>();
        threadHandler.setFinalCallback(this::onFinish);
        threadHandler.setCommandEvent(event);
        PruneActionData data = new PruneActionData();
        for (String arg : args) {
            arg = arg.trim();
            matcher = ARG.matcher(arg);
            if (matcher.find())
                switch (matcher.group().trim().toLowerCase()) {
                    case BOT_PRUNE:
                        pruneBots = true;
                        break;
                    case USER_PRUNE:
                        mentionFinder = Message.MentionType.USER.getPattern().matcher(arg);
                        while (mentionFinder.find()) {
                            idFinder = ANY_ID.matcher(mentionFinder.group());
                            idFinder.find();
                            data.addUserId(idFinder.group());
                        }
                        for (String s : arg.trim().split(",")) {
                            s = s.trim();
                            mentionFinder = USER_MENTION.matcher(s);
                            idFinder = ID.matcher(s);
                            if (!mentionFinder.find()) {
                                if (idFinder.find()) {
                                    data.addUserId(idFinder.group());
                                } else {
                                    String finalS = s;
                                    threadHandler.addTask(
                                            guild.retrieveMembersByPrefix(s, 1),
                                            l -> {
                                                if (l.isEmpty())
                                                    data.addUserId(finalS);
                                                else
                                                    data.addMember(l.get(0));
                                                threadHandler.onExecutionFinish();
                                            },
                                            e -> {
                                                e.printStackTrace();
                                                threadHandler.onExecutionFinish();
                                            }
                                    );
                                }
                            }
                        }
                        break;
                    case CONTENT_PRUNE:
                        data.setContent(arg.substring(matcher.end()).trim());
                        break;
                    default:
                        amountString = matcher.group().trim();
                }
        }

        if (amountString.isEmpty()) {
            printHelp(event);
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountString.trim());
        } catch (NumberFormatException e) {
            event.replyError("That's not a valid amount of messages.");
            return;
        }
        if (amount < 5) {
            event.reply("That amount is too small. If you don't have much messages to prune then consider doing a manual prune.");
            return;
        }
        if (amount > 100) {
            event.reply("That amount is too big. The limit is 100 messages.");
            return;
        }

        data.setAmount(amount);
        data.setBotsFlag(pruneBots);
        cmdData.put(event, data);
        threadHandler.run();

    }

    private void printHelp(CommandEvent event) {
        String prefix = event.getClient().getPrefix();
        event.reply("```css\n" +
                "[Prune command help]\n" +
                "\n" +
                "To prune a specified amount of messages:\n" +
                "   " + prefix + "prune [amount] [options]\n\n" +
                "Options:\n" +
                "   --users [mentions or ids separated by commas]" +
                " - include this to prune messages from specific users in a given amount of messages;\n\n" +
                "   --bots - prune messages from all bots in a given amount of messages;\n\n" +
                "   --content [any content] - prune messages that contain specific content.\n\n" +
                "You don't need to always set these options or to always change a role's name.\n\n" +
                "Note: don't write the [] in your commands.\n" +
                " In servers with over 250 accounts connected this command is more reliable with user mentions than with user names/nicknames.\n" +
                "For more help join my support server. Use the link in " + prefix + "support.```");
    }

    @Override
    protected void onFinish(CommandEvent event) {

        lock.lock();
        PruneActionData data = cmdData.remove(event);
        lock.unlock();
        int amount = data.getAmount();
        List<String> users = data.getUsers();
        List<Member> members = data.getMembers();
        boolean bots = data.getBotsFlag();
        TextChannel channel = event.getTextChannel();
        Message message = event.getMessage();
        List<Message> messages = new ArrayList<>(amount);
        channel.getHistoryBefore(event.getMessage(), amount).queue(h -> {
            message.delete().queue();
            int amountDeleted = 0;
            for (Message m : h.getRetrievedHistory()) {
                User author = m.getAuthor();
                if ((bots && author.isBot())
                        || users.contains(author.getId())
                        || members.contains(m.getMember())
                        || (!bots && users.isEmpty()) && members.isEmpty() && data.getContent().isEmpty()
                        || m.getContentRaw().contains(data.getContent())) {
                    messages.add(m);
                    amountDeleted++;
                }
            }

            try {
                int finalAmountDeleted = amountDeleted;
                if (finalAmountDeleted == 1)
                    messages.get(0).delete().queue(
                            v -> event.reply("<:KawaiiThumbup:361601400079253515> "
                                            + "1 message pruned with success.",
                                    msg -> {
                                        try {
                                            TimeUnit.SECONDS.sleep(5);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } finally {
                                            msg.delete().queue();
                                        }
                                    })
                    );
                else
                    channel.deleteMessages(messages).queue(
                            v -> event.reply("<:KawaiiThumbup:361601400079253515> "
                                            + finalAmountDeleted + " messages pruned with success.",
                                    msg -> {
                                        try {
                                            TimeUnit.SECONDS.sleep(5);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } finally {
                                            msg.delete().queue();
                                        }
                                    })
                    );
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                event.replyError(
                        "Sadly I could not delete some or all of the messages because they are way too old."
                );
            } catch (ErrorResponseException e) {
                e.printStackTrace();
                event.replyError(
                        "Sadly I could not delete some or all of the messages because they don't exist anymore."
                );
            }
        });
    }

}