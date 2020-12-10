package ayaya.commands.moderator;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import ayaya.core.enums.PermissionNames;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Class of the role command.
 */
public class Role extends Command {

    private static final int NAME_LENGTH = 100;

    private static final String COLOR = "color";
    private static final String HOISTED = "hoisted";
    private static final String DISPLAYED_SEPARATELY = "displayed-separately";
    private static final String MENTIONABLE = "mentionable";
    private static final String PERMS = "permissions";
    private static final String NEW_NAME = "new-name";
    private static final String ADD_PERMS = "add-permissions";
    private static final String REMOVE_PERMS = "remove-permissions";
    private static final String ADD_ROLES = "add-roles";
    private static final String REMOVE_ROLES = "remove-roles";

    public Role() {

        this.name = "role";
        this.help = "With this command you can create, delete or edit any role in a server.";
        this.arguments = "{prefix}role";
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.userPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.isGuildOnly = true;

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        if (message.isEmpty()) {
            printHelp(event);
            return;
        }
        Matcher matcher = WORD.matcher(message);
        if (matcher.find()) {
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
                    if (matcher.find())
                        createRole(event, message.substring(action.length() + 1).trim());
                    else event.replyError("You didn't provide any information for that action.");
                    break;
                case "delete":
                    if (matcher.find())
                        deleteRole(event, message.substring(action.length() + 1).trim());
                    else event.replyError("You didn't provide any information for that action.");
                    break;
                case "edit":
                    if (matcher.find())
                        editRole(event, message.substring(action.length() + 1).trim());
                    else event.replyError("You didn't provide any information for that action.");
                    break;
                case "manage":
                    if (matcher.find())
                        manageRoles(event, message.substring(action.length() + 1).trim());
                    else event.replyError("You didn't provide any information for that action.");
                    break;
                default:
                    event.replyError("You were trying to perform an unknown action." +
                            " You can only create, delete or edit roles.");
            }
        } else printHelp(event);
    }

    /**
     * Creates a new role.
     *
     * @param event the event that triggered this command
     */
    private void createRole(CommandEvent event, String input) {

        String args[] = input.split("--");

        String name = "", colorCode = "";
        boolean hoisted = false, mentionable = false;
        List<Permission> permissions = new ArrayList<>(30);

        Matcher matcher;
        boolean hasAll = true;
        for (String arg : args) {
            matcher = WORD.matcher(arg);
            if (matcher.find())
                switch (matcher.group().trim().toLowerCase()) {
                    case COLOR:
                        colorCode = arg.substring(matcher.end()).trim();
                        break;
                    case HOISTED:
                    case DISPLAYED_SEPARATELY:
                        hoisted = arg.substring(matcher.end()).trim().equals("yes");
                        break;
                    case MENTIONABLE:
                        mentionable = arg.substring(matcher.end()).trim().equals("yes");
                        break;
                    case PERMS:
                        for (String permName : arg.substring(matcher.end()).trim().split(" ")) {
                            Permission p = getPermission(permName);
                            if (p != null)
                                if (event.getSelfMember().hasPermission(p)) permissions.add(p);
                                else hasAll = false;
                        }
                        break;
                    default:
                        name = arg.trim();
                }
        }

        if (name.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't provide any name for the new role.");
            return;
        }
        if (name.length() > NAME_LENGTH) {
            event.replyError("The name of the role must not be bigger than " + NAME_LENGTH + " characters.");
            return;
        }

        RoleAction roleCreation = event.getGuild().createRole();

        if (!colorCode.isBlank()) {
            if (!colorCode.startsWith("#"))
                colorCode = "#" + colorCode;
            roleCreation = roleCreation.setColor(Color.decode(colorCode));
        }
        roleCreation = roleCreation.setHoisted(hoisted);
        roleCreation = roleCreation.setMentionable(mentionable);
        if (permissions.size() == 0) {
            for (Permission p : ALL_TEXT_PERMISSIONS) {
                if (event.getSelfMember().hasPermission(p))
                    permissions.add(p);
            }
        }
        roleCreation = roleCreation.setPermissions(permissions);

        final String roleName = name;
        final boolean success = hasAll;
        roleCreation.setName(name)
                .reason("Role creation requested by " + event.getMember().getEffectiveName() + ".")
                .queue(r -> {
                    if (success) event.replySuccess("Role `" + roleName + "` created with success.");
                    else event.replyWarning(
                            "Role `" + roleName + "` created with success." +
                                    " Unfortunately, I do not have all the permissions you specified," +
                                    " so consider manually assigning the missing ones."
                    );
                });

    }

    /**
     * Deletes a role.
     *
     * @param event the event that triggered this command
     */
    private void deleteRole(CommandEvent event, String name) {

        List<net.dv8tion.jda.api.entities.Role> roles = event.getGuild().getRolesByName(name, false);
        if (roles.isEmpty()) {
            event.replyError("I can't find that role. Make sure you typed its name correctly.");
            return;
        }
        net.dv8tion.jda.api.entities.Role role = roles.get(0);

        switch (ableToManageRole(role, event.getMember(), event.getSelfMember())) {
            case -1:
                event.replyError("You don't have enough permissions to manage this role.");
                return;
            case 0:
                event.replyError("I don't have enough permissions to manage this role.");
                return;
            default:
        }
        if (role.isPublicRole()) {
            event.replyError("The public role cannot be deleted.");
            return;
        }
        if (role.isManaged()) {
            event.replyError("This role cannot be deleted because it's managed by an integration.");
            return;
        }

        try {
            role.delete()
                    .reason("Role deletion requested by " + event.getMember().getEffectiveName() + ".")
                    .queue(r -> event.replySuccess("Role `" + name + "` deleted with success."));
        } catch (HierarchyException e) {
            event.replyError("Sadly I don't have enough permissions to delete this role.");
        }

    }

    /**
     * Edits a role.
     *
     * @param event the event that triggered this command
     */
    private void editRole(CommandEvent event, String input) {

        String args[] = input.split("--");

        String name = "", newName = "", colorCode = "", hoisted = "", mentionable = "";
        List<Permission> permsToAdd = new ArrayList<>(30);
        List<Permission> permsToRemove = new ArrayList<>(30);

        Matcher matcher;
        boolean hasAll = true;
        for (String arg : args) {
            matcher = WORD.matcher(arg);
            if (matcher.find())
                switch (matcher.group().trim().toLowerCase()) {
                    case COLOR:
                        colorCode = arg.substring(matcher.end()).trim();
                        break;
                    case HOISTED:
                    case DISPLAYED_SEPARATELY:
                        hoisted = arg.substring(matcher.end()).trim();
                        break;
                    case MENTIONABLE:
                        mentionable = arg.substring(matcher.end()).trim();
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
                    case NEW_NAME:
                        newName = arg.substring(matcher.end()).trim();
                        break;
                    default:
                        name = arg.trim();
                }
        }

        if (name.isBlank()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't provide the name of the role to edit.");
            return;
        }
        if (newName.isBlank() && colorCode.isBlank() && hoisted.isBlank() && mentionable.isBlank()
                && permsToAdd.isEmpty() && permsToRemove.isEmpty()) {
            event.replyError("You did not specify anything to change in this role.");
            return;
        }
        if (newName.length() > NAME_LENGTH) {
            event.replyError("The new name of the role must not be bigger than " + NAME_LENGTH + " characters.");
            return;
        }

        List<net.dv8tion.jda.api.entities.Role> roles = event.getGuild().getRolesByName(name, false);
        if (roles.isEmpty()) {
            event.replyError("I can't find that role. Make sure you typed its name correctly.");
            return;
        }

        net.dv8tion.jda.api.entities.Role role = roles.get(0);
        switch (ableToManageRole(role, event.getMember(), event.getSelfMember())) {
            case -1:
                event.replyError("You don't have enough permissions to manage this role.");
                return;
            case 0:
                event.replyError("I don't have enough permissions to manage this role.");
                return;
            default:
        }

        if (!colorCode.startsWith("#"))
            colorCode = "#" + colorCode;
        Color color = Color.decode(colorCode);
        if (name.equals(newName) && (role.getColor() == null || role.getColor().equals(color))
                && role.isHoisted() == hoisted.equals("yes") && role.isMentionable() == mentionable.equals("yes")
                && permsToAdd.isEmpty() && permsToRemove.isEmpty()) {
            event.replyError("All the changes specified are already present in the role.");
            return;
        }

        try {
            RoleManager manager = role.getManager();
            if (role.isPublicRole())
                event.reply("Note: The name of this role could not be changed because this is the public role.");
            else manager = manager.setName(newName);
            manager = manager.setColor(color)
                    .setHoisted(hoisted.equals("yes"))
                    .setMentionable(mentionable.equals("yes"))
                    .givePermissions(permsToAdd)
                    .revokePermissions(permsToRemove);

            final String roleName = name;
            final boolean success = hasAll;
            manager
                    .reason("Role changes requested by " + event.getMember().getEffectiveName() + ".")
                    .queue(r -> {
                        if (success) event.replySuccess("Role `" + roleName + "` edited with success.");
                        else event.replyWarning("Role `" + roleName + "` edited with success." +
                                " Unfortunately, I do not have all the permissions you specified," +
                                " so consider manually adding/removing the ones that weren't changed.");
                    });
        } catch (HierarchyException e) {
            event.replyError("Sadly I don't have enough permissions to edit this role.");
        }

    }

    /**
     * Returns a discord permission from a given string.
     *
     * @param s the string with the permission name
     * @return the permission
     */
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

    /**
     * Returns the name of a permission from a string.
     *
     * @param s the string with the permission name
     * @return the permission name
     */
    private String getPermissionName(String s) {
        String name = s;
        for (PermissionNames pn : PermissionNames.values())
            if (pn.getShortName().equals(s.toLowerCase()))
                name = pn.getName();
        return name;
    }

    /**
     * Manages roles. This is meant to add and/or remove roles from members.
     *
     * @param event the command event that triggered this
     * @param input the data input string
     */
    private void manageRoles(CommandEvent event, String input) {

        String[] args = input.split("--");

        String users = "", toAdd = "", toRemove = "";
        Matcher matcher;
        for (String arg : args) {
            matcher = WORD.matcher(arg);
            if (matcher.find()) {
                switch (matcher.group().trim().toLowerCase()) {
                    case ADD_ROLES:
                        toAdd = arg.substring(matcher.end()).trim();
                        break;
                    case REMOVE_ROLES:
                        toRemove = arg.substring(matcher.end()).trim();
                        break;
                    default:
                        users = arg.trim();
                }
            }
        }

        if (users.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't provide any users." +
                    " How can I know who I should add or remove roles from?");
            return;
        }
        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            event.reply("<:AyaWhat:362990028915474432> You didn't specify any roles to add or remove.");
            return;
        }

        Guild guild = event.getGuild();
        Member author = event.getMember();
        List<net.dv8tion.jda.api.entities.Role> roles;
        List<net.dv8tion.jda.api.entities.Role> rolesToAdd = new ArrayList<>(20);
        List<net.dv8tion.jda.api.entities.Role> rolesToRemove = new ArrayList<>(20);
        Matcher mentionFinder, idFinder;

        String rolesToAddInput[] = toAdd.split(",");
        String rolesToRemoveInput[] = toRemove.split(",");

        net.dv8tion.jda.api.entities.Role role = null;
        for (String s : rolesToAddInput) {
            s = s.trim();
            idFinder = ID.matcher(s);
            if (!idFinder.find()) {
                roles = guild.getRolesByName(s, false);
                if (!roles.isEmpty()) role = roles.get(0);
            } else {
                role = guild.getRoleById(s);
            }
            if (role != null && ableToManageRole(role, author, event.getSelfMember()) == 0) {
                rolesToAdd.add(role);
            }
        }
        for (String s : rolesToRemoveInput) {
            s = s.trim();
            idFinder = ID.matcher(s);
            if (!idFinder.find()) {
                roles = guild.getRolesByName(s, false);
                if (!roles.isEmpty()) role = roles.get(0);
            } else {
                role = guild.getRoleById(s);
            }
            if (role != null && ableToManageRole(role, author, event.getSelfMember()) == 0) {
                rolesToRemove.add(role);
            }
        }

        if (rolesToAdd.isEmpty() && rolesToRemove.isEmpty()) {
            event.replyError(
                    "Either I couldn't find any of the roles in the server," +
                            " or I don't have permission to manage them or you don't have permission to manage them."
            );
            return;
        }

        Member member;
        mentionFinder = Message.MentionType.USER.getPattern().matcher(users);
        while (mentionFinder.find()) {
            idFinder = ANY_ID.matcher(mentionFinder.group());
            idFinder.find();
            final String id = idFinder.group();
            guild.retrieveMemberById(id).queue(m -> {
                if (m != null)
                    manageRolesForMember(m, author, guild, rolesToAdd, rolesToRemove);
            });
        }
        for (String s : users.split(",")) {
            s = s.trim();
            mentionFinder = USER_MENTION.matcher(s);
            if (!mentionFinder.find()) {
                final String arg = s;
                guild.retrieveMembersByPrefix(s, 1).onSuccess(l -> {
                    if (l.isEmpty()) {
                        guild.retrieveMemberById(arg, true).queue(m -> {
                            if (m != null)
                                manageRolesForMember(m, author, guild, rolesToAdd, rolesToRemove);
                        }, t -> {
                        });
                    } else
                        manageRolesForMember(l.get(0), author, guild, rolesToAdd, rolesToRemove);
                }).onError(t -> {
                });
            }
        }

        StringBuilder answer = new StringBuilder().append("I ");
        if (!rolesToAdd.isEmpty())
            answer.append("added ").append(rolesToAdd.size()).append(" roles");
        if (!rolesToRemove.isEmpty()) {
            if (!rolesToAdd.isEmpty())
                answer.append(" and ");
            answer.append("removed ").append(rolesToRemove.size()).append(" roles from ");
        } else
            answer.append(" to ");
        answer.append("all the members mentioned I could find.");
        if (rolesToAdd.size() < rolesToAddInput.length && rolesToRemove.size() < rolesToRemoveInput.length)
            answer.append(" Couldn't add/remove all the roles due to me or you having a lack of permissions" +
                    " or the roles not existing.");
        event.replySuccess(answer.toString());

    }

    /**
     * Method to check if a given role can be managed.
     *
     * @param role       the role to check for
     * @param member     the member user that triggered the command
     * @param selfMember the member bot representing this bot
     * @return 1 if the role can be managed, 0 if the bot doesn't have enough permissions, -1 if the user doesn't have
     * enough permissions
     */
    private int ableToManageRole(
            net.dv8tion.jda.api.entities.Role role, Member member, Member selfMember
    ) {
        if (role.getPosition() >= selfMember.getRoles().get(0).getPosition()) {
            return 0;
        }
        int author_top_role_position = 0;
        List<net.dv8tion.jda.api.entities.Role> roles = member.getRoles();
        if (!roles.isEmpty())
            author_top_role_position = roles.get(0).getPosition();
        if (!member.isOwner() && role.getPosition() >= author_top_role_position) {
            return -1;
        }
        return 1;
    }

    /**
     * Adds and removes roles from a guild member.
     *
     * @param member        the target member
     * @param author        the author of the triggered command
     * @param guild         the guild where the command was triggered
     * @param rolesToAdd    the list of roles to add
     * @param rolesToRemove the list of roles to remove
     */
    private void manageRolesForMember(
            Member member, Member author, Guild guild,
            List<net.dv8tion.jda.api.entities.Role> rolesToAdd,
            List<net.dv8tion.jda.api.entities.Role> rolesToRemove
    ) {
        for (net.dv8tion.jda.api.entities.Role roleToAdd : rolesToAdd) {
            guild.addRoleToMember(member, roleToAdd)
                    .reason("Role assignment to user requested by "
                            + author.getEffectiveName() + ".")
                    .queue();
        }
        for (net.dv8tion.jda.api.entities.Role roleToRemove : rolesToRemove) {
            guild.removeRoleFromMember(member, roleToRemove)
                    .reason("Role unassignment from user requested by "
                            + author.getEffectiveName() + ".")
                    .queue();
        }
    }

    /**
     * Sends a message with the help message of this command.
     *
     * @param event the event that triggered this command
     */
    private void printHelp(CommandEvent event) {
        String prefix = event.getClient().getPrefix();
        event.reply("```css\n[Role Command Usage]\n\n" +
                "To create a role:\n" +
                "   " + prefix + "role create [role name] [options]\n\n" +
                "To delete a role:\n" +
                "   " + prefix + "role delete [role name]\n\n" +
                "To edit a role:\n" +
                "   " + prefix + "role edit [role name] [options]\n\n" +
                "To add or remove a role from members:\n" +
                "   " + prefix + "role manage" +
                " [user names/nicknames or mentions or ids separated by commas] [options]\n\n" +
                "Options:\n" +
                "   --new-name [new name] - include this to change the name of the role;\n\n" +
                "   --color [hex color code] - include this in the command to set a role's color;\n\n" +
                "   --hoisted [yes/no] or --displayed-separately" +
                " - include this in the command to set if the role members should" +
                " or should not to be displayed separately from others;\n\n" +
                "   --mentionable [yes/no] - include this in the command to set the role to be" +
                " or not to be mentionable;\n\n" +
                "   --permissions [the permissions names]" +
                " - include this in the command, when creating the role, to set custom permissions for a role," +
                " to see the permission keywords use "
                + prefix + "role permissions or " + prefix + "role perms;\n\n" +
                "   --add-permissions [the permissions names]" +
                " - include this in the command, when editing a role," +
                " to set the custom permissions to add to the role," +
                " to see the permission keywords use "
                + prefix + "role permissions or " + prefix + "role perms;\n\n" +
                "   --remove-permissions [the permissions names]" +
                " - include this in the command, when editing a role," +
                " to set the custom permissions to remove from the role," +
                " to see the permission keywords use "
                + prefix + "role permissions or " + prefix + "role perms;\n\n" +
                "   --add-roles [role names/ids separated by commas]" +
                " - include this in the command when managing roles to add roles to members;\n\n" +
                "   --remove-roles [role names/ids separated by commas]" +
                " - include this in the command when managing roles to remove roles from members.\n\n" +
                "You don't need to always set these options or to always change a role's name.\n\n" +
                "Note: don't write the [] in your commands.\n" +
                "For more help join my support server. Use the link in " + prefix + "support.```");
    }

}