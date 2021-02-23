package ayaya.commands.action;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import static ayaya.core.enums.ActionQuotes.NORMAL_UNFLIPTABLE;
import static ayaya.core.enums.CommandCategories.ACTION;

/**
 * Class of the unfliptable command.
 */
public class Unfliptable extends Command {

    public Unfliptable() {

        this.name = "unfliptable";
        this.help = "Did you calm down? Then put the table where it was, please.";
        this.arguments = "{prefix}unfliptable";
        this.aliases = new String[]{"unflip"};
        this.category = ACTION.asCategory();

    }

    @Override
    protected void executeInstructions(CommandEvent event) {
        event.reply(NORMAL_UNFLIPTABLE.getQuote());
    }

}