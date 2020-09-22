package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_WAKEUP;
import static ayaya.core.enums.ActionQuotes.SELF_WAKEUP;
import static ayaya.core.enums.ActionQuotes.AYAYA_WAKEUP;

/**
 * Class of the wakeup command.
 */
public class Wakeup extends ActionNormalTemplate {

    public Wakeup() {

        super("wakeup", "Is someone having problems at waking up? Then maybe they need some help!",
                "{prefix}wakeup <@user>", new String[]{"awake"}, NORMAL_WAKEUP.getQuote(),
                NORMAL_WAKEUP.getFooter(), SELF_WAKEUP.getQuote(), SELF_WAKEUP.getFooter(), AYAYA_WAKEUP.getQuote());

    }

}