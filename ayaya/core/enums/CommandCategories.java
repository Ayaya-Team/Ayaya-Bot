package ayaya.core.enums;

import ayaya.commands.ListCategory;
import com.jagrosh.jdautilities.command.Command;

/**
 * The command categories
 */
public enum CommandCategories {

    MUSIC("Music"), ACTION("Action"), FUNNY("Funny"), INFORMATION("Information"),
    UTILITIES("Utilities"), MODERATOR("Moderator"), ADMINISTRATOR("Administrator"),
    OWNER("Owner");

    public static final String OWNER_CATEGORY ="Owner";

    private Command.Category category;
    private ListCategory listCategory;

    CommandCategories(String name) {
        this.category = new Command.Category(name);
        listCategory = new ListCategory(category.getName());
    }

    public Command.Category asCategory() {return category;}

    public ListCategory asListCategory() {return listCategory;}

    /**
     * Fetches the list category by name.
     *
     * @param name list category's name
     * @return list category
     */
    public static ListCategory getListCategory(String name) {
        for (CommandCategories c : CommandCategories.values())
            if (c.asCategory().getName().equals(name))
                return c.asListCategory();
        return null;
    }

}