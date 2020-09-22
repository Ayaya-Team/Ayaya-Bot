package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.Objects;

/**
 * Class of the repeat command.
 */
public class Repeat extends MusicCommand {

    public Repeat() {

        this.name = "repeat";
        this.help = "I can repeat the current queue if you want.";
        this.arguments = "{prefix}repeat";
        this.isGuildOnly = true;
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPermissions = new Permission[]{Permission.VOICE_CONNECT};
        this.isPremium = true;

    }

    @Override
    protected void executeMusicCommand(CommandEvent event) {

        VoiceChannel voiceChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            try {
                musicHandler.join(guild, voiceChannel);
                event.reply("Now connected to the voice channel `" + Objects.requireNonNull(voiceChannel).getName() + "`.");
                musicHandler.repeat(guild);
            } catch (InsufficientPermissionException e) {
                event.replyError("Could not connect to the voice channel because it's already full.");
            }
            if (musicHandler.isRepeating(guild))
                event.reply("Repeat mode on.");
            else
                event.reply("Repeat mode off.");
        } else if (voiceChannel == voiceState.getChannel()) {
            musicHandler.repeat(guild);
            if (musicHandler.isRepeating(guild))
                event.reply("Repeat mode on.");
            else
                event.reply("Repeat mode off.");
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}