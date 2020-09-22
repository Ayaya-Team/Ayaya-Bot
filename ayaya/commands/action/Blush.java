package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_BLUSH;

/**
 * Class of the blush command.
 */
public class Blush extends ActionBasicTemplate {

    public Blush() {

        super("blush", "Oh look, you're blushing!", "{prefix}blush", new String[]{},
                NORMAL_BLUSH.getQuote(), NORMAL_BLUSH.getFooter());

    }

}