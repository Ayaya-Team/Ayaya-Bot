package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_SLAP;
import static ayaya.core.enums.ActionQuotes.SELF_SLAP;
import static ayaya.core.enums.ActionQuotes.AYAYA_SLAP;
import static ayaya.core.enums.ActionQuotes.DEV_SLAP;

/**
 * Class of the slap command.
 */
public class Slap extends ActionExtraTemplate {

    public Slap() {

        super("slap", "Wow do you need to be that bad?", "{prefix}slap <@user>", new String[]{},
                NORMAL_SLAP.getQuote(), NORMAL_SLAP.getFooter(), SELF_SLAP.getQuote(), SELF_SLAP.getFooter(),
                AYAYA_SLAP.getQuote(), DEV_SLAP.getQuote());

    }

}