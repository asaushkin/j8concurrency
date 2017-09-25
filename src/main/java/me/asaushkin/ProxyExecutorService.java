package me.asaushkin;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ProxyExecutorService implements ExecutorService {

    private final ExecutorService target;

    public ProxyExecutorService(ExecutorService target) {
        this.target = target;
    }

    protected ExecutorService getTarget() {
        return target;
    }

    @Override
    public void shutdown() {
        target.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return target.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return target.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return target.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return target.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return target.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return target.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return target.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return target.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return target.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        target.execute(command);
    }

}
