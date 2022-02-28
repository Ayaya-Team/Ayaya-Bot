package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.List;

/**
 * Class of the move command.
 */
public class Move extends MusicCommand {

    public Move() {
        this.name = "move";
        this.help = "You need to move voice channels and take the music that is playing with you? Now you can with this command.";
        this.arguments = "{prefix}move <channel name/id>";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};
        this.isPremium = false;
        this.cooldownTime = 5;
    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.reply("If you want me to join a voice channel, you can use the command `"
                    + event.getClient().getPrefix() + "join` while being in a voice channel of your choice.");
        } else if (voiceChannel == voiceState.getChannel()) {
            String args = event.getArgs();
            if (args.isEmpty()) {
                event.reply("I can't guess which channel you want me to go to.");
                return;
            }

            VoiceChannel channel = null;
            if (args.indexOf(' ') >= 0) {
                try {
                    channel = guild.getVoiceChannelById(args);
                } catch (NumberFormatException e) {
                    //Now we know this isn't a voice channel id.
                }
            }

            if (channel == null) {
                List<VoiceChannel> channels = guild.getVoiceChannelsByName(args, false);
                if (channels.isEmpty()) {
                    event.reply("I could not find that channel in this server. Make sure you provided the correct name or id.");
                    return;
                }
                channel = channels.get(0);
            }

            if (musicHandler.move(guild, voiceChannel, channel))
                event.replySuccess("I moved to the channel `" + channel.getName() + "`. Get over here to keep listening to the music!");
            else if (musicHandler.getMusicAmount(guild) == 0)
                event.replyError("I could not connect to the voice channel `" + channel.getName()
                        + "` and could not connect back to the previous channel. The queue was emptied.");
            else
                event.reply("I could not connect to the voice channel `" + channel.getName() + "`, so I returned to the previous channel.");
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}