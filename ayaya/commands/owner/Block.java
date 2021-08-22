package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.BotData;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class of the block command.
 */
public class Block extends Command {

    public Block() {

        this.name = "block";
        this.category = CommandCategories.OWNER.asCategory();
        this.isOwnerCommand = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String id = event.getArgs();
        if (id.isEmpty()) {
            if (event.getChannelType() == ChannelType.TEXT && event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                event.reply("<:AyaWhat:362990028915474432> You didn't tell me who to block.");
            return;
        } else if (event.getClient().getOwnerId().equals(id)) {
            if (event.getChannelType() == ChannelType.TEXT && event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                event.reply("Owner and Co-Owners cannot be blacklisted.");
            return;
        }
        for (String s: event.getClient().getCoOwnerIds()) {
            if (s.equals(id)) {
                if (event.getChannelType() == ChannelType.TEXT && event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                    event.reply("Owner and Co-Owners cannot be blacklisted.");
                return;
            }
        }
        addToBlacklist(event, id);

    }

    /**
     * Adds an user to the blacklist.
     *
     * @param event the event that triggered this command
     * @param id    the id of the user
     */
    private void addToBlacklist(CommandEvent event, String id) {

        event.getJDA().retrieveUserById(id).queue(u -> {
            if (u == null) {
                event.reply("That user does not exist.");
                return;
            }
            if (isBlocked(id)) {
                event.reply("That user is already blocked.");
                return;
            }
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(
                        BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword()
                );
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO blacklist(user_id,block_date) VALUES(?, ?);"
                );
                statement.setString(1, id);
                OffsetDateTime date = OffsetDateTime.now();
                statement.setString(2, date.format(DateTimeFormatter.ofPattern(DATE_PATTERN)));
                statement.executeUpdate();
            } catch (SQLException e) {
                if (event.getGuild() == null || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                    event.replyError("There was a problem while trying to block this user. If the problem persists, check my server logs.");
                else
                    System.err.println("There was a problem while trying to block this user. If the problem persists, check my server logs.");
                System.err.println(e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null)
                        connection.close();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Checks if a user, given it's id, is blocked.
     *
     * @param id the id of the user
     * @return true if the user is blocked, false if not
     */
    private boolean isBlocked(String id) {
        boolean blocked;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword()
            );
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(5);
            ResultSet result = statement
                    .executeQuery("SELECT * FROM blacklist WHERE user_id = '" + id + "';");
            blocked = result.next();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            blocked = false;
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return blocked;
    }

}