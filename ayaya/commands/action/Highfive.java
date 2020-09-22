package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_HIGHFIVE;
import static ayaya.core.enums.ActionQuotes.SELF_HIGHFIVE;
import static ayaya.core.enums.ActionQuotes.AYAYA_HIGHFIVE;

/**
 * Class of the highfive command.
 */
public class Highfive extends ActionNormalTemplate {

    public Highfive() {

        super("highfive", "Good job!", "{prefix}highfive <@user>", new String[]{},
                NORMAL_HIGHFIVE.getQuote(), NORMAL_HIGHFIVE.getFooter(), SELF_HIGHFIVE.getQuote(),
                SELF_HIGHFIVE.getFooter(), AYAYA_HIGHFIVE.getQuote());

    }

}