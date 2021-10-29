package ayaya.commands.owner;

import ayaya.commands.ListCategory;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;

import java.util.List;

import static ayaya.core.enums.CommandCategories.OWNER;

/**
 * Class of the load command.
 */
public class Load extends ayaya.commands.Command {

    public Load() {
        this.name = "load";
        this.category = OWNER.asCategory();
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String[] args = event.getArgs().split(" ");
        if (event.getArgs().isEmpty()) {
            if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                event.reply("<:AyaWhat:362990028915474432> You didn't specify any command for me to load.");
            return;
        }
        String commandName = args[0].toLowerCase();
        List<Command> commands = event.getClient().getCommands();
        ayaya.commands.Command command = null;
        for (Command c : commands) {
            command = (ayaya.commands.Command) c;
            if (c.getName().equals(commandName) && !command.isDisabled()) {
                if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                    event.reply("That command is already loaded.");
                return;
            }
        }

        if (command != null) {
            ListCategory listCategory = CommandCategories.getListCategory(command.getCategory().getName());
            if (listCategory != null && !command.isHidden()) listCategory.add(command.getName());
            command.enable();
            event.replySuccess("Command `" + commandName + "` loaded with success.");
        } else if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
            event.replyError("Command not found.");

    }

}