package ayaya.commands.information;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import static ayaya.core.enums.CommandCategories.INFORMATION;

/**
 * Class of the roleinfo command.
 */
public class Roleinfo extends Command {

    private static final int FIELD_LIMIT = 1024;

    public Roleinfo() {

        this.name = "roleinfo";
        this.help = "When you want to know more about a specific role you can use this command.";
        this.arguments =
                "{prefix}roleinfo <role's name>\n\nThe name must be equal to the one of the role you wish to check, " +
                        "but the upper case and lower case don't matter.";
        this.category = INFORMATION.asCategory();
        this.isGuildOnly = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String requested_role = event.getArgs();
        if (!requested_role.isEmpty()) {
            EmbedBuilder roleinfo_embed = new EmbedBuilder();
            List<Role> roles = event.getGuild().getRolesByName(requested_role, true);
            if (roles.isEmpty()) {
                event.reply(":x: I couldn't find a role with that name. " +
                        "Please, check if you typed the role's name exactly as it is displayed. " +
                        "Be aware that the role search isn't case sensitive.");
                return;
            }
            Role role = roles.get(0);
            OffsetDateTime creationTime = role.getTimeCreated();
            String creation_week_day = creationTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
            String hoist = "No", managed = "No", mentionable = "No";
            if (role.isHoisted()) hoist = "Yes";
            if (role.isManaged()) managed = "Yes";
            if (role.isMentionable()) mentionable = "Yes";
            StringBuilder perms_list = new StringBuilder();
            for (Permission permission : role.getPermissions()) {
                String s = permission.getName();
                if (perms_list.length() + s.length() + 4 > FIELD_LIMIT)
                    break;
                if (perms_list.length() == 0) perms_list.append('`').append(s).append('`');
                else perms_list.append(", `").append(s).append("`");
            }
            String permissions = perms_list.toString();
            if (role.getColor() == null) {
                roleinfo_embed.addField("Color", "None", true);
            } else {
                roleinfo_embed.addField(
                        "Color", "#" + Integer.toHexString(role.getColor().getRGB() & 0xffffff),
                        true
                )
                        .setColor(role.getColor());
            }
            roleinfo_embed.setTitle(role.getName())
                    .addField("Displayed Separately", hoist, true)
                    .addField("Mentionable", mentionable, true)
                    .addField("Is Bot Role", managed, true)
                    .addField(
                            "Created on",
                            String.format("%s, %02d/%02d/%02d at %02d:%02d:%02d",
                                    creation_week_day, creationTime.getDayOfMonth(), creationTime.getMonthValue(),
                                    creationTime.getYear(), creationTime.getHour(), creationTime.getMinute(),
                                    creationTime.getSecond()),
                            false
                    )
                    .addField("Permissions", permissions, false)
                    .setFooter(
                            String.format("Requested by %s     Role ID: %s", event.getAuthor().getName(), role.getId()),
                            null
                    );
            event.reply(roleinfo_embed.build());
        } else {
            event.reply(":x: Tell me the role you want the info from!");
        }

    }

}