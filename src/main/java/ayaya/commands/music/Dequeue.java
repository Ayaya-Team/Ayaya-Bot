package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Class of the dequeue command.
 */
public class Dequeue extends MusicCommand {

    public Dequeue() {

        this.name = "dequeue";
        this.help = "Any song queued by mistake? I can remove them from the queue.";
        this.arguments = "{prefix}dequeue <track number>";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        if (event.getArgs().isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't tell me which track to dequeue. Remember that "
                    + event.getClient().getPrefix() + "dequeue 0 will do the same as skipping the song.");
            return;
        }
        String[] args = event.getArgs().split(" ");
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        Guild guild = event.getGuild();
        int trackNumber;
        try {
            trackNumber = Integer.parseInt(args[args.length-1]);
        } catch (NumberFormatException e) {
            trackNumber = -1;
        }
        if (trackNumber < 0) {
            event.replyError("That's not a valid number.");
            return;
        }
        try {
            if ((voiceState == null || !voiceState.inVoiceChannel()) && musicHandler.connect(guild, voiceChannel, event.getTextChannel()))
            {
                event.reply("Now connected to the voice channel `" + voiceChannel.getName()
                        + "`.\nThe queue is already empty.");
            }
            else if (voiceState != null && voiceChannel == voiceState.getChannel()) {
                AudioTrack track = musicHandler.dequeueMusic(guild, trackNumber);
                if (track == null) {
                    event.replyError("The track number " + trackNumber + " wasn't found in the queue.");
                    return;
                }
                String track_title = track.getInfo().title;
                if (track_title == null || track_title.isEmpty())
                    track_title = "Undefined";
                event.reply("The track `" + track_title + "` was removed from the queue.");
            }
            else {
                event.reply("I only listen to the music commands of who is in the same voice channel as me.");
            }
        } catch (InsufficientPermissionException e) {
            event.replyError("Could not connect to the voice channel because it's already full.");
        }
    }

}