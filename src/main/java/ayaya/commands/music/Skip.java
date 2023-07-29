package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * Class of the skip command.
 */
public class Skip extends MusicCommand {

    public Skip() {

        this.name = "skip";
        this.help = "Skips the current track.";
        this.arguments = "{prefix}skip";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT,Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.reply("I'm not playing anything right now.");
        } else if (voiceChannel == voiceState.getChannel()) {
            if (musicHandler.skipMusic(guild)) {
                AudioTrack track = musicHandler.getCurrentMusic(guild);
                if (track == null)
                    event.reply("Skipped to the next track.");
                else {
                    String playingTrackTitle = musicHandler.getCurrentMusic(guild).getInfo().title;
                    playingTrackTitle =
                            (playingTrackTitle == null || playingTrackTitle.isEmpty()) ? "Undefined" : playingTrackTitle;
                    event.reply("Skipped to the next track.\nNow playing `" + playingTrackTitle + "`.");
                }
            } else event.reply("There aren't any more tracks to skip.");
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}