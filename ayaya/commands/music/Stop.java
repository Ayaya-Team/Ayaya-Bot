package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * Class of the stop command.
 */
public class Stop extends MusicCommand {

    public Stop() {

        this.name = "stop";
        this.help = "Stops the music and clears the queue.";
        this.arguments = "{prefix}stop";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel() || musicHandler.noMusicPlaying(guild)) {
            event.reply("I'm not playing anything right now.");
        } else if (voiceChannel == voiceState.getChannel()) {
            if (musicHandler.stopMusic(guild))
                event.reply("The music was stopped and the queue was cleared.");
            else
                event.reply("There's no music playing or in the queue.");
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}