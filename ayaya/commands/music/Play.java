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
 * Class of the play command.
 */
public class Play extends MusicCommand {

    public Play() {

        this.name = "play";
        this.help = "Listen to some music while you do whatever you have to do.";
        this.arguments = "{prefix}play <url or search query>\n\nTo just play what's already in the queue do {prefix}play.";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeMusicCommand(CommandEvent event) {

        VoiceChannel voiceChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
        TextChannel textChannel = event.getTextChannel();
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        String prefix = event.getClient().getPrefix();
        String url = event.getArgs();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            try {
                musicHandler.join(guild, voiceChannel);
                event.reply("Now connected to the voice channel `" + Objects.requireNonNull(voiceChannel).getName() + "`.");
                musicHandler.play(textChannel, url);
            } catch (InsufficientPermissionException e) {
                event.replyError("Could not connect to the voice channel because it's already full.");
            }
        } else if (voiceChannel == voiceState.getChannel()) {
            musicHandler.play(textChannel, url);
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}