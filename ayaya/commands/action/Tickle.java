package ayaya.commands.action;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.List;

import static ayaya.core.enums.ActionQuotes.NORMAL_TICKLE;
import static ayaya.core.enums.ActionQuotes.SELF_TICKLE;
import static ayaya.core.enums.ActionQuotes.AYAYA_TICKLE;

/**
 * Class of the tickle command.
 */
public class Tickle extends ActionBasicTemplate {

    protected String self_description, self_footer, ayaya_description, everyone_description,
            everyone_footer;

    public Tickle() {

        super("tickle", "Oh, I see. You like to tickle people.", "{prefix}tickle <@user>",
                new String[]{}, NORMAL_TICKLE.getQuote(), NORMAL_TICKLE.getFooter());
        this.self_description = SELF_TICKLE.getQuote();
        this.self_footer = SELF_TICKLE.getFooter();
        this.ayaya_description = AYAYA_TICKLE.getQuote();
        this.everyone_description = SELF_TICKLE.getQuote();
        this.everyone_footer = SELF_TICKLE.getFooter();

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
                event.reply(ayaya_description);
                return;
            } else if (mentioned == author) {
                embed.setDescription(String.format(self_description, author.getEffectiveName()));
                embed.setFooter(self_footer, null);
            } else {
                embed.setDescription(String.format(description, author.getEffectiveName(),
                        mentioned.getEffectiveName()));
                embed.setFooter(String.format(footer, mentioned.getEffectiveName()), null);
            }
        } else {
            embed.setDescription(String.format(everyone_description, author.getEffectiveName()));
            embed.setFooter(everyone_footer, null);
        }
        try {
            embed.setColor(guild.getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            embed.setColor(Color.decode("#155FA0"));
        }
        embed.setImage(getRandomGif());
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
                event.reply(ayaya_description);
                return;
            } else if (mentioned == author) {
                embed.setDescription(String.format(self_description, author.getName()));
                embed.setFooter(self_footer, null);
            } else {
                embed.setDescription(String.format(description, author.getName(),
                        mentioned.getName()));
                embed.setFooter(footer, null);
            }
        } else {
            embed.setDescription(String.format(everyone_description, author.getName()));
            embed.setFooter(everyone_footer, null);
        }
        embed.setColor(Color.decode("#155FA0"));
        embed.setImage(getRandomGif());
        event.reply(embed.build());

    }

}