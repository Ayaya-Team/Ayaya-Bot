package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_SCOLD;
import static ayaya.core.enums.ActionQuotes.SELF_SCOLD;
import static ayaya.core.enums.ActionQuotes.AYAYA_SCOLD;

/**
 * Class of the scold command.
 */
public class Scold extends ActionNormalTemplate {

    public Scold() {

        super("scold", "Is someone behaving bad?", "{prefix}scold <@user>", new String[]{"bonk"},
                NORMAL_SCOLD.getQuote(), NORMAL_SCOLD.getFooter(), SELF_SCOLD.getQuote(), SELF_SCOLD.getFooter(),
                AYAYA_SCOLD.getQuote());

    }

}