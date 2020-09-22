package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_THROW;
import static ayaya.core.enums.ActionQuotes.SELF_THROW;
import static ayaya.core.enums.ActionQuotes.AYAYA_THROW;
import static ayaya.core.enums.ActionQuotes.DEV_THROW;

/**
 * Class of the throw command.
 */
public class Throw extends ActionExtraTemplate {

    public Throw() {

        super("throw", "Throw something at someone! But hey, do not hurt them!",
                "{prefix}throw <@user>", new String[]{}, NORMAL_THROW.getQuote(), NORMAL_THROW.getFooter(),
                SELF_THROW.getQuote(), SELF_THROW.getFooter(), AYAYA_THROW.getQuote(), DEV_THROW.getQuote());

    }

}