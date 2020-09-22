package ayaya.commands.action;

import static ayaya.core.enums.ActionQuotes.NORMAL_SMILE;

/**
 * Class of the smile command.
 */
public class Smile extends ActionBasicTemplate {

    public Smile() {

        super("smile", "Hmm, you look very happy. <:AyaSmile:331115374739324930>",
                "{prefix}smile", new String[]{"happy"}, NORMAL_SMILE.getQuote(), NORMAL_SMILE.getFooter());

    }

}