package ayaya.core.enums;

import ayaya.commands.action.*;
import ayaya.commands.funny.Bigtext;
import ayaya.commands.funny.Kawaii;
import ayaya.commands.information.About;
import ayaya.commands.information.Stats;
import ayaya.commands.moderator.Prune;
import com.jagrosh.jdautilities.command.Command;
import ayaya.commands.funny.Flipcoin;
import ayaya.commands.funny.Roll;

/**
 * All of the aliases of the commands.
 */
public enum CommandAliases {

    AWAKE("awake", new Wakeup()), COIN("coin", new Flipcoin()), CUDDLE("cuddle", new Hug()),
    CUTE("cute", new Kawaii()), DICE("dice", new Roll()), DIE("die", new Roll()),
    EMOJITEXT("emojitext", new Bigtext()), FLIP("flip", new Flipcoin()),
    HAPPY("happy", new Smile()), HI("hi", new Greet()), INFO("info", new About()),
    KONNICHIWA("konnichiwa", new Greet()), NANI("nani", new Confused()),
    PURGE("purge", new Prune()), REGIONALTEXT("regionaltext", new Bigtext()),
    SHRUGS("shrugs", new Shrug()), STATISTICS("statistics", new Stats()),
    TABLEFLIP("tableflip", new Fliptable()), TSUN("tsun", new Tsundere()),
    UNFLIP("unflip", new Unfliptable()), WAA("waa", new Cry()), WHAT("what", new Confused());

    private String name;
    private Command cmd;

    CommandAliases(String name, Command cmd) {
        this.name = name;
        this.cmd = cmd;
    }

    public String getName() {return name;}

    public Command getCommand() {return cmd;}

}