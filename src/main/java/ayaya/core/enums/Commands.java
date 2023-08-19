package ayaya.core.enums;

import ayaya.commands.action.*;
import ayaya.commands.admin.News;
import ayaya.commands.funny.*;
import ayaya.commands.information.*;
import ayaya.commands.moderator.*;
import ayaya.commands.utilities.*;
import com.jagrosh.jdautilities.command.Command;

/**
 * All the commands.
 */
public enum Commands {

    ABOUT(new About()), ASK(new Ask()), AVATAR(new Avatar()), AYAYA(new AyayaCommand()), BAKA(new Baka()),
    BAN(new Ban()), BANNER(new Banner()), BIGTEXT(new Bigtext()), BITE(new Bite()), BLUSH(new Blush()),
    CATEGORY(new Category()), CC(new CC()), CHANGELOG(new Changelog()), CHANNEL(new Channel()),
    CHANNELINFO(new Channelinfo()), CHOOSE(new Choose()), COLOR(new CheckColor()), CONFUSED(new Confused()),
    CONVERT(new Convert()), COOKIE(new Cookie()), CRY(new Cry()), DANCE(new Dance()), DONATE(new Donate()),
    EMOTESEARCH(new EmoteSearch()), FLIPCOIN(new Flipcoin()), FACEDESK(new Facedesk()), FLIPTABLE(new Fliptable()),
    GREET(new Greet()), HELP(new Help()), HIGHFIVE(new Highfive()), HUG(new Hug()), INVITE(new Invite()),
    KAWAII(new Kawaii()), KICK(new Kick()), KISS(new Kiss()), LAUGH(new Laugh()), MUTE(new Mute()), NEWS(new News()),
    NOM(new Nom()), NUZZLE(new Nuzzle()), PAT(new Pat()), PING(new Ping()), PRIVACY_POLICY(new PrivacyPolicy()),
    NSFW(new NSFW()), POKE(new Poke()), POUT(new Pout()), PREMIUM(new Premium()), PRUNE(new Prune()),
    RATE(new Rate()), ROLE(new Role()), ROLEINFO(new Roleinfo()), ROLL(new Roll()), RPS(new RPS()), SAY(new Say()),
    SERVERINFO(new Serverinfo()), SCARED(new Scared()), SCOLD(new Scold()), SHRUG(new Shrug()), SLAP(new Slap()),
    SLEEP(new Sleep()), SMILE(new Smile()), SNUGGLE(new Snuggle()), STARE(new Stare()), STATS(new Stats()),
    SUPPORT(new Support()), TEEHEE(new Teehee()), THROW(new Throw()), THUMBUP(new ThumbUp()), TICKLE(new Tickle()),
    TIME(new Time()), TSUNDERE(new Tsundere()), UNFLIPTABLE(new Unfliptable()), UNMUTE(new Unmute()),
    USERINFO(new Userinfo()), VOTE(new Vote()), WAKEUP(new Wakeup());

    private Command cmd;

    Commands(Command cmd) {
        this.cmd = cmd;
    }

    public String getName() {
        return cmd.getName();
    }

    public Command getCommand() {
        return cmd;
    }

}