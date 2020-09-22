package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.SQLException;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the donate command.
 */
public class Donate extends Command {

    private String link;

    public Donate() {

        this.name = "donate";
        this.help = "This command will give you the link of my patreon page. If you really like me, please, consider donating.";
        this.arguments = "{prefix}donate";
        this.category = INFORMATION.asCategory();

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        getData();
        event.reply("If you are considering in supporting me, here is the link of my patreon page: <" + link +
                ">\nIf you donate you can get yourself a premium key which allows you to use premium only commands. Every bit helps.");

    }

    /**
     * Fetches the required data from the database to execute this command
     */
    private void getData() {
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            link = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'donate';", 5)
                    .getString("value");
        } catch (SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the " + this.name
                            + " command! Aborting the read process...");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                jdbc.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

}