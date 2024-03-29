package ayaya.commands.action;

import ayaya.commands.GuildDMSCommand;
import ayaya.core.BotData;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;

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
        Member author = event.getMember();
        Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(event.getArgs());
        Matcher idFinder;
        EmbedBuilder embed = new EmbedBuilder();
        if (mentionFinder.find()) {
            idFinder = ANY_ID.matcher(mentionFinder.group());
            idFinder.find();
            guild.retrieveMemberById(idFinder.group(), true).queue(mentioned -> {
                String url;
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
                prepareEmbedAndSend(embed, event.getTextChannel());
            }, t -> event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention in this server."));
        } else {
            embed.setDescription(String.format(SELF_TSUNDERE.getQuote(),
                    author.getEffectiveName()));
            String url = getRandomGif();
            if (url.equals(NULL)) {
                event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                return;
            }
            embed.setImage(url);
            embed.setFooter(SELF_TSUNDERE.getFooter(), null);
            prepareEmbedAndSend(embed, event.getTextChannel());
        }

    }

    /**
     * Finishes the embed and sends it.
     *
     * @param embed   the embed to finish
     * @param channel the channel to send the embed to
     */
    private void prepareEmbedAndSend(EmbedBuilder embed, TextChannel channel) {
        embed.setColor(Color.decode("#155FA0"));
        try {
            embed.setColor(channel.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            embed.setColor(Color.decode("#155FA0"));
        }
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    protected void executeInDMS(CommandEvent event) {

        User author = event.getAuthor();
        Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(event.getArgs());
        Matcher idFinder;
        EmbedBuilder embed = new EmbedBuilder();
        if (mentionFinder.find()) {
            idFinder = ANY_ID.matcher(mentionFinder.group());
            idFinder.find();
            event.getJDA().retrieveUserById(idFinder.group(), true).queue(mentioned -> {
                String url;
                if (mentioned == null)
                    event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention here.");
                else if (mentioned == event.getSelfUser()) {
                    embed.setDescription(AYAYA_TSUNDERE.getQuote())
                            .setImage(
                                    "https://cdn.discordapp.com/attachments/332853782247374848/351137455761522691/"
                                            + "tumblr_nn2ki3jKnu1s0ifhgo2_500.gif"
                            );
                } else if (mentioned == author) {
                    embed.setDescription(String.format(SELF_TSUNDERE.getQuote(),
                            author.getName()));
                    url = getRandomGif();
                    if (url.equals(NULL)) {
                        event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                        return;
                    }
                    embed.setImage(url)
                            .setFooter(SELF_TSUNDERE.getFooter(), null);
                } else {
                    embed.setDescription(String.format(NORMAL_TSUNDERE.getQuote(),
                            author.getName(), mentioned.getName()));
                    url = getRandomGif();
                    if (url.equals(NULL)) {
                        event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                        return;
                    }
                    embed.setImage(url)
                            .setFooter(NORMAL_TSUNDERE.getFooter(), null)
                            .setColor(Color.decode("#155FA0"));
                    event.reply(embed.build());
                }
            });
        } else {
            embed.setDescription(String.format(SELF_TSUNDERE.getQuote(),
                    author.getName()));
            String url = getRandomGif();
            if (url.equals(NULL)) {
                event.reply("There was a problem while connecting with the database. If this persists then try again later.");
                return;
            }
            embed.setImage(url)
                    .setFooter(SELF_TSUNDERE.getFooter(), null)
                    .setColor(Color.decode("#155FA0"));
            event.reply(embed.build());
        }

    }

    private synchronized String getRandomGif() {
        int amount = getGifsAmount();
        int id = ThreadLocalRandom.current().nextInt((amount & 0xff))+1;
        String url = null;
        SQLController jdbc = new SQLController();
        try
        {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            Serializable[] o = new Serializable[]{id};
            ResultSet rs = jdbc.sqlSelect("SELECT * FROM " + this.name + " WHERE gif_id = ?;", o, 5);
            url = rs.next() ? rs.getString("link") : "";
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

    private synchronized int getGifsAmount() {
        int amount = 0;
        SQLController jdbc = new SQLController();
        try
        {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            ResultSet rs = jdbc.sqlSelect("SELECT last_value FROM " + this.name + "_gif_id_seq;", 5);
            if (rs.next())
                amount = rs.getInt(1);
        }
        catch(SQLException e)
        {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the " + this.name
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