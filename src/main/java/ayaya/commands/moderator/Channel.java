package ayaya.commands.moderator;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import ayaya.core.enums.PermissionNames;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.ChannelManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Class of the channel command.
 */
public class Channel extends Command {

    private static final int NAME_LENGTH = 100;
    private static final int TOPIC_LENGTH = 1024;
    private static final int USERLIMIT = 99;
    private static final int MIN_BITRATE = 8;

    private static final String TOPIC = "topic";
    private static final String CATEGORY = "category";
    private static final String SLOWMODE = "slowmode";
    private static final String BITRATE = "bitrate";
    private static final String USER_LIMIT = "user-limit";
    private static final String NSFW = "nsfw";
    private static final String NEW_NAME = "new-name";
    private static final String ADD_PERMS = "add-permissions";
    private static final String REMOVE_PERMS = "remove-permissions";

    public Channel() {

        this.name = "channel";
        this.help = "With this command you can create, delete or edit any kind of channel in a server.";
        this.arguments = "{prefix}channel";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MANAGE_CHANNEL, Permission.MESSAGE_WRITE};
        this.userPerms = new Permission[]{Permission.MANAGE_CHANNEL};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        Matcher matcher = WORD.matcher(message);
        if (message.isEmpty()) {
            printHelp(event);
            return;
        } else if (!matcher.find()) {
            event.replyError("You didn't provide any information for that action.");
            return;
        }
        String action = matcher.group().trim().toLowerCase();
        switch (action) {
            case "permissions":
            case "perms":
                StringBuilder roleHelp = new StringBuilder().append("```less\n[Discord Permissions]\n\n");
                for (PermissionNames pn : PermissionNames.values()) {
                    if (pn.hasShortName())
                        roleHelp.append(pn.getShortName()).append(" - ");
                    roleHelp.append(pn.getName()).append('\n');
                }
                roleHelp.append("\nUse the `_` only for the short names. The lower or upper case does not matter.\n```");
                event.reply(roleHelp.toString());
                return;
            case "create":
                String type = "";
                if (matcher.find())
                    type = matcher.group().trim().toLowerCase();
                else {
                    event.replyError("You have to provide at least a type and a name for the new channel");
                    return;
                }
                if (!matcher.find()) {
                    event.replyError("You have to provide at least a name for the new channel.");
                    return;
                }
                createChannel(
                        event, type, message.substring(action.length() + type.length() + 2).trim()
                );
                break;
            case "delete":
                deleteChannel(event, message.substring(action.length() + 1).trim());
                break;
            case "edit":
                if (!matcher.find() && !matcher.find()) {
                    event.replyError("You didn't say what you wanted to change on the channel or you didn't provide it's current name.");
                    return;
                }
                editChannel(event, message.substring(action.length() + 1).trim());
                break;
            default:
                event.replyError("You were trying to perform an unknown action. You can only create, delete or edit channels.");
        }

    }

    /**
     * Creates a new channel.
     *
     * @param event the event that triggered this command.
     */
    private void createChannel(CommandEvent event, String type, String input) {

        String args[] = input.split("--");

        String channel = "", topic = "", category = "";
        int slowmode = 0, bitrate = 64, userLimit = 0;
        boolean nsfw = false;
        List<Permission> permsToAdd = new ArrayList<>(30);
        List<Permission> permsToRemove = new ArrayList<>(30);

        Matcher matcher;
        boolean hasAll = true;
        for (String arg : args) {
            matcher = WORD.matcher(arg);
            if (matcher.find())
                switch (matcher.group().trim().toLowerCase()) {
                    case TOPIC:
                        topic = arg.substring(matcher.end()).trim();
                        break;
                    case CATEGORY:
                        category = arg.substring(matcher.end()).trim();
                        break;
                    case SLOWMODE:
                        try {
                            slowmode = Integer.parseInt(arg.substring(matcher.end()).trim());
                        } catch (NumberFormatException e) {
                        }
                        break;
                    case BITRATE:
                        try {
                            bitrate = Integer.parseInt(arg.substring(matcher.end()).trim());
                        } catch (NumberFormatException e) {
                        }
                        break;
                    case USER_LIMIT:
                        try {
                            userLimit = Integer.parseInt(arg.substring(matcher.end()).trim());
                        } catch (NumberFormatException e) {
                        }
                        break;
                    case NSFW:
                        nsfw = arg.substring(matcher.end()).trim().equals("yes");
                        break;
                    case ADD_PERMS:
                        for (String permName : arg.substring(matcher.end()).trim().split(" ")) {
                            Permission p = getPermission(permName);
                            if (p != null)
                                if (event.getSelfMember().hasPermission(p)) permsToAdd.add(p);
                                else hasAll = false;
                        }
                        break;
                    case REMOVE_PERMS:
                        for (String permName : arg.substring(matcher.end()).trim().split(" ")) {
                            Permission p = getPermission(permName);
                            if (p != null)
                                if (event.getSelfMember().hasPermission(p)) permsToRemove.add(p);
                                else hasAll = false;
                        }
                        break;
                    default:
                        channel = arg.trim();
                }
        }

        if (channel.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't provide any name for the new channel.");
            return;
        }
        if (channel.length() > NAME_LENGTH) {
            event.replyError("The name of the channel must not be bigger than " + NAME_LENGTH + " characters.");
            return;
        }
        if (category.length() > NAME_LENGTH) {
            event.replyError("The name of the category must not be bigger than " + NAME_LENGTH + " characters.");
            return;
        }

        Guild guild = event.getGuild();
        final String channelName = channel;
        final String categoryName = category;
        final boolean success = hasAll;
        switch (type) {
            case "text":
                if (topic.length() > TOPIC_LENGTH) {
                    event.replyError("The topic length must not be bigger than " + TOPIC_LENGTH + " characters.");
                    return;
                }
                slowmode = Math.max(slowmode, 0);
                slowmode = Math.min(slowmode, TextChannel.MAX_SLOWMODE);

                event.getGuild().createTextChannel(channel.toLowerCase().replaceAll(" ", "_"))
                        .setTopic(topic)
                        .setNSFW(nsfw)
                        .setSlowmode(slowmode)
                        .addPermissionOverride(event.getGuild().getPublicRole(), permsToAdd, permsToRemove)
                        .reason("Channel creation requested by " + event.getMember().getEffectiveName() + ".")
                        .queue(c -> {
                            addChannelToCategory(c, categoryName, event.getMember().getEffectiveName());
                            if (success) event.replySuccess("Channel `" + channelName + "` created with success.");
                            else event.replyWarning(
                                    "Channel `" + channelName + "` created with success." +
                                            " Unfortunately, I do not have all the permissions you specified," +
                                            " so consider manually allowing or denying the missing ones."
                            );
                        });
                break;
            case "voice":
                userLimit = Math.max(userLimit, 0);
                userLimit = Math.min(userLimit, USERLIMIT);
                int bitrateLimit = guild.getMaxBitrate() / 1000;
                bitrate = Math.max(bitrate, MIN_BITRATE);
                bitrate = Math.min(bitrate, bitrateLimit);
                guild.createVoiceChannel(channel).setUserlimit(userLimit)
                        .setBitrate(bitrate * 1000)
                        .addPermissionOverride(event.getGuild().getPublicRole(), permsToAdd, permsToRemove)
                        .reason("Channel creation requested by " + event.getMember().getEffectiveName() + ".")
                        .queue(c -> {
                            addChannelToCategory(c, categoryName, event.getMember().getEffectiveName());
                            if (success) event.replySuccess("Channel `" + channelName + "` created with success.");
                            else event.replyWarning(
                                    "Channel `" + channelName + "` created with success." +
                                            " Unfortunately, I do not have all the permissions you specified," +
                                            " so consider manually allowing or denying the missing ones."
                            );
                        });
                break;
            default:
                event.replyError("Unknown channel type.");
        }

    }

    /**
     * Deletes a channel.
     *
     * @param event the event that triggered this command.
     */
    private void deleteChannel(CommandEvent event, String input) {

        Member member = event.getMember();
        List<net.dv8tion.jda.api.entities.GuildChannel> channels = event.getGuild().getChannels(true);
        net.dv8tion.jda.api.entities.GuildChannel channel = null;

        for (net.dv8tion.jda.api.entities.GuildChannel c : channels) {
            if (c.getName().equals(input) && !(c instanceof net.dv8tion.jda.api.entities.Category)) {
                channel = c;
                break;
            }
        }

        if (channel == null) {
            event.replyError("I couldn't find a channel with that name." +
                    " Please make sure you typed it correctly.");
            return;
        }

        if (!member.hasPermission(channel, Permission.MANAGE_CHANNEL)) {
            event.replyError("You don't have enough permissions to manage this channel.");
            return;
        }

        if (!event.getSelfMember().hasPermission(channel, Permission.MANAGE_CHANNEL)) {
            event.replyError("Sadly I don't have permission to manage this channel.");
            return;
        }

        channel.delete()
                .reason("Channel deletion requested by " + member.getEffectiveName() + ".")
                .queue(c -> event.replySuccess("Channel `" + input + "` deleted with success."));

    }

    /**
     * Edits a channel.
     *
     * @param event the event that triggered this command.
     */
    private void editChannel(CommandEvent event, String input) {

        Member member = event.getMember();
        String args[] = input.split("--");

        String name = "", newName = "", topic = "", categoryName = "", nsfw = "";
        int slowmode = -1, bitrate = -1, userLimit = -1;
        List<Permission> permsToAdd = new ArrayList<>(30);
        List<Permission> permsToRemove = new ArrayList<>(30);

        Matcher matcher;
        boolean hasAll = true;
        for (String arg : args) {
            matcher = WORD.matcher(arg);
            if (matcher.find())
                switch (matcher.group().trim().toLowerCase()) {
                    case NEW_NAME:
                        newName = arg.substring(matcher.end()).trim();
                        break;
                    case TOPIC:
                        topic = arg.substring(matcher.end()).trim();
                        break;
                    case CATEGORY:
                        categoryName = arg.substring(matcher.end()).trim();
                        break;
                    case SLOWMODE:
                        try {
                            slowmode = Integer.parseInt(arg.substring(matcher.end()).trim());
                        } catch (NumberFormatException e) {
                            slowmode = -1;
                        }
                        break;
                    case BITRATE:
                        try {
                            bitrate = Integer.parseInt(arg.substring(matcher.end()).trim());
                        } catch (NumberFormatException e) {
                            bitrate = -1;
                        }
                        break;
                    case USER_LIMIT:
                        try {
                            userLimit = Integer.parseInt(arg.substring(matcher.end()).trim());
                        } catch (NumberFormatException e) {
                            userLimit = -1;
                        }
                        break;
                    case NSFW:
                        nsfw = arg.substring(matcher.end()).trim();
                        break;
                    case ADD_PERMS:
                        for (String permName : arg.substring(matcher.end()).trim().split(" ")) {
                            Permission p = getPermission(permName);
                            if (p != null)
                                if (event.getSelfMember().hasPermission(p)) permsToAdd.add(p);
                                else hasAll = false;
                        }
                        break;
                    case REMOVE_PERMS:
                        for (String permName : arg.substring(matcher.end()).trim().split(" ")) {
                            Permission p = getPermission(permName);
                            if (p != null)
                                if (event.getSelfMember().hasPermission(p)) permsToRemove.add(p);
                                else hasAll = false;
                        }
                        break;
                    default:
                        name = arg.trim();
                }
        }

        if (name.isBlank()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't provide the name of the channel to edit.");
            return;
        }
        if (newName.isBlank() && topic.isBlank() && nsfw.isBlank()
                && slowmode < 0 && bitrate < 0 && userLimit < 0
                && permsToAdd.isEmpty() && permsToRemove.isEmpty() && categoryName.isBlank()) {
            event.replyError("You did not specify anything to change in this channel.");
            return;
        }

        List<net.dv8tion.jda.api.entities.GuildChannel> channels = event.getGuild().getChannels(true);
        net.dv8tion.jda.api.entities.GuildChannel channel = null;

        for (net.dv8tion.jda.api.entities.GuildChannel c : channels) {
            if (c.getName().equals(name) && !(c instanceof net.dv8tion.jda.api.entities.Category)) {
                channel = c;
                break;
            }
        }

        if (channel == null) {
            event.replyError("I couldn't find a channel with that name. Please make sure you typed it correctly.");
            return;
        }

        if (!member.hasPermission(channel, Permission.MANAGE_CHANNEL)) {
            event.replyError("You don't have enough permissions to manage this channel.");
            return;
        }

        if (!event.getSelfMember().hasPermission(channel, Permission.MANAGE_CHANNEL)) {
            event.replyError("Sadly I don't have permission to manage this channel.");
            return;
        }

        if (newName.length() > NAME_LENGTH) {
            event.replyError("The new name of the channel must not be bigger than " + NAME_LENGTH + " characters.");
            return;
        }
        if (categoryName.length() > NAME_LENGTH) {
            event.replyError("The name of the category must not be bigger than " + NAME_LENGTH + " characters.");
            return;
        }

        ChannelManager manager = channel.getManager();

        if (channel instanceof TextChannel) {
            if (topic.length() > TOPIC_LENGTH) {
                event.replyError("The topic length must not be bigger than " + TOPIC_LENGTH + " characters.");
                return;
            }
            TextChannel textChannel = (TextChannel) channel;
            if (name.equals(newName) && topic.equals(textChannel.getTopic())
                    && nsfw.equals("yes") == textChannel.isNSFW() && slowmode == textChannel.getSlowmode()
                    && permsToAdd.isEmpty() && permsToRemove.isEmpty()) {
                if (channel.getParent() != null && categoryName.equals(channel.getParent().getName())) {
                    event.replyError("All the changes specified are already present in the channel.");
                    return;
                }
            } else {
                if (!newName.isEmpty())
                    manager = manager.setName(newName.toLowerCase().replaceAll(" ", "_"));
                if (!topic.isEmpty())
                    manager = manager.setTopic(topic);
                if (!nsfw.isEmpty())
                    manager = manager.setNSFW(nsfw.equals("yes"));
                if (slowmode > -1)
                    manager = manager.setSlowmode(slowmode);
            }
        } else {
            VoiceChannel voiceChannel = (VoiceChannel) channel;
            if (name.equals(newName) && bitrate == voiceChannel.getBitrate()
                    && userLimit == voiceChannel.getUserLimit()
                    && permsToAdd.isEmpty() && permsToRemove.isEmpty()) {
                if (channel.getParent() != null && categoryName.equals(channel.getParent().getName())) {
                    event.replyError("All the changes specified are already present in the channel.");
                    return;
                }
            } else {
                if (!newName.isEmpty())
                    manager = manager.setName(newName);
                if (bitrate > -1)
                    manager = manager.setBitrate(bitrate * 1000);
                if (userLimit > -1)
                    manager = manager.setUserLimit(userLimit);
            }
        }
        if (!categoryName.isEmpty())
            addChannelToCategory(channel, categoryName, event.getMember().getEffectiveName());

        channel.upsertPermissionOverride(event.getGuild().getPublicRole())
                .setAllow(permsToAdd)
                .setDeny(permsToRemove)
                .reason("Channel permissions editing requested by " + member.getEffectiveName() + ".")
                .queue();
        final String channelName = name;
        final boolean success = hasAll;
        manager
                .reason("Channel editing requested by " + member.getEffectiveName() + ".")
                .queue(c -> {
                    if (success) event.replySuccess("Channel `" + channelName + "` edited with success.");
                    else event.replyWarning(
                            "Channel `" + channelName + "` edited with success." +
                                    " Unfortunately, I do not have all the permissions you specified," +
                                    " so consider manually allowing or denying the missing ones."
                    );
                });

    }

    /**
     * Creates a new category with a given name and adds a channel to that category.
     *
     * @param channel       the channel to add
     * @param category_name the name of the category to create
     * @param username      the username of who made the request
     */
    private void addChannelToCategory(GuildChannel channel,
                                      String category_name,
                                      String username) {
        Guild guild = channel.getGuild();
        if (category_name.length() > 0) {
            List<net.dv8tion.jda.api.entities.Category> categories =
                    guild.getCategoriesByName(category_name, false);
            if (categories.size() == 0) {
                guild.createCategory(category_name)
                        .reason("Category creation requested by " + username + ".")
                        .queue(c -> channel.getManager().setParent(c).queue());
            } else {
                channel.getManager().setParent(categories.get(0)).queue();
            }
        }
    }

    private Permission getPermission(String s) {
        s = getPermissionName(s);
        Permission p = null;
        for (Permission p1 : Permission.values())
            if (p1.getName().toLowerCase().equals(s.replace('_', ' ').toLowerCase())) {
                p = p1;
                break;
            }
        return p;
    }

    private String getPermissionName(String s) {
        String name = s;
        for (PermissionNames pn : PermissionNames.values())
            if (pn.getShortName().equals(s.toLowerCase()))
                name = pn.getName();
        return name;
    }

    /**
     * Sends a message with the help message of this command.
     *
     * @param event the event that triggered this command.
     */
    private void printHelp(CommandEvent event) {
        String prefix = event.getClient().getPrefix();
        event.reply("```css\n" +
                "[Channel command help]\n\n" +
                "To create a channel:\n" +
                "   " + prefix + "channel create text/voice [channel name] [options]" +
                " - remember that text channels have no spaces or capital letters\n\n" +
                "To delete a channel:\n   " + prefix + "channel delete [channel name]\n\n" +
                "To edit a channel:\n   " + prefix + "channel edit [channel name] [options]\n\n" +
                "Options:\n" +
                "   --new-name [new name] - include this to change the name of the channel;\n\n" +
                "   --topic [channel topic] - include this to set the topic of a text channel;\n\n" +
                "   --slowmode [amount] - include this to set the slowmode of a text channel;\n\n" +
                "   --nsfw [yes/no] - include this if you want to make a text channel an nsfw channel or not;\n\n" +
                "   --bitrate [amount in kbps] - include this if you want to set the bitrate of a voice channel;\n\n" +
                "   --user_limit [amount] - include this if you want to set the user limit of a voice channel," +
                " setting it to 0 will set it to infinite;\n\n" +
                "   --category [category name]" +
                " - include thus if you want to add the channel to a category, if you just" +
                " include --category without any argument for the name when editing," +
                " then the channel will be out of any" +
                " categories of the server. If the category does not exist, it will be created;\n\n" +
                "   --add-permissions [the permissions names]" +
                " - include this in the command, when editing a channel," +
                " to set the permissions to allow to the everyone role in the channel," +
                "  to see the permission keywords use "
                + prefix + "channel permissions or " + prefix + "channel perms;\n\n" +
                "   --remove-permissions [the permissions names]" +
                " - include this in the command, when editing a channel," +
                " to set the permissions to restrict from the everyone role in the channel," +
                "  to see the permission keywords use "
                + prefix + "channel permissions or " + prefix + "channel perms.\n\n" +
                "You don't need to always set these options or to always change a channel's name.\n\n" +
                "Note: don't write the []\n" +
                "For more help join my support server. Use the link in " + prefix + "support```");
    }

}