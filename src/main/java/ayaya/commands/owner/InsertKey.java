package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.BotData;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.*;
import java.util.Random;

/**
 * Class of the insertkey command.
 */
public class InsertKey extends Command {

    public InsertKey() {

        this.name = "insertkey";
        this.category = CommandCategories.OWNER.asCategory();
        this.isOwnerCommand = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};
        this.isDisabled = true;

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
        if (storeKey(key, duration)) {
            TextChannel consoleChannel = event.getJDA().getTextChannelById(BotData.getConsoleID());
            if (consoleChannel != null)
                consoleChannel.sendMessage("The new key is `" + key + "`.").queue();
            else {
                System.out.println("The new key is " + key + ".");
                event.reply("The key was sent to the server logs.");
            }
        } else
            event.replyError("There was a problem when trying to store the key. If this occurs again, check my server console.");

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
     * @return true if the key was stored, false if there was a problem
     */
    private boolean storeKey(String key, int duration) {

        boolean success = false;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword()
            );
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO patreon_keys(key,duration) VALUES(?, ?);"
            );
            statement.setString(1, key);
            statement.setInt(2, duration);
            statement.executeUpdate();
            success = true;
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
        return success;

    }

}