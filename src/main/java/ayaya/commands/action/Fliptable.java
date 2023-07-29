package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_FLIPTABLE;

/**
 * Class of the fliptable command.
 */
public class Fliptable extends ActionBasicTemplate {

    public Fliptable() {

        super("fliptable", "Are you that mad?!", "{prefix}fliptable", new String[]{"tableflip"},
                NORMAL_FLIPTABLE.getQuote(), NORMAL_FLIPTABLE.getFooter());

    }

}