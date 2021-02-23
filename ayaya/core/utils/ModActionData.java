package ayaya.core.utils;

/**
 * Class to store data of a moderation action.
 */
public class ModActionData {

    private int successes;
    private boolean notFound;
    private boolean lackingPerms;
    private boolean redundantAction;
    private boolean leftGuild;
    private boolean exception;

    public ModActionData() {
        successes = 0;
        notFound = false;
        lackingPerms = false;
        redundantAction = false;
        leftGuild = false;
        exception = false;
    }

    /**
     * Returns the amount of successes.
     *
     * @return amount of successes
     */
    public synchronized int getSuccesses() {
        return successes;
    }

    /**
     * Increments the amount of successes.
     */
    public synchronized void incrementSuccesses() {
        successes++;
    }

    /**
     * Returns the not found flag.
     *
     * @return not found flag
     */
    public synchronized boolean getNotFound() {
        return notFound;
    }

    /**
     * Sets the not found flag to true
     */
    public synchronized void putNotFound() {
        notFound = true;
    }

    /**
     * Returns the lacking perms flag.
     *
     * @return lacking perms flag
     */
    public synchronized boolean getLackingPerms() {
        return lackingPerms;
    }

    /**
     * Sets the lacking perms flag to true.
     */
    public synchronized void putLackingPerms() {
        lackingPerms = true;
    }

    /**
     * Returns the redundant action flag.
     *
     * @return redundant action flag
     */
    public synchronized boolean getRedundantAction() {
        return redundantAction;
    }

    /**
     * Sets the redundant action flag to true.
     */
    public synchronized void putRedundantAction() {
        redundantAction = true;
    }

    /**
     * Returns the left guild flag.
     *
     * @return left guild flag
     */
    public synchronized boolean getLeftGuild() {
        return leftGuild;
    }

    /**
     * Sets the left guild flag to true.
     */
    public synchronized void putLeftGuild() {
        leftGuild = true;
    }

    /**
     * Returns the exception flag.
     *
     * @return exception flag
     */
    public synchronized boolean hasException() {
        return exception;
    }

    /**
     * Sets the exception flag to true.
     */
    public synchronized void putException() {
        exception = true;
    }

}