package ayaya.commands;

import ayaya.core.BotData;
import ayaya.core.exceptions.general.NullValueException;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * <h1><b>Commands for Ayaya</b></h1>
 *
 * <p>Class made specifically for Ayaya commands.</p>
 * <p>Some of the code was taken from the Command class of the JDA Utilities and changed to meet my requirements.</p>
 * <p>The author of the original code is John Grosh (jagrosh)</p>
 *
 * @author Ayaya#7541
 */
public class Command extends com.jagrosh.jdautilities.command.Command {

    //Regex patterns.
    protected static final Pattern ARG = Pattern.compile("(?:([^\\p{Blank}])+([\\p{Blank}])?)");
    protected static final Pattern WORD = Pattern.compile("(?:([^\\p{Blank}\\p{Digit}])+([\\p{Blank}])?)");
    protected static final Pattern ID = Pattern.compile("([\\p{Digit}])++");
    protected static final Pattern ANY_ID = Pattern.compile("([\\p{Digit}])+");
    protected static final Pattern USER_MENTION = Pattern.compile("(<@!?([\\p{Digit}])+>){1}+");
    protected static final Pattern CHANNEL_MENTION = Pattern.compile("(<@#([\\p{Digit}])+>){1}+");
    protected static final Pattern ROLE_MENTION = Pattern.compile("(<@&([\\p{Digit}])+>){1}+");

    protected static final String DATE_PATTERN = "d-MM-yyyy";
    protected static final String INFINITE = "f";

    protected static final Permission[] ALL_TEXT_PERMISSIONS = {
            Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE, Permission.MESSAGE_TTS,
            Permission.MESSAGE_MANAGE, Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY,
            Permission.MESSAGE_MENTION_EVERYONE
    };
    protected static final List<Message.MentionType> ALLOWED_MENTIONS = Arrays.asList(
            Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.USER
    );

    protected int cooldownTime;
    protected Permission[] botPerms;
    protected Permission[] userPerms;
    protected boolean isOwnerCommand;
    protected boolean isGuildOnly;
    protected boolean isDisabled;
    protected boolean isPaginated;
    protected Paginator.Builder pbuilder;

    public Command() {

        this.cooldownTime = 2;
        botPerms = new Permission[]{};
        userPerms = new Permission[]{};
        this.ownerCommand = false;
        this.guildOnly = false;
        this.isGuildOnly = false;
        this.isDisabled = false;
        this.isPaginated = false;

    }

    public void initPaginator(EventWaiter eventWaiter) {

        if (pbuilder == null) {
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
                    .setEventWaiter(eventWaiter)
                    .setTimeout(1, TimeUnit.MINUTES);
        }

    }

