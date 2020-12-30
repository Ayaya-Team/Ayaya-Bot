package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.Objects;

/**
 * The class of the seek command.
 */
public class Seek extends MusicCommand {

    public Seek() {

        this.name = "seek";
        this.help = "If you need to skip or go back to a certain point of a song, use this command.";
        this.arguments = "{prefix}seek <seconds>";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};
        this.isPremium = true;

    }

    @Override
    protected void executeMusicCommand(CommandEvent event) {

        String message = event.getArgs();
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            try {
                VoiceChannel voiceChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
                musicHandler.join(guild, voiceChannel);
                event.reply("Now connected to the voice channel `" + Objects.requireNonNull(voiceChannel).getName() + "`.");
            } catch (InsufficientPermissionException e) {
                event.replyError("Could not connect to the voice channel because it's already full.");
                return;
            }
        }
        if (musicHandler.getGuildAudioPlayer(event.getGuild()).getScheduler().noMusicPlaying()) {
            event.reply("There is no music being played right now.");
            return;
        }
        if (message.isEmpty()) {
            event.reply(
                    "<:AyaWhat:362990028915474432> You didn't tell me how many seconds I should skip or rewind." +
                            " Remember to put a negative value in case you want to rewind."
            );
            return;
        }
        long amount = amountInString(message);
        if (amount == 0) {
            event.reply("Please use a value different than 0.");
            return;
        }
        if (musicHandler.seek(event.getGuild(), amount)) {
            if (amount < 0) event.replySuccess("Time rewinded.");
            else event.replySuccess("Time skipped.");
        } else {
            event.reply(
                    "Could not perform this action due to either the track not being seekable or" +
                            " the amount not being a valid integer."
            );
        }

    }

    /**
     * Returns the first integer in a string.
     *
     * @param s the string
     * @return integer
     */
    private long amountInString(String s) {
        String[] args = s.split(" ");
        long num = 0;
        for (String arg : args) {
            try {
                num = Long.parseLong(arg);
                break;
            } catch (NumberFormatException e) {
                //Skip to the next arg
            }
        }
        return num;
    }

}