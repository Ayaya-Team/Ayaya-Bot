package ayaya.commands.information;

import ayaya.commands.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import static ayaya.core.enums.CommandCategories.INFORMATION;

public class PrivacyPolicy extends Command {

    private static final String PRIVACY_POLICY = "Privacy Policy\n" +
            "\n" +
            "The only data collected to my database from users is usually from premium users" +
            " or from users who have been blacklisted temporarily or permanently." +
            " For patrons the data is collected the moment the user claims a premium key" +
            " while for blacklisted users the data is collected the moment they are blacklisted." +
            " In each of these cases, the data collected per user is solely their discord id" +
            " and nothing else," +
            " in any other case the data collected from the user is absolutely none." +
            " From time to time, my creator will wipe out user data that isn't in use anymore.\n" +
            "\n" +
            "In case you are a patron and you don't want to have your id collected," +
            " you can simply not to claim your premium keys." +
            " If you have already claimed one and want to get the data wiped out" +
            " you can ask my creator to do so," +
            " however you must understand that you will give up on your access to" +
            " premium commands and you won't get the premium key used back." +
            " In case you are no longer premium and want your data wiped out right away," +
            " you can also ask my creator.\n" +
            "\n" +
            "For users that do get blacklisted," +
            " if you have spammed a command that caused me to throw an error," +
            " therefore breaking my usage rule, you will get blacklisted." +
            " For you to remain blacklisted," +
            " your discord id needs to stay stored on my database and therefore," +
            " in this case, you cannot ask for that data to be wiped." +
            " The data will be only wiped when you are no longer blacklisted.\n" +
            "\n" +
            "When you use any of my commands, you agree with this privacy policy.";

    public PrivacyPolicy() {
        this.name = "privacypolicy";
        this.help = "This command will show you my privacy policy. This information might be important to read so I recommend you to take a look.";
        this.arguments = "{prefix}privacypolicy";
        this.category = INFORMATION.asCategory();
        this.aliases = new String[]{"pp", "privacy"};
        this.botPerms = new Permission[]{Permission.MESSAGE_WRITE};
    }

    @Override
    protected void executeInstructions(CommandEvent event) {
        event.reply(PRIVACY_POLICY);
    }
}