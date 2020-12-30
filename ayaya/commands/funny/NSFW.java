package ayaya.commands.funny;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Prank command for the lewd ones.
 */
public class NSFW extends Command {

    public NSFW() {

        this.name = "nsfw";
        this.category = FUNNY.asCategory();
        this.hidden = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        if (event.isFromType(ChannelType.TEXT))
            event.reply("EWW! "+event.getMember().getEffectiveName()+" is such a lewdie! Go away!");
        else event.reply("EWW! "+event.getAuthor().getName()+" is such a lewdie! Go away!");

    }

}