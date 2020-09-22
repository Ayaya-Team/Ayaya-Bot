package ayaya.commands.admin;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class of the news command.
 */
public class News extends Command {

    public News() {

        this.name = "news";
        this.help = "Informs if there is a news channel set for the server" +
                " and can be used to create or delete the news channel of the server.";
        this.arguments = "{prefix}news create/delete" +
                " | to check if there is a news channel just run the command without any additional keyword.";
        this.isGuildOnly = true;
        this.category = CommandCategories.ADMINISTRATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MANAGE_CHANNEL};
        this.userPerms = new Permission[]{Permission.ADMINISTRATOR};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String[] args = event.getArgs().split(" ");
        String prefix = event.getClient().getPrefix();

        if (event.getArgs().isEmpty())
            switch (checkNewsChannel(event.getGuild())) {
                case 0:
                    event.reply("This server doesn't have any news channel at this moment." +
                            " Would you like to create one?" +
                            " If yes do `" + prefix + "news create`.\\n" +
                            "Note: this will create a channel called `ayaya_news`." +
                            " You can perform this action manually by creating the `ayaya_news` channel yourself." +
                            " If you wish to delete it you can either do it manually or by typing" +
                            " `" + prefix + "news delete`.");
                    break;
                case 1:
                    event.reply("This server already has a news channel. If you want to remove it type `"
                            + prefix + "news delete`.");
                    break;
                default:
                    event.replyError(
                            "This server has 2 or more channels with the name `ayaya_news`." +
                                    " As this announcements system works with channels which have this name," +
                                    " for safety reasons, this command won't work until there are only 1 or 0 channels" +
                                    " with the mentioned name in this server.");
            }
        else
            switch (args[0].toLowerCase()) {
                case "create":
                    createNewsChannel(event);
                    break;
                case "delete":
                    deleteNewsChannel(event);
                    break;
                default:
                    event.replyError("You were trying to perform an unknown action for this command.");
            }

    }

    /**
     * Fetches the amount of news channels in the given guild.
     *
     * @param guild the guild to do this check
     * @return amount of channels
     */
    private int checkNewsChannel(Guild guild) {

        int channels = 0;
        for (TextChannel c : guild.getTextChannels()) {
            if (c.getName().equals("ayaya_news")) channels++;
        }
        return channels;

    }

    /**
     * Creates a news channel.
     *
     * @param event the event that triggered this command
     */
    private void createNewsChannel(CommandEvent event) {

        Collection<Permission> bot_permissions = new ArrayList<>(3);
        bot_permissions.add(Permission.MESSAGE_READ);
        bot_permissions.add(Permission.MESSAGE_WRITE);
        bot_permissions.add(Permission.MANAGE_CHANNEL);
        Collection<Permission> user_permissions = new ArrayList<>(1);
        user_permissions.add(Permission.MESSAGE_READ);
        if (checkNewsChannel(event.getGuild()) > 0) {
            event.replyError("There is already a news channel in this server.");
            return;
        }
        event.getGuild().createTextChannel("ayaya_news")
                .addPermissionOverride(
                        event.getSelfMember().getRoles().get(0), bot_permissions, null)
                .addPermissionOverride(
                        event.getGuild().getPublicRole(), user_permissions, null)
                .reason("Ayaya news channel requested by " + event.getMember().getEffectiveName() + ".")
                .queue();
        event.replySuccess("News channel created with success. You can chance the permissions of it if you need.");

    }

    /**
     * Deletes the news channel.
     *
     * @param event the event that triggered this command
     */
    private void deleteNewsChannel(CommandEvent event) {

        TextChannel channel = getNewsChannel(event.getGuild());
        if (channel == null) {
            event.replyError("There isn't any news channel in this server.");
            return;
        }
        if (!event.getSelfMember().hasPermission(channel, Permission.MANAGE_CHANNEL)) {
            event.replyError("My permission to manage this channel was removed or overrided." +
                    " Pease give me permission to manage this channel so I can delete it or delete the channel manually.");
            return;
        }
        channel.delete()
                .reason("Ayaya news channel deletion requested by " + event.getMember().getEffectiveName() + ".")
                .queue();
        event.replySuccess("News channel deleted with success.");

    }

    /**
     * Fetches the news channel of a given guild.
     *
     * @param guild the guild
     * @return the news channel
     */
    private TextChannel getNewsChannel(Guild guild) {

        for (TextChannel c : guild.getTextChannels()) {
            if (c.getName().equals("ayaya_news")) return c;
        }
        return null;

    }

}