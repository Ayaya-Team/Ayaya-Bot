package ayaya.commands.moderator;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Class of the kick command.
 */
public class Kick extends Command {

    private Member member;
    private int amountKicked;
    private boolean apiError;
    private boolean lackingPerms;
    private boolean notFound;

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

        amountKicked = 0;
        apiError = false;
        lackingPerms = false;
        notFound = false;
        String message = event.getArgs();
        Guild guild = event.getGuild();
        Member author = event.getMember();
        if (!message.isEmpty()) {
            String[] input = message.split(",");
            Matcher mentionFinder, idFinder;
            for (String s: input) {
                member = null;
                s = s.trim();
                mentionFinder = USER_MENTION.matcher(s);
                idFinder = ID.matcher(s);
                if (mentionFinder.find() && idFinder.find()) {
                    guild.retrieveMemberById(idFinder.group(), true)
                            .queue(m -> member = m, e -> apiError = true);
                } else {
                    final Matcher finalIdFinder = idFinder;
                    final String arg = s;
                    guild.retrieveMembersByPrefix(s, 1).onSuccess(l -> {
                        if (l.isEmpty() && finalIdFinder.find())
                            guild.retrieveMemberById(arg, true)
                                    .queue(m -> member = m, e -> apiError = true);
                        else
                            member = l.get(0);
                    }).onError(e -> apiError = true);
                }
                if (member != null)
                    kick(author, event.getSelfMember(), member, guild);
                else
                    notFound = true;
            }
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to kick? You didn't tell me yet.");
            return;
        }
        switch (amountKicked) {
            case 0:
                if (apiError)
                    event.replyError(
                            "There was an issue with the Discord API or my Internet connection" +
                                    " so I could not finish your request."
                    );
                else if (lackingPerms)
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
                if (apiError)
                    answer += " Couldn't kick all the people mentioned due to" +
                            " an issue with the Discord API or my Internet connection";
                else if (lackingPerms)
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
                if (apiError)
                    answer += " Couldn't ban all the people mentioned due to" +
                            " an issue with the Discord API or my Internet connection";
                else if (lackingPerms)
                    answer += " Couldn't kick all the people mentioned due to lack of permissions.";
                else if (notFound)
                    answer +=
                            " Couldn't kick all the people mentioned" +
                                    " because I did not find any of them.";
                event.reply(answer);
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
            try {
                guild.kick(member,"Ban requested by " + author.getEffectiveName() + ".")
                        .queue(s -> {}, e -> apiError = true);
                amountKicked++;
            } catch (ErrorResponseException e) {
                notFound = true;
            }
        } else lackingPerms = true;
    }

}