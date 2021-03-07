package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Class of the repeat command.
 */
public class Repeat extends MusicCommand {

    public Repeat() {

        this.name = "repeat";
        this.help = "I can repeat the queue if you want.";
        this.arguments = "{prefix}repeat";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};
        this.isPremium = true;

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        Guild guild = event.getGuild();
        try {
            if (musicHandler.connect(guild, voiceChannel))
                event.reply("Now connected to the voice channel `" + voiceChannel.getName() + "`.");
        } catch (InsufficientPermissionException e) {
            event.replyError("Could not connect to the voice channel because it's already full.");
        }
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState != null && voiceChannel == voiceState.getChannel()) {
            if (musicHandler.repeat(guild))
                event.reply("Repeat mode on.");
            else
                event.reply("Repeat mode off.");
        } else
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");

    }

}