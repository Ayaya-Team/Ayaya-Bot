package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_POKE;
import static ayaya.core.enums.ActionQuotes.SELF_POKE;
import static ayaya.core.enums.ActionQuotes.AYAYA_POKE;

/**
 * Class of the poke command.
 */
public class Poke extends ActionNormalTemplate {

    public Poke() {

        super("poke", "You like attention, don't you?", "{prefix}poke <@user>", new String[]{},
                NORMAL_POKE.getQuote(), NORMAL_POKE.getFooter(), SELF_POKE.getQuote(), SELF_POKE.getFooter(),
                AYAYA_POKE.getQuote());

    }

}