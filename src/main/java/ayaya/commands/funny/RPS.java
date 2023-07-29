package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.BotData;
import ayaya.core.utils.SQLController;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Random;

/**
 * Class of the rps command.
 */
public class RPS extends Command {

    private static final String WIN = "I win! <:AyaStar:331115397501943808>";
    private static final String LOSE = "You win. Congrats! <:AyaSmile:331115374739324930>";
    private static final String TIE = "Well, I guess it's a tie.";
    private static final String[] OPTIONS = {"rock", "paper", "scissors"};
    private static final int OPTIONS_AMOUNT = 3;

    public RPS() {

        this.name = "rps";
        this.help = "Play the rock paper scissors game!";
        this.arguments = "{prefix}rps rock/paper/scissors";
        this.category = CommandCategories.FUNNY.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        if (message.isEmpty()) {
            event.reply("You need to specify what you will choose: rock, paper or scissors.");
            return;
        }
        String option = message.toLowerCase();
        int ayaya_option = rng();
        int result;
        if (OPTIONS[ayaya_option].equals(option)) {
            event.reply("I chose " + option + ". " + getEmoji(option) + " " + TIE);
            return;
        }
        switch (option) {
            case "rock":
                result = ayaya_option-1;
                break;
            case "paper":
                result = ayaya_option-2;
                break;
            case "scissors":
                result = ayaya_option;
                break;
            default:
                event.replyError("There are only 3 options in this game: rock, paper or scissors.");
                return;
        }
        if (result == 0)
            event.reply("I chose " + OPTIONS[ayaya_option] + ". " + getEmoji(OPTIONS[ayaya_option]) + " " + WIN);
        else event.reply("I chose " + OPTIONS[ayaya_option] + ". " + getEmoji(OPTIONS[ayaya_option]) + " " + LOSE);

    }

    /**
     * Returns a random number between 0 and the total amount of options on the rps game (in this case, 3).
     * The total amount is excluded from the set of results.
     *
     * @return number
     */
    private int rng() {
        Random option = new Random();
        return option.nextInt(OPTIONS_AMOUNT);
    }

    /**
     * Fetches the emoji of the option chosed by the bot in the database.
     *
     * @param name the name of the option
     * @return emoji
     */
    private String getEmoji(String name) {
        String emoji = null;
        SQLController jdbc = new SQLController();
        try
        {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            Serializable[] o = new Serializable[]{name};
            emoji = jdbc.sqlSelect("SELECT * FROM emojis WHERE emoji_name = ?;", o, 5)
                    .getString("emoji");
        }
        catch(SQLException e)
        {
            System.out.println(
                    "A problem occurred while trying to get emoji! Aborting the process...");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                jdbc.close();
            }
            catch(SQLException e)
            {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return emoji;
    }

}