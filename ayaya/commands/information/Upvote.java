package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.BotData;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the upvote command.
 */
public class Upvote extends Command {

    public Upvote() {

        this.name = "upvote";
        this.help = "If you like me you can upvote me in discordbots.org. This command will give you the link to upvote.";
        this.arguments = "{prefix}upvote";
        this.category = INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
        this.aliases = new String[]{"vote"};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        StringBuilder s = new StringBuilder("You can upvote me in one of these websites:");
        for (String[] botlistData : BotData.getBotlists()) {
            if (botlistData[4] != null && !botlistData[4].isEmpty())
                s.append("\n[").append(botlistData[0]).append("](").append(botlistData[4]).append(")");
        }
        EmbedBuilder upvoteEmbed = new EmbedBuilder()
                .setDescription(s.toString());
        try {
            upvoteEmbed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            upvoteEmbed.setColor(Color.decode("#155FA0"));
        }
        event.reply(upvoteEmbed.build());

    }

}