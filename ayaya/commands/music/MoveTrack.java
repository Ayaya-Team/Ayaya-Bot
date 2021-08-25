package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * Class of the movetrack command.
 */
public class MoveTrack extends MusicCommand {

    public MoveTrack() {
        this.name = "movetrack";
        this.help = "If you need to move tracks in the queue, use this command.";
        this.arguments = "{prefix}movetrack <number of track> <number of position in the queue>";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};
        this.isPremium = true;
        this.cooldownTime = 5;
    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        TextChannel textChannel = event.getTextChannel();
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.reply("There are no tracks in the queue to move.");
        } else if (voiceChannel == voiceState.getChannel()) {
            int musicAmount = musicHandler.getMusicAmount(guild);
            if (musicAmount == 0) {
                event.reply("There are no tracks in the queue to move.");
            }
            else {
                String[] args = event.getArgs().split(" ");
                if (args.length < 2) {
                    event.reply("I need 2 numbers to perform this action.");
                    return;
                }

                int numTrackToMove = 0;
                int newPosition = 0;
                try {
                    numTrackToMove = Integer.parseInt(args[0]);
                    newPosition = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    event.replyError("At least one of the numbers isn't valid.");
                }

                if (numTrackToMove > musicAmount) {
                    event.reply(String.format("There is no track queued with the number %d.", numTrackToMove));
                    return;
                }

                if (numTrackToMove == 0 && musicHandler.getCurrentMusic(guild) != null) {
                    event.reply("I can't move a track that I already started to play.");
                    return;
                }

                if (newPosition == 0 && musicHandler.getCurrentMusic(guild) != null) {
                    event.reply("I can't move a track to the index 0 if that track is already being played.");
                }

                if (musicHandler.moveMusic(guild, numTrackToMove, newPosition)) {
                    event.replySuccess("Track moved with success.");
                } else {
                    event.reply("That position is out of range.");
                }
            }
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}