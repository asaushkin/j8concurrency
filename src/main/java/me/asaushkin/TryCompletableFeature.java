package me.asaushkin;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class TryCompletableFeature {

    static final Logger logger = LoggerFactory.getLogger(TryCompletableFeature.class);

    static class MyExecutorService extends ProxyExecutorService {

        static final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("executor-%d").build();

        public MyExecutorService() {
            super(Executors.newCachedThreadPool(threadFactory));

            logger.debug("Created an ExecutorService instance");
        }
    }


    public static void main(String[] args) throws InterruptedException, ExecutionException {

        logger.debug("Starting an application");

        ExecutorService service = new MyExecutorService();
        Supplier<String> s = () -> "Completable";

        CompletableFuture<String> future = CompletableFuture.supplyAsync(s,
                service);

        future.
                thenApplyAsync(a -> a + "Feature!").
                thenAcceptAsync(System.out::println);

        future.get();

        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);


        if (!service.isTerminated()) {
            logger.warn("Service wasn't terminated successfully. Force shutdown now.");

            List<Runnable> runnables = service.shutdownNow();

            if (runnables.size() > 0) {
                logger.info("Service was terminated with {} tasks in the queue", runnables.size());
            }

        } else {
            logger.info("Service terminated successfully");
        }
    }
}