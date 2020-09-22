package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.sql.SQLException;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the upvote command.
 */
public class Upvote extends Command {

    private String dblLink = "";
    private String dboatsLink = "";
    private String dbotLink = "";

    public Upvote() {

        this.name = "upvote";
        this.help = "If you like me you can upvote me in discordbots.org. This command will give you the link to upvote.";
        this.arguments = "{prefix}upvote";
        this.category = INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        getData();
        EmbedBuilder upvoteEmbed = new EmbedBuilder()
                .setDescription(
                        "You can upvote me in one of these websites:\n"
                        + "[Top GG](" + dblLink + ")\n"
                        + "[Discord Boats](" + dboatsLink + ")\n"
                        + "[Discord Bot List](" + dbotLink + ")"
                );
        try {
            upvoteEmbed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            upvoteEmbed.setColor(Color.decode("#155FA0"));
        }
        event.reply(upvoteEmbed.build());

    }

    /**
     * Fetches the required data from the database to execute this command.
     */
    private void getData() {
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            dblLink = jdbc.sqlSelect("SELECT * FROM botlists WHERE `list` LIKE 'dbl';", 5)
                    .getString("upvote");
            dboatsLink = jdbc.sqlSelect("SELECT * FROM botlists WHERE `list` LIKE 'dboats';", 5)
                    .getString("upvote");
            dbotLink = jdbc.sqlSelect("SELECT * FROM botlists WHERE `list` LIKE 'dbot';", 5)
                    .getString("upvote");
        } catch (SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the " + this.name + " command! Aborting the read process...");
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