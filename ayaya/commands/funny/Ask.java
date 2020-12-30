package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.sql.SQLException;
import java.util.Random;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the ask command.
 */
public class Ask extends Command {

    public Ask() {

        this.name = "ask";
        this.help = "Ask me anything! I will try to give the best answer possible.";
        this.arguments = "{prefix}ask <message>";
        this.category = FUNNY.asCategory();
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        if (event.getArgs().trim().isEmpty())
            event.reply("<:AyaWhat:362990028915474432> Did you want to ask me something?");
        else {
            event.reply(getRandomAnswer());
        }

    }

    /**
     * Fetches an answer randomly in the database.
     *
     * @return the answer
     */
    private String getRandomAnswer() {
        int amount = getAnswersAmount();
        Random rng = new Random();
        int id = rng.nextInt((amount & 0xff)) + 1;
        String answer = null;
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            answer = jdbc.sqlSelect("SELECT * FROM answers WHERE `id` LIKE '"+id+"';", 5)
                    .getString("string");
        } catch(SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the "+name+" command! Aborting the read process...");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                jdbc.close();
            } catch(SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return answer;
    }

    /**
     * Retrieves the amount of possible answers.
     *
     * @return answers amount
     */
    private int getAnswersAmount() {
        int amount = 0;
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            amount = Integer.parseInt(
                    jdbc.sqlSelect("SELECT * FROM sqlite_sequence WHERE name LIKE 'answers';", 5)
                    .getString("seq")
            );
        } catch(SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the "+name+" command! Aborting the read process...");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                jdbc.close();
            } catch(SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return amount;
    }

}