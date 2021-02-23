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
import java.util.Iterator;

/**
 * Class of the queue command.
 */
public class Queue extends MusicCommand {

    private static final int LIST_LIMIT = 10;

    public Queue() {

        this.name = "queue";
        this.help = "Queue some songs before I start playing them.";
        this.arguments = "{prefix}queue <url or search query>\n\nDo {prefix}queue to get the list of tracks in the queue.";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        TextChannel textChannel = event.getTextChannel();
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        String prefix = event.getClient().getPrefix();
        String url = event.getArgs();
        try {
            if (musicHandler.connect(guild, voiceChannel)) {
                event.reply(
                        "Now connected to the voice channel `" + voiceChannel.getName() + "`."
                );
                Iterator<AudioTrack> iterator = queueOrShowList(textChannel, url);
                if (iterator != null) {
                    printTrackList(event, iterator);
                }
            } else if (voiceState != null && voiceChannel == voiceState.getChannel()) {
                Iterator<AudioTrack> iterator = queueOrShowList(textChannel, url);
                if (iterator != null) {
                    printTrackList(event, iterator);
                }
            } else {
                event.reply("I only listen to the music commands of who is in the same voice channel as me.");
            }
        } catch (InsufficientPermissionException e) {
            event.replyError("Could not connect to the voice channel because it's already full.");
        }

    }

    private Iterator<AudioTrack> queueOrShowList(TextChannel textChannel, String url) {
        if (url.isEmpty()) return musicHandler.getTrackIterator(textChannel.getGuild());
        else {
            musicHandler.queue(textChannel, url);
            return null;
        }
    }

    private void printTrackList(CommandEvent event, Iterator<AudioTrack> iterator) {
        if (!iterator.hasNext()) {
            event.reply("There are no tracks in the queue right now.");
        } else {
            Guild guild = event.getGuild();
            StringBuilder queueList = new StringBuilder();
            int i = 0;
            String trackTitle;

            do {
                AudioTrack track = iterator.next();
                if (i == 0)
                    queueList.append((musicHandler.musicPaused(guild)) ? "Next music: `" : "Now playing: `");
                else
                    queueList.append(i).append(": `");
                trackTitle = track.getInfo().title;
                queueList.append((trackTitle != null && !trackTitle.isEmpty()) ? track.getInfo().title : "Undefined")
                        .append("`\n");
                if (i == 10) {
                    queueList.append("And ").append(musicHandler.getTrackAmount(guild) - 11).append(" more tracks.");
                    break;
                }
                i++;
            } while (iterator.hasNext());

            EmbedBuilder queueEmbed = new EmbedBuilder()
                    .setAuthor("Queue", null, event.getSelfUser().getAvatarUrl())
                    .setDescription(queueList.toString())
                    .setFooter((musicHandler.isRepeating(guild)) ? "Repeat mode on" : "Repeat mode off", null);
            try {
                queueEmbed.setColor(guild.getSelfMember().getColor());
            } catch (NullPointerException e) {
                queueEmbed.setColor(Color.decode("#155FA0"));
            }
            event.reply(queueEmbed.build());
        }
    }

}