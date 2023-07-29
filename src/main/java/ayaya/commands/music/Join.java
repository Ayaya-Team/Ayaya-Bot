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
        this.help = "Makes me join the voice channel you're in.";
        this.arguments = "{prefix}join";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        try {
            if (musicHandler.connect(guild, voiceChannel, event.getTextChannel()))
                event.reply("Now connected to the voice channel `" + Objects.requireNonNull(voiceChannel).getName() + "`.");
            else if (voiceState != null && voiceChannel == voiceState.getChannel()) {
                event.reply("I'm already connected to your channel.");
            } else {
                event.reply(
                        "I only listen to the music commands of who is in the same voice channel as me in the server."
                );
            }
        } catch (InsufficientPermissionException e) {
            event.replyError("Could not connect to the voice channel because it's already full.");
        }

    }

}