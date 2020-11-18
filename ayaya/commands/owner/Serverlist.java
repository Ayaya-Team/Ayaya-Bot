package ayaya.commands.owner;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static ayaya.core.enums.CommandCategories.OWNER;

/**
 * Modified by Aya Komichi#7541
 *
 * @author John Grosh (jagrosh)
 */
public class Serverlist extends Command {

    private final Paginator.Builder pbuilder;

    public Serverlist(EventWaiter waiter) {
        this.name = "serverlist";
        this.category = OWNER.asCategory();
        this.arguments = "[pagenum]";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION};
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
        pbuilder = new Paginator.Builder().setColumns(1)
                .setItemsPerPage(15)
                .showPageNumbers(true)
                .waitOnSinglePage(false)
                .useNumberedItems(false)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException | IllegalStateException ex) {
                    }
                })
                .setEventWaiter(waiter)
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        int page = 1;
        if (!event.getArgs().isEmpty()) {
            try {
                page = Integer.parseInt(event.getArgs());
            } catch (IllegalStateException | NumberFormatException e) {
                event.replyError("That's not a valid number.");
                return;
            }
        }
        List<Guild> guilds = event.getJDA().getGuilds();
        for (Guild guild: guilds)
            guild.retrieveOwner(true).queue();
        pbuilder.clearItems();
        guilds.stream()
                .map(g -> "**" + g.getName() + "** `" + g.getId() + "`: " + g.getMembers().size()
                        + " members\nOwner: " + Objects.requireNonNull(g.getOwner()).getUser().getName() + "#" + g.getOwner().getUser().getDiscriminator()
                        + " `" + g.getOwner().getUser().getId() + "`")
                .forEach(pbuilder::addItems);
        event.getJDA().getShardInfo();
        Paginator p = pbuilder
                .setColor(event.isFromType(ChannelType.TEXT) ? event.getSelfMember().getColor() : Color.decode("#155FA0"))
                .setText(event.getClient().getSuccess() + " Guilds I am in at this moment"
                        + ("(Shard ID " + event.getJDA().getShardInfo().getShardId() + "):"))
                .setUsers(event.getAuthor())
                .build();
        p.paginate(event.getChannel(), page);

    }

}