package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.BotData;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.internal.entities.UserById;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ayaya.core.enums.CommandCategories.OWNER;

/**
 * Class of the blacklist command.
 */
public class Blacklist extends Command {

    private Paginator.Builder pbuilder;

    public Blacklist() {

        this.name = "blacklist";
        this.category = OWNER.asCategory();
        this.arguments = "[pagenum]";
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE};
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
        this.isPaginated = true;
        this.isDisabled = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        int page = 1;
        if (!event.getArgs().isEmpty()) {
            try {
                page = Integer.parseInt(event.getArgs());
            } catch (IllegalStateException | NumberFormatException e) {
                event.replyError("That's not a valid number.");
                return;
            }
        }
        final int listPage = page;
        List<String[]> blacklist = getBlacklist();
        if (blacklist.size() == 0) {
            event.reply("No one is blacklisted right now.");
            return;
        }
        for (int i = 0; i < blacklist.size(); i++) {
            final int index = i;
            event.getJDA().retrieveUserById(blacklist.get(i)[1]).queue(u -> {
                if (index == blacklist.size() - 1) {
                    printList(blacklist, event, listPage);
                }
            });
        }

    }

    /**
     * Fetches the blacklist.
     *
     * @return list
     */
    private List<String[]> getBlacklist() {
        List<String[]> blacklist = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword()
            );
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);
            ResultSet result = statement
                    .executeQuery("SELECT * FROM blacklist;");
            while (result.next()) {
                blacklist.add(new String[]{
                        result.getString(2),
                        result.getString(3)
                });
            }
        } catch (SQLException e) {
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
        return blacklist;

    }

    /**
     * Prints the list on the channel of the event.
     *
     * @param blacklist the blacklist
     * @param event     the event of this command
     * @param page      the page of the list to print
     */
    private void printList(List<String[]> blacklist, CommandEvent event, int page) {

        pbuilder.clearItems();
        blacklist.stream().map(
                array -> array[1] + " | " + Objects.requireNonNullElse(event.getJDA().getUserById(array[1]), new UserById(Long.parseLong(array[1]))).getAsTag() +
                        " | " + array[2] + "\n"
        )
                .forEach(pbuilder::addItems);
        Paginator p = pbuilder
                .setColor(event.isFromType(ChannelType.TEXT) ? event.getSelfMember().getColor() : Color.decode("#155FA0"))
                .setUsers(event.getAuthor())
                .build();
        p.paginate(event.getChannel(), page);

    }

}