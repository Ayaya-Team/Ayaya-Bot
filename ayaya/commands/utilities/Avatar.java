package ayaya.commands.utilities;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.List;

import static ayaya.core.enums.CommandCategories.UTILITIES;

/**
 * Class of the avatar command.
 */
public class Avatar extends Command {

    public Avatar() {

        this.name = "avatar";
        this.help = "A command to an image and it's link of the avatar of someone. It works with yours too!";
        this.arguments = "{prefix}avatar <@user or name>\n\nIf you don't mention anyone, I will just get your avatar.";
        this.category = UTILITIES.asCategory();
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.isGuildOnly = false;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String name = event.getArgs();
        EmbedBuilder avatar_embed = new EmbedBuilder();
        User user;
        if (event.getMessage().getMentionedUsers().size() > 0) {
            if (event.getMessage().getMentionedUsers().get(0) == event.getSelfUser()) {
                avatar_embed.setTitle("My avatar!");
                user = event.getSelfUser();
            } else {
                user = event.getMessage().getMentionedUsers().get(0);
                avatar_embed.setTitle(user.getName() + "'s avatar!");
            }
        } else {
            if (!name.isEmpty() && event.getGuild() != null) {
                if (name.equals(event.getSelfMember().getEffectiveName()) ||
                        name.equals(event.getSelfUser().getName())) {
                    avatar_embed.setTitle("My avatar!");
                    user = event.getSelfUser();
                } else {
                    List<Member> members = event.getGuild().getMembersByEffectiveName(name, false);
                    if (members.isEmpty()) {
                        members = event.getGuild().getMembersByName(name, false);
                    }
                    if (members.isEmpty()) {
                        event.replyError("I couldn't find anyone with that name or nickname. Please, make sure you typed it correctly.");
                        return;
                    }
                    user = members.get(0).getUser();
                    avatar_embed.setTitle(user.getName() + "'s avatar!");
                }
            } else {
                user = event.getAuthor();
                avatar_embed.setTitle("Your avatar!");
            }
        }
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