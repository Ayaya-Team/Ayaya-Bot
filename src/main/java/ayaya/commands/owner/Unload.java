package ayaya.commands.owner;

import ayaya.commands.ListCategory;
import ayaya.core.Emotes;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;

import java.util.List;

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
                event.reply(Emotes.CONFUSED_EMOTE + " You didn't specify any command for me to unload.");
            return;
        }
        String commandName = args[0].toLowerCase();
        if (commandName.equals("load") || commandName.equals("unload")) {
            if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                event.replyError("That command cannot be unloaded.");
            return;
        }
        List<Command> commands = event.getClient().getCommands();
        for (Command c : commands) {
            ayaya.commands.Command command = (ayaya.commands.Command) c;
            if (c.getName().equals(commandName) && !command.isDisabled()) {
                ListCategory listCategory = CommandCategories.getListCategory(c.getCategory().getName());
                if (listCategory != null) listCategory.remove(commandName);
                command.disable();
                if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                    event.replySuccess("Command `" + commandName + "` unloaded with success.");
                return;
            }
        }
        if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
            event.reply("That command is already unloaded or doesn't exist.");

    }

}