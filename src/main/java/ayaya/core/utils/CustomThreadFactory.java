package ayaya.core.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

/**
 * Class to generate threads with custom names.
 */
public class CustomThreadFactory implements ThreadFactory {

    private final String threadName;

    public CustomThreadFactory(String name) {
        threadName = name;
    }

    @Override
    public Thread newThread(@NotNull Runnable runnable) {
        return new Thread(runnable, threadName);
    }

}