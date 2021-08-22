package ayaya.commands.funny;

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
 * Class of the kawaii command.
 */
public class Kawaii extends ayaya.commands.Command {

    public Kawaii() {

        this.name = "kawaii";
        this.help = "Gets you a random kawaii gif. <:AyaSmile:331115374739324930>";
        this.arguments = "{prefix}kawaii";
        this.aliases = new String[]{"cute"};
        this.category = FUNNY.asCategory();
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        EmbedBuilder embed = new EmbedBuilder();
        embed.setDescription("So kawaii. <:AyaSmile:331115374739324930>");
        try {
            embed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            embed.setColor(Color.decode("#155FA0"));
        }
        embed.setImage(getRandomGif());
        event.reply(embed.build());

    }

    /**
     * Fetches a random gif url from the database.
     *
     * @return the url
     */
    protected String getRandomGif() {
        int amount = getGifsAmount();
        Random rng = new Random();
        int id = rng.nextInt((amount & 0xff))+1;
        String url = null;
        SQLController jdbc = new SQLController();
        try
        {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            ResultSet rs = jdbc.sqlSelect("SELECT * FROM " + name + " WHERE gif_id=" + id + ";", 5);
            url = rs.next() ? rs.getString("link") : "";
        }
        catch(SQLException e)
        {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the " + name +
                            " command! Aborting the read process...");
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
        return url;
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