package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.Emotes;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the flipcoin command.
 */
public class Flipcoin extends Command {

    private static final int LIMIT = 1000;

    public Flipcoin() {

        this.name = "flipcoin";
        this.help = "Flip a coin and see what's the result!";
        this.arguments = "{prefix}flipcoin <amount>\n\nWhen not specifying the amount you will flip a single coin.";
        this.category = FUNNY.asCategory();
        this.aliases = new String[]{"coin", "flip"};
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        String[] args = message.split(" ");
        if (!message.isEmpty()) {
            int amount = getAmount(args);
            if (amount < 0)
                event.replyError("That's not a valid positive amount of coins.");
            else if (amount == 0) event.reply("Throwing an air coin? " + Emotes.CONFUSED_EMOTE);
            else if (amount == 1) {
                event.reply("You throw a coin up in the air...", m ->
                    m.editMessage(
                            "You throw a coin up in the air...\nAnd you got "
                                    + flip() + "!").queueAfter(1, TimeUnit.SECONDS));
            } else if (amount > LIMIT) {
                event.replyError("Sorry but, the maximum limit of coins at once is "+LIMIT+".");
            } else {
                int heads = 0;
                int tails = 0;
                String side;
                for (int i = 0; i < amount; i++) {
                    side = flip();
                    if (side.equals("Heads")) heads++;
                    else tails++;
                }
                int finalHeads = heads, finalTails = tails;
                event.reply("You threw the coins up in the air...", m -> m.editMessage(
                        "You threw the coins up in the air...\nAnd you got "
                                + finalHeads + " Heads and " + finalTails + " Tails!")
                        .queueAfter(1, TimeUnit.SECONDS));
            }
        } else {
            event.reply("You throw a coin up in the air...", m ->
                m.editMessage(
                        "You throw a coin up in the air...\nAnd you got "
                                + flip() + "!").queueAfter(1, TimeUnit.SECONDS));
        }

    }

    /**
     * Retrieves the amount of coins.
     *
     * @param args the argument with the number
     * @return the number
     */
    private int getAmount(String[] args) {

        int value;
        for (String arg: args) {
            try {
                value = Integer.parseInt(arg);
                return value;
            } catch (NumberFormatException e) {}
        }
        return -1;

    }

    /**
     * Flips a coin and randomly returns "Heads" or "Tails".
     *
     * @return result
     */
    private String flip() {
        if (ThreadLocalRandom.current().nextBoolean()) {
            return "Heads";
        } else {
            return "Tails";
        }
    }

}