package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_POUT;
import static ayaya.core.enums.ActionQuotes.SELF_POUT;
import static ayaya.core.enums.ActionQuotes.AYAYA_POUT;

/**
 * Class of the pout command.
 */
public class Pout extends ActionNormalTemplate {

    public Pout() {

        super("pout", "Are you mad?", "{prefix}pout <@user>", new String[]{},
                NORMAL_POUT.getQuote(), NORMAL_POUT.getFooter(), SELF_POUT.getQuote(), SELF_POUT.getFooter(),
                AYAYA_POUT.getQuote());

    }

}