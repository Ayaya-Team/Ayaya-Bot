package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * Class of the message command.
 */
public class Message extends Command {

    public Message() {

        this.name = "message";
        this.category = CommandCategories.OWNER.asCategory();
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};
        this.isDisabled = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String command = event.getArgs();
        String[] args = command.split(" ");
        if (command.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't provide the id of the channel you want to send the message to.");
            return;
        }
        String channel_id = args[0];
        String message = command.replace(args[0], "").trim();
        TextChannel channel = event.getJDA().getTextChannelById(channel_id);
        if (channel == null || !channel.canTalk()) {
            event.replyError("Something went wrong. Probably I have no permission to talk in this channel or it does not exist.");
            return;
        }
        channel.sendMessage(message).queue();
        event.replySuccess("Your message was sent with success.");

    }

}