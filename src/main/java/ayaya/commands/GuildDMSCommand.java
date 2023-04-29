package ayaya.commands;

import com.jagrosh.jdautilities.command.CommandEvent;

/**
 * Class of the commands which are meant to be handled differently on a server channel or direct messages channel.
 */
public class GuildDMSCommand extends Command {

    public GuildDMSCommand() {
        super();
    }

    /**
     * Main body of a command.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command
     */
    protected void executeInstructions(CommandEvent event) {
        try {
            event.getGuild();
            executeInGuild(event);
        } catch (IllegalStateException e) {
            executeInDMS(event);
        }
    }

    /**
     * Main body of a command executed on a server.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command
     */
    protected void executeInGuild(CommandEvent event) {
    }

    /**
     * Main body of a command executed on direct message
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command
     */
    protected void executeInDMS(CommandEvent event) {
    }

}