package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_BAKA;
import static ayaya.core.enums.ActionQuotes.SELF_BAKA;
import static ayaya.core.enums.ActionQuotes.AYAYA_BAKA;
import static ayaya.core.enums.ActionQuotes.DEV_BAKA;

/**
 * Class of the baka command.
 */
public class Baka extends ActionExtraTemplate {

    public Baka() {

        super("baka", "Want to point out a baka? Then go ahead!", "{prefix}baka <@user>",
                new String[]{}, NORMAL_BAKA.getQuote(), NORMAL_BAKA.getFooter(), SELF_BAKA.getQuote(),
                SELF_BAKA.getFooter(), AYAYA_BAKA.getQuote(), DEV_BAKA.getQuote());

    }

}