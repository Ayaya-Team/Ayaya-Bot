package ayaya.commands.information;

import ayaya.core.BotData;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class of the help command.
 */
public class Help extends ayaya.commands.Command {

    public Help() {

        this.name = "help";
        this.help = "Whenever you need I will give you the list of commands." +
                " If you also need I will show the help for a specific comnmand! <:AyaSmile:331115374739324930>";
        this.arguments = "{prefix}help <command>\n\nTo just get the help list run: {prefix}help";
        this.category = CommandCategories.INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

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
                event.reply("Excuse me, but you don't have permission to see any help for this command.");
                return;
            }
            helpEmbed.setTitle(cmdName.substring(0, 1).toUpperCase() + cmdName.substring(1))
                    .setDescription(cmd.getHelp())
                    .addField("Category", cmd.getCategory().getName(), true)
                    .addField("Cooldown", cmd.getCooldown() + " seconds", true)
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
            if (((ayaya.commands.Command) cmd).isDisabled())
                helpEmbed.addField("Disabled:", "Yes", true);
            else
                helpEmbed.addField("Disabled:", "No", true);
        } else {
            String description = String.format(
                    "This is the list with all my commands. Don't forget my prefix is %s." +
                            " For more help on a certain command, do \"%shelp <command name>\".", prefix, prefix
            );
            String serverInvite = BotData.getServerInvite();
            if (!serverInvite.isEmpty())
                description = description.concat(" For support, please join my [server](" + serverInvite + ").");
            if (CommandCategories.MUSIC.asListCategory().getCommands().isEmpty())
                description = description
                        .concat("\n\n**The music system is currently disabled.**" +
                                " My developer sometimes disables it when my current host is weak or doesn't have a good " +
                                "internet connection. For updates on the situation, please head to my support server.");
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

}