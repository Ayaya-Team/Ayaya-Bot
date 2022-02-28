package ayaya.commands.action;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.regex.Matcher;

import static ayaya.core.enums.ActionQuotes.*;

/**
 * Class of the tickle command.
 */
public class Tickle extends ActionBasicTemplate {

    private String selfDescription, selfFooter, ayayaDescription, everyoneDescription,
            everyoneFooter;

    public Tickle() {

        super("tickle", "Oh, I see. You like to tickle people.", "{prefix}tickle <@user>",
                new String[]{}, NORMAL_TICKLE.getQuote(), NORMAL_TICKLE.getFooter());
        this.selfDescription = SELF_TICKLE.getQuote();
        this.selfFooter = SELF_TICKLE.getFooter();
        this.ayayaDescription = AYAYA_TICKLE.getQuote();
        this.everyoneDescription = SELF_TICKLE.getQuote();
        this.everyoneFooter = SELF_TICKLE.getFooter();

    }

    @Override
    protected void executeInGuild(CommandEvent event) {

        Guild guild = event.getGuild();
        Member author = event.getMember();
        Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(event.getArgs());
        Matcher idFinder;
        EmbedBuilder embed = new EmbedBuilder();
        if (mentionFinder.find()) {
            idFinder = ANY_ID.matcher(mentionFinder.group());
            idFinder.find();
            guild.retrieveMemberById(idFinder.group(), true).queue(mentioned -> {
                if (mentioned == null)
                    event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention in this server.");
                else if (mentioned == event.getSelfMember()) {
                    event.reply(ayayaDescription);
                    return;
                } else if (mentioned == author) {
                    embed.setDescription(String.format(selfDescription, author.getEffectiveName()))
                            .setFooter(selfFooter, null);
                } else {
                    embed.setDescription(String.format(description, author.getEffectiveName(),
                            mentioned.getEffectiveName()))
                            .setFooter(String.format(footer, mentioned.getEffectiveName()), null);
                }
                prepareEmbedAndSend(embed, event.getTextChannel());
            });
        } else {
            embed.setDescription(String.format(everyoneDescription, author.getEffectiveName()))
                    .setFooter(everyoneFooter, null);
            prepareEmbedAndSend(embed, event.getTextChannel());
        }

    }

    /**
     * Finishes the embed and sends it.
     *
     * @param embed   the embed to finish
     * @param channel the channel to send the embed to
     */
    private void prepareEmbedAndSend(EmbedBuilder embed, TextChannel channel) {
        try {
            embed.setColor(channel.getGuild().getSelfMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            embed.setColor(Color.decode("#155FA0"));
        }
        embed.setImage(getRandomGif());
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    protected void executeInDMS(CommandEvent event) {

        User author = event.getAuthor();
        Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(event.getArgs());
        Matcher idFinder;
        EmbedBuilder embed = new EmbedBuilder();
        if (mentionFinder.find()) {
            idFinder = ANY_ID.matcher(mentionFinder.group());
            idFinder.find();
            event.getJDA().retrieveUserById(idFinder.group(), true).queue(mentioned -> {
                if (mentioned == null)
                    event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention here.");
                else if (mentioned == event.getSelfUser()) {
                    event.reply(ayayaDescription);
                    return;
                } else if (mentioned == author) {
                    embed.setDescription(String.format(selfDescription, author.getName()))
                            .setFooter(selfFooter, null);
                } else {
                    embed.setDescription(String.format(description, author.getName(),
                            mentioned.getName()))
                            .setFooter(footer, null);
                }
                embed.setColor(Color.decode("#155FA0"))
                        .setImage(getRandomGif());
                event.reply(embed.build());
            }, t -> event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention here."));
        } else {
            embed.setDescription(String.format(everyoneDescription, author.getName()))
                    .setFooter(everyoneFooter, null);
            embed.setColor(Color.decode("#155FA0"))
                    .setImage(getRandomGif());
            event.reply(embed.build());
        }

    }

}