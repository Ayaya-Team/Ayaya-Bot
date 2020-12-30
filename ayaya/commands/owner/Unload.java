package ayaya.commands.owner;

import ayaya.commands.ListCategory;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;

import static ayaya.core.enums.CommandCategories.OWNER;

/**
 * Class of the unload command.
 */
public class Unload extends ayaya.commands.Command {

    public Unload() {
        this.name = "unload";
        this.category = OWNER.asCategory();
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String[] args = event.getArgs().split(" ");
        if (event.getArgs().isEmpty()) {
            if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                event.reply("<:AyaWhat:362990028915474432> You didn't specify any command for me to unload.");
            return;
        }
        String commandName = args[0].toLowerCase();
        if (commandName.equals("load") || commandName.equals("unload")) {
            if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                event.replyError("That command cannot be unloaded.");
            return;
        }
        CommandClient client = event.getClient();
        for (Command c : client.getCommands()) {
            if (c.getName().equals(commandName)) {
                ListCategory listCategory = CommandCategories.getListCategory(c.getCategory().getName());
                if (listCategory != null) listCategory.remove(commandName);
                client.removeCommand(commandName);
                if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                    event.replySuccess("Command `" + commandName + "` unloaded with success.");
                return;
            }
        }
        if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
            event.reply("That command is already unloaded or doesn't exist.");

    }

}