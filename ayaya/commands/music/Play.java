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
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        TextChannel textChannel = event.getTextChannel();
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        String url = event.getArgs();
        try {
            if (musicHandler.connect(guild, voiceChannel, event.getTextChannel())) {
                event.reply("Now connected to the voice channel `" + voiceChannel.getName() + "`.");
                musicHandler.queueAndPlay(textChannel, url, true);
            } else if (voiceState != null && voiceState.getChannel() == voiceChannel) {
                musicHandler.queueAndPlay(textChannel, url, true);
            } else {
                event.reply("I only listen to the music commands of who is in the same voice channel as me.");
            }
        } catch (InsufficientPermissionException e) {
            event.replyError("Could not connect to the voice channel because it's already full.");
        }

    }

}