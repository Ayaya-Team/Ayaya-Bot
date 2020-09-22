package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_SNUGGLE;
import static ayaya.core.enums.ActionQuotes.SELF_SNUGGLE;
import static ayaya.core.enums.ActionQuotes.AYAYA_SNUGGLE;

/**
 * Class of the snuggle command.
 */
public class Snuggle extends ActionNormalTemplate {

    public Snuggle() {

        super("snuggle", "Snuggle with someone! Wait, apacheGet a room first! <:AyaBlush:331115100658204672>",
                "{prefix}snuggle <@user>", new String[]{}, NORMAL_SNUGGLE.getQuote(),
                NORMAL_SNUGGLE.getFooter(), SELF_SNUGGLE.getQuote(), SELF_SNUGGLE.getFooter(),
                AYAYA_SNUGGLE.getQuote());

    }

}