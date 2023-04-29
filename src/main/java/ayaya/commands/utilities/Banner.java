package ayaya.commands.utilities;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import ayaya.core.utils.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;

public class Banner extends Command {

    private static final int MAX_SIZE = 2048;
    private static final int MIN_SIZE = 512;

    private static final String JPG = ".jpg";
    private static final String PNG = ".png";
    private static final String WEBP = ".webp";
    private static final String GIF = ".gif";

    public Banner() {

        this.name = "banner";
        this.help = "This will give you access to the banner image of a server, if there's one set.";
        this.arguments = "{prefix}banner";
        this.category = CommandCategories.UTILITIES.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
        this.isGuildOnly = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {
        String bannerUrl = event.getGuild().getBannerUrl();
        if (bannerUrl == null) {
            event.reply("This server does not have a banner set.");
        }
        else {
            displayBanner(event);
        }
    }

    /**
     * Displays the banner of a server in an embed with the download links.
     *
     * @param event       the event that triggered the command
     */
    private void displayBanner(CommandEvent event) {
        String originalURl = event.getGuild().getBannerUrl();
        if (originalURl == null)
            return;
        String urls[] = Utils.getUrls(originalURl, MIN_SIZE, MAX_SIZE);
        String url = urls[0];
        String displayUrl = urls[1];

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

        EmbedBuilder bannerEmbed = new EmbedBuilder().setDescription(
                "**Original:** " + (originalGIF.isEmpty() ? "" : "[GIF](" + originalGIF + ") | ")
                        + "[PNG](" + originalPNG + ") | [JPG](" + originalJPG + ") | [WEBP](" + originalWEBP + ")" +
                        "\n**Upscaled:** " + (gif.isEmpty() ? "" : "[GIF](" + gif + ") | ")
                        + "[PNG](" + png + ") | [JPG](" + jpg + ") | [WEBP](" + webp + ")"
        );
        bannerEmbed.setTitle("Server Banner").setImage(displayUrl);

        try {
            bannerEmbed.setColor(event.getMember().getColor());
        } catch (IllegalStateException | NullPointerException e) {
            bannerEmbed.setColor(Color.decode("#155FA0"));
        }

        bannerEmbed.setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl());
        event.reply(bannerEmbed.build());
    }

}