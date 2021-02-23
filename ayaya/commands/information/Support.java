package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.sql.SQLException;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the support command.
 */
public class Support extends Command {

    private String link;

    public Support() {

        this.name = "support";
        this.help = "For troubles with any command or direct bug reports, use this command.";
        this.arguments = "{prefix}support";
        this.category = INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        getData();
        event.reply(
                "Having troubles with any of my commands or want to do a direct bug report?" +
                " Then you can join my support server through this link: <" + link + ">"
        );

    }

    /**
     * Fetches the required data from the database to execute this command.
     */
    private void getData() {
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            link = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'support';", 5)
                    .getString("value");
        } catch (SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the " + this.name +" command! Aborting the read process...");
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