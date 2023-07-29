package ayaya.core.utils;

import ayaya.core.exceptions.threadhandler.ThreadHandlerNotReadyException;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.concurrent.Task;

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
    private volatile int threads;
    private volatile boolean allThreadsSubmitted;

    /**
     * Builds a thread handler with a command event and a final callback already included.
     *
     * @param event         the command event
     * @param finalCallback the callback to call after the threads
     */
    public ParallelThreadHandler(CommandEvent event, Consumer<CommandEvent> finalCallback) {
        this.event = event;
        this.finalCallback = finalCallback;
        threads = 0;
        allThreadsSubmitted = false;
    }

    /**
     * Builds an empty thread handler.
     */
    public ParallelThreadHandler() {
        this.event = null;
        this.finalCallback = null;
        threads = 0;
        allThreadsSubmitted = false;
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
     * Executes a RestAtion and its callbacks.
     *
     * @param restAction      RestAction to add
     * @param successCallback the success callback
     * @param failureCallback the failure callback
     */
    public void executeRestAction(
            RestAction<R> restAction, Consumer<R> successCallback, Consumer<Throwable> failureCallback
    ) {
        if (!isReady())
            throw new ThreadHandlerNotReadyException();
        restAction.queue(successCallback, failureCallback);
        threads++;
    }

    /**
     * Executes a Task and its callbacks
     *
     * @param task            Task to add
     * @param successCallback the success callback
     * @param failureCallback the failure callback
     */
    public void executeTask(
            Task<T> task, Consumer<T> successCallback, Consumer<Throwable> failureCallback
    ) {
        if (!isReady())
            throw new ThreadHandlerNotReadyException();
        task.onSuccess(successCallback).onError(failureCallback);
        threads++;
    }

    private boolean isReady() {
        return event != null && finalCallback != null;
    }

    /**
     * Tell this handler when all threads are submitted.
     */
    public void submittedAllThreads() {
        allThreadsSubmitted = true;
        if (threads == 0 && allThreadsSubmitted) {
            finalCallback.accept(event);
        }
    }

    /**
     * When all threads finished, executes the last callback.
     */
    public void onExecutionFinish() {
        threads--;
        if (threads == 0 && allThreadsSubmitted) {
            finalCallback.accept(event);
        }
    }

}