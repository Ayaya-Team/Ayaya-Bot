package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class Volume extends MusicCommand {

    public Volume() {
        this.name = "volume";
        this.help = "Checks the current volume or changes it.";
        this.arguments = "{prefix}volume <integer>\nRun the command without arguments to check the current volume." +
                " The volume is always a number from 1 to 100.";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};
        this.isPremium = true;
    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {
        String[] args = event.getArgs().split(" ");
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (event.getArgs().isEmpty()) {
            int volume = musicHandler.getMusicVolume(guild);
            event.reply("The current volume is " + volume + ".");
        } else if (
                voiceState == null || !voiceState.inVoiceChannel() || voiceChannel == voiceState.getChannel()
        ) {
            int volume = 0;
            for (String s : args) {
                try {
                    volume = Integer.parseInt(s);
                    break;
                } catch (NumberFormatException e) {}
            }
            if (volume > 0 && volume < 101) {
                musicHandler.setMusicVolume(guild, volume);
                event.replySuccess("Volume successfully set to " + volume + ".");
            } else
                event.replyError("The volume can only be a number from 1 to 100");
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }
    }

}