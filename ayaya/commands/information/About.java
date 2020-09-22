package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the about command.
 */
public class About extends Command {

    private String version;
    private String quote;
    private String invite_link;
    private String discord_link;
    private String upvote_link;
    private String patreon_link;

    public About() {

        this.name = "about";
        this.help = "A command to check info about me.";
        this.arguments = "{prefix}about";
        this.category = INFORMATION.asCategory();
        this.aliases = new String[]{"info"};
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        quote = "";
        invite_link = "";
        discord_link = "";
        version = "";

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        fetchData();
        String java_version = System.getProperty("java.version");
        LocalDateTime uptime = getUptime(event);
        EmbedBuilder about_embed = new EmbedBuilder();
        about_embed.setTitle("About " + event.getSelfUser().getName())
                .setDescription(quote)
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                .addField("Version", version, false)
                .addField("Invite", "[click here](" + invite_link + ")", true)
                .addField("Discord Invite", "[click here](" + discord_link + ")", true)
                .addField("Upvote", "[click here](" + upvote_link + ")", true)
                .addField("Donate", "[click here](" + patreon_link + ")", true)
                .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getAvatarUrl());
        try {
            about_embed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            about_embed.setColor(Color.decode("#155FA0"));
        }
        event.reply(about_embed.build());

    }

    /**
     * Fetches the total uptime of the bot.
     *
     * @param event the event tha triggered this command
     * @return uptime
     */
    private LocalDateTime getUptime(CommandEvent event) {
        LocalDateTime start_time = event.getClient().getStartTime().toLocalDateTime();
        LocalDateTime current_time = OffsetDateTime.now().toLocalDateTime();
        return current_time.minusDays(start_time.getDayOfYear() - 1).minusHours(start_time.getHour())
                .minusMinutes(start_time.getMinute()).minusSeconds(start_time.getSecond());
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
            invite_link = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'invite';", 5)
                    .getString("value");
            discord_link = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'support';", 5)
                    .getString("value");
            upvote_link = jdbc.sqlSelect("SELECT * FROM `botlists` WHERE `list` LIKE 'dbl';", 5)
                    .getString("upvote");
            patreon_link = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'donate';", 5)
                    .getString("value");
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