    /**
     * The main body method of a {@link com.jagrosh.jdautilities.command.Command Command}.
     * <br>This is the "response" for a successful
     * {@link com.jagrosh.jdautilities.command.Command#run(CommandEvent) #run(CommandEvent)}. Modified to wrap any
     * NullpointerException into a NullValueException.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command
     */
    @Override
    protected void execute(CommandEvent event) {

        if (isBlocked(event.getAuthor().getId()))
            return;

        if (isGuildOnly && !(event.getChannel() instanceof TextChannel)) {

            event.reply("This command can only be used in a server.");
            return;

        }

        if (event.getChannelType() == ChannelType.TEXT) {

            if (isDisabled)
                return;

            if (isOwnerCommand && !event.isOwner()) {

                if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                    event.replyError("You do not own the required permission to execute this command.");
                return;

            }

            for (Permission p : botPerms) {

                if (p.isVoice()) {

                    if (p.getName().startsWith("VOICE")) {

                        GuildVoiceState vs = event.getMember().getVoiceState();
                        if (vs == null || vs.getChannel() == null) {

                            if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                                event.replyError("You must be in a voice channel to use that.");
                            return;

                        } else if (!event.getSelfMember().hasPermission(vs.getChannel(), p)) {

                            if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                                event.replyError("I need the permission **" + p.getName()
                                        + "** in the channel " + vs.getChannel().getName() + " to execute this command.");
                            return;

                        }

                    } else if (!event.getSelfMember().hasPermission(event.getTextChannel(), p)) {

                        if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                            event.replyError("I need the permission **" + p.getName() + "** to execute this command in this channel.");
                        return;

                    }

                } else {

                    if (p.isChannel()) {

                        if (!event.getSelfMember().hasPermission(event.getTextChannel(), p)) {

                            if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                                event.replyError("I need the permission **" + p.getName() + "** in this channel to use this command.");
                            return;

                        }

                    } else {

                        if(!event.getSelfMember().hasPermission(p)) {

                            if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                                event.replyError("I need the permission **" + p.getName() + "** to execute this command.");
                            return;

                        }

                    }

                }

            }

            for (Permission p : userPerms) {

                if (p.isChannel()) {

                    if (!event.getMember().hasPermission(event.getTextChannel(), p)) {

                        if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                            event.replyError("You need the permission **" + p.getName() + "** in this channel to use this command.");
                        return;

                    }

                } else {

                    if (!event.getMember().hasPermission(p)) {

                        if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE))
                            event.replyError("You need the permission **" + p.getName() + "** to use this command.");
                        return;

                    }

                }

            }

        } else if (isOwnerCommand && !event.isOwner()) {

            event.replyError("You do not own the required permission to execute this command.");
            return;

        }

        if (cooldownTime > 0) {
            String key = getCooldownKey(event);
            int remaining = event.getClient().getRemainingCooldown(key);
            if (remaining > 0) {
                String error = getCooldownError(event, remaining);
                if (error != null) {
                    return;
                }
            } else event.getClient().applyCooldown(key, cooldownTime);
        }

        try {

            event.getClient().getScheduleExecutor().submit(() -> executeInstructions(event));

        } catch (NullPointerException e) {

            NullValueException exception = new NullValueException();
            exception.setStackTrace(e.getStackTrace());
            throw exception;

        }

    }

    /**
     * Main body of a Command.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command
     */
    protected void executeInstructions(CommandEvent event) {
    }

    @Override
    public Permission[] getUserPermissions() {
        return userPerms;
    }

    @Override
    public Permission[] getBotPermissions() {
        return botPerms;
    }

    @Override
    public boolean isGuildOnly() {
        return isGuildOnly;
    }

    @Override
    public boolean isOwnerCommand() {
        return isOwnerCommand;
    }

    @Override
    public int getCooldown() {
        return cooldownTime;
    }

    /**
     * Returns if this command is disabled or not
     *
     * @return true or false
     */
    public boolean isDisabled() {
        return isDisabled;
    }

    /**
     * Enables this command.
     */
    public void enable() {
        isDisabled = false;
    }

    /**
     * Disables this command.
     */
    public void disable() {
        isDisabled = true;
    }

    /**
     * Returns if this is a paginated command or not
     *
     * @return true or false
     */
    public boolean isPaginated() {
        return isPaginated;
    }

    /**
     * Checks in the database if the user that triggered the command is blacklisted or not. If the verification fails
     * the command will be always executed.
     *
     * @param id the id of the user
     * @return true if the user is blacklisted, false if it's not
     */
    private boolean isBlocked(String id) {
        boolean blocked;
        SQLController jdbc = new SQLController();
        try {
            jdbc.open(BotData.getDBConnection(), BotData.getDBUser(), BotData.getDbPassword());
            Serializable[] o = new Serializable[]{id};
            ResultSet result = jdbc.sqlSelect("SELECT * FROM blacklist WHERE user_id = ?;", o, 5);
            blocked = result.next();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            blocked = false;
        } finally {
            try {
                jdbc.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return blocked;
    }

    /**
     * Checks if the user is the bot's owner or a co-owner. The owner and co-owners have access to all commands without
     * exceptions.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command
     * @return true if the user is the owner or a co-owner, false if not
     */
    protected boolean isOwner(CommandEvent event) {
        String id = event.getAuthor().getId();
        if (id.equals(event.getClient().getOwnerId())) {
            return true;
        }
        for (String co_owner : event.getClient().getCoOwnerIds())
            if (id.equals(co_owner)) {
                return true;
            }
        return false;
    }

}