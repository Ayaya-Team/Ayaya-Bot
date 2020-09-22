package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.commands.ListCategory;
import ayaya.core.music.MusicHandler;
import ayaya.core.enums.CommandCategories;
import ayaya.core.enums.MusicCommands;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

import static ayaya.core.enums.CommandCategories.OWNER;

/**
 * Class of the musicswitch command.
 */
public class MusicSwitch extends Command {

    private static final String REPLY = "The music system is %s.";
    private static final String ON = "on";
    private static final String OFF = "off";
    private boolean isOn;

    public MusicSwitch() {

        this.name = "mswitch";
        this.category = OWNER.asCategory();
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
        isOn = false;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String[] args = event.getArgs().split(" ");
        if (event.getArgs().isEmpty()) {
            if (isOn) event.reply(String.format(REPLY, ON));
            else event.reply(String.format(REPLY, OFF));
        } else {
            String option = args[0];
            ListCategory listCategory;
            String categoryName;
            if (option.equals(ON) && !isOn) {
                MusicHandler musicHandler = new MusicHandler();
                for (MusicCommands command : MusicCommands.values()) {
                    command.getCommandAsMusicCommand().setMusicHandler(musicHandler);
                    event.getClient().addCommand(command.getCommand());
                    if (command.getCommand().getCategory() != null) {
                        categoryName = command.getCommand().getCategory().getName();
                        listCategory = CommandCategories.getListCategory(categoryName);
                        if (listCategory != null) listCategory.add(command.getName());
                    }
                }
                isOn = !isOn;
                event.reactSuccess();
            } else if (option.equals(OFF) && isOn) {
                AudioManager audioManager;
                for (Guild guild : event.getJDA().getGuilds()) {
                    audioManager = guild.getAudioManager();
                    if (audioManager.isConnected())
                        audioManager.closeAudioConnection();
                }
                for (MusicCommands command : MusicCommands.values()) {
                    event.getClient().removeCommand(command.getCommand().getName());
                    if (command.getCommand().getCategory() != null) {
                        categoryName = command.getCommand().getCategory().getName();
                        listCategory = CommandCategories.getListCategory(categoryName);
                        if (listCategory != null) listCategory.remove(command.getName());
                    }
                }
                isOn = !isOn;
                event.reactSuccess();
            }
        }

    }

}