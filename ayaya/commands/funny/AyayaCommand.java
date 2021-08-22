package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.BotData;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the ayaya command.
 */
public class AyayaCommand extends Command {

    protected static final String NULL = "null";

    public AyayaCommand() {

        this.name = "ayaya";
        this.help = "This gives you a random gif of me. Wait wha?! N-No don't use it p-please!!";
        this.arguments = "{prefix}ayaya";
        this.category = FUNNY.asCategory();
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        Random rand = new Random();
        int id = rand.nextInt((getGifsAmount() & 0xff)) + 1;
        String[] data = getData(id);
        String url = data[0], quote = data[1], footer_quote = data[2];
        if (id == 19)
            event.reply(quote);
        else {
            EmbedBuilder ayaya_embed = new EmbedBuilder().setImage(url);
            if (quote != null && !quote.isEmpty() && !quote.toLowerCase().equals(NULL))
                ayaya_embed.setDescription(quote);
            if (footer_quote != null && !footer_quote.isEmpty() && !footer_quote.toLowerCase().equals(NULL))
                ayaya_embed.setFooter(footer_quote, null);
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
    private String[] getData(int id) {

        String[] result = new String[3];
        SQLController jdbc = new SQLController();
        try {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            result[0] = jdbc.sqlSelectNext("SELECT * FROM ayaya WHERE gif_id=" + id + ";", 5)
                    .getString("link");
            result[1] = jdbc.sqlSelectNext("SELECT * FROM ayaya_quotes WHERE quote_id=" + id + ";", 5)
                    .getString("quote");
            result[2] = jdbc.sqlSelectNext(
                    "SELECT * FROM ayaya_footer_quotes WHERE quote_id=" + id + ";", 5)
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
        return result;

    }

    /**
     * Retrieves the total amount of gifs for this command.
     *
     * @return gif amount
     */
    private int getGifsAmount() {
        int amount = 0;
        SQLController jdbc = new SQLController();
        try
        {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            ResultSet rs = jdbc.sqlSelect("SELECT last_value FROM " + name + "_gif_id_seq;", 5);
            if (rs.next())
                amount = rs.getInt(1);
        }
        catch(SQLException e)
        {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the " + name
                            + " command! Aborting the read process...");
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