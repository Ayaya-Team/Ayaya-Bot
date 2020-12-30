package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import ayaya.core.music.GuildMusicManager;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.Objects;

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
    protected void executeMusicCommand(CommandEvent event) {

        VoiceChannel voiceChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        GuildMusicManager musicManager = musicHandler.getGuildAudioPlayer(guild);
        if (voiceState == null || !voiceState.inVoiceChannel() || musicManager.getScheduler().getCurrentTrack() == null) {
            event.reply("I'm not playing anything right now.");
        } else if (voiceChannel == voiceState.getChannel()) {
            event.reply("Stopping the music.");
            musicHandler.stop(guild);
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}