package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_SCARED;

public class Scared extends ActionBasicTemplate {

    public Scared() {

        super("scared", "Are you scared? What happened?",
                "{prefix}scared", new String[]{}, NORMAL_SCARED.getQuote(), NORMAL_SCARED.getFooter());

    }

}