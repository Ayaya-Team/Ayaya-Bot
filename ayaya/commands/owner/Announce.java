package ayaya.commands.owner;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import static ayaya.core.enums.CommandCategories.OWNER;

/**
 * Class of the announce command.
 */
public class Announce extends Command {

    public Announce() {

        this.name = "announce";
        this.category = OWNER.asCategory();
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};
        this.isDisabled = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        if (message.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't tell me what you wanted to announce.");
            return;
        }
        TextChannel channel;
        for (Guild guild : event.getJDA().getGuilds()) {
            channel = getNewsChannel(guild);
            if (channel != null && channel.canTalk())
                channel.sendMessage(message).queue();
        }
        event.replySuccess("The announce was sent to all the news channels.");

    }

    /**
     * Retrieves the news channel from a given guild.
     *
     * @param guild the guild to get the channel from
     * @return text channel
     */
    private TextChannel getNewsChannel(Guild guild) {

        for (TextChannel c : guild.getTextChannels()) {
            if (c.getName().equals("ayaya_news")) return c;
        }
        return null;

    }

}