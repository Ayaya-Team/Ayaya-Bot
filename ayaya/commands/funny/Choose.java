package ayaya.commands.funny;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.Random;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the choose command.
 */
public class Choose extends Command {

    public Choose() {

        this.name = "choose";
        this.help = "Want me to pick an option for you?";
        this.arguments = "{prefix}choose <option 1>, <option 2>, ... <option n>\n\n" +
                "If no commas are found in the input, the options will be considered as " +
                "delimited by spaces.";
        this.category = FUNNY.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        if (message.isEmpty()) {
            event.reply("You didn't tell me any options to choose. <:AyaWhat:362990028915474432>");
            return;
        }
        String[] options;
        if (message.contains(","))
            options = message.split(",+?");
        else
            options = message.split(" +?");
        if (options.length < 2) {
            event.reply("You want me to be able to choose when there's only one option? <:AyaWhat:362990028915474432>");
            return;
        }
        int amount = options.length;
        Random rng = new Random();
        int num = rng.nextInt(amount);
        event.reply(
                "Hmmm... I choose " + options[num].trim() + "."
        );

    }

}