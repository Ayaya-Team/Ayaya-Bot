package ayaya.commands.owner;

import ayaya.commands.Command;
import ayaya.core.BotData;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.io.IOException;
import java.sql.SQLException;

import static ayaya.core.enums.CommandCategories.OWNER;

public class Refresh extends Command {

    private static final String CONFIG = "config";
    private static final String DB = "db";
    private static final String ALL = "all";

    public Refresh() {
        this.name = "refresh";
        this.category = OWNER.asCategory();
        this.isGuildOnly = false;
        this.isOwnerCommand = true;
        this.botPerms = new Permission[]{Permission.MESSAGE_ADD_REACTION};
    }

    @Override
    protected void execute(CommandEvent event) {

        String arg = event.getArgs().split(" ")[0];

        if (!arg.isEmpty()) {
            switch (arg.toLowerCase()) {
                case CONFIG:
                    if (refreshConfig())
                        event.reactSuccess();
                    else
                        event.reactError();
                    break;
                case DB:
                    if (refreshDB())
                        event.reactSuccess();
                    else
                        event.reactError();
                    break;
                case ALL:
                    boolean configSuccess = refreshConfig();
                    boolean dbSuccess = refreshDB();
                    if (configSuccess && dbSuccess)
                        event.reactSuccess();
                    else if (!configSuccess && !dbSuccess)
                        event.reactError();
                    else
                        event.reactWarning();
                    break;
                default:
                    // ignore
            }
        }

    }

    private boolean refreshConfig() {
        try {
            BotData.refreshJSONData();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean refreshDB() {
        try {
            BotData.refreshDBData();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}