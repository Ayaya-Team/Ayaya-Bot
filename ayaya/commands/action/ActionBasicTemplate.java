package ayaya.commands.action;

import ayaya.commands.GuildDMSCommand;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.sql.SQLException;
import java.util.Random;

import static ayaya.core.enums.CommandCategories.ACTION;

/**
 * Base template for basic action commands and other templates.
 */
public class ActionBasicTemplate extends GuildDMSCommand {

    protected static final String NULL = "null";

    protected String description, footer;

    public ActionBasicTemplate(String name, String help, String arguments, String[] aliases, String description,
                               String footer) {

        this.name = name;
        this.help = help;
        this.arguments = arguments;
        this.aliases = aliases;
        this.category = ACTION.asCategory();
        this.isGuildOnly = false;
        this.description = description;
        this.footer = footer;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void executeInGuild(CommandEvent event) {

        event.getGuild().retrieveMember(event.getAuthor()).queue(author -> {
            EmbedBuilder embed = new EmbedBuilder();
            if (description != null && !description.isEmpty())
                embed.setDescription(String.format(description, author.getEffectiveName()));
            if (footer != null && !footer.isEmpty())
                embed.setFooter(String.format(footer, author.getEffectiveName()), null);
            try {
                embed.setColor(event.getGuild().getSelfMember().getColor());
            } catch (IllegalStateException | NullPointerException e) {
                embed.setColor(Color.decode("#155FA0"));
            }
            String url = getRandomGif();
            if (url.equals(NULL)) {
                event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                return;
            }
            embed.setImage(url);
            event.reply(embed.build());
        });

    }

    @Override
    protected void executeInDMS(CommandEvent event) {

        User author = event.getAuthor();
        EmbedBuilder embed = new EmbedBuilder();
        if (description != null && !description.isEmpty())
            embed.setDescription(String.format(description, author.getName()));
        if (footer != null && !footer.isEmpty())
            embed.setFooter(String.format(footer, author.getName()), null);
        embed.setColor(Color.decode("#155FA0"));
        String url = getRandomGif();
        if (url.equals(NULL)) {
            event.reply("There was a problem while connecting with the database. If this persists then try again later.");
            return;
        }
        embed.setImage(url);
        event.reply(embed.build());

    }

    /**
     * Retrieves a random gif link from the database.
     *
     * @return url
     */
    protected String getRandomGif() {
        int amount = getGifsAmount();
        Random rng = new Random();
        int id = rng.nextInt((amount & 0xff))+1;
        String url = NULL;
        SQLController jdbc = new SQLController();
        try
        {
            jdbc.open("jdbc:sqlite:data.db");
            url = jdbc.sqlSelect("SELECT * FROM "+name+" WHERE `gif id` LIKE '"+id+"';", 5)
                    .getString("link");
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
        return url;
    }

    /**
     * Retrieves the total amount of gifs for the commands triggered.
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