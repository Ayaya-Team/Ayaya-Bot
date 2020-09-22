package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_GREET;
import static ayaya.core.enums.ActionQuotes.EVERYONE_GREET;
import static ayaya.core.enums.ActionQuotes.AYAYA_GREET;

/**
 * Class of the greet command.
 */
public class Greet extends ActionNormalTemplate {

    public Greet() {

        super("greet", "Greet someone!", "{prefix}greet <@user>", new String[]{"hi", "konnichiwa"},
                NORMAL_GREET.getQuote(), NORMAL_GREET.getFooter(), EVERYONE_GREET.getQuote(), EVERYONE_GREET.getFooter(),
                AYAYA_GREET.getQuote());

    }

}