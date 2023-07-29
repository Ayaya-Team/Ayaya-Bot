package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_CRY;

/**
 * Class of the cry command.
 */
public class Cry extends ActionBasicTemplate {

    public Cry() {

        super("cry", "Why are you so sad?!", "{prefix}cry", new String[]{"waa"},
                NORMAL_CRY.getQuote(), NORMAL_CRY.getFooter());

    }

}