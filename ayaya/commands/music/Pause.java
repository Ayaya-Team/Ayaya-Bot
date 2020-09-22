package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.Objects;

/**
 * Class of the pause command.
 */
public class Pause extends MusicCommand {

    public Pause() {

        this.name = "pause";
        this.help = "If you need a break I can pause the music.";
        this.arguments = "{prefix}pause";
        this.isGuildOnly = true;
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPermissions = new Permission[]{Permission.VOICE_CONNECT};
        this.isPremium = true;

    }

    @Override
    protected void executeMusicCommand(CommandEvent event) {

        TextChannel textChannel = event.getTextChannel();
        VoiceChannel voiceChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            try {
                musicHandler.join(guild, voiceChannel);
                event.reply("Now connected to the voice channel `" + Objects.requireNonNull(voiceChannel).getName() + "`.");
                musicHandler.pause(textChannel);
            } catch (InsufficientPermissionException e) {
                event.replyError("Could not connect to the voice channel because it's already full.");
            }
        } else if (voiceChannel == event.getSelfMember().getVoiceState().getChannel()) {
            musicHandler.pause(textChannel);
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}