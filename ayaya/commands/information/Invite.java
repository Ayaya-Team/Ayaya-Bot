package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.BotData;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the invite command.
 */
public class Invite extends Command {

    public Invite() {

        this.name = "invite";
        this.help = "So you want to invite me to your server? Then use this command to see how.";
        this.arguments = "{prefix}invite";
        this.category = INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        EmbedBuilder invite_embed = new EmbedBuilder()
                .setDescription("So you want to invite me to your server. How nice! <:AyaSmile:331115374739324930>\n"
                        + "Then you can select one of the options below:\n\n"
                        + "For normal permissions [click here](" + BotData.getInviteNormal() + ");\n"
                        + "For administrator permissions [click here](" + BotData.getInviteAdmin() + ");\n"
                        + "For minimal permissions [click here](" + BotData.getInviteMinimal() + ").\n\n"
                        + "Note that with minimal permissions the moderation and administrator commands won't work.");
        try {
            invite_embed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            invite_embed.setColor(Color.decode("#155FA0"));
        }
        event.reply(invite_embed.build());

    }

}