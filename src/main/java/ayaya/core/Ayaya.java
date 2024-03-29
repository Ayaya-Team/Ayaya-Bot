package ayaya.core;

import ayaya.commands.Command;
import ayaya.commands.ListCategory;
import ayaya.core.enums.CommandCategories;
import ayaya.core.enums.Commands;
import ayaya.core.enums.MusicCommands;
import ayaya.core.enums.OwnerCommands;
import ayaya.core.listeners.CommandListener;
import ayaya.core.listeners.EventListener;
import ayaya.core.listeners.VoiceEventListener;
import ayaya.core.music.MusicHandler;
import ayaya.core.utils.CustomThreadFactory;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

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
            GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS
    };
    private static final String SHUTDOWN_GAME = "Going to bed.";
    private static final int STATUS_AMOUNT = 7;

    public static final int THREAD_AMOUNT_PER_CORE = 5;
    private static final int INITIAL_AMOUNT = THREAD_AMOUNT_PER_CORE * Runtime.getRuntime().availableProcessors();

    private static ThreadPoolExecutor threads;
    private static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(
            INITIAL_AMOUNT, new CustomThreadFactory("command-thread"), new ThreadPoolExecutor.CallerRunsPolicy()
    );
    private static MusicHandler musicHandler = new MusicHandler();

    public static void main(String[] args) {
        try {
            BotData.refreshJSONData();
            BotData.refreshDBData();
            EventWaiter eventWaiter = new EventWaiter();
            CommandClient client = buildCommandClient(eventWaiter);
            startup(client, eventWaiter);
        } catch (IOException e) {
            System.err.println("The configuration file wasn't found. Aborting...");
            e.printStackTrace();
        } catch (SQLException throwables) {
            System.err.println(
                    "The database wasn't found, wasn't set up correctly or the password is wrong. Aborting..."
            );
            throwables.printStackTrace();
        }
    }

    /**
     * Builds the JDA-Utilities Command Client that will handle all commands.
     *
     * @return the Command Client
     */
    private static CommandClient buildCommandClient(EventWaiter eventWaiter) {

        List<String> owners = BotData.getOwners();
        CommandClientBuilder client = new CommandClientBuilder()
                .setOwnerId(owners.get(0))
                .setCoOwnerIds(owners.subList(1, owners.size()).toArray(new String[owners.size()-1]))
                .setPrefix(BotData.getPrefix())
                .setAlternativePrefix(String.format("<@%s> ", BotData.getId()))
                .setHelpConsumer(null)
                .setHelpWord(null)
                .setEmojis(Emotes.OK_EMOTE, Emotes.WARNING_EMOTE, Emotes.ERROR_EMOTE)
                .setListener(new CommandListener())
                .useHelpBuilder(false);

        loadCommands(client, eventWaiter);

        return client.build();

    }

    /**
     * Loads the commands to the command client
     *
     * @param client the command client
     */
    private static void loadCommands(CommandClientBuilder client, EventWaiter eventWaiter) {

        ListCategory listCategory;
        String categoryName;

        // Load the general purpose commands
        for (Commands command : Commands.values()) {
            client.addCommand(command.getCommand());
            categoryName = command.getCommand().getCategory().getName();
            listCategory = CommandCategories.getListCategory(categoryName);
            if (listCategory != null
                    && !((Command) command.getCommand()).isDisabled()
                    && !command.getCommand().isHidden())
                listCategory.add(command.getName());
        }

        // Load the music commands
        //MusicHandler musicHandler = new MusicHandler();
        for (MusicCommands command : MusicCommands.values()) {
            command.getCommandAsMusicCommand().setMusicHandler(musicHandler);

            client.addCommand(command.getCommand());
            categoryName = command.getCommand().getCategory().getName();
            listCategory = CommandCategories.getListCategory(categoryName);
            if (listCategory != null
                    && !((Command) command.getCommand()).isDisabled()
                    && !command.getCommand().isHidden())
                listCategory.add(command.getName());
        }

        // Load the owner control commands
        for (OwnerCommands command : OwnerCommands.values()) {
            com.jagrosh.jdautilities.command.Command jagroshCommand = command.command();
            Command ayayaCommand = (Command) jagroshCommand;
            if (ayayaCommand.isPaginated())
                ayayaCommand.initPaginator(eventWaiter);
            client.addCommand(jagroshCommand);
            categoryName = jagroshCommand.getCategory().getName();
            listCategory = CommandCategories.getListCategory(categoryName);
            if (listCategory != null && !ayayaCommand.isDisabled())
                listCategory.add(command.getName());
        }

    }

    /**
     * Builds the JDA client and starts up the bot.
     *
     * @param client the Command CLient to handle the commands.
     */
    private static void startup(CommandClient client, EventWaiter eventWaiter) {

        JDA ayaya;
        try {
            Collection<GatewayIntent> intents = Arrays.asList(INTENTS);
            ayaya = JDABuilder.create(BotData.getToken(), intents)
                    .enableCache(CacheFlag.ROLE_TAGS, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                    .addEventListeners(client, eventWaiter, new EventListener(), new VoiceEventListener(musicHandler))
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
                2, 2, 0, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>(),
                new CustomThreadFactory("background-thread")
        );
        threads.execute(() -> gameChanger(ayaya));

    }

    /**
     * Changes the status once per minute.
     */
    private static void gameChanger(JDA ayaya) {

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
     * Forces a disconnection of any voice channel from a given server.
     *
     * @param guild the server to disconnect the voice
     */
    public static void disconnectVoice(Guild guild) {
        musicHandler.forceDisconnect(guild);
    }

    /**
     * Shuts down the threads of the thread pool executor.
     */
    public static void shutdownThreads() {
        threads.shutdownNow();
    }

}