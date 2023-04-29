package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_HUG;
import static ayaya.core.enums.ActionQuotes.SELF_HUG;
import static ayaya.core.enums.ActionQuotes.AYAYA_HUG;

/**
 * Class of the hug command.
 */
public class Hug extends ActionNormalTemplate {

    public Hug() {

        super("hug", "Hug someone! How nice! <:AyaSmile:331115374739324930>", "{prefix}hug <@user>",
                new String[]{"cuddle"}, NORMAL_HUG.getQuote(), NORMAL_HUG.getFooter(), SELF_HUG.getQuote(), SELF_HUG.getFooter(),
                AYAYA_HUG.getQuote());

    }

}