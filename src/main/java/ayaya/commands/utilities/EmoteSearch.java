package ayaya.commands.utilities;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;

import static ayaya.core.enums.CommandCategories.UTILITIES;

public class EmoteSearch extends Command {

    private static final int DESCRIPTION_LIMIT = 4096;

    public EmoteSearch() {
        this.name = "emotesearch";
        this.help = "You can view nitro only emotes with this command." +
                " This might show either 0 or more emotes depending on your search.";
        this.arguments = "{prefix}emotesearch <emote name>";
        this.category = UTILITIES.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
        this.isGuildOnly = false;
    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String emoteName = event.getArgs().toLowerCase();
        if (emoteName.isEmpty())
            event.reply("As far as I know, there are no emotes with an empty name.");

        StringBuilder s = new StringBuilder();
        event.getJDA().getEmoteCache().forEach(emote -> {
            if (emote.getName().toLowerCase().contains(emoteName)) {
                String emoteMention = emote.getAsMention();
                if (s.length() + emoteMention.length() + 1 <= DESCRIPTION_LIMIT)
                    s.append(emote.getAsMention()).append(' ');
            }
        });

        String emoteList = s.toString().trim();
        if (emoteList.isEmpty()) {
            event.reply("I can't find any emotes by that name. Most likely the emote you're looking for isn't in a server I am currently in.");
        }
        else {
            User user = event.getAuthor();
            EmbedBuilder emoteEmbed = new EmbedBuilder().setTitle("Emotes Found").setDescription(emoteList)
                    .setFooter(String.format("Requested by %s", user.getName()), user.getAvatarUrl());
            try {
                emoteEmbed.setColor(event.getGuild().getSelfMember().getColor());
            } catch (IllegalStateException | NullPointerException e) {
                emoteEmbed.setColor(Color.decode("#155FA0"));
            }
            event.reply(emoteEmbed.build());
        }

    }

}