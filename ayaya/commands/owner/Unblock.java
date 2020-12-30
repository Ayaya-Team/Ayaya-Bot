package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;

import java.sql.*;

/**
 * Class of the unblock command.
 */
public class Unblock extends Command {

    public Unblock() {

        this.name = "unblock";
        this.category = CommandCategories.OWNER.asCategory();
        this.isOwnerCommand = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String id = event.getArgs();
        if (id.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't tell me who to unblock.");
            return;
        }
        removeFromBlacklist(event, id);

    }

    /**
     * Attempts to remove a user from the blacklist.
     *
     * @param event the event that triggered this command
     * @param id    the id of the user to remove
     */
    private void removeFromBlacklist(CommandEvent event, String id) {

        User u = event.getJDA().getUserById(id);
        if (u == null) {
            if (event.getChannelType() == ChannelType.TEXT && event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                event.reply("That user does not exist.");
            return;
        }
        if (!isBlocked(id)) {
            if (event.getChannelType() == ChannelType.TEXT && event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                event.reply("That user isn't blocked.");
            return;
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:data.db");
            PreparedStatement statement = connection
                    .prepareStatement("DELETE FROM blacklist WHERE user_id = ?;");
            statement.setString(1, id);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            if (event.getChannelType() == ChannelType.TEXT && event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                event.replyError("There was a problem while trying to unblock this user. If the problem persists, check my server logs.");
            else
                System.err.println("There was a problem while trying to unblock this user. If the problem persists, check my server logs.");
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

    }

    /**
     * Checks if a certain user, given the id, is blocked.
     *
     * @param id the user id
     * @return true if the user is blocked, false if not
     */
    private boolean isBlocked(String id) {

        boolean blocked;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:data.db");
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