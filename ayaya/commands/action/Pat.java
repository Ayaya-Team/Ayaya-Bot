package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_PAT;
import static ayaya.core.enums.ActionQuotes.SELF_PAT;
import static ayaya.core.enums.ActionQuotes.AYAYA_PAT;

/**
 * Class of the pat command.
 */
public class Pat extends ActionNormalTemplate {

    public Pat() {

        super("pat", "Pat someone!", "{prefix}pat <@user>", new String[]{}, NORMAL_PAT.getQuote(),
                NORMAL_PAT.getFooter(), SELF_PAT.getQuote(), SELF_PAT.getFooter(), AYAYA_PAT.getQuote());

    }

}