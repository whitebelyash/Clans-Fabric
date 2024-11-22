package ru.whbex.develop.clans.fabric.task;

import org.slf4j.event.Level;
import ru.whbex.develop.clans.common.task.Task;
import ru.whbex.develop.clans.common.task.TaskScheduler;
import ru.whbex.lib.log.LogContext;

import java.util.concurrent.*;

public class TaskSchedulerFabric implements TaskScheduler {
    private final ExecutorService dbPool = Executors.newSingleThreadExecutor(); // Using single thread pool for now
    @Override
    public Task run(Runnable runnable) {
        return null;
    }

    @Override
    public Task runAsync(Runnable runnable) {
        return null;
    }

    @Override
    public Task runRepeating(Runnable runnable, long l, long l1) {
        return null;
    }

    @Override
    public Task runRepeatingAsync(Runnable runnable, long l, long l1) {
        return null;
    }

    @Override
    public Task runLater(Runnable runnable, long l) {
        return null;
    }

    @Override
    public Task runLaterAsync(Runnable runnable, long l) {
        return null;
    }

    @Override
    public <T> Future<T> runCallable(Callable<T> callable) {
        return dbPool.submit(callable);
    }

    @Override
    public ExecutorService getDatabasePool() {
        return dbPool;
    }

    @Override
    public void stopAll() {
        LogContext.log(Level.INFO, "Closing database thread pool...");
        if(!dbPool.isShutdown())
            dbPool.shutdown();
        if(!dbPool.isTerminated()){
            LogContext.log(Level.INFO, "Waiting for tasks to terminate...");
            try {
                if(!dbPool.awaitTermination(5, TimeUnit.SECONDS)){
                    LogContext.log(Level.INFO, "Timed out waiting for terminate, ignoring");
                    dbPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                LogContext.log(Level.ERROR, "Interrupted task shutdown wait timeout");
            }
        }
    }
}
