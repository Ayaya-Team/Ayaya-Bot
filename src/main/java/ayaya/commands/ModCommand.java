package ayaya.commands;

import com.jagrosh.jdautilities.command.CommandEvent;

/**
 * Abstract class representing a moderation command.
 */
public abstract class ModCommand extends Command {

    /**
     * When all threads finish, this method must be called mainly to process the output.
     *
     * @param event the command event that triggered this command
     */
    protected abstract void onFinish(CommandEvent event);

}