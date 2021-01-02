package ayaya.core;

import ayaya.commands.Command;
import ayaya.commands.ListCategory;
import ayaya.commands.owner.Load;
import ayaya.commands.owner.MusicSwitch;
import ayaya.commands.owner.Unload;
import ayaya.core.enums.CommandCategories;
import ayaya.core.enums.Commands;
import ayaya.core.enums.MusicCommands;
import ayaya.core.exceptions.db.DBNotConnectedException;
import ayaya.core.exceptions.http.MissingHeaderInfoException;
import ayaya.core.listeners.CommandListener;
import ayaya.core.listeners.EventListener;
import ayaya.core.music.MusicHandler;
import ayaya.core.utils.HTTP;
import ayaya.core.utils.SQLController;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Main class of the bot.
 */
public class Ayaya {

    private static final GatewayIntent[] INTENTS = {
            GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES
    };
    private static final String SHUTDOWN_GAME = "Going to bed.";
    private static final int STATUS_AMOUNT = 7;

    private static JDA ayaya = null;
    private static ThreadPoolExecutor threads;
    private static String prefix = "";
    private static String token = "";
    private static String dservicesToken = "";
    private static String botsOnDiscordToken = "";
    private static String dboatsToken = "";
    private static String dbotsToken = "";
    private static String dbotToken = "";
    private static String dblToken = "";
    private static String owner = "";
    private static String[] coOwners;
    private static final int idsAmount = 3;
    private static int status = 1;

    public static void main(String[] args) {

        coOwners = new String[idsAmount - 1];
        fetchSettings();
        CommandClient client = buildCommandClient();
        startup(client);

    }

    /**
     * Method to retrieve the settings configuration from the database and assign the values to the variables.
     */
    private static void fetchSettings() {

        SQLController jdbc = new SQLController();

        try {

            jdbc.open("jdbc:sqlite:data.db");
            prefix = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'prefix';", 60)
                    .getString("value");
            token = jdbc.sqlSelect("SELECT * FROM settings WHERE option LIKE 'token';", 60)
                    .getString("value");
            owner = jdbc.sqlSelect("SELECT * FROM owners WHERE person LIKE 'owner';", 60)
                    .getString("discord_id");
            for (int i = idsAmount - 1; i > 0; i--)
                coOwners[i - 1] = jdbc.sqlSelect("SELECT * FROM owners WHERE id LIKE '" +
                        (i + 1) + "';", 60).getString("discord_id");

        } catch (SQLException e) {

            System.out.println("A problem occurred while trying to get necessary information! Aborting the process...");
            System.err.println(e.getMessage());
            e.printStackTrace();

        } catch (DBNotConnectedException e) {

            System.out.println("No database connected.");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);

        } finally {

            try {
                jdbc.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }

    }

    /**
     * Builds the JDA-Utilities Command Client that will handle all commands.
     *
     * @return the Command Client
     */
    private static CommandClient buildCommandClient() {

        CommandClientBuilder client = new CommandClientBuilder()
                .setOwnerId(owner)
                .setCoOwnerIds(coOwners)
                .setPrefix(prefix)
                .setHelpConsumer(null)
                .setHelpWord(null)
                .setEmojis("<:KawaiiThumbup:361601400079253515>", ":warning:", ":x:")
                .setListener(new CommandListener())
                .useHelpBuilder(false);

        loadCommands(client);
        loadMusicCommands(client);

        return client.build();

    }

    /**
     * Loads the commands to the command client
     *
     * @param client the command client
     */
    private static void loadCommands(CommandClientBuilder client) {

        Command load = new Load();
        Command unload = new Unload();
        Command mswitch = new MusicSwitch();

        client.addCommands(load, unload, mswitch);
        ListCategory owner = CommandCategories.OWNER.asListCategory();
        owner.add(load.getName());
        owner.add(unload.getName());
        owner.add(mswitch.getName());

        ListCategory listCategory;
        String categoryName;

        for (Commands command : Commands.values()) {
            client.addCommand(command.getCommand());
            if (command.getCommand().getCategory() != null && !command.getCommand().isHidden()) {
                categoryName = command.getCommand().getCategory().getName();
                listCategory = CommandCategories.getListCategory(categoryName);
                if (listCategory != null) listCategory.add(command.getName());
            }
        }

    }

