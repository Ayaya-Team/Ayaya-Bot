package ayaya.core.enums;

import ayaya.commands.action.*;
import ayaya.commands.funny.Bigtext;
import ayaya.commands.funny.Flipcoin;
import ayaya.commands.funny.Kawaii;
import ayaya.commands.funny.Roll;
import ayaya.commands.information.*;
import ayaya.commands.moderator.Prune;
import ayaya.commands.utilities.Avatar;
import com.jagrosh.jdautilities.command.Command;

/**
 * All of the aliases of the commands.
 */
public enum CommandAliases {

    AWAKE("awake", new Wakeup()), BONK("bonk", new Scold()), COIN("coin", new Flipcoin()),
    CUDDLE("cuddle", new Hug()), CUTE("cute", new Kawaii()), DICE("dice", new Roll()),
    DIE("die", new Roll()), EMOJITEXT("emojitext", new Bigtext()), FLIP("flip", new Flipcoin()),
    GUILDINFO("guildinfo", new Serverinfo()), HAPPY("happy", new Smile()), HI("hi", new Greet()),
    INFO("info", new About()), KONNICHIWA("konnichiwa", new Greet()), NANI("nani", new Confused()),
    PFP("pfp", new Avatar()), PP("pp", new PrivacyPolicy()), PRIVACY("privacy", new PrivacyPolicy()),
    PURGE("purge", new Prune()), REGIONALTEXT("regionaltext", new Bigtext()),
    SHRUGS("shrugs", new Shrug()), STATISTICS("statistics", new Stats()),
    TABLEFLIP("tableflip", new Fliptable()), TSUN("tsun", new Tsundere()),
    UNFLIP("unflip", new Unfliptable()), VOTE("vote", new Upvote()),
    WAA("waa", new Cry()), WHAT("what", new Confused());

    private String name;
    private Command cmd;

    CommandAliases(String name, Command cmd) {
        this.name = name;
        this.cmd = cmd;
    }

    public String getName() {return name;}

    public Command getCommand() {return cmd;}

}