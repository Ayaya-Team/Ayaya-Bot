package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_CONFUSED;

/**
 * Class of the confused command.
 */
public class Confused extends ActionBasicTemplate {

    public Confused() {

        super("confused", "Are you lost?", "{prefix}confused", new String[]{"what", "nani"},
                NORMAL_CONFUSED.getQuote(), NORMAL_CONFUSED.getFooter());

    }

}