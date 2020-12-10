package ayaya.commands;

import ayaya.core.exceptions.general.NullValueException;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private String link;
    protected int cooldownTime;
    protected Permission[] botPerms;
    protected Permission[] userPerms;
    protected boolean isOwnerCommand;
    protected boolean isGuildOnly;
    protected boolean isPremium;

    public Command() {

        botPerms = new Permission[]{};
        userPerms = new Permission[]{};
        this.ownerCommand = false;
        this.guildOnly = false;
        this.isGuildOnly = false;
        this.isPremium = false;
        this.cooldownTime = 2;

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

        if (isOwnerCommand && !event.isOwner()) {

            event.replyError("You do not own the required permission to execute this command.");
            return;

        }

        if (isGuildOnly && !(event.getChannel() instanceof TextChannel)) {

            event.reply("This command can only be used in a server.");
            return;

        }

        if (event.getChannelType() == ChannelType.TEXT) {

            for (Permission p : botPerms) {

                if (p.isVoice()) {

                    if (p.getName().startsWith("VOICE")) {

                        GuildVoiceState vs = event.getMember().getVoiceState();
                        if (vs == null || vs.getChannel() == null) {

                            event.replyError("You must be in a voice channel to use that.");
                            return;

                        } else if (!event.getSelfMember().hasPermission(vs.getChannel(), p)) {

                            event.replyError("I need the permission **" + p.getName()
                                    + "** in the channel " + vs.getChannel().getName() + " to execute this command.");
                            return;

                        }

                    } else if (!event.getSelfMember().hasPermission(event.getTextChannel(), p)) {

                        event.replyError("I need the permission **" + p.getName() + "** to execute this command in this channel.");
                        return;

                    }

                } else {

                    if(!event.getSelfMember().hasPermission(p)) {

                        event.replyError("I need the permission **" + p.getName() + "** to execute this command.");
                        return;

                    }

                }

            }

            for (Permission p : userPerms) {

                if (p.isChannel()) {

                    if (!event.getMember().hasPermission(event.getTextChannel(), p)) {

                        event.replyError("You need the permission **" + p.getName() + "** in this channel to use this command.");
                        return;

                    }

                } else {

                    if (!event.getMember().hasPermission(p)) {

                        event.replyError("You need the permission **" + p.getName() + "** to use this command.");
                        return;

                    }

                }

            }

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

            if (!isPremium || isPremium(event))
                executeInstructions(event);
            else
                event.replyError("You need to be premium to use this command.");

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

    /**
     * Returns if this command is premium or not
     *
     * @return true or false
     */
    public boolean isPremium() {
        return isPremium;
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
            jdbc.open("jdbc:sqlite:data.db");
            ResultSet result = jdbc.sqlSelect("SELECT * FROM blacklist WHERE user_id = '" + id + "';", 5);
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
     * Checks wether a user is or isn't a patron. If the verification fails, the command will never be executed.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command
     * @return true if the user that triggered the command is a patron, false if not
     */
    private boolean isPremium(CommandEvent event) {
        return getPremiumExpirationDate(event) != null;
    }

    /**
     * Retrieves the string with the expiration date for user who triggered the command event. In case the user isn't on
     * the whitelist or is no longer premium, returns null.
     *
     * @param event the command event
     * @return (possibly null) string
     */
    protected String getPremiumExpirationDate(CommandEvent event) {
        if (isOwner(event))
            return INFINITE;

        String answer = null;
        String id = event.getAuthor().getId();
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            ResultSet resultSet = jdbc
                    .sqlSelect("SELECT * FROM patreon_whitelist WHERE user_id = " + id + ";", 5);
            if (resultSet.next()) {
                String result = resultSet.getString("expiration_date");
                if (result.equals(INFINITE))
                    answer = INFINITE;
                else {
                    LocalDate date = LocalDate.parse(result, DateTimeFormatter.ofPattern(DATE_PATTERN));
                    LocalDate now = LocalDate.now();
                    int compare = date.compareTo(now);
                    if (compare < 0) {
                        Serializable[] o = {id};
                        jdbc.sqlInsertUpdateOrDelete(
                                "DELETE FROM patreon_whitelist WHERE user_id = ?;", o, 5
                        );
                    } else answer = result;
                }
            }
        } catch (SQLException e) {
            event.replyError("There was a problem while checking wether you are or aren't a premium. If this error persists, try again later.");
            e.printStackTrace();
        } finally {
            try {
                jdbc.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return answer;
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

    /**
     * Retrieves the Patreon page link for the case a non patron user attempts to trigger a PremiumCommand.
     */
    private void getPatreonLink() {
        SQLController jdbc = new SQLController();
        try {
            jdbc.open("jdbc:sqlite:data.db");
            link = jdbc.sqlSelect("SELECT * FROM `settings` WHERE `option` LIKE 'donate';", 5)
                    .getString("value");
        } catch (SQLException e) {
            System.out.println(
                    "A problem occurred while trying to get necessary information for the " + this.name
                            + " command! Aborting the read process...");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                jdbc.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

}