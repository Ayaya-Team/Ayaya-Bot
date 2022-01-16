package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.BotData;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the vote command.
 */
public class Vote extends Command {

    public Vote() {

        this.name = "vote";
        this.help = "If you like me you can vote for me in some of the bot lists. This command will give you the links.";
        this.arguments = "{prefix}vote";
        this.category = INFORMATION.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
        this.aliases = new String[]{"upvote"};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        StringBuilder s = new StringBuilder("You can vote for me in one of these websites:");
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