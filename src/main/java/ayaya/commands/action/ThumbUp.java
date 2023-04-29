package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_THUMBUP;
import static ayaya.core.enums.ActionQuotes.EVERYONE_THUMBUP;
import static ayaya.core.enums.ActionQuotes.AYAYA_THUMBUP;

/**
 * Class of the thumbup command.
 */
public class ThumbUp extends ActionNormalTemplate {

    public ThumbUp() {

        super("thumbup", "So, you're okay. Glad to hear. <:AyaSmile:331115374739324930>",
                "{prefix}thumbup <@user>", new String[]{}, NORMAL_THUMBUP.getQuote(),
                NORMAL_THUMBUP.getFooter(), EVERYONE_THUMBUP.getQuote(), EVERYONE_THUMBUP.getFooter(),
                AYAYA_THUMBUP.getQuote());

    }

}