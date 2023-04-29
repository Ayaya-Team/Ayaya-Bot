package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Class of the pause command.
 */
public class Pause extends MusicCommand {

    public Pause() {

        this.name = "pause";
        this.help = "If you need a break I can pause the music.";
        this.arguments = "{prefix}pause";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};
        this.isPremium = true;

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        try {
            if (musicHandler.connect(guild, voiceChannel, event.getTextChannel())) {
                event.reply("Now connected to the voice channel `" + voiceChannel.getName()
                        + "`.\nThere is no track in the queue to pause.");
            } else if (voiceState != null && voiceChannel == voiceState.getChannel()) {
                if (musicHandler.getMusicAmount(guild) == 0)
                    event.reply("There is no track in the queue to pause.");
                else if (musicHandler.pauseQueue(guild))
                    event.reply("The music was paused.");
                else
                    event.reply("The music is already paused.");
            } else {
                event.reply("I only listen to the music commands of who is in the same voice channel as me.");
            }
        } catch (InsufficientPermissionException e) {
            event.replyError("Could not connect to the voice channel because it's already full.");
        }

    }

}