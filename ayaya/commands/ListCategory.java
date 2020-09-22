package ayaya.commands;

import ayaya.core.utils.NameComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a command category as a string list.
 * This is to wrap the weak JDA-Utilities category system with more functionalities.
 */
public class ListCategory {

    private String name;
    private List<String> commands;
    private boolean ordered;

    /**
     * Builds a category with a given name
     *
     * @param name the name of the category
     */
    public ListCategory(String name) {
        this.name = name;
        commands = new ArrayList<>();
        ordered = true;
    }

    /**
     * Gets the category's name.
     *
     * @return category's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of current commands. The list is ordered before sending if needed.
     *
     * @return command list
     */
    public List<String> getCommands() {
        if (!ordered) {
            commands.sort(new NameComparator());
            ordered = true;
        }
        return commands;
    }

    /**
     * Adds a command to the category list and marks the list as not ordered.
     *
     * @param n the command name to add
     */
    public void add(String n) {
        commands.add(n);
        ordered = false;
    }

    /**
     * Removes a command from the category list.
     *
     * @param n the command name to remove
     */
    public void remove(String n) {
        commands.remove(n);
    }

}