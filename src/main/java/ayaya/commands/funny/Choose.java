package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.Emotes;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.concurrent.ThreadLocalRandom;

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
            event.reply("You didn't tell me any options to choose. " + Emotes.CONFUSED_EMOTE);
            return;
        }
        String[] options;
        if (message.contains(","))
            options = message.split(",+?");
        else
            options = message.split(" +?");
        if (options.length < 2) {
            event.reply("You want me to be able to choose when there's only one option? " + Emotes.CONFUSED_EMOTE);
            return;
        }
        int amount = options.length;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int num = rng.nextInt(amount);
        event.getChannel()
                .sendMessage("Hmmm... I choose " + options[num].trim() + ".")
                .allowedMentions(ALLOWED_MENTIONS).queue();


    }

}