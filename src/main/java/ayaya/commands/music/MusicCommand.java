package ayaya.commands.music;

import ayaya.commands.Command;
import ayaya.core.music.MusicHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * This is the superclass of the music commands.
 */
public class MusicCommand extends Command {

    protected MusicHandler musicHandler;

    public MusicCommand() {
        super();
        this.isGuildOnly = true;
    }

    /**
     * Configures the music handler for this command.
     *
     * @param musicHandler the music handler to assign to this command
     */
    public void setMusicHandler(MusicHandler musicHandler) {
        this.musicHandler = musicHandler;
    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        GuildVoiceState voiceState = event.getMember().getVoiceState();
        VoiceChannel voiceChannel;
        if (voiceState != null && (voiceChannel = voiceState.getChannel()) != null) {
            executeMusicCommand(event, voiceChannel);
        } else event.reply("You must be in a voice channel to use this command.");

    }

    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {}

}