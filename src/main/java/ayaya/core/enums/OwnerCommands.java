package ayaya.core.enums;

import ayaya.commands.owner.Shutdown;
import ayaya.commands.owner.*;
import com.jagrosh.jdautilities.command.Command;

/**
 * The owner commands.
 */
public enum OwnerCommands {

    BLACKLIST("blacklist", new Blacklist()), BLOCK("block", new Block()),
    LOAD("load", new Load()), MUSICSWITCH("mswitch", new MusicSwitch()),
    REFRESH("refresh", new Refresh()), SHUTDOWN("shutdown", new Shutdown()),
    UNBLOCK("unblock", new Unblock()), UNLOAD("unload", new Unload());

    private String name;
    private Command cmd;

    OwnerCommands(String name, Command cmd) {
        this.name = name;
        this.cmd = cmd;
    }

    public String getName() {return name;}

    public Command command() {return cmd;}

}