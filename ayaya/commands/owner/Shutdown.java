package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.Ayaya;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.concurrent.TimeUnit;

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

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        event.reply(SHUTDOWN_REPLY);
        JDA jda = event.getJDA();
        jda.getPresence().setActivity(Activity.playing(SHUTDOWN_GAME));
        event.getClient().shutdown();
        AudioManager audioManager;
        for (Guild guild : event.getJDA().getGuilds()) {
            audioManager = guild.getAudioManager();
            if (audioManager.isConnected())
                audioManager.closeAudioConnection();
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {}
        Ayaya.shutdownThreads();
        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        jda.shutdownNow();
        System.exit(0);

    }

}