package ayaya.core;

import ayaya.commands.Command;
import ayaya.commands.ListCategory;
import ayaya.commands.owner.Load;
import ayaya.commands.owner.MusicSwitch;
import ayaya.commands.owner.Unload;
import ayaya.core.enums.CommandCategories;
import ayaya.core.enums.Commands;
import ayaya.core.enums.MusicCommands;
import ayaya.core.exceptions.http.MissingHeaderInfoException;
import ayaya.core.listeners.CommandListener;
import ayaya.core.listeners.EventListener;
import ayaya.core.music.MusicHandler;
import ayaya.core.utils.HTTP;
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
import java.util.List;
import java.util.concurrent.*;

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

    private static final int THREAD_AMOUNT_PER_CORE = 20;
    public static final int INITIAL_AMOUNT = THREAD_AMOUNT_PER_CORE * Runtime.getRuntime().availableProcessors();

    private static JDA ayaya = null;
    private static ThreadPoolExecutor threads;
    private static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(
            INITIAL_AMOUNT, new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public static void main(String[] args) {
        try {
            BotData.refreshJSONData();
            BotData.refreshDBData();
            CommandClient client = buildCommandClient();
            startup(client);
        } catch (IOException e) {
            System.err.println("The configuration file wasn't found. Aborting...");
            e.printStackTrace();
        } catch (SQLException throwables) {
            System.err.println("The database wasn't found or wasn't set up correctly. Aborting...");
            throwables.printStackTrace();
        }
    }

    /**
     * Builds the JDA-Utilities Command Client that will handle all commands.
     *
     * @return the Command Client
     */
    private static CommandClient buildCommandClient() {

        List<String> owners = BotData.getOwners();
        CommandClientBuilder client = new CommandClientBuilder()
                .setOwnerId(owners.get(0))
                .setCoOwnerIds(owners.subList(1, owners.size()).toArray(new String[owners.size()-1]))
                .setPrefix(BotData.getPrefix())
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
            ayaya = JDABuilder.create(BotData.getToken(), intents)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                    .enableCache(CacheFlag.ROLE_TAGS)
                    .addEventListeners(client, new EventWaiter(), new EventListener())
                    .setAudioSendFactory(new NativeAudioSendFactory())
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .setEventPool(executor)
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
        //getBotListsTokens();
        threads.execute(Ayaya::updateBotListsStats);

    }

    /**
     * Changes the status once per minute.
     */
    private static void gameChanger() {

        int status = 0;
        while (ayaya.getPresence().getActivity() != null
                && !ayaya.getPresence().getActivity().getName().equals(SHUTDOWN_GAME)) {

            String quote = BotData.getStatusQuotes().get(status);

            ayaya.getPresence().setActivity(Activity.playing(BotData.getPrefix() + "help | " + quote));
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                //Skip ahead.
            }

            status++;
            if (status == STATUS_AMOUNT) status = 0;

        }

    }

    /**
     * Updates the player count at the Discord Bot List website twice per hour.
     */
    private static void updateBotListsStats() {

        List<String[]> botlists = BotData.getBotlists();
        boolean startThread = false;
        for (String[] array: botlists) {
            String value = array[1];
            if (value != null && !value.isEmpty()) {
                startThread = true;
                break;
            }
        }

        if (startThread) {
            JSONObject json;
            while (ayaya.getPresence().getActivity() != null
                    && !ayaya.getPresence().getActivity().getName().equals(SHUTDOWN_GAME)) {

                try {
                    TimeUnit.MINUTES.sleep(30);

                    for (String[] array: botlists) {
                        String value1 = array[1];
                        String value2 = array[2];
                        String value3 = array[3];
                        if (value1 != null && !value1.isEmpty() &&
                                value2 != null && !value2.isEmpty() &&
                                value3 != null && !value3.isEmpty()) {
                            String[] headers = value3.split(",");
                            json = new JSONObject().put(headers[0], ayaya.getGuilds().size());
                            if (headers.length > 1)
                                json.put(headers[1], 1);
                            if (
                                    HTTP.postJSON(String.format(value2, ayaya.getSelfUser().getId()), json,
                                            "Authorization", value1
                                    )
                            ) System.out.println("Stats successfully posted to " + array[0] + ".");
                            else System.out.println("Failed to post the stats to " + array[0] + ".");
                        }
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