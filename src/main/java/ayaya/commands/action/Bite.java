package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_BITE;
import static ayaya.core.enums.ActionQuotes.SELF_BITE;
import static ayaya.core.enums.ActionQuotes.AYAYA_BITE;
import static ayaya.core.enums.ActionQuotes.DEV_BITE;

/**
 * Class of the bite command.
 */
public class Bite extends ActionExtraTemplate {

    public Bite() {

        super("bite", "Wow, are you some wolf?", "{prefix}bite <@user>", new String[]{},
                NORMAL_BITE.getQuote(), NORMAL_BITE.getFooter(), SELF_BITE.getQuote(), SELF_BITE.getFooter(),
                AYAYA_BITE.getQuote(), DEV_BITE.getQuote());

    }

}