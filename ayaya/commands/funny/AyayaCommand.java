package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.sql.SQLException;
import java.util.Random;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the ayaya command.
 */
public class AyayaCommand extends Command {

    private static final String NULL = "NULL";

    private String url, quote, footer_quote;

    public AyayaCommand() {

        this.name = "ayaya";
        this.help = "This gives you a random gif of me. Wait wha?! N-No don't use it p-please!!";
        this.arguments = "{prefix}ayaya";
        this.category = FUNNY.asCategory();
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        int id = fetchData();
        if (id == 19)
            event.reply(quote);
        else {
            EmbedBuilder ayaya_embed = new EmbedBuilder().setImage(url);
            if (!quote.equals(NULL)) ayaya_embed.setDescription(quote);
            if (!footer_quote.equals(NULL)) ayaya_embed.setFooter(footer_quote, null);
            try {
                ayaya_embed.setColor(event.getGuild().getSelfMember().getColor());
            } catch (IllegalStateException | NullPointerException e) {
                ayaya_embed.setColor(Color.decode("#155FA0"));
            }
            event.reply(ayaya_embed.build());
        }

    }

    /**
     * Fetches a random resnpose from the database and returns it's id.
     *
     * @return response's id
     */
    private int fetchData() {

        Random rand = new Random();
        int id = rand.nextInt((getGifsAmount() & 0xff)) + 1;
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            url = jdbc.sqlSelect("SELECT * FROM ayaya WHERE `gif id` LIKE '" + id + "';", 5)
                    .getString("link");
            quote = jdbc.sqlSelect("SELECT * FROM `ayaya quotes` WHERE `quote id` LIKE '" + id + "';", 5)
                    .getString("quote");
            footer_quote = jdbc.sqlSelect(
                    "SELECT * FROM `ayaya footer quotes` WHERE `quote id` LIKE '" + id + "';", 5)
                    .getString("quote");
        } catch (SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the ayaya command! Aborting the read process...");
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
        return id;

    }

    /**
     * Retrieves the total amount of gifs for this command.
     *
     * @return gif amount
     */
    protected int getGifsAmount() {
        int amount = 0;
        SQLController jdbc = new SQLController();
        try
        {
            jdbc.open("jdbc:sqlite:data.db");
            amount = Integer.parseInt(
                    jdbc.sqlSelect("SELECT * FROM sqlite_sequence WHERE name LIKE '"+name+"';", 5)
                            .getString("seq")
            );
        }
        catch(SQLException e)
        {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the "+name+" command! Aborting the read process...");
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
        return amount;
    }

}