package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_TEEHEE;
import static ayaya.core.enums.ActionQuotes.SELF_TEEHEE;
import static ayaya.core.enums.ActionQuotes.AYAYA_TEEHEE;
import static ayaya.core.enums.ActionQuotes.EVERYONE_TEEHEE;

/**
 * Class of the teehee command.
 */
public class Teehee extends ActionCompleteTemplate {

    public Teehee() {

        super("teehee", "Let's tease someone!","{prefix}teehee <@user>", new String[]{"tease"},
                NORMAL_TEEHEE.getQuote(), NORMAL_TEEHEE.getFooter(), SELF_TEEHEE.getQuote(), SELF_TEEHEE.getFooter(),
                AYAYA_TEEHEE.getQuote(), EVERYONE_TEEHEE.getQuote(), EVERYONE_TEEHEE.getFooter());
    }

}