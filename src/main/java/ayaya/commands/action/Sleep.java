package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_SLEEP;
import static ayaya.core.enums.ActionQuotes.SELF_SLEEP;
import static ayaya.core.enums.ActionQuotes.AYAYA_SLEEP;

/**
 * Class of the sleep command.
 */
public class Sleep extends ActionNormalTemplate {

    public Sleep() {

        super("sleep", "Someone awake until very late? Remind them to sleep!",
                "{prefix}sleep <@user>", new String[]{}, NORMAL_SLEEP.getQuote(), NORMAL_SLEEP.getFooter(),
                SELF_SLEEP.getQuote(), SELF_SLEEP.getFooter(), AYAYA_SLEEP.getQuote());

    }

}