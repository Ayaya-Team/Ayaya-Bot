package ayaya.commands.owner;

import ayaya.commands.ListCategory;
import ayaya.core.enums.CommandAliases;
import ayaya.core.enums.CommandCategories;
import ayaya.core.enums.Commands;
import ayaya.core.enums.OwnerCommands;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

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
            event.reply("<:AyaWhat:362990028915474432> You didn't specify any command for me to load.");
            return;
        }
        CommandClient client = event.getClient();
        for (Command c : client.getCommands()) {
            if (c.getName().equals(args[0].toLowerCase())) {
                event.reply("That command is already loaded.");
                return;
            }
        }
        Command command = null;
        switch (args[0].toLowerCase()) {
            case "serverlist2":
                for (Object listener : event.getJDA().getRegisteredListeners())
                    if (listener instanceof EventWaiter)
                        command = new Serverlist((EventWaiter) listener);

                break;
            default:
                command = getCommand(args[0].toLowerCase());
        }
        if (command != null) {
            client.addCommand(command);
            ListCategory listCategory = CommandCategories.getListCategory(command.getCategory().getName());
            if (listCategory != null && !command.isHidden()) listCategory.add(command.getName());
            event.replySuccess("Command `" + args[0] + "` loaded with success.");
        } else event.replyError("Command not found.");

    }

    /**
     * Fetches the command to be loaded.
     *
     * @param name the name of the command
     * @return command
     */
    private Command getCommand(String name) {

        for (Commands cmd : Commands.values()) if (cmd.getName().equals(name)) return cmd.getCommand();
        for (CommandAliases cmd : CommandAliases.values()) if (cmd.getName().equals(name)) return cmd.getCommand();
        for (OwnerCommands cmd : OwnerCommands.values()) if (cmd.getName().equals(name)) return cmd.command();
        return null;

    }

}