package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import ayaya.core.music.GuildMusicManager;
import ayaya.core.music.TrackScheduler;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;

public class NP extends MusicCommand {

    private static final int BAR_LENGTH = 20;

    public NP() {

        this.name = "np";
        this.help = "Check the music that is currently playing.";
        this.arguments = "{prefix}np";
        this.isGuildOnly = false;
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPermissions = new Permission[]{Permission.VOICE_CONNECT};

    }

    @Override
    protected void executeMusicCommand(CommandEvent event) {

        GuildMusicManager musicManager = musicHandler.getGuildMusicManager(event.getGuild());
        TrackScheduler scheduler = musicManager.getScheduler();
        String message;
        if (scheduler.getTracks().isEmpty())
            message = "There are no tracks in the queue right now.";
        else {
            AudioTrack track = scheduler.getCurrentTrack();
            String trackName = track.getInfo().title;
            long time = track.getDuration() / 1000;
            long current = track.getPosition() / 1000;
            if (trackName == null)
                trackName = "Undefined";
            StringBuilder bar = new StringBuilder();
            long fullChars = Math.round((double)(current) / (double)(time) * BAR_LENGTH);
            for (int i = 1; i <= BAR_LENGTH; i++) {
                if (i <= fullChars) bar.append('▓');
                else bar.append('░');
            }
            if (scheduler.musicStopped())
                message = String.format(
                        "Current track to play is `%s`\n\n**%02d:%02d / %02d:%02d** ー %s",
                        trackName, current/60, current%60, time/60, time%60, bar.toString()
                );
            else
                message = String.format(
                        "Currently playing `%s`\n\n**%02d:%02d / %02d:%02d** ー %s",
                        trackName, current/60, current%60, time/60, time%60, bar.toString()
                );
        }
        EmbedBuilder npEmbed = new EmbedBuilder()
                .setAuthor("Now Playing", null, event.getSelfUser().getAvatarUrl())
                .setDescription(message);
        if (musicHandler.isRepeating(event.getGuild()))
            npEmbed.setFooter(
                    "Repeat mode on | " + scheduler.getTracks().size() + " tracks queued", null
            );
        else
            npEmbed.setFooter(
                    "Repeat mode off | " + scheduler.getTracks().size() + " tracks queued", null
            );
        try {
            npEmbed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            npEmbed.setColor(Color.decode("#155FA0"));
        }
        event.reply(npEmbed.build());

    }

}