    /**
     * Loads the music commands to the command client
     *
     * @param client the command client
     */
    private static void loadMusicCommands(CommandClientBuilder client) {

        ListCategory listCategory;
        String categoryName;
        MusicHandler musicHandler = new MusicHandler();
        for (MusicCommands command : MusicCommands.values()) {
            command.getCommandAsMusicCommand().setMusicHandler(musicHandler);
            client.addCommand(command.getCommand());
            if (command.getCommand().getCategory() != null) {
                categoryName = command.getCommand().getCategory().getName();
                listCategory = CommandCategories.getListCategory(categoryName);
                if (listCategory != null) listCategory.add(command.getName());
            }
        }

    }

    /**
     * Builds the JDA client and starts up the bot.
     *
     * @param client the Command CLient to handle the commands.
     */
    private static void startup(CommandClient client) {

        try {
            Collection<GatewayIntent> intents = Arrays.asList(INTENTS);
            ayaya = JDABuilder.create(token, intents)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                    .enableCache(CacheFlag.ROLE_TAGS)
                    .addEventListeners(client, new EventWaiter(), new EventListener())
                    .setAudioSendFactory(new NativeAudioSendFactory())
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .build();
        } catch (LoginException e) {
            System.out.println("Error while trying to log in Discord. Probably the authentication failed. Shutting down...");
            e.printStackTrace();
            return;
        } catch (IllegalArgumentException e) {
            System.out.println("The account type specified isn't valid. Shutting down...");
            e.printStackTrace();
            return;
        } catch (ErrorResponseException e) {
            System.out.println("Discord isn't responding. Shutting down...");
            e.printStackTrace();
            return;
        }
        boolean interrupted;
        do {
            try {
                ayaya.awaitStatus(JDA.Status.CONNECTED);
                interrupted = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                interrupted = true;
            }
        } while (interrupted);
        threads = new ThreadPoolExecutor(
                3, 3, 0, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>());
        threads.execute(Ayaya::gameChanger);
        getBotListsTokens();
        threads.execute(Ayaya::updateBotListsStats);

    }

