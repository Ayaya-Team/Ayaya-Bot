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
 * Class of the about command.
 */
public class About extends Command {

    private String version;
    private String quote;
    private String inviteLink;
    private String discordLink;
    private String patreonLink;
    private String dblLink;
    private String dboatsLink;
    private String dbotLink;

    public About() {

        this.name = "about";
        this.help = "A command to check info about me.";
        this.arguments = "{prefix}about";
        this.category = INFORMATION.asCategory();
        this.aliases = new String[]{"info"};
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        quote = "";
        inviteLink = "";
        discordLink = "";
        version = "";

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        fetchData();
        String java_version = System.getProperty("java.version");
        EmbedBuilder about_embed = new EmbedBuilder();
        about_embed.setTitle("About " + event.getSelfUser().getName())
                .setDescription(quote)
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                .addField("Version", version, false)
                .addField("Invite", "[click here](" + inviteLink + ")", true)
                .addField("Support Server Invite", "[click here](" + discordLink + ")",
                        true)
                .addField("Donate", "[click here](" + patreonLink + ")", true)
                .addField("Upvote", "[Top gg](" + dblLink + ")\n"
                        + "[Discord Boats](" + dboatsLink + ")\n"
                        + "[Discord Bot List](" + dbotLink + ")", true)
                .setFooter("Requested by " + event.getAuthor().getName(),
                        event.getAuthor().getAvatarUrl());
        try {
            about_embed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            about_embed.setColor(Color.decode("#155FA0"));
        }
        event.reply(about_embed.build());

    }

    /**
     * Fetch the required data to process the command.
     */
    private void fetchData() {

        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            version = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'version';", 5)
                    .getString("value");
            quote = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'about quote';", 5)
                    .getString("value");
            inviteLink = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'invite';", 5)
                    .getString("value");
            discordLink = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'support';", 5)
                    .getString("value");
            patreonLink = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'donate';", 5)
                    .getString("value");
            dblLink = jdbc.sqlSelect("SELECT * FROM botlists WHERE `list` LIKE 'dbl';", 5)
                    .getString("upvote");
            dboatsLink = jdbc.sqlSelect("SELECT * FROM botlists WHERE `list` LIKE 'dboats';", 5)
                    .getString("upvote");
            dbotLink = jdbc.sqlSelect("SELECT * FROM botlists WHERE `list` LIKE 'dbot';", 5)
                    .getString("upvote");
        } catch (SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the " + this.name
                            + " command! Aborting the read process...");
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