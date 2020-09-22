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
 * Class of the ban command.
 */
public class Ban extends Command {

    private Member member;
    private String id;
    private int amountBanned;
    private boolean apiError;
    private boolean lackingPerms;
    private boolean notFound;

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

        amountBanned = 0;
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
                id = "";
                s = s.trim();
                mentionFinder = USER_MENTION.matcher(s);
                idFinder = ID.matcher(s);
                if (mentionFinder.find() && idFinder.find()) {
                    id = idFinder.group();
                    guild.retrieveMemberById(id, true).queue(m -> {
                        if (m != null) {
                            member = m;
                            id = "";
                        }
                    });
                } else {
                    final Matcher finalIdFinder = idFinder;
                    final String arg = s.trim();
                    guild.retrieveMembersByPrefix(s.trim(), 1).onSuccess(l -> {
                        if (l.isEmpty() && finalIdFinder.find())
                            guild.retrieveMemberById(arg, true).queue(m -> {
                                if (m != null) {
                                    member = m;
                                } else
                                    id = arg;
                            }, t -> apiError = true);
                        else
                            member = l.get(0);
                    });
                }
                if (member != null)
                    ban(author, event.getSelfMember(), member, guild);
                else if (!id.isEmpty())
                    ban(author, id, guild);
            }
        } else {
            event.reply("<:AyaWhat:362990028915474432> Who do you want me to ban? You didn't tell me yet.");
            return;
        }
        switch (amountBanned) {
            case 0:
                if (apiError)
                    event.replyError(
                            "There was an issue with the Discord API or my Internet connection" +
                                    " so I could not finish your request."
                    );
                else if (lackingPerms)
                    event.reply(
                            "Due to lack of permissions I couldn't ban any of the people you mentioned."
                                    + " If you wanted to ban yourself, you can't really do that."
                    );
                else
                    event.replyError(
                            "I'm sorry, but I can't find anyone with that name, mention or id."
                    );
                break;
            case 1:
                String answer = "1 member was banned." +
                        " Now you don't have to worry with that person anymore.";
                if (apiError)
                    answer += " Couldn't ban all the people mentioned due to" +
                            " an issue with the Discord API or my Internet connection";
                else if (lackingPerms)
                    answer += " Couldn't ban all the people mentioned due to lack of permissions.";
                else if (notFound)
                    answer +=
                            " Couldn't ban all the people mentioned" +
                                    " because I did not find any of them.";
                event.replySuccess(answer);
                break;
            default:
                answer = amountBanned + " members were banned." +
                        " Now you don't have to worry with them anymore.";
                if (apiError)
                    answer += " Couldn't ban all the people mentioned due to" +
                            " an issue with the Discord API or my Internet connection";
                else if (lackingPerms)
                    answer += " Couldn't ban all the people mentioned due to lack of permissions.";
                else if (notFound)
                    answer += " Couldn't ban all the people mentioned" +
                            " because I did not find all of them.";
                event.replySuccess(answer);
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
            try {
                guild.ban(member, 0, "Ban requested by " + author.getEffectiveName() + ".")
                        .queue(s -> {}, e -> apiError = true);
                amountBanned++;
            } catch (ErrorResponseException e) {
                notFound = true;
            }
        } else lackingPerms = true;
    }

    private void ban(Member author, String id, Guild guild) {
        try {
            guild.ban(id, 0, "Ban requested by " + author.getEffectiveName() + ".")
                    .queue(s -> {}, e -> apiError = true);
            amountBanned++;
        } catch (NumberFormatException e) {
            notFound = true;
        }
    }

}