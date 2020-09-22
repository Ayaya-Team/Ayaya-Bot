package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_LAUGH;

/**
 * Class of the laugh command.
 */
public class Laugh extends ActionBasicTemplate {

    public Laugh() {

        super("laugh", "What's so funny?", "{prefix}laugh", new String[]{},
                NORMAL_LAUGH.getQuote(), NORMAL_LAUGH.getFooter());

    }

}