package ayaya.core.utils;

import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store role management action data.
 */
public class RoleManageActionData {

    private int memberAmount;
    private int amountOfRolesToAdd;
    private int amountOfRolesToRemove;
    private List<Role> rolesToAdd;
    private List<Role> rolesToRemove;

    public RoleManageActionData() {
        memberAmount = 0;
        amountOfRolesToAdd = 0;
        amountOfRolesToRemove = 0;
        rolesToAdd = new ArrayList<>(20);
        rolesToRemove = new ArrayList<>(20);
    }

    /**
     * Returns the member amount.
     *
     * @return member amount
     */
    public synchronized int getMemberAmount() {
        return memberAmount;
    }

    /**
     * Increments the member amount.
     */
    public synchronized void incrementMemberAmount() {
        memberAmount++;
    }

    /**
     * Returns the amount of roles to add.
     *
     * @return amount of roles to add
     */
    public synchronized int getAmountOfRolesToAdd() {
        return amountOfRolesToAdd;
    }

    /**
     * Change the amount of roles to add.
     *
     * @param amountOfRolesToAdd new amount
     */
    public synchronized void setAmountOfRolesToAdd(int amountOfRolesToAdd) {
        this.amountOfRolesToAdd = amountOfRolesToAdd;
    }

    /**
     * Return the amount of roles to remove.
     *
     * @return amount of roles to remove
     */
    public synchronized int getAmountOfRolesToRemove() {
        return amountOfRolesToRemove;
    }

    /**
     * Change the amount of roles to remove.
     *
     * @param amountOfRolesToRemove new amount
     */
    public synchronized void setAmountOfRolesToRemove(int amountOfRolesToRemove) {
        this.amountOfRolesToRemove = amountOfRolesToRemove;
    }

    /**
     * Returns the list of roles to add.
     *
     * @return roles to add
     */
    public synchronized List<Role> getRolesToAdd() {
        return rolesToAdd;
    }

    /**
     * Sets role to add.
     *
     * @param role new role
     */
    public synchronized void addRole(Role role) {
        rolesToAdd.add(role);
    }

    /**
     * Returns the list of roles to remove.
     *
     * @return roles to remove
     */
    public List<Role> getRolesToRemove() {
        return rolesToRemove;
    }

    /**
     * Sets role to remove.
     *
     * @param role new role
     */
    public synchronized void removeRole(Role role) {
        rolesToRemove.add(role);
    }
}