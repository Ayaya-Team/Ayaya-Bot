package ayaya.commands.action;

import ayaya.commands.GuildDMSCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;

import java.util.regex.Matcher;

import static ayaya.core.enums.CommandCategories.ACTION;

/**
 * Class of the cookie command.
 */
public class Cookie extends GuildDMSCommand {

    public Cookie() {

        this.name = "cookie";
        this.help = "Gives cookies to your friends! But be careful not to make them get diabetes. ^^";
        this.arguments = "{prefix}cookie <@user>";
        this.category = ACTION.asCategory();

    }

    @Override
    protected void executeInGuild(CommandEvent event) {

        Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(event.getArgs());
        Matcher idFinder;
        if (!mentionFinder.find()) {
            event.reply("<:AngryAya:331115100771450880> Mention the person you want to give a cookie to, you baka!");
            return;
        }
        idFinder = ANY_ID.matcher(mentionFinder.group());
        idFinder.find();
        event.getGuild().retrieveMemberById(idFinder.group(), true).queue(member -> {
            if (member == null)
                event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention in this server.");
            else if (member == event.getMember()) {
                event.reply("Come on, don't save all the cookies for yourself, don't be greedy!");
            } else if (member == event.getSelfMember()) {
                event.reply("Oh, a :cookie: for me! Arigatou. <:AyaSmile:331115374739324930>");
            } else {
                event.reply("**" + member.getEffectiveName() + "**, you got a :cookie: from **"
                        + event.getAuthor().getName() + "**!");
            }
        }, t -> event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention in this server."));

    }

    @Override
    protected void executeInDMS(CommandEvent event) {

        Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(event.getArgs());
        Matcher idFinder;
        if (!mentionFinder.find()) {
            event.reply("<:AngryAya:331115100771450880> Mention the person you want to give a cookie to, you baka!");
            return;
        }
        idFinder = ANY_ID.matcher(mentionFinder.group());
        idFinder.find();
        event.getJDA().retrieveUserById(idFinder.group(), true).queue(user -> {
            if (user == null)
                event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention here.");
            else if (user == event.getAuthor()) {
                event.reply("Come on, don't save all the cookies for yourself, don't be greedy!");
            } else if (user == event.getSelfUser()) {
                event.reply("Oh, a :cookie: for me! Arigatou. <:AyaSmile:331115374739324930>");
            } else {
                event.reply("**" + user.getName() + "**, you got a :cookie: from **"
                        + event.getAuthor().getName() + "**!");
            }
        }, t -> event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention here."));

    }

}