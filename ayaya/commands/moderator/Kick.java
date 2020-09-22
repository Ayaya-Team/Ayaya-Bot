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
 * Class of the kick command.
 */
public class Kick extends Command {

    public Kick() {

        this.name = "kick";
        this.help = "Is someone doing bad things in your server without permission? Then why not kicking them?";
        this.arguments = "{prefix}kick <mention, name/nickname or id>" +
                "\n\nYou can mention more than one person or put more than one name/nickname/id in your command." +
                " Altough, separate all names/nicknames/ids and separate them from the mentions with a comma." +
                " The mentions don't need to be separated between themselves."
                + "\nIn servers with over 250 accounts connected this command is more reliable with user mentions.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.KICK_MEMBERS};
        this.userPerms = new Permission[]{Permission.KICK_MEMBERS};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        if (!event.getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.replyError("I don't have permission to kick people. Try to ask an Admin to give me the Kick Members permission.");
            return;
        }
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.replyError("You don't have the Kick Members permission, therefore I won't kick someone with your order.");
            return;
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
                list = event.getGuild().getMembersByEffectiveName(s.trim(), false);
                if (list.isEmpty()) event.getGuild().getMembersByName(s.trim(), false);
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
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to kick? You didn't tell me yet.");
            return;
        }
		int authorHighestPosition = -1;
        if (!author.getRoles().isEmpty())
            authorHighestPosition = author.getRoles().get(0).getPosition();
        int highestPosition = -1;
        if (!event.getSelfMember().getRoles().isEmpty())
            highestPosition = event.getSelfMember().getRoles().get(0).getPosition();
        boolean lackingPerms = false;
        List<Role> roles;
        int amountKicked = 0;
        int memberHighestPosition;
        for (Member member : members) {
            memberHighestPosition = -1;
            roles = member.getRoles();
            if (!roles.isEmpty())
                memberHighestPosition = roles.get(0).getPosition();
            if (
                    !member.getId().equals(guild.getOwnerId())
                            && (author.getId().equals(guild.getOwnerId())
                            || memberHighestPosition < authorHighestPosition)
                            && memberHighestPosition < highestPosition
                            && !member.equals(author)
                            && !member.equals(event.getSelfMember())
            ) {
                guild.kick(member, "Kick requested by " + author.getEffectiveName() + ".").queue();
                amountKicked++;
            } else lackingPerms = true;
        }
        switch (amountKicked) {
            case 0:
                if (lackingPerms)
                    event.replyError(
                            "Due to lack of permissions I couldn't kick any of the people you mentioned." +
                                    " If you wanted to kick yourself, you can't do that," +
                                    " but you can leave the server."
                    );
                else
                    event.replyError(
                            "I'm sorry, but I can't find anyone with that name, mention or id."
                    );
                break;
            case 1:
                String answer = "<:KawaiiThumbup:361601400079253515> 1 member was kicked." +
                        " No more actions are needed now.";
                if (lackingPerms)
                    answer += " Couldn't kick all the people mentioned due to lack of permissions.";
                else if (notFound)
                    answer +=
                            " Couldn't kick all the people mentioned" +
                                    " because I did not find any of them.";
                event.reply(answer);
                break;
            default:
                answer = "<:KawaiiThumbup:361601400079253515> "
                        + amountKicked + " members were kicked. No more actions are needed now.";
                if (lackingPerms)
                    answer += " Couldn't kick all the people mentioned due to lack of permissions.";
                else if (notFound)
                    answer +=
                            " Couldn't kick all the people mentioned" +
                                    " because I did not find any of them.";
                event.reply(answer);
        }

    }

}