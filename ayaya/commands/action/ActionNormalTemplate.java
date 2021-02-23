package ayaya.commands.action;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.regex.Matcher;

/**
 * Template based on the ActionCompleteTemplate but with less customization options.
 */
public class ActionNormalTemplate extends ActionBasicTemplate {

    protected String selfDescription, selfFooter, ayayaDescription;

    public ActionNormalTemplate(String name, String help, String arguments, String[] aliases, String description,
                                  String footer, String self_description, String self_footer, String ayaya_description)
    {

        super(name, help, arguments, aliases, description, footer);
        this.selfDescription = self_description;
        this.selfFooter = self_footer;
        this.ayayaDescription = ayaya_description;

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
                    if (ayayaDescription != null && !ayayaDescription.isEmpty())
                        event.reply(ayayaDescription);
                    return;
                } else if (mentioned == author) {
                    if (selfDescription != null && !selfDescription.isEmpty())
                        embed.setDescription(String.format(selfDescription, author.getEffectiveName()));
                    if (selfFooter != null && !selfFooter.isEmpty())
                        embed.setFooter(String.format(selfFooter, author.getEffectiveName()), null);
                } else {
                    if (description != null && !description.isEmpty())
                        embed.setDescription(String.format(description, author.getEffectiveName(),
                                mentioned.getEffectiveName()));
                    if (footer != null && !footer.isEmpty())
                        embed.setFooter(String.format(footer, mentioned.getEffectiveName()), null);
                }
                prepareEmbedAndSend(embed, event.getTextChannel());
            }, t -> event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention in this server."));
        } else {
            if (selfDescription != null && !selfDescription.isEmpty())
                embed.setDescription(String.format(selfDescription, author.getEffectiveName()));
            if (selfFooter != null && !selfFooter.isEmpty())
                embed.setFooter(String.format(selfFooter, author.getEffectiveName()), null);
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
        String url = getRandomGif();
        if (url.equals(NULL)) {
            channel.sendMessage(
                    "There was a problem while connecting with the database. If this persists then try again later."
            ).queue();
            return;
        }
        embed.setImage(url);
        channel.sendMessage(embed.build()).queue();
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
                    if (ayayaDescription != null && !ayayaDescription.isEmpty())
                        event.reply(ayayaDescription);
                    return;
                } else if (mentioned == author) {
                    if (selfDescription != null && !selfDescription.isEmpty())
                        embed.setDescription(String.format(selfDescription, author.getName()));
                    if (selfFooter != null && !selfFooter.isEmpty())
                        embed.setFooter(String.format(selfFooter, author.getName()), null);
                } else {
                    if (description != null && !description.isEmpty())
                        embed.setDescription(String.format(description, author.getName(),
                                mentioned.getName()));
                    if (footer != null && !footer.isEmpty())
                        embed.setFooter(String.format(footer, author.getName()), null);
                }
                prepareEmbedAndSend(embed, event.getPrivateChannel());
            }, t -> event.reply("<:AyaWhat:362990028915474432> I couldn't find anyone with that mention here."));
        } else {
            if (selfDescription != null && !selfDescription.isEmpty())
                embed.setDescription(String.format(selfDescription, author.getName()));
            if (selfFooter != null && !selfFooter.isEmpty())
                embed.setFooter(String.format(selfFooter, author.getName()), null);
            prepareEmbedAndSend(embed, event.getPrivateChannel());
        }

    }

    /**
     * Finishes the embed and sends it.
     *
     * @param embed   the embed to finish
     * @param channel the channel to send the embed to
     */
    private void prepareEmbedAndSend(EmbedBuilder embed, PrivateChannel channel) {
        embed.setColor(Color.decode("#155FA0"));
        String url = getRandomGif();
        if (url.equals(NULL)) {
            channel.sendMessage(
                    "There was a problem while connecting with the database. If this persists then try again later."
            ).queue();
            return;
        }
        embed.setImage(url);
        channel.sendMessage(embed.build()).queue();
    }

}