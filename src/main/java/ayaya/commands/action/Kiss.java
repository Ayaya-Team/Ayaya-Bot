package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_KISS;
import static ayaya.core.enums.ActionQuotes.SELF_KISS;
import static ayaya.core.enums.ActionQuotes.AYAYA_KISS;

/**
 * Class of the kiss command.
 */
public class Kiss extends ActionNormalTemplate {

    public Kiss() {

        super("kiss", "Wew, your love must be that big!", "{prefix}kiss <@user>", new String[]{},
                NORMAL_KISS.getQuote(), NORMAL_KISS.getFooter(), SELF_KISS.getQuote(), SELF_KISS.getFooter(),
                AYAYA_KISS.getQuote());

    }

}