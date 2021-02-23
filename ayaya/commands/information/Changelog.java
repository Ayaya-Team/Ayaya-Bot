package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.utils.SQLController;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class of the changelog command.
 */
public class Changelog extends Command {

    private static final String APPEND = "`, ";

    public Changelog() {

        this.name = "changelog";
        this.help = "A command to check what changed in the last update! " +
                "This will be updated when new versions of me are deployed so don't worry, you shall always get the latest info! ^^";
        this.arguments = "{prefix}changelog <version>\n\n" +
                "To check any older versions write `{prefix}changelog list`.\n" +
                "Run the command without any arguments if you just want to check the changes of the latest minor updates.";
        this.category = CommandCategories.INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String option = event.getArgs();
        EmbedBuilder changelog_embed = new EmbedBuilder()
                .setAuthor("Changelog", null, event.getSelfUser().getAvatarUrl())
                .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getAvatarUrl());
        if (!option.isEmpty()) {
            if (option.toLowerCase().equals("list")) {
                changelog_embed
                        .setDescription(getVersionList());
            } else {
                changelog_embed.setDescription(getChangelog(option));
            }
        } else {
            changelog_embed.setDescription(getChangelog());
        }
        try {
            changelog_embed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            changelog_embed.setColor(Color.decode("#155FA0"));
        }
        event.reply(changelog_embed.build());

    }

    /**
     * Fetches the changelog from the database.
     *
     * @param version the changelog version
     * @return changelog string
     */
    private String getChangelog(String version) {

        SQLController jdbc = new SQLController();
        String changelog = null;

        try {

            jdbc.open("jdbc:sqlite:data.db");
            changelog = jdbc.sqlSelect("SELECT * FROM changelogs WHERE version LIKE '" + version +"';", 10)
                    .getString("text");

        } catch (SQLException e) {

            System.out.println("A problem occurred while trying to fetch the changelog! Aborting the process...");
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

        if (changelog == null)
            changelog = getChangelog();

        return changelog;

    }

    /**
     * Fetches the latest changelog.
     *
     * @return changelog string
     */
    private String getChangelog() {

        SQLController jdbc = new SQLController();
        String changelog = null;

        try {

            jdbc.open("jdbc:sqlite:data.db");
            String version = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'version';", 5)
                    .getString("value");
            changelog = jdbc.sqlSelect("SELECT * FROM changelogs WHERE version LIKE '" + version +"';", 5)
                    .getString("text");

        } catch (SQLException e) {

            System.out.println("A problem occurred while trying to fetch the changelog! Aborting the process...");
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

        return changelog;

    }

    private String getVersionList() {

        SQLController jdbc = new SQLController();
        StringBuilder list = new StringBuilder();

        try {

            jdbc.open("jdbc:sqlite:data.db");
            ResultSet result = jdbc.sqlSelect("SELECT version FROM changelogs;", 5);
            list.append("Versions: ");
            while (result.next()) {
                list.append("`").append(result.getString("version")).append(APPEND);
            }

        } catch (SQLException e) {

            System.out.println("A problem occurred while trying to fetch the changelog! Aborting the process...");
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

        list.replace(list.length() - APPEND.length(), list.length() - 1, "`");
        return list.toString();

    }

}