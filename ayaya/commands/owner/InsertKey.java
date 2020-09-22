package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.*;
import java.util.Objects;
import java.util.Random;

/**
 * Class of the insertkey command.
 */
public class InsertKey extends Command {

    public InsertKey() {

        this.name = "insertkey";
        this.category = CommandCategories.OWNER.asCategory();
        this.isOwnerCommand = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String input = event.getArgs();
        if (input.isEmpty()) {
            event.replyError("You didn't insert a duration for the key.");
            return;
        }
        int duration = Integer.parseInt(event.getArgs());
        String key = generateKey();
        storeKey(key, duration);
        Objects.requireNonNull(event.getJDA().getTextChannelById(getConsoleID()))
                .sendMessage("The new key is `" + key + "`.").queue();

    }

    /**
     * Fetches the id of the console channel of the bot from the database.
     *
     * @return id
     */
    private String getConsoleID() {

        String console = "";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:data.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(5);
            console = statement.executeQuery("SELECT * FROM settings WHERE option = 'console';")
                    .getString("value");
        } catch (SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the error handler! Unable to report the error to the discord console..."
            );
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
        return console;

    }

    /**
     * Generates a random key.
     *
     * @return key
     */
    private String generateKey() {
        StringBuilder key = new StringBuilder();
        Random rng = new Random();
        int chars = 20;
        int character;
        for (int i = 0; i < chars; i++) {
            character = rng.nextInt(86) + 40;
            key.append((char) character);
        }
        return key.toString();
    }

    /**
     * Stores the key in the database.
     *
     * @param key      the key to store
     * @param duration the duration of the key
     */
    private void storeKey(String key, int duration) {

        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:data.db");
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO patreon_keys(key,duration) VALUES(?, ?);"
            );
            statement.setString(1, key);
            statement.setInt(2, duration);
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

}