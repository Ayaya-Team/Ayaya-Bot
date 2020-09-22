package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_NUZZLE;
import static ayaya.core.enums.ActionQuotes.SELF_NUZZLE;
import static ayaya.core.enums.ActionQuotes.AYAYA_NUZZLE;

/**
 * Class of the nuzzle command.
 */
public class Nuzzle extends ActionNormalTemplate {

    public Nuzzle() {

        super("nuzzle", "Nuzzle with someone! How nice! <:AyaSmile:331115374739324930>",
                "{prefix}nuzzle <@user>", new String[]{}, NORMAL_NUZZLE.getQuote(), NORMAL_NUZZLE.getFooter(),
                SELF_NUZZLE.getQuote(), SELF_NUZZLE.getFooter(), AYAYA_NUZZLE.getQuote());

    }

}