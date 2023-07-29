package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_NOM;

/**
 * Class of the nom command.
 */
public class Nom extends ActionBasicTemplate {

    public Nom() {

        super("nom", "Hungry? Why don't you eat something?", "{prefix}nom", new String[]{},
                NORMAL_NOM.getQuote(), NORMAL_NOM.getFooter());

    }

}