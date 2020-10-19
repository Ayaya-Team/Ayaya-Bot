package ayaya.commands.utilities;

import ayaya.commands.Command;
import ayaya.core.utils.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.regex.Matcher;

import static ayaya.core.enums.CommandCategories.UTILITIES;

/**
 * Class of the avatar command.
 */
public class Avatar extends Command {

    public Avatar() {

        this.name = "avatar";
        this.help = "A command to an image and it's link of the avatar of someone. It works with yours too!";
        this.arguments = "{prefix}avatar <mention, name/nickname or id>\n\nIf you don't mention anyone, I will just get your avatar.";
        this.category = UTILITIES.asCategory();
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.isGuildOnly = false;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String content = event.getArgs();
        EmbedBuilder avatar_embed = new EmbedBuilder();
        Matcher mentionFinder = Message.MentionType.USER.getPattern().matcher(content);
        Matcher idFinder;
        if (mentionFinder.find()) {
            idFinder = ANY_ID.matcher(mentionFinder.group());
            idFinder.find();
            event.getJDA().retrieveUserById(idFinder.group(), true).queue(
                    user -> {
                        if (user == event.getSelfUser())
                            avatar_embed.setTitle("My avatar!");
                        else
                            avatar_embed.setTitle(user.getName() + "'s avatar!");
                        displayAvatar(event, avatar_embed, user);
                    },
                    t -> event.replyError("I couldn't find anyone with that mention." +
                            " Please, make sure you typed it correctly.")
            );
        } else {
            Guild guild;
            if (!content.isEmpty() && (guild = event.getGuild()) != null) {
                if (content.equals(event.getSelfMember().getEffectiveName()) ||
                        content.equals(event.getSelfUser().getName())) {
                    avatar_embed.setTitle("My avatar!");
                    displayAvatar(event, avatar_embed, event.getSelfUser());
                } else if (content.equals(event.getMember().getEffectiveName()) ||
                        content.equals(event.getAuthor().getName())) {
                    avatar_embed.setTitle("Your avatar!");
                    displayAvatar(event, avatar_embed, event.getAuthor());
                } else {
                    guild.retrieveMembersByPrefix(content, 1).onSuccess(l -> {
                        if (l.isEmpty()) {
                            if (!Utils.isLong(content)) {
                                event.replyError("I couldn't find anyone with that name/nickname or id." +
                                        " Please, make sure you typed it correctly.");
                                return;
                            }
                            guild.retrieveMemberById(content, true).queue(m -> {
                                if (m != null) {
                                    avatar_embed.setTitle(m.getEffectiveName() + "'s avatar!");
                                    displayAvatar(event, avatar_embed, m.getUser());
                                } else {
                                    event.replyError("I couldn't find anyone with that name/nickname or id." +
                                            " Please, make sure you typed it correctly.");
                                }
                            }, t -> event.replyError("I couldn't find anyone with that name/nickname or id." +
                                    " Please, make sure you typed it correctly."));
                        } else {
                            avatar_embed.setTitle(l.get(0).getEffectiveName() + "'s avatar!");
                            displayAvatar(event, avatar_embed, l.get(0).getUser());
                        }
                    });
                }
            } else {
                avatar_embed.setTitle("Your avatar!");
                displayAvatar(event, avatar_embed, event.getAuthor());
            }
        }

    }

    private void displayAvatar(CommandEvent event, EmbedBuilder avatar_embed, User user) {
        String url = user.getEffectiveAvatarUrl().replace(".webp", ".png")
                .replace("?size=1024", "?size=512");
        if (url.endsWith(".png")) url = url.replace(".png", ".png?size=512");
        avatar_embed.setDescription(
                "[PNG](" + url + ") | [JPG](" + url.replace(".png", ".jpg") + ")"
        );
        avatar_embed.setImage(url);
        try {
            avatar_embed.setColor(event.getMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            avatar_embed.setColor(Color.decode("#155FA0"));
        }
        avatar_embed.setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl());
        event.reply(avatar_embed.build());
    }

}