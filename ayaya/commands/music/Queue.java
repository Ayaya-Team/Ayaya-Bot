package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Class of the queue command.
 */
public class Queue extends MusicCommand {

    public Queue() {

        this.name = "queue";
        this.help = "Queue some songs before I start playing them.";
        this.arguments = "{prefix}queue <url or search query>\n\nDo {prefix}queue to get the list of tracks in the queue.";
        this.isGuildOnly = true;
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPermissions = new Permission[]{Permission.VOICE_CONNECT};

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
                event.reply(
                        "Now connected to the voice channel `"
                                + Objects.requireNonNull(voiceChannel).getName() + "`."
                );
                List<AudioTrack> tracks = queueOrShowList(textChannel, url);
                if (tracks != null) {
                    printTrackList(event, tracks);
                }
            } catch (InsufficientPermissionException e) {
                event.replyError("Could not connect to the voice channel because it's already full.");
            }
        } else if (voiceChannel == voiceState.getChannel()) {
            List<AudioTrack> tracks = queueOrShowList(textChannel, url);
            if (tracks != null) {
                printTrackList(event, tracks);
            }
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

    private List<AudioTrack> queueOrShowList(TextChannel textChannel, String url) {
        if (url.isEmpty()) return musicHandler.trackList(textChannel.getGuild());
        else {
            musicHandler.queue(textChannel, url);
            return null;
        }
    }

    private void printTrackList(CommandEvent event, List<AudioTrack> tracks) {
        if (tracks.isEmpty())
            event.reply("There are no tracks in the queue right now.");
        else {
            StringBuilder queue_list = new StringBuilder();
            int i = 0;
            String trackTitle;
            for (AudioTrack track : tracks) {
                if (i == 0) {
                    if (musicHandler.musicStopped(event.getGuild()))
                        queue_list.append("Next music: `");
                    else
                        queue_list.append("Now playing: `");
                } else
                    queue_list.append(i).append(": `");
                trackTitle = track.getInfo().title;
                if (trackTitle != null && !trackTitle.isEmpty())
                    queue_list.append(track.getInfo().title);
                else
                    queue_list.append("Undefined");
                queue_list.append("`\n");
                if (i == 10) {
                    queue_list.append("And ").append(tracks.size() - 11).append(" more tracks.");
                    break;
                }
                i++;
            }
            EmbedBuilder queue_embed = new EmbedBuilder()
                    .setAuthor("Queue", null, event.getSelfUser().getAvatarUrl())
                    .setDescription(queue_list.toString());
            try {
                queue_embed.setColor(event.getGuild().getSelfMember().getColor());
            } catch (NullPointerException e) {
                queue_embed.setColor(Color.decode("#155FA0"));
            }
            if (musicHandler.isRepeating(event.getGuild()))
                queue_embed.setFooter("Repeat mode on", null);
            else
                queue_embed.setFooter("Repeat mode off", null);
            event.reply(queue_embed.build());
        }
    }

}