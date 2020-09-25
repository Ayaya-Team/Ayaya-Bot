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
 * Class of the kick command.
 */
public class Kick extends Command {

    public Kick() {

        this.name = "kick";
        this.help = "Is someone doing bad things in your server without permission? Then why not kicking them?";
        this.arguments = "{prefix}kick <mention, name/nickname or id>" +
                "\n\nYou can mention more than one person or put more than one name/nickname/id in your command." +
                " Altough, separate all names/nicknames/ids/mentions with a comma.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.KICK_MEMBERS};
        this.userPerms = new Permission[]{Permission.KICK_MEMBERS};

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
                guild.retrieveMemberById(idFinder.group()).queue(m -> {
                    if (m != null)
                        kick(author, event.getSelfMember(), m, guild);
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
                                    kick(author, event.getSelfMember(), m, guild);
                            }, t -> {});
                        } else
                            kick(author, event.getSelfMember(), l.get(0), guild);
                    }).onError(t -> {});
                }
            }
            event.replySuccess("I attempted to kick all the members mentioned.");
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to kick? You didn't tell me yet.");
        }
    }

    private void kick(Member author, Member self, Member member, Guild guild) {
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
                    .queue(s -> {}, t -> {});
        }
    }

}