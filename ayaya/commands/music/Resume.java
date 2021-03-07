package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Class of the resume command.
 */
public class Resume extends MusicCommand {

    public Resume() {

        this.name = "resume";
        this.help = "Resumes the current music if it is paused.";
        this.arguments = "{prefix}resume";
        this.aliases = new String[]{"resume"};
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};
        this.isPremium = true;

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        TextChannel textChannel = event.getTextChannel();
        Guild guild = event.getGuild();
        try {
            if (musicHandler.connect(guild, voiceChannel))
                event.reply("Now connected to the voice channel `" + voiceChannel.getName() + "`.");
        } catch (InsufficientPermissionException e) {
            event.replyError("Could not connect to the voice channel because it's already full.");
        }
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState != null && voiceChannel == voiceState.getChannel()) {
            if (musicHandler.resume(textChannel))
                event.reply("The music was resumed.");
            else if (musicHandler.noMusicPlaying(guild))
                event.reply("There is no music playing.");
            else
                event.reply("The music is already playing.");
        } else
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");

    }

}