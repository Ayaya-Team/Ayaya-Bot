package ayaya.commands.information;

import ayaya.commands.Command;
import ayaya.core.BotData;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the about command.
 */
public class About extends Command {

    private String dblLink;
    private String dboatsLink;
    private String dbotLink;

    public About() {

        this.name = "about";
        this.help = "A command to check info about me.";
        this.arguments = "{prefix}about";
        this.category = INFORMATION.asCategory();
        this.aliases = new String[]{"info"};
        this.isGuildOnly = false;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String javaVersion = System.getProperty("java.version");
        String upvoteList = "";
        StringBuilder s = new StringBuilder();
        for (String[] data : BotData.getBotlists()) {
            String upvoteLink = data[4];
            if (upvoteLink != null && !upvoteLink.isEmpty())
                s.append('[').append(data[0]).append("](").append(upvoteLink).append(")\n");
        }
        upvoteList = s.toString();
        EmbedBuilder about_embed = new EmbedBuilder();
        about_embed.setTitle("About " + event.getSelfUser().getName())
                .setDescription(BotData.getDescription())
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                .addField("Version", BotData.getVersion(), false)
                .addField("Invite", "[click here](" + BotData.getInviteNormal() + ")", true)
                .addField("Support Server Invite", "[click here](" + BotData.getServerInvite() + ")",
                        true)
                .addField("Donate", "[click here](" + BotData.getPatreonLink() + ")", true)
                .addField("Upvote", upvoteList, true)
                .setFooter("Requested by " + event.getAuthor().getName(),
                        event.getAuthor().getAvatarUrl());
        try {
            about_embed.setColor(event.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            about_embed.setColor(Color.decode("#155FA0"));
        }
        event.reply(about_embed.build());

    }

}