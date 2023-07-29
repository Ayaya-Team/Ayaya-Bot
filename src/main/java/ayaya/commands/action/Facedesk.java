package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_FACEDESK;

/**
 * Class of the facedesk command.
 */
public class Facedesk extends ActionBasicTemplate {

    public Facedesk() {

        super("facedesk", "Are you feeling baka?", "{prefix}facedesk", new String[]{},
                NORMAL_FACEDESK.getQuote(), NORMAL_FACEDESK.getFooter());

    }

}