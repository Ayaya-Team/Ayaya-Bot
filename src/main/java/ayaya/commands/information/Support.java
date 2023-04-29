package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.BotData;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the support command.
 */
public class Support extends Command {

    public Support() {

        this.name = "support";
        this.help = "For troubles with any command or direct bug reports, use this command.";
        this.arguments = "{prefix}support";
        this.category = INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        event.reply(
                "Having troubles with any of my commands or want to do a bug report?" +
                " Then you can join my support server through this link: <" + BotData.getServerInvite() + ">"
        );

    }

}