package ayaya.commands.action;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.List;

/**
 * Template based on the ActionCompleteTemplate but with less customization options.
 */
public class ActionNormalTemplate extends ActionBasicTemplate {

    protected String self_description, self_footer, ayaya_description;

    public ActionNormalTemplate(String name, String help, String arguments, String[] aliases, String description,
                                  String footer, String self_description, String self_footer, String ayaya_description)
    {

        super(name, help, arguments, aliases, description, footer);
        this.self_description = self_description;
        this.self_footer = self_footer;
        this.ayaya_description = ayaya_description;

    }

    @Override
    protected void executeInGuild(CommandEvent event) {

        Guild guild = event.getGuild();
        Member author = event.getMember();
        Member mentioned;
        List<IMentionable> mentions = event.getMessage().getMentions(Message.MentionType.USER);
        EmbedBuilder embed = new EmbedBuilder();
        if (!mentions.isEmpty()) {
            mentioned = guild.retrieveMemberById(mentions.get(0).getId()).complete();
            if (mentioned == null)
                event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention in this server.");
            else if (mentioned == event.getSelfMember()) {
                if (ayaya_description != null && !ayaya_description.isEmpty())
                    event.reply(ayaya_description);
                return;
            } else if (mentioned == author) {
                if (self_description != null && !self_description.isEmpty())
                    embed.setDescription(String.format(self_description, author.getEffectiveName()));
                if (self_footer != null && !self_footer.isEmpty())
                    embed.setFooter(String.format(self_footer, author.getEffectiveName()), null);
            } else {
                if (description != null && !description.isEmpty())
                    embed.setDescription(String.format(description, author.getEffectiveName(),
                            mentioned.getEffectiveName()));
                if (footer != null && !footer.isEmpty())
                    embed.setFooter(String.format(footer, mentioned.getEffectiveName()), null);
            }
        } else {
            if (self_description != null && !self_description.isEmpty())
                embed.setDescription(String.format(self_description, author.getEffectiveName()));
            if (self_footer != null && !self_footer.isEmpty())
                embed.setFooter(String.format(self_footer, author.getEffectiveName()), null);
        }
        try {
            embed.setColor(guild.getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            embed.setColor(Color.decode("#155FA0"));
        }
        String url = getRandomGif();
        if (url.equals(NULL)) {
            event.reply("There was a problem while connecting with the database. If this persists then try again later.");
            return;
        }
        embed.setImage(url);
        event.reply(embed.build());

    }


    @Override
    protected void executeInDMS(CommandEvent event) {

        User author = event.getAuthor();
        User mentioned;
        List<User> users_list = event.getMessage().getMentionedUsers();
        EmbedBuilder embed = new EmbedBuilder();
        if (users_list.size() > 0) {
            mentioned = users_list.get(0);
            if (mentioned == event.getSelfUser()) {
                if (ayaya_description != null && !ayaya_description.isEmpty())
                    event.reply(ayaya_description);
                return;
            } else if (mentioned == author) {
                if (self_description != null && !self_description.isEmpty())
                    embed.setDescription(String.format(self_description, author.getName()));
                if (self_footer != null && !self_footer.isEmpty())
                    embed.setFooter(String.format(self_footer, author.getName()), null);
            } else {
                if (description != null && !description.isEmpty())
                    embed.setDescription(String.format(description, author.getName(),
                            mentioned.getName()));
                if (footer != null && !footer.isEmpty())
                    embed.setFooter(String.format(footer, author.getName()), null);
            }
        } else {
            if (self_description != null && !self_description.isEmpty())
                embed.setDescription(String.format(self_description, author.getName()));
            if (self_footer != null && !self_footer.isEmpty())
                embed.setFooter(String.format(self_footer, author.getName()), null);
        }
        embed.setColor(Color.decode("#155FA0"));
        String url = getRandomGif();
        if (url.equals(NULL)) {
            event.reply("There was a problem while connecting with the database. If this persists then try again later.");
            return;
        }
        embed.setImage(url);
        event.reply(embed.build());

    }

}