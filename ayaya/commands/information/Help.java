package ayaya.commands.information;

import ayaya.commands.ListCategory;
import ayaya.core.utils.SQLController;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class of the help command.
 */
public class Help extends ayaya.commands.Command {

    private String discord_link;
    private String patreon_link;

    public Help() {

        this.name = "help";
        this.help = "Whenever you need I will give you the list of commands." +
                " If you also need I will show the help for a specific comnmand! <:AyaSmile:331115374739324930>";
        this.arguments = "{prefix}help <command>\n\nTo just get the help list run: {prefix}help";
        this.category = CommandCategories.INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        discord_link = "";
        patreon_link = "";

    }

    @Override
    protected void executeInstructions(CommandEvent event) {
        String cmdName = event.getArgs();
        String prefix = event.getClient().getPrefix();
        EmbedBuilder helpEmbed = new EmbedBuilder();
        if (!cmdName.isEmpty()) {
            Command cmd = null;
            for (Command command : event.getClient().getCommands()) {
                if (command.getName().equals(cmdName.toLowerCase()) && !command.isHidden()) {
                    cmd = command;
                    cmdName = command.getName();
                }
                for (String alias : command.getAliases())
                    if (alias.equals(cmdName) && !command.isHidden()) {
                        cmd = command;
                        cmdName = command.getName();
                    }
            }
            if (cmd == null) {
                event.replyError(
                        "I can't find any command or alias with that name! Make sure to check the commands list with `"
                                + prefix +
                                "help`.\nIf you want to suggest a new command you can do it at my support server! You can find the link with `"
                                + prefix + "support`.");
                return;
            }
            if (cmd.isOwnerCommand() && !event.isOwner()) {
                event.reply("Excuse me, but you don't even have permission to use this command.");
                return;
            }
            helpEmbed.setTitle(cmdName.substring(0, 1).toUpperCase() + cmdName.substring(1))
                    .setDescription(cmd.getHelp())
                    .addField("Category", cmd.getCategory().getName(), false)
                    .addField("Way to use:",
                            cmd.getArguments().replaceAll(Pattern.quote("{prefix}"), prefix)
                                    + "\n**Please do not type the <> in any of the commands.**", false)
                    .setFooter("Requested by " + event.getMember().getEffectiveName(), event.getAuthor().getAvatarUrl());
            String[] aliases = cmd.getAliases();
            Permission[] perms = cmd.getUserPermissions();
            if (aliases.length > 0) {
                StringBuilder aliasesStr = new StringBuilder();
                for (String alias : aliases)
                    aliasesStr.append(alias).append("\n");
                helpEmbed.addField("Aliases:", aliasesStr.toString(), false);
            }
            if (perms.length > 0) {
                StringBuilder permissionsStr = new StringBuilder();
                for (Permission perm : perms)
                    permissionsStr.append(perm.getName()).append("\n");
                helpEmbed.addField("Permissions required:", permissionsStr.toString(), false);
            }
            if (((ayaya.commands.Command) cmd).isPremium())
                helpEmbed.addField("Premium:", "Yes", false);
            else
                helpEmbed.addField("Premium:", "No", false);
        } else {
            String description = "This is the list with all my commands. Don't forget that my prefix is `" +
                    event.getClient().getPrefix() + "` and that all commands have a 2 seconds cooldown.";
            getData();
            if (!discord_link.isEmpty())
                description = description.concat(" For support, please join my [server](" + discord_link + ").");
            if (!patreon_link.isEmpty())
                description =
                        description
                                .concat("\nJust a friendly reminder, my developer needs your help. If you could donate on my [patreon page](" + patreon_link + ") that would be very appreciated.");
            helpEmbed.setAuthor("Command's List", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .setDescription(description)
                    .setFooter("Requested by " + event.getAuthor().getName() + " | Total commands: "
                            + String.valueOf(event.getClient().getCommands().size()), event.getAuthor().getAvatarUrl());
            StringBuilder s;
            String after_command = ", ";
            List<String> l;
            for (CommandCategories c : CommandCategories.values()) {
                if (!c.asCategory().getName().equals(CommandCategories.OWNER_CATEGORY) || isOwner(event)) {
                    s = new StringBuilder();
                    l = c.asListCategory().getCommands();
                    for (String n : l)
                        s.append("`").append(n).append("`").append(after_command);
                    if (s.length() > 0)
                        helpEmbed.addField(
                                c.asCategory().getName(), s.substring(0, s.length() - after_command.length()), false
                        );
                }
            }
        }
        try {
            helpEmbed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            helpEmbed.setColor(Color.decode("#155FA0"));
        }
        event.reply(helpEmbed.build());
    }

    /**
     * Fetches the list category by name.
     *
     * @param name list category's name
     * @return list category
     */
    private ListCategory getListCategory(String name) {
        for (CommandCategories c : CommandCategories.values())
            if (c.asCategory().getName().equals(name))
                return c.asListCategory();
        return null;
    }

    /**
     * Fetches the required data from the database to execute this command.
     */
    private void getData() {

        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            discord_link = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'support';", 5)
                    .getString("value");
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