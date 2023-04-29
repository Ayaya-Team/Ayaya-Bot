package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * Class of the leave command.
 */
public class Leave extends MusicCommand {

    public Leave() {

        this.name = "leave";
        this.help = "Once all the songs are over, you can tell me to leave the channel.\n" +
                "Please check if the queue is empty beforehand.";
        this.arguments = "{prefix}leave";
        this.category = CommandCategories.MUSIC.asCategory();
        this.aliases = new String[]{"disconnect"};
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.reply("I'm not connected to a voice channel.");
        } else if (voiceChannel == voiceState.getChannel()) {
            if (musicHandler.disconnect(guild))
                event.reply("Disconnected from voice channel.");
            else
                event.reply("There's still music being played right now.");
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}