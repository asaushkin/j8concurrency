package me.asaushkin.ch02.knn;

import me.asaushkin.ch02.knn.book.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

public class KnnCoarseGrainedClassifier {

    static final Logger logger = LoggerFactory.getLogger(KnnCoarseGrainedClassifier.class);

    private List<? extends Sample> dataSet;

    private int k;

    private boolean parallelSort;

    private int numThreads = Runtime.getRuntime().availableProcessors();

    private ExecutorService executor;

    LongAccumulator execAccumulator = new LongAccumulator((a, b) -> a + b, 0);
    LongAccumulator sortAccumulator = new LongAccumulator((a, b) -> a + b, 0);
    LongAccumulator collectAccumulator = new LongAccumulator((a, b) -> a + b, 0);

    LongAdder execCount = new LongAdder();

    public KnnCoarseGrainedClassifier(List<? extends Sample> dataSet, int k, boolean parallelSort) {
        Objects.requireNonNull(dataSet);

        this.dataSet = dataSet;
        this.k = k;

        this.parallelSort = parallelSort;
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    class GroupDistanceTask implements Runnable {

        Distance []distances;
        int startIndex, endIndex;
        List<? extends Sample> dataSet;
        Sample example;
        CountDownLatch countDownLatch;

        public GroupDistanceTask(Distance []distances, int startIndex, int endIndex,
                                 List<? extends Sample> dataSet, Sample example, CountDownLatch countDownLatch) {
            this.distances = distances;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.dataSet = dataSet;
            this.example = example;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            for (int i = startIndex; i < endIndex; i++) {
                distances[i] = new Distance();
                distances[i].setIndex(i);
                distances[i].setDistance(EuclideanDistanceCalculator.calculate(dataSet.get(i), example));
            }
            countDownLatch.countDown();
        }
    }

    public String classify(Sample example) throws InterruptedException {

        int totalSize = dataSet.size();
        Distance []distances = new Distance[totalSize];

        Instant start = Instant.now();

        CountDownLatch endController = new CountDownLatch(numThreads);

        int length = totalSize / numThreads;
        int startIndex = 0, endIndex = length;

        for (int j = 0; j < numThreads; j++) {
            execCount.increment();
            executor.execute(new GroupDistanceTask(distances, startIndex, endIndex, dataSet, example, endController));

            startIndex = endIndex;

            if (j == numThreads - 2) { // last iteration
                endIndex = totalSize;
            }
            else {
                endIndex = endIndex + length;
            }
        }

        endController.await();

        Instant beginSort = Instant.now();
        execAccumulator.accumulate(Duration.between(start, beginSort).getNano());

        for (int j = 0; j < distances.length; j++) {
            assert distances[j] != null : "Distance is null: " + j;
        }

        if (parallelSort)
            Arrays.parallelSort(distances);
        else
            Arrays.sort(distances);


        Instant beginCollect = Instant.now();
        sortAccumulator.accumulate(Duration.between(beginSort, beginCollect).getNano());

        Map<String, Integer> result = new HashMap<>();

        for (int i = 0; i < k; i++) {
            Sample localExample = dataSet.get(distances[i].getIndex());
            result.merge(localExample.getTag(), 1, (a, b) -> a + b);
        }

        String key = Collections.max(result.entrySet(), Map.Entry.comparingByValue()).getKey();

        Instant end = Instant.now();
        collectAccumulator.accumulate(Duration.between(beginCollect, end).getNano());

        return key;
    }

    public static void main(String[] args) throws URISyntaxException {

        List<BankMarketing> train = BankMarketingLoader.load("bank.data");
        System.out.println("Train: " + train.size());

        List<BankMarketing> test = BankMarketingLoader.load("bank.test");
        System.out.println("Test: " + test.size());

        double currentTime = 0d;
        int success = 0, mistakes = 0;

        int k = 10;
        if (args.length > 0)
            k = Integer.parseInt(args[0]);

        KnnCoarseGrainedClassifier classifier = new KnnCoarseGrainedClassifier(train, k, false);
        try {
            Date start, end;
            start = new Date();
            for (BankMarketing example : test) {
                String tag = classifier.classify(example);
                if (tag.equals(example.getTag())) {
                    success++;
                } else {
                    mistakes++;
                }
            }
            end = new Date();

            currentTime = end.getTime() - start.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            classifier.close();
        }
        System.out.println("******************************************");
        System.out.println("Parallel Coarse Grained Classifier - K: " + k);
        System.out.println("Is parallel sort: " + classifier.parallelSort);
        System.out.println("Success: " + success);
        System.out.println("Mistakes: " + mistakes);
        System.out.println("Execution Time: " + (currentTime / 1000) + " seconds.");

        System.out.println("Task execution time: " + classifier.execAccumulator.doubleValue()/1_000_000_000 + " seconds.");
        System.out.println("Sort time: " + classifier.sortAccumulator.doubleValue()/1_000_000_000 + " seconds.");
        System.out.println("Collect time: " + classifier.collectAccumulator.doubleValue()/1_000_000_000 + " seconds.");

        System.out.println("Total tasks: " + classifier.execCount);

        System.out.println("******************************************");
    }

    private void close() {
        executor.shutdown();
    }
}
