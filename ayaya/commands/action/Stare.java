package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_STARE;
import static ayaya.core.enums.ActionQuotes.SELF_STARE;
import static ayaya.core.enums.ActionQuotes.AYAYA_STARE;

/**
 * Class of the stare command.
 */
public class Stare extends ActionNormalTemplate {

    public Stare() {

        super("stare", "You like to make people feel uncomfortable, don't you?",
                "{prefix}stare <@user>", new String[]{}, NORMAL_STARE.getQuote(), NORMAL_STARE.getFooter(),
                SELF_STARE.getQuote(), SELF_STARE.getFooter(), AYAYA_STARE.getQuote());

    }

}