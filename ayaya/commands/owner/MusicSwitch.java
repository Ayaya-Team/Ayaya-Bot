package ayaya.commands.owner;

import ayaya.commands.ListCategory;
import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
//import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;

import static ayaya.core.enums.CommandCategories.OWNER;
import static ayaya.core.Ayaya.disconnectVoice;

/**
 * Class of the musicswitch command.
 */
public class MusicSwitch extends ayaya.commands.Command {

    private static final String REPLY = "The music system is %s.";
    private static final String ON = "on";
    private static final String OFF = "off";

    public MusicSwitch() {

        this.name = "mswitch";
        this.category = OWNER.asCategory();
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_ADD_REACTION};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String[] args = event.getArgs().split(" ");
        boolean isOn = false;
        List<Command> commands = event.getClient().getCommands();
        for (Command command: commands)
            if (command.getCategory().equals(CommandCategories.MUSIC.asCategory())
                    && !((ayaya.commands.Command) command).isDisabled())
                isOn = true;
        if (event.getArgs().isEmpty()) {
            if (isOn) event.reply(String.format(REPLY, ON));
            else event.reply(String.format(REPLY, OFF));
        } else {
            String option = args[0];
            ListCategory listCategory;
            //String categoryName;
            if (option.equals(ON) && !isOn) {
                /*MusicHandler musicHandler = new MusicHandler();
                for (MusicCommands command : MusicCommands.values()) {
                    command.getCommandAsMusicCommand().setMusicHandler(musicHandler);
                    event.getClient().addCommand(command.getCommand());
                    if (command.getCommand().getCategory() != null) {
                        categoryName = command.getCommand().getCategory().getName();
                        listCategory = CommandCategories.getListCategory(categoryName);
                        if (listCategory != null) listCategory.add(command.getName());
                    }
                }*/
                for (Command command: commands) {
                    if (command.getCategory().equals(CommandCategories.MUSIC.asCategory())) {
                        ((ayaya.commands.Command) command).enable();
                        listCategory = CommandCategories.getListCategory(command.getCategory().getName());
                        if (listCategory != null) listCategory.add(command.getName());
                    }
                }
                event.reactSuccess();
            } else if (option.equals(OFF) && isOn) {
                //AudioManager audioManager;
                for (Guild guild : event.getJDA().getGuilds()) {
                    disconnectVoice(guild);
                }
                /*for (MusicCommands command : MusicCommands.values()) {
                    event.getClient().removeCommand(command.getCommand().getName());
                    if (command.getCommand().getCategory() != null) {
                        categoryName = command.getCommand().getCategory().getName();
                        listCategory = CommandCategories.getListCategory(categoryName);
                        if (listCategory != null) listCategory.remove(command.getName());
                    }
                }*/
                for (Command command: commands) {
                    if (command.getCategory().equals(CommandCategories.MUSIC.asCategory())) {
                        ((ayaya.commands.Command) command).disable();
                        listCategory = CommandCategories.getListCategory(command.getCategory().getName());
                        if (listCategory != null) listCategory.remove(command.getName());
                    }
                }
                event.reactSuccess();
            }
        }

    }

}