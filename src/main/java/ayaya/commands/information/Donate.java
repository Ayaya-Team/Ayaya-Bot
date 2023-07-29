package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.BotData;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the donate command.
 */
public class Donate extends Command {

    private static final String TEXT = "Hey, I hope you've had a nice experience with me so far." +
            " Most of my features are free and new ones will keep coming in the future," +
            " but hosting a discord bot will never be for free." +
            " If you like the work of my developer and you have some extra money you may not need," +
            " it would be nice if you could support me by pledging on patreon page." +
            "\nIf you pledge, you'll receive a premium key you can use or give to another user." +
            " When claimed, a premium key unlocks access to premium-only commands." +
            " To claim a premium key you received, just do `%sclaimkey <key>`.\nMy patreon page is here: <%s>";

    public Donate() {

        this.name = "donate";
        this.help = "This command will give you the link of my patreon page. If you really like me, please, consider donating.";
        this.arguments = "{prefix}donate";
        this.category = INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        //event.reply("If you are considering in supporting me, here is the link of my patreon page: <" + BotData.getPatreonLink() +
        //        ">\nIf you donate you can get yourself a premium key which allows you to use premium only commands. Every bit helps.");
        event.reply(String.format(TEXT, event.getClient().getPrefix(), BotData.getPatreonLink()));

    }

}