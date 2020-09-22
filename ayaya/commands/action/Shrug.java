package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_SHRUG;
import static ayaya.core.enums.ActionQuotes.SELF_SHRUG;
import static ayaya.core.enums.ActionQuotes.AYAYA_SHRUG;

/**
 * Class of the shrug command.
 */
public class Shrug extends ActionNormalTemplate {

    public Shrug() {

        super("shrug", "Don't know what to do?", "{prefix}shrug <@user>", new String[]{"shrugs"},
                NORMAL_SHRUG.getQuote(), NORMAL_SHRUG.getFooter(), SELF_SHRUG.getQuote(), SELF_SHRUG.getFooter(),
                AYAYA_SHRUG.getQuote());

    }

}