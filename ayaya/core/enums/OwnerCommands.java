package ayaya.core.enums;

import ayaya.commands.owner.Shutdown;
import ayaya.commands.owner.*;
import com.jagrosh.jdautilities.command.Command;

/**
 * The owner commands.
 */
public enum OwnerCommands {

    ANNOUNCE("announce", new Announce()), BLACKLIST("blacklist", new Blacklist()),
    BLOCK("block", new Block()), CHANNELLIST("channellist", new Channellist()),
    INSERTKEY("insertkey", new InsertKey()), LOAD("load", new Load()),
    MESSAGE("message", new Message()), MUSICSWITCH("mswitch", new MusicSwitch()),
    REFRESH("refresh", new Refresh()), SERVERLIST("serverlist", new Serverlist()),
    SHUTDOWN("shutdown", new Shutdown()), UNBLOCK("unblock", new Unblock()),
    UNLOAD("unload", new Unload());

    private String name;
    private Command cmd;

    OwnerCommands(String name, Command cmd) {
        this.name = name;
        this.cmd = cmd;
    }

    public String getName() {return name;}

    public Command command() {return cmd;}

}