    /**
     * Retrieves the Discord Bots List token to update the server count on the list website.
     */
    private static void getBotListsTokens() {

        SQLController jdbc = new SQLController();

        try {
            jdbc.open("jdbc:sqlite:data.db");
            dservicesToken = jdbc.sqlSelect("SELECT * FROM botlists WHERE list LIKE 'dservices';", 60)
                    .getString("token");
            botsOnDiscordToken = jdbc.sqlSelect("SELECT * FROM botlists WHERE list LIKE 'botsondiscord';", 60)
                    .getString("token");
            dboatsToken = jdbc.sqlSelect("SELECT * FROM botlists WHERE list LIKE 'dboats';", 60)
                    .getString("token");
            dbotsToken = jdbc.sqlSelect("SELECT * FROM botlists WHERE list LIKE 'dbots';", 60)
                    .getString("token");
            dbotToken = jdbc.sqlSelect("SELECT * FROM botlists WHERE list LIKE 'dbot';", 60)
                    .getString("token");
            dblToken = jdbc.sqlSelect("SELECT * FROM botlists WHERE list LIKE 'dbl';", 60)
                    .getString("token");
        } catch (SQLException e) {
            System.out.println("A problem occurred while trying to get one or more of the bot list tokens.");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (DBNotConnectedException e) {
            System.out.println("No database connected.");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {

            try {
                jdbc.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

        }

    }

    /**
     * Changes the status once per minute.
     */
    private static void gameChanger() {

        while (ayaya.getPresence().getActivity() != null
                && !ayaya.getPresence().getActivity().getName().equals(SHUTDOWN_GAME)) {

            String quote = "";
            SQLController jdbc = new SQLController();

            try {

                jdbc.open("jdbc:sqlite:data.db");
                quote = jdbc.sqlSelect("SELECT * FROM `status quotes` WHERE id LIKE '%" + status + "%';", 5)
                        .getString("quote");

            } catch (SQLException e) {
                System.out.println("A problem occurred while trying to get the next status quote.");
                System.err.println(e.getMessage());
                e.printStackTrace();
            } catch (DBNotConnectedException e) {
                System.out.println("No database connected.");
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.exit(1);
            } finally {
                try {
                    jdbc.close();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }

            ayaya.getPresence().setActivity(Activity.playing(prefix + "help | " + quote));
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                //Skip ahead.
            }

            status++;
            if (status > STATUS_AMOUNT) status = 1;

        }

    }

    /**
     * Updates the player count at the Discord Bot List website twice per hour.
     */
    private static void updateBotListsStats() {

        if (!dblToken.isEmpty() || !dservicesToken.isEmpty() || !dboatsToken.isEmpty()) {
            JSONObject json;
            while (ayaya.getPresence().getActivity() != null
                    && !ayaya.getPresence().getActivity().getName().equals(SHUTDOWN_GAME)) {

                try {
                    TimeUnit.MINUTES.sleep(30);
                    if (!dservicesToken.isEmpty()) {
                        json = new JSONObject()
                                .put("servers", ayaya.getGuilds().size())
                                .put("shards", 1);
                        if (
                                HTTP.postJSON(
                                        "https://api.discordservices.net/bot/"
                                                + ayaya.getSelfUser().getId() + "/stats", json,
                                        "Authorization", dservicesToken
                                )
                        ) System.out.println("Stats successfully posted to discordservices.net.");
                        else System.out.println("Failed to post the stats to discordservices.net.");
                    }

                    if (!dboatsToken.isEmpty()) {
                        json = new JSONObject()
                                .put("server_count", ayaya.getGuilds().size());
                        if (
                                HTTP.postJSON(
                                        "https://discord.boats/api/bot/"
                                                + ayaya.getSelfUser().getId(), json,
                                        "Authorization", dboatsToken
                                )
                        ) System.out.println("Stats successfully posted to discord.boats.");
                        else System.out.println("Failed to post the stats to discord.boats.");
                    }

                    if (!dbotsToken.isEmpty()) {
                        json = new JSONObject()
                                .put("guildCount", ayaya.getGuilds().size())
                                .put("shardCount", 1);
                        if (
                                HTTP.postJSON(
                                        "https://discord.bots.gg/api/v1/bots/"
                                                + ayaya.getSelfUser().getId() + "/stats", json,
                                        "Authorization", dbotsToken
                                )
                        ) System.out.println("Stats successfully posted to discord.bots.gg.");
                        else System.out.println("Failed to post the stats to discord.bots.gg.");
                    }

                    if (!dbotToken.isEmpty()) {
                        json = new JSONObject()
                                .put("guilds", ayaya.getGuilds().size());
                        if (
                                HTTP.postJSON(
                                        "https://discordbotlist.com/api/v1/bots/"
                                                + ayaya.getSelfUser().getId() + "/stats", json,
                                        "Authorization", dbotToken
                                )
                        ) System.out.println("Stats successfully posted to discordbotlist.com.");
                        else System.out.println("Failed to post the stats to discordbotlist.com.");
                    }

                    if (!dblToken.isEmpty()) {
                        json = new JSONObject()
                                .put("server_count", ayaya.getGuilds().size());
                        if (
                                HTTP.postJSON(
                                        "https://top.gg/api/bots/"
                                                + ayaya.getSelfUser().getId() + "/stats", json,
                                        "Authorization", dblToken
                                )
                        ) System.out.println("Stats successfully posted to top.gg.");
                        else System.out.println("Failed to post the stats to top.gg.");
                    }
                    if (!botsOnDiscordToken.isEmpty()) {
                        json = new JSONObject()
                                .put("guildCount", ayaya.getGuilds().size());
                        if (
                                HTTP.postJSON(
                                        "https://bots.ondiscord.xyz/bot-api/bots/"
                                                + ayaya.getSelfUser().getId() + "/guilds", json,
                                        "Authorization", botsOnDiscordToken
                                )
                        ) System.out.println("Stats successfully posted to Bots on Discord.");
                        else System.out.println("Failed to post the stats to Bots on Discord.");
                    }
                } catch (InterruptedException e) {
                    //Retry.
                } catch (IOException | MissingHeaderInfoException ignored) {}

            }
        } else {
            System.out.println("None of the bot list tokens were found." +
                    " The stats posting thread won't be started.");
        }

    }

    /**
     * Shuts down the threads of the thread pool executor.
     */
    public static void shutdownThreads() {
        threads.shutdownNow();
    }

}