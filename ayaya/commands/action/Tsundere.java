package ayaya.commands.action;

import ayaya.commands.GuildDMSCommand;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import static ayaya.core.enums.ActionQuotes.*;
import static ayaya.core.enums.CommandCategories.ACTION;

/**
 * Class of the tsundere command.
 */
public class Tsundere extends GuildDMSCommand {

    private static final String NULL = "null";

    public Tsundere() {

        this.name = "tsundere";
        this.help = "Want to point out the tsundere? Then go ahead!";
        this.arguments = "{prefix}tsundere <@user>";
        this.aliases = new String[]{"tsun"};
        this.category = ACTION.asCategory();
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void executeInGuild(CommandEvent event) {

        Guild guild = event.getGuild();
        Member author = event.getMember(), mentioned;
        List<IMentionable> mentions = event.getMessage().getMentions(Message.MentionType.USER);
        EmbedBuilder embed = new EmbedBuilder();
        String url;
        if (!mentions.isEmpty()) {
            mentioned = guild.retrieveMemberById(mentions.get(0).getId()).complete();
            if (mentioned == null)
                event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention in this server.");
            else if (mentioned == event.getSelfMember()) {
                embed.setDescription(AYAYA_TSUNDERE.getQuote());
                embed.setImage("https://cdn.discordapp.com/attachments/332853782247374848/351137455761522691/" +
                        "tumblr_nn2ki3jKnu1s0ifhgo2_500.gif");
            } else if (mentioned == author) {
                embed.setDescription(String.format(SELF_TSUNDERE.getQuote(),
                        author.getEffectiveName()));
                url = getRandomGif();
                if (url.equals(NULL)) {
                    event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                    return;
                }
                embed.setImage(url);
                embed.setFooter(SELF_TSUNDERE.getFooter(), null);
            } else {
                embed.setDescription(String.format(NORMAL_TSUNDERE.getQuote(),
                        author.getEffectiveName(), mentioned.getEffectiveName()));
                url = getRandomGif();
                if (url.equals(NULL)) {
                    event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                    return;
                }
                embed.setImage(url);
                embed.setFooter(NORMAL_TSUNDERE.getFooter(), null);
            }
        } else {
            embed.setDescription(String.format(SELF_TSUNDERE.getQuote(),
                    author.getEffectiveName()));
            url = getRandomGif();
            if (url.equals(NULL)) {
                event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                return;
            }
            embed.setImage(url);
            embed.setFooter(SELF_TSUNDERE.getFooter(), null);
        }
        embed.setColor(Color.decode("#155FA0"));
        try {
            embed.setColor(guild.getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            embed.setColor(Color.decode("#155FA0"));
        }
        event.reply(embed.build());

    }

    @Override
    protected void executeInDMS(CommandEvent event) {

        User author = event.getAuthor(), mentioned;
        List<User> users_list = event.getMessage().getMentionedUsers();
        EmbedBuilder embed = new EmbedBuilder();
        String url;
        if (users_list.size() > 0) {
            mentioned = users_list.get(0);
            if (mentioned == event.getSelfUser()) {
                embed.setDescription(AYAYA_TSUNDERE.getQuote());
                embed.setImage("https://cdn.discordapp.com/attachments/332853782247374848/351137455761522691/" +
                        "tumblr_nn2ki3jKnu1s0ifhgo2_500.gif");
            } else if (mentioned == author) {
                embed.setDescription(String.format(SELF_TSUNDERE.getQuote(),
                        author.getName()));
                url = getRandomGif();
                if (url.equals(NULL)) {
                    event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                    return;
                }
                embed.setImage(url);
                embed.setFooter(SELF_TSUNDERE.getFooter(), null);
            } else {
                embed.setDescription(String.format(NORMAL_TSUNDERE.getQuote(),
                        author.getName(), mentioned.getName()));
                url = getRandomGif();
                if (url.equals(NULL)) {
                    event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                    return;
                }
                embed.setImage(url);
                embed.setFooter(NORMAL_TSUNDERE.getFooter(), null);
            }
        } else {
            embed.setDescription(String.format(SELF_TSUNDERE.getQuote(),
                    author.getName()));
            url = getRandomGif();
            if (url.equals(NULL)) {
                event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                return;
            }
            embed.setImage(url);
            embed.setFooter(SELF_TSUNDERE.getFooter(), null);
        }
        embed.setColor(Color.decode("#155FA0"));
        event.reply(embed.build());

    }

    private String getRandomGif() {
        int amount = getGifsAmount();
        Random rng = new Random();
        int id = rng.nextInt((amount & 0xff))+1;
        String url = null;
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
                    "A problem occurred while trying to get necessary information for the tsundere command! Aborting the read process...");
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

    private int getGifsAmount() {
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