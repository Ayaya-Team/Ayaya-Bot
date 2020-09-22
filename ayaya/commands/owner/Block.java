package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.User;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class of the block command.
 */
public class Block extends Command {

    private static final String PATTERN = "d-MM-yyyy";

    public Block() {

        this.name = "block";
        this.category = CommandCategories.OWNER.asCategory();
        this.isOwnerCommand = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String id = event.getArgs();
        if (id.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't tell me who to block.");
            return;
        } else if (event.getClient().getOwnerId().equals(id)) {
            event.reply("Owner and Co-Owners cannot be blacklisted.");
            return;
        }
        for (String s: event.getClient().getCoOwnerIds()) {
            if (s.equals(id)) {
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

        User u = event.getJDA().getUserById(id);
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
            connection = DriverManager.getConnection("jdbc:sqlite:data.db");
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO blacklist(user_id,block_date) VALUES(?, ?, ?);"
            );
            statement.setString(1, id);
            OffsetDateTime date = OffsetDateTime.now();
            statement.setString(2, date.format(DateTimeFormatter.ofPattern(PATTERN)));
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("A problem occurred while trying to store the key. Aborting the process...");
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
     * Checks if a user, given it's id, is blocked.
     *
     * @param id the id of the user
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