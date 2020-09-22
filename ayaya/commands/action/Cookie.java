package ayaya.commands.action;

import ayaya.commands.GuildDMSCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

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

        List<IMentionable> mentions = event.getMessage().getMentions(Message.MentionType.USER);
        if (mentions.isEmpty()) {
            event.reply("<:AngryAya:331115100771450880> Mention the person you want to give a cookie to, you baka!");
            return;
        }
        Member member = event.getGuild().retrieveMemberById(mentions.get(0).getId()).complete();
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

    }

    @Override
    protected void executeInDMS(CommandEvent event) {

        if (event.getMessage().getMentionedUsers().isEmpty()) {
            event.reply("<:AngryAya:331115100771450880> Mention the person you want to give a cookie to, you baka!");
            return;
        }
        User user = event.getMessage().getMentionedUsers().get(0);
        if (user == event.getAuthor()) {
            event.reply("Come on, don't save all the cookies for yourself, don't be greedy!");
        } else if (user == event.getSelfUser()) {
            event.reply("Oh, a :cookie: for me! Arigatou. <:AyaSmile:331115374739324930>");
        } else {
            event.reply("**" + user.getName() + "**, you got a :cookie: from **"
                    + event.getAuthor().getName() + "**!");
        }

    }

}