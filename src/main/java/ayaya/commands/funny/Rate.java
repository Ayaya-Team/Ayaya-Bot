package ayaya.commands.funny;

import ayaya.commands.Command;
import ayaya.core.Emotes;
import ayaya.core.enums.RateSpecialCases;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static ayaya.core.enums.CommandCategories.FUNNY;

/**
 * Class of the rate command.
 */
public class Rate extends Command {

    public Rate() {

        this.name = "rate";
        this.help = "Want my opinion about someone or something?";
        this.arguments = "{prefix}rate <@user, name or whatever to rate>";
        this.category = FUNNY.asCategory();
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};

    }

    @Override
    protected void executeInstructions(CommandEvent event) {

        String object = event.getArgs();
        List<User> mentioned = event.getMessage().getMentionedUsers();
        if (object.isEmpty()) {
            event.reply("What do you want me to rate? You didn't tell me! " + Emotes.CONFUSED_EMOTE);
            return;
        }
        if (!mentioned.isEmpty() && mentioned.get(0) == event.getSelfUser())
            event.reply("Me? I don't know.");
        else  {
            String quote = object.toLowerCase().trim();
            RateSpecialCases e = getEnum(quote);
            if (e != null) {
                event.reply(e.getAnswer());
            } else {
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                int rate = rng.nextInt(11);
                event.reply("Hmmm... I give " + object + " a " + rate + "/10.");
            }
        }

    }

    /**
     * Fetches an enum related with the quote, in case it exists, else returns null.
     *
     * @param quote the quote
     * @return enum
     */
    private RateSpecialCases getEnum(String quote) {
        for (RateSpecialCases e: RateSpecialCases.values()) {
            if (e.toString().equals(quote)) return e;
        }
        return null;
    }

}