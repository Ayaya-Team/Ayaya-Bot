package ayaya.commands.moderator;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * Class of the prune command.
 */
public class Prune extends Command {

    public Prune() {

        this.name = "prune";
        this.help = "Is the channel too messy? I can clean it for you! <:AyaSmile:331115374739324930>";
        this.arguments = "{prefix}prune <amount of messages>\n\n" +
                "To get the complete help list of this command, run it without any arguments.";
        this.aliases = new String[]{"purge"};
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_MANAGE};
        this.userPerms = new Permission[]{Permission.MESSAGE_MANAGE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        String input = event.getArgs();
        String args[] = input.split("--");
        String amountString = "";
        List<String> users = new LinkedList<>();
        boolean pruneBots = false;

        Matcher matcher, mentionFinder, idFinder;
        List<User> userList;
        List<Member> memberList;
        for (String arg: args) {
            arg = arg.trim();
            matcher = ARG.matcher(arg);
            if (matcher.find())
                switch (matcher.group().trim().toLowerCase()) {
                    case "bots":
                        pruneBots = true;
                        break;
                    case "users":
                        mentionFinder = Message.MentionType.USER.getPattern().matcher(arg);
                        while (mentionFinder.find()) {
                            idFinder = ANY_ID.matcher(mentionFinder.group());
                            idFinder.find();
                            users.add(idFinder.group());
                        }
                        for (String s: arg.trim().split(",")) {
                            s = s.trim();
                            mentionFinder = USER_MENTION.matcher(s);
                            idFinder = ID.matcher(s);
                            if (!mentionFinder.find()) {
                                if (idFinder.find())
                                    users.add(s);
                                else {
                                    userList = event.getJDA().getUsersByName(s, false);
                                    if (userList.isEmpty()) {
                                        memberList = guild.getMembersByEffectiveName(s, false);
                                        if (!memberList.isEmpty()) users.add(memberList.get(0).getId());
                                    } else users.add(userList.get(0).getId());
                                }
                            }
                        }
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

        final boolean bots = pruneBots;
        event.getMessage().delete().queue();
        List<Message> messages = new ArrayList<>(amount);
        channel.getHistoryBefore(event.getMessage(), amount).queue(h -> {
            int amountDeleted = 0;
            for (Message message : h.getRetrievedHistory()) {
                User author = message.getAuthor();
                if ((bots && message.getAuthor().isBot())
                        || (users.contains(author.getId()))
                        || (!bots && users.isEmpty())) {
                    messages.add(message);
                    amountDeleted++;
                }
            }

            try {
                ((TextChannel) channel).deleteMessages(messages).queue();
                event.reply("<:KawaiiThumbup:361601400079253515> "
                                + amountDeleted + " messages pruned with success.",
                        msg -> {
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                msg.delete().queue();
                            }
                        });
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

    private void printHelp(CommandEvent event) {
        String prefix = event.getClient().getPrefix();
        event.reply("```css\n" +
                "[Prune command help]\n" +
                "\n" +
                "To prune a specified amount of messages:\n" +
                "   " + prefix + "prune [amount] [options]\n\n" +
                "Options:\n" +
                "   --users [mentions, usernames or ids separated by commas]" +
                " - include this to prune messages from a specific user in a given amount of messages;\n\n" +
                "   --bots - prune messages from bots in a given amount of messages.\n\n" +
                "You don't need to always set these options or to always change a role's name.\n\n" +
                "Note: don't write the [] in your commands.\n" +
                " In servers with over 250 accounts connected this command is more reliable with user mentions than with user names/nicknames.\n" +
                "For more help join my support server. Use the link in " + prefix + "support.```");
    }

}