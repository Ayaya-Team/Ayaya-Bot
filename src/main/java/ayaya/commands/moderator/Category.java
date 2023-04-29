package ayaya.commands.moderator;

import ayaya.commands.Command;
import ayaya.core.enums.CommandCategories;
import ayaya.core.enums.PermissionNames;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Class of the category command.
 */
public class Category extends Command {

    private static final int NAME_LENGTH = 100;

    private static final String NEW_NAME = "new-name";
    private static final String ADD_PERMS = "add-permissions";
    private static final String REMOVE_PERMS = "remove-permissions";

    public Category() {

        this.name = "category";
        this.help = "With this command you can create, delete or edit any category in a server.";
        this.arguments = "{prefix}category";
        this.isGuildOnly = true;
        this.category = CommandCategories.MODERATOR.asCategory();
        this.botPerms = new Permission[]{Permission.MANAGE_CHANNEL, Permission.MESSAGE_WRITE};
        this.userPerms = new Permission[]{Permission.MANAGE_CHANNEL};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String message = event.getArgs();
        Matcher matcher = WORD.matcher(message);
        if (!matcher.find()) {
            printHelp(event);
            return;
        } else if (matcher.end() == message.length()) {
            event.replyError("You didn't provide any information for that action.");
            return;
        }
        switch (matcher.group().trim().toLowerCase()) {
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
                createCategory(event, message.substring(matcher.end()).trim());
                break;
            case "delete":
                deleteCategory(event, message.substring(matcher.end()).trim());
                break;
            case "edit":
                editCategory(event, message.substring(matcher.end()).trim());
                break;
            default:
                event.replyError("You were trying to perform an unknown action. You can only create, delete or edit categories.");
        }

    }

    /**
     * Creates a new category.
     *
     * @param event the event that triggered this command
     * @param input the string with the input data for the category
     */
    private void createCategory(CommandEvent event, String input) {

        String args[] = input.split("--");

        String category = "";
        List<Permission> permsToAdd = new ArrayList<>(30);
        List<Permission> permsToRemove = new ArrayList<>(30);

        Matcher matcher;
        boolean hasAll = true;
        for (String arg : args) {
            matcher = WORD.matcher(arg);
            if (matcher.find())
                switch (matcher.group().trim().toLowerCase()) {
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
                        category = arg.trim();
                }
        }

        final boolean success = hasAll;
        event.getGuild().createCategory(category)
                .addPermissionOverride(event.getGuild().getPublicRole(), permsToAdd, permsToRemove)
                .reason("Category creation requested by " + event.getMember().getEffectiveName() + ".")
                .queue(c -> {
                    if (success) event.replySuccess("Category `" + input + "` created with success.");
                    else event.replyWarning(
                            "Category `" + input + "` created with success." +
                                    " Unfortunately, I do not have all the permissions you specified," +
                                    " so consider manually allowing or denying the missing ones."
                    );
                });

    }

    /**
     * Deletes a category.
     *
     * @param event the event that triggered this command
     * @param name  the name of the category to delete
     */
    private void deleteCategory(CommandEvent event, String name) {

        if (name.length() > NAME_LENGTH) {
            event.replyError("The name of the category must not be bigger than " + NAME_LENGTH + " characters.");
            return;
        }

        Member member = event.getMember();
        List<net.dv8tion.jda.api.entities.Category> categories =
                event.getGuild().getCategoriesByName(name, true);

        if (categories.size() == 0) {
            event.replyError("I couldn't find a category with that name. Please make sure you typed it correctly.");
            return;
        }

        net.dv8tion.jda.api.entities.Category category = categories.get(0);

        if (!member.hasPermission(category, Permission.MANAGE_CHANNEL)) {
            event.replyError("You don't have enough permissions to manage this category.");
            return;
        }

        if (!event.getSelfMember().hasPermission(category, Permission.MANAGE_CHANNEL)) {
            event.replyError("Sadly I don't have permission to manage this category.");
            return;
        }

        category.delete()
                .reason("Category deletion requested by " + member.getEffectiveName() + ".")
                .queue(c -> event.replySuccess("Category `" + name + "` deleted with success."));

    }

    /**
     * Edits a category.
     *
     * @param event the event that triggered this command
     * @param input the input data to edit the category
     */
    private void editCategory(CommandEvent event, String input) {

        String args[] = input.split("--");

        String name = "", newName = "";
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

        if (newName.length() > NAME_LENGTH) {
            event.replyError("The new name of the category must not be bigger than " + NAME_LENGTH + " characters.");
            return;
        }
        if (newName.isBlank() && permsToAdd.isEmpty() && permsToRemove.isEmpty()) {
            event.replyError("You did not specify anything to change in this category.");
            return;
        }

        List<net.dv8tion.jda.api.entities.Category> categories =
                event.getGuild().getCategoriesByName(name, true);

        if (categories.size() == 0) {
            event.replyError("I couldn't find a category with that name. Please make sure you typed it correctly.");
            return;
        }

        Member member = event.getMember();
        net.dv8tion.jda.api.entities.Category category = categories.get(0);

        if (!member.hasPermission(category, Permission.MANAGE_CHANNEL)) {
            event.replyError("You don't have enough permissions to manage this category.");
            return;
        }
        if (!event.getSelfMember().hasPermission(category, Permission.MANAGE_CHANNEL)) {
            event.replyError("Sadly I don't have permission to manage this category.");
            return;
        }
        if (name.equals(newName) && permsToAdd.isEmpty() && permsToRemove.isEmpty()) {
            event.replyError("All the changes specified are already present in the category.");
            return;
        }

        category.upsertPermissionOverride(event.getGuild().getPublicRole())
                .setAllow(permsToAdd)
                .setDeny(permsToRemove)
                .reason("Category permissions editing requested by " + member.getEffectiveName() + ".")
                .queue();

        final String categoryName = name;
        final boolean success = hasAll;
        category.getManager().setName(newName)
                .reason("Category editing requested by " + member.getEffectiveName() + ".")
                .queue(c -> {
                    if (success) event.replySuccess("Category `" + categoryName + "` edited with success.");
                    else event.replyWarning(
                            "Category `" + categoryName + "` edited with success." +
                                    " Unfortunately, I do not have all the permissions you specified," +
                                    " so consider manually allowing or denying the missing ones."
                    );
                });

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
                "[Category command help]\n\n" +
                "To create a category:\n" +
                "   " + prefix + "category create [category name] [options]\n\n" +
                "To delete a category:\n   " + prefix + "category delete [category name]\n\n" +
                "To edit a category:\n   " + prefix + "category edit [category name] [options]\n\n" +
                "   --new-name [new name] - include this to change the name of the category;\n\n" +
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
                "Note: don't write the []\n" +
                "For more help join my support server. Use the link in " + prefix + "support```");
    }

}