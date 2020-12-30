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
 * Class of the invite command.
 */
public class Invite extends Command {

    private String invite_link;
    private String invite_admin;
    private String invite_minimal;

    public Invite() {

        this.name = "invite";
        this.help = "So you want to invite me to your server? Then use this command to see how.";
        this.arguments = "{prefix}invite";
        this.category = INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        getData();
        EmbedBuilder invite_embed = new EmbedBuilder()
                .setDescription("So you want to invite me to your server. How nice! <:AyaSmile:331115374739324930>\n"
                        + "Then you can select one of the options below:\n\n"
                        + "For normal permissions [click here](" + invite_link + ");\n"
                        + "For administrator permissions [click here](" + invite_admin + ");\n"
                        + "For minimal permissions [click here](" + invite_minimal + ").\n\n"
                        + "Note that with minimal permissions the moderation and administrator commands won't work.");
        try {
            invite_embed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            invite_embed.setColor(Color.decode("#155FA0"));
        }
        event.reply(invite_embed.build());

    }

    /**
     * Fetches the required data from the database to execute this command.
     */
    private void getData() {
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            invite_link = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'invite';", 5)
                    .getString("value");
            invite_admin = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'invite admin';", 5)
                    .getString("value");
            invite_minimal = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'invite minimal';", 5)
                    .getString("value");
        } catch (SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the invite command! Aborting the read process...");
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