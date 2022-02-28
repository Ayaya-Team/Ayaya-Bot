package ayaya.commands.action;

import ayaya.commands.GuildDMSCommand;
import ayaya.core.BotData;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import static ayaya.core.enums.ActionQuotes.NORMAL_DANCE;
import static ayaya.core.enums.CommandCategories.ACTION;

public class Dance extends GuildDMSCommand {

    private static final String NULL = "null";
    private static final int EASTEREGG = 50;
    private static final String VIDEO = "https://www.youtube.com/watch?v=oQTo6aSHWz8";

    private String description, footer;

    public Dance() {

        this.name = "dance";
        this.help = "So you want to dance.";
        this.arguments = "{prefix}dance";
        this.category = ACTION.asCategory();
        this.isGuildOnly = false;
        this.description = NORMAL_DANCE.getQuote();
        this.footer = NORMAL_DANCE.getFooter();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void executeInGuild(CommandEvent event) {

        if (eastereggDeploy(event.getChannel())) return;
        Member author = event.getMember();
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

    }

    @Override
    protected void executeInDMS(CommandEvent event) {

        if (eastereggDeploy(event.getChannel())) return;
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
    private synchronized String getRandomGif() {

        int amount = getGifsAmount();
        Random rng = new Random();
        int id = rng.nextInt((amount & 0xff))+1;
        String url = NULL;
        SQLController jdbc = new SQLController();
        try
        {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            Serializable[] o = new Serializable[]{id};
            ResultSet rs = jdbc.sqlSelect("SELECT * FROM " + name + " WHERE gif_id = ?;", o, 5);
            url = rs.next() ? rs.getString("link") : "";
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
        return url;

    }

    /**
     * Retrieves the total amount of gifs for the commands triggered.
     *
     * @return gif amount
     */
    private synchronized int getGifsAmount() {

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

    private synchronized boolean eastereggDeploy(MessageChannel channel) {

        int choice = new Random().nextInt(100);
        if (choice == EASTEREGG) {
            EmbedBuilder easteregg = new EmbedBuilder().setTitle("Easter Egg").setImage(VIDEO);
            channel.sendMessageEmbeds(easteregg.build()).queue();
            return true;
        }
        return false;

    }

}