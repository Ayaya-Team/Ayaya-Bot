package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.Ayaya;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.concurrent.TimeUnit;

import static ayaya.core.Ayaya.disconnectVoice;
import static ayaya.core.enums.CommandCategories.OWNER;

/**
 * Class of the shutdown command.
 */
public class Shutdown extends Command {

    private static final String SHUTDOWN_REPLY = "I'm feeling sleepy, I think I will take a nap. Goodbye... :sleeping:";
    private static final String SHUTDOWN_GAME = "Going to bed.";

    public Shutdown() {

        this.name = "shutdown";
        this.category = OWNER.asCategory();
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
        this.isDisabled = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        if (event.getChannelType() != ChannelType.TEXT || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
            event.reply(SHUTDOWN_REPLY);
        ShardManager shardManager = event.getJDA().getShardManager();
        JDA jda = event.getJDA();
        shardManager.setActivity(Activity.playing(SHUTDOWN_GAME));
        event.getClient().shutdown();
        for (Guild guild : event.getJDA().getGuilds()) {
            disconnectVoice(guild);
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {}
        Ayaya.shutdownThreads();
        shardManager.setStatus(OnlineStatus.OFFLINE);
        shardManager.shutdown();
        System.exit(0);

    }

}
