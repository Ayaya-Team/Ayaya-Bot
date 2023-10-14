package ayaya.commands.funny;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the roll command.
 */
public class Roll extends Command {

    private static final int LIMIT = 10000;

    public Roll() {

        this.name = "roll";
        this.help = "Launch the die and see what you get!";
        this.arguments = "{prefix}roll <number of dies>d<number of faces>\n\n" +
                "To only set the number of 6 faces dies just type `{prefix}roll <amount>` after the command. " +
                "You can also just roll a 6 faces die by typing the command with no arguments.";
        this.category = FUNNY.asCategory();
        this.aliases = new String[]{
                "dice",
                "die"};
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        if (!message.isEmpty()) {
            int amount;
            int faces = 6;
            try {
                amount = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                try {
                    String[] data = message.toLowerCase().split("d");
                    amount = Integer.parseInt(data[0]);
                    faces = Integer.parseInt(data[1]);
                } catch (NumberFormatException f) {
                    event.replyError("That's not a valid amount of dies.");
                    return;
                }
            }
            if (amount < 0)
                event.replyError("The amount of an object in real life is a positive number, not a negative one, baka.");
            else if (amount == 0)
                event.reply("Huh? Are you trying to roll an air die? <:AyaWhat:362990028915474432>");
            else if (amount > LIMIT)
                event.replyError("S-Sorry but, the maximum limit of dies at once is " + LIMIT + ".");
            else if (amount == 1) {
                if (faces < 0)
                    event.replyError("Any die in real life has a positive amount of faces, baka.");
                else if (faces == 0) {
                    event.reply("How can you even make a die with 0 faces? <:AyaWhat:362990028915474432>");
                } else if (faces < 4) {
                    event.replyError("There aren't any dies with less than 4 faces.");
                } else if (faces > 20) {
                    event.replyError("I don't think there can be dies with such a high amount of faces. For now I only accept the existence of dies with a maximum of 20 faces.");
                } else {
                    int finalFaces = faces;
                    event.reply("You rolled the die... :game_die:", m ->
                            m.editMessage("You rolled the die... :game_die:\nAnd you got a " +
                                roll(finalFaces, ThreadLocalRandom.current()) + "!").queueAfter(1, TimeUnit.SECONDS));
                }
            } else {
                if (faces < 0)
                    event.replyError("Any die in real life has a positive amount of faces, baka.");
                else if (faces == 0) {
                    event.reply("How can you even make a die with 0 faces? <:AyaWhat:362990028915474432>");
                } else if (faces < 4) {
                    event.replyError("There aren't any dies with less than 4 faces.");
                } else if (faces > 20) {
                    event.replyError("I don't think there can be dies with such a high amount of faces. " +
                            "For now I only accept the existence of dies with a maximum of 20 faces.");
                } else {
                    int finalAmount = amount;
                    int finalFaces = faces;
                    event.reply("You rolled the dies... :game_die:", m -> {
                        int total = 0;
                        ThreadLocalRandom rng = ThreadLocalRandom.current();
                        for (int i = 0; i < finalAmount; i++)
                            total += roll(finalFaces, rng);
                        m.editMessage("You rolled the dies... :game_die:\nAnd you got a total of " +
                                total + "!").queueAfter(1, TimeUnit.SECONDS);
                    });
                }
            }
        } else {
            event.reply("You rolled the die... :game_die:", m ->
                m.editMessage("You rolled the die... :game_die:\nAnd you got a " +
                        roll(6, ThreadLocalRandom.current()) + "!").queueAfter(1, TimeUnit.SECONDS));
        }

    }

    /**
     * Rolls a die with a given amount of faces and returns the result.
     *
     * @param face_amount the amount of faces
     * @return result
     */
    private int roll(int face_amount, ThreadLocalRandom die) {
        return die.nextInt(face_amount) + 1;
    }

}