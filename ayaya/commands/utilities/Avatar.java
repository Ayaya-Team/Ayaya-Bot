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

    private static final int MAX_SIZE = 2048;
    private static final int MIN_SIZE = 512;

    private static final String JPG = ".jpg";
    private static final String PNG = ".png";
    private static final String WEBP = ".webp";
    private static final String GIF = ".gif";

    public Avatar() {

        this.name = "avatar";
        this.help = "A command to show an image and it's link of the avatar of someone. It works with yours too!";
        this.arguments = "{prefix}avatar <mention, name/nickname or id>\n\nIf you don't mention anyone, I will just get your avatar.";
        this.category = UTILITIES.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
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

    /**
     * Displays the avatar of a user in an embed.
     *
     * @param event       the event that triggered the command
     * @param avatarEmbed the embed to use
     * @param user        the user
     */
    private void displayAvatar(CommandEvent event, EmbedBuilder avatarEmbed, User user) {
        String urls[] = Utils.getAvatarUrls(user, MIN_SIZE, MAX_SIZE);
        String originalURl = urls[0];
        String url = urls[1];
        String displayUrl = urls[2];

        String originalJPG, originalPNG, originalWEBP, originalGIF;
        String jpg, png, webp, gif;
        if (url.contains(JPG)) {
            jpg = url;
            png = url.replace(JPG, PNG);
            webp = url.replace(JPG, WEBP);
            gif = "";

            originalJPG = originalURl;
            originalPNG = originalURl.replace(JPG, PNG);
            originalWEBP = originalURl.replace(JPG, WEBP);
            originalGIF = "";
        }
        else if (url.contains(PNG)) {
            jpg = url.replace(PNG, JPG);
            png = url;
            webp = url.replace(PNG, WEBP);
            gif = "";

            originalJPG = originalURl.replace(PNG, JPG);
            originalPNG = originalURl;
            originalWEBP = originalURl.replace(PNG, WEBP);
            originalGIF = "";
        }
        else if (url.contains(WEBP)) {
            jpg = url.replace(WEBP, JPG);
            png = url.replace(WEBP, PNG);
            webp = url;
            gif = "";

            originalJPG = originalURl.replace(WEBP, JPG);
            originalPNG = originalURl.replace(WEBP, PNG);
            originalWEBP = originalURl;
            originalGIF = "";
        }
        else {
            jpg = url.replace(GIF, JPG);
            png = url.replace(GIF, PNG);
            webp = url.replace(GIF, WEBP);
            gif = url;

            originalJPG = originalURl.replace(GIF, JPG);
            originalPNG = originalURl.replace(GIF, PNG);
            originalWEBP = originalURl.replace(GIF, WEBP);
            originalGIF = originalURl;
        }

        avatarEmbed.setDescription(
                "**Original:** " + (originalGIF.isEmpty() ? "" : "[GIF](" + originalGIF + ") | ")
                        + "[PNG](" + originalPNG + ") | [JPG](" + originalJPG + ") | [WEBP](" + originalWEBP + ")" +
                "\n**2048px:** " + (gif.isEmpty() ? "" : "[GIF](" + gif + ") | ")
                        + "[PNG](" + png + ") | [JPG](" + jpg + ") | [WEBP](" + webp + ")"
        );
        avatarEmbed.setImage(displayUrl);

        try {
            avatarEmbed.setColor(event.getMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            avatarEmbed.setColor(Color.decode("#155FA0"));
        }

        avatarEmbed.setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl());
        event.reply(avatarEmbed.build());
    }

}