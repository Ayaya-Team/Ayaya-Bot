package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

public class Volume extends MusicCommand {

    public Volume() {
        this.name = "volume";
        this.help = "Check the current volume or change it.";
        this.arguments = "{prefix}volume <integer>\nRun the command without arguments to check the current volume." +
                " The volume is always a number from 1 to 100.";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};
        this.isPremium = true;
    }

    @Override
    protected void executeMusicCommand(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        Guild guild = event.getGuild();
        if (event.getArgs().isEmpty()) {
            int volume = musicHandler.getVolume(guild);
            event.reply("The current volume is " + volume + ".");
        } else {
            int volume = 0;
            for (String s : args) {
                try {
                    volume = Integer.parseInt(s);
                    break;
                } catch (NumberFormatException e) {}
            }
            if (volume > 0 && volume < 101) {
                musicHandler.setVolume(guild, volume);
                event.replySuccess("Volume successfully set to " + volume + ".");
            } else
                event.replyError("The volume can only be a number from 1 to 100");
        }
    }

}