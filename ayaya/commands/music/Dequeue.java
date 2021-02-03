package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.Objects;

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
        TextChannel textChannel = event.getTextChannel();
        Guild guild = event.getGuild();
        int track_number;
        try {
            track_number = Integer.parseInt(args[args.length-1]);
        } catch (NumberFormatException e) {
            track_number = -1;
        }
        if (track_number < 0) {
            event.replyError("That's not a valid number.");
            return;
        }
        
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            musicHandler.join(guild, voiceChannel);
            event.reply("Now connected to the voice channel `"
                    + Objects.requireNonNull(voiceChannel).getName() + "`.");
            event.reply("The queue is already empty.");
        } else if (voiceChannel == voiceState.getChannel()) {
            try {
                AudioTrack track = musicHandler.dequeue(textChannel, track_number);
                String track_title = track.getInfo().title;
                if (track_title == null || track_title.isEmpty())
                    track_title = "Undefined";
                event.reply("The track `" + track_title + "` was removed from the queue.");
            } catch (IndexOutOfBoundsException e) {
                event.replyError("The track number " + track_number + " wasn't found in the queue.");
            }
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}