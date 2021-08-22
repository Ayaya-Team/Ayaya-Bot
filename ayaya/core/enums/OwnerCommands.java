package ayaya.core.enums;

import com.jagrosh.jdautilities.command.Command;
import ayaya.commands.owner.*;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

/**
 * The owner commands.
 */
public enum OwnerCommands {

    ANNOUNCE("announce", new Announce()), BLACKLIST("blacklist", new Blacklist(new EventWaiter())),
    BLOCK("block", new Block()), CHANNELLIST("channellist", new Channellist()),
    INSERTKEY("insertkey", new InsertKey()), MESSAGE("message", new Message()),
    MUSICSWITCH("mswitch", new MusicSwitch()), REFRESH("refresh", new Refresh()),
    SERVERLIST("serverlist", new Serverlist(new EventWaiter())), SHUTDOWN("shutdown", new Shutdown()),
    UNBLOCK("unblock", new Unblock());

    private String name;
    private Command cmd;

    OwnerCommands(String name, Command cmd) {
        this.name = name;
        this.cmd = cmd;
    }

    public String getName() {return name;}

    public Command command() {return cmd;}

}