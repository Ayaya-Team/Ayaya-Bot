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
 * Class of the join command.
 */
public class Join extends MusicCommand {

    public Join() {

        this.name = "join";
        this.help = "Have me to join the voice channel you're in.";
        this.arguments = "{prefix}join";
        this.isGuildOnly = true;
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPermissions = new Permission[]{Permission.VOICE_CONNECT};

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
            } catch (InsufficientPermissionException e) {
                event.replyError("Could not connect to the voice channel because it's already full.");
            }
        } else if (voiceChannel == voiceState.getChannel()) {
            event.reply("I'm already connected to your channel.");
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}