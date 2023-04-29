package ayaya.core.utils;

import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store prune action data.
 */
public class PruneActionData {

    private int amount;
    private List<String> users;
    private List<Member> members;
    private boolean bots;
    private String content;

    public PruneActionData() {
        amount = 0;
        users = new ArrayList<>(20);
        members = new ArrayList<>(20);
        bots = false;
        content = "";
    }

    /**
     * Returns the amount of prunes.
     *
     * @return amount of prunes
     */
    public synchronized int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of prunes.
     *
     * @param amount new amount
     */
    public synchronized void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Returns the list of user ids to prune.
     *
     * @return user ids
     */
    public synchronized List<String> getUsers() {
        return users;
    }

    /**
     * Adds an user to prune by id.
     *
     * @param id the user id
     */
    public synchronized void addUserId(String id) {
        users.add(id);
    }

    /**
     * Returns the list of members to prune.
     *
     * @return members
     */
    public synchronized List<Member> getMembers() {
        return members;
    }

    /**
     * Adds a member to prune.
     *
     * @param member the member
     */
    public synchronized void addMember(Member member) {
        members.add(member);
    }

    /**
     * Returns the bots flag.
     *
     * @return bots flag
     */
    public synchronized boolean getBotsFlag() {
        return bots;
    }

    /**
     * Sets bots flag value to true or false.
     *
     * @param bots new boolean value
     */
    public synchronized void setBotsFlag(boolean bots) {
        this.bots = bots;
    }

    /**
     * Returns the content to look for when pruning.
     *
     * @return content
     */
    public synchronized String getContent() {
        return content;
    }

    /**
     * Sets the content to prune.
     *
     * @param content the new content to prune.
     */
    public synchronized void setContent(String content) {
        this.content = content;
    }

}