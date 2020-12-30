package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * Class of the channellist command.
 */
public class Channellist extends Command {

    public Channellist() {

        this.name = "channellist";
        this.category = CommandCategories.OWNER.asCategory();
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String command = event.getArgs();
        String[] args = command.split(" ");
        if (command.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't tell me the id of the channel from which you want the channel list.");
            return;
        }
        String guild_id = args[0];
        Guild guild = event.getJDA().getGuildById(guild_id);
        String channel_info;
        StringBuilder s = new StringBuilder();
        if (guild == null)
            event.replyError("There isn't a server with that id.");
        else {
            for (TextChannel channel : guild.getTextChannels()) {
                s.append(channel.getName()).append(" | ").append(channel.getId()).append('\n');
            }
            channel_info = "Name | ID\n" + s.toString();
            event.reply("```css\nChannel List for the server " + guild.getName() + ":\n" + channel_info + "```");
        }
    }

}