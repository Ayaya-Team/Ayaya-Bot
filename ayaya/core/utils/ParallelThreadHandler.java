package ayaya.core.utils;

import ayaya.core.exceptions.threadhandler.ThreadHandlerInExecutionException;
import ayaya.core.exceptions.threadhandler.ThreadHandlerNotReadyException;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.concurrent.Task;

import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * A thread handler to make RestActions, Tasks and their callbacks run all in parallel.
 *
 * @author 小路綾#7541 (Ayaya)
 * @param <R> RestAction type
 * @param <T> Task type
 */
public class ParallelThreadHandler<R, T> {

    private CommandEvent event;
    private Consumer<CommandEvent> finalCallback;
    private LinkedList<Trio<RestAction<R>, Consumer<R>, Consumer<Throwable>>> restActions;
    private LinkedList<Trio<Task<T>, Consumer<T>, Consumer<Throwable>>> tasks;
    private int threads;
    private boolean executing;

    /**
     * Builds a thread handler with a command event and final callback already included.
     *
     * @param event         the command event
     * @param finalCallback the callback to call after the threads
     */
    public ParallelThreadHandler(CommandEvent event, Consumer<CommandEvent> finalCallback) {
        this.event = event;
        this.finalCallback = finalCallback;
        restActions = new LinkedList<>();
        tasks = new LinkedList<>();
        threads = 0;
        executing = false;
    }

    /**
     * Builds an empty thread handler.
     */
    public ParallelThreadHandler() {
        this.event = null;
        this.finalCallback = null;
        restActions = new LinkedList<>();
        tasks = new LinkedList<>();
        threads = 0;
        executing = false;
    }

    /**
     * Changes the command event of this handler object, so it can be reused.
     *
     * @param event the new command event
     */
    public void setCommandEvent(CommandEvent event) {
        this.event = event;
    }

    /**
     * Changes the final callback of this handler object, so it can be reused.
     *
     * @param finalCallback the new final callback
     */
    public void setFinalCallback(Consumer<CommandEvent> finalCallback) {
        this.finalCallback = finalCallback;
    }

    /**
     * Adds a RestAtion and its callbacks.
     *
     * @param restAction      RestAction to add
     * @param successCallback the success callback
     * @param failureCallback the failure callback
     */
    public synchronized void addRestAction(
            RestAction<R> restAction, Consumer<R> successCallback, Consumer<Throwable> failureCallback
    ) {
        if (executing)
            throw new ThreadHandlerInExecutionException();
        else {
            restActions.addLast(new Trio<>(restAction, successCallback, failureCallback));
            threads++;
        }
    }

    /**
     * Adds a Task and its callbacks
     *
     * @param task            Task to add
     * @param successCallback the success callback
     * @param failureCallback the failure callback
     */
    public synchronized void addTask(
            Task<T> task, Consumer<T> successCallback, Consumer<Throwable> failureCallback
    ) {
        if (executing)
            throw new ThreadHandlerInExecutionException();
        else {
            tasks.addLast(new Trio<>(task, successCallback, failureCallback));
            threads++;
        }
    }

    /**
     * Starts the execution of all threads.
     */
    public void run() {
        if (executing)
            throw new ThreadHandlerInExecutionException();
        if (!isReady())
            throw new ThreadHandlerNotReadyException();
        if (restActions.isEmpty() && tasks.isEmpty()) {
            finalCallback.accept(event);
            return;
        }
        executing = true;
        runThreads();
    }

    private boolean isReady() {
        return event != null && finalCallback != null;
    }

    private void runThreads() {
        while (!restActions.isEmpty()) {
            Trio<RestAction<R>, Consumer<R>, Consumer<Throwable>> restActionRequest =
                    restActions.removeFirst();
            restActionRequest.getFirst().queue(restActionRequest.getSecond(),
                    restActionRequest.getThird());
        }
        while (!tasks.isEmpty()) {
            Trio<Task<T>, Consumer<T>, Consumer<Throwable>> taskRequest =
                    tasks.removeFirst();
            taskRequest.getFirst()
                    .onSuccess(taskRequest.getSecond())
                    .onError(taskRequest.getThird());
        }
    }

    /**
     * When all threads finished, executes the last callback.
     */
    public synchronized void onExecutionFinish() {
        threads--;
        if (threads == 0) {
            finalCallback.accept(event);
            executing = false;
        }
    }

}