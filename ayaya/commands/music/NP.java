package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import ayaya.core.utils.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.awt.*;

public class NP extends MusicCommand {

    private static final int BAR_LENGTH = 20;

    public NP() {

        this.name = "np";
        this.help = "Displays information on the current song.";
        this.arguments = "{prefix}np";
        this.category = CommandCategories.MUSIC.asCategory();
        this.aliases = new String[]{"nowplaying"};
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            EmbedBuilder npEmbed = new EmbedBuilder()
                    .setAuthor("Now Playing", null, event.getSelfUser().getAvatarUrl())
                    .setDescription("I'm not playing anything right now.")
                    .setFooter(
                    "Repeat mode off | 0 tracks queued", null
            );
            try {
                npEmbed.setColor(event.getGuild().getSelfMember().getColor());
            } catch (IllegalStateException | NullPointerException e) {
                npEmbed.setColor(Color.decode("#155FA0"));
            }
            event.reply(npEmbed.build());
            return;
        }

        Guild guild = event.getGuild();
        int trackAmount = musicHandler.getMusicAmount(guild);
        String message;
        if (trackAmount == 0)
            message = "There are no tracks in the queue right now.";
        else if (musicHandler.getCurrentMusic(guild) == null)
            message = "No music is being played, but there are musics queued.";
        else {
            AudioTrack track = musicHandler.getCurrentMusic(guild);
            String trackName = track.getInfo().title;
            long time = track.getDuration() / 1000;
            long current = track.getPosition() / 1000;
            if (trackName == null)
                trackName = "Undefined";
            String bar = Utils.printBar(current, time, BAR_LENGTH);
            if (musicHandler.getCurrentMusic(guild) == null || musicHandler.queueIsPaused(guild))
                message = String.format(
                        "Current track to play is `%s`\n\n**%02d:%02d / %02d:%02d** ー %s",
                        trackName, current/60, current%60, time/60, time%60, bar
                );
            else
                message = String.format(
                        "Currently playing `%s`\n\n**%02d:%02d / %02d:%02d** ー %s",
                        trackName, current/60, current%60, time/60, time%60, bar
                );
        }
        EmbedBuilder npEmbed = new EmbedBuilder()
                .setAuthor("Now Playing", null, event.getSelfUser().getAvatarUrl())
                .setDescription(message);
        if (musicHandler.queueIsRepeating(event.getGuild()))
            npEmbed.setFooter(
                    "Repeat mode on | " + trackAmount + " tracks queued", null
            );
        else
            npEmbed.setFooter(
                    "Repeat mode off | " + trackAmount + " tracks queued", null
            );
        try {
            npEmbed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            npEmbed.setColor(Color.decode("#155FA0"));
        }
        event.reply(npEmbed.build());

    }

}