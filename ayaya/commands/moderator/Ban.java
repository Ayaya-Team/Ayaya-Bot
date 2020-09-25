package ayaya.commands.moderator;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Class of the ban command.
 */
public class Ban extends Command {

    public Ban() {

        this.name = "ban";
        this.help = "Someone being specially annoying in your server? Then let's ban that person!";
        this.arguments = "{prefix}ban <mention, name/nickname or id>" +
                "\n\nYou can mention more than one person or put more than one name/nickname/id in your command." +
                " Altough, separate all names/nicknames/ids/mentions with a comma.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.BAN_MEMBERS};
        this.userPerms = new Permission[]{Permission.BAN_MEMBERS};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        Guild guild = event.getGuild();
        Member author = event.getMember();
        if (!message.isEmpty()) {
            Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(message);
            Matcher idFinder;
            while (mentionFinder.find()) {
                idFinder = ANY_ID.matcher(mentionFinder.group());
                idFinder.find();
                final String id = idFinder.group();
                guild.retrieveMemberById(id).queue(m -> {
                    if (m == null)
                        ban(author, id, guild);
                    else
                        ban(author, event.getSelfMember(), m, guild);
                }, t -> {});
            }
            String[] input = message.split(",");
            for (String s: input) {
                s = s.trim();
                mentionFinder = USER_MENTION.matcher(s);
                if (!mentionFinder.find()) {
                    final String arg = s;
                    guild.retrieveMembersByPrefix(s, 1).onSuccess(l -> {
                       if (l.isEmpty()) {
                           guild.retrieveMemberById(arg, true).queue(m -> {
                               if (m == null)
                                   ban(author, arg, guild);
                               else
                                   ban(author, event.getSelfMember(), m, guild);
                           }, t -> {});
                       } else
                           ban(author, event.getSelfMember(), l.get(0), guild);
                    }).onError(t -> {});
                }
            }
            event.replySuccess("I attempted to ban all the members mentioned.");
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to ban? You didn't tell me yet.");
        }

    }

    private void ban(Member author, Member self, Member member, Guild guild) {
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
                    .queue(s -> {}, t -> {});
        }
    }

    private void ban(Member author, String id, Guild guild) {
        try {
            guild.ban(id, 0, "Ban requested by " + author.getEffectiveName() + ".")
                    .queue(s -> {}, t -> {});
        } catch (NumberFormatException e) {}
    }

}