package me.asaushkin.ch02.knn;

import me.asaushkin.ch02.knn.book.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

public class KnnFineGrainedClassifier {

    static final Logger logger = LoggerFactory.getLogger(KnnFineGrainedClassifier.class);

    private List<? extends Sample> dataSet;

    private int k;

    private boolean parallelSort;

    private int numThreads = Runtime.getRuntime().availableProcessors();

    private ExecutorService executor;

    LongAccumulator execAccumulator = new LongAccumulator((a, b) -> a + b, 0);
    LongAccumulator sortAccumulator = new LongAccumulator((a, b) -> a + b, 0);
    LongAccumulator collectAccumulator = new LongAccumulator((a, b) -> a + b, 0);

    LongAdder execCount = new LongAdder();

    public KnnFineGrainedClassifier(List<? extends Sample> dataSet, int k, boolean parallelSort) {
        Objects.requireNonNull(dataSet);

        this.dataSet = dataSet;
        this.k = k;

        this.parallelSort = parallelSort;
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    class IndividualDistanceTask implements Runnable {

        Distance []distances;
        int index;
        Sample localExample, example;
        CountDownLatch countDownLatch;

        public IndividualDistanceTask(Distance []distances, int index,
                                      Sample localExample, Sample example, CountDownLatch countDownLatch) {
            this.distances = distances;
            this.index = index;
            this.localExample = localExample;
            this.example = example;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            distances[index] = new Distance();
            distances[index].setIndex(index);
            distances[index].setDistance(EuclideanDistanceCalculator.calculate(localExample, example));
            countDownLatch.countDown();
        }
    }

    public String classify(Sample example) throws InterruptedException {

        Distance []distances = new Distance[dataSet.size()];

        Instant start = Instant.now();

        int i = 0;
        CountDownLatch endController = new CountDownLatch(dataSet.size());
        for (Sample localExample : dataSet) {
            execCount.increment();
            executor.execute(new IndividualDistanceTask(distances, i++, localExample, example, endController));
        }
        endController.await();

        Instant beginSort = Instant.now();
        execAccumulator.accumulate(Duration.between(start, beginSort).getNano());

        if (parallelSort)
            Arrays.parallelSort(distances);
        else
            Arrays.sort(distances);


        Instant beginCollect = Instant.now();
        sortAccumulator.accumulate(Duration.between(beginSort, beginCollect).getNano());

        Map<String, Integer> result = new HashMap<>();

        for (i = 0; i < k; i++) {
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

        KnnFineGrainedClassifier classifier = new KnnFineGrainedClassifier(train, k, true);
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
        System.out.println("Parallel Fine Grained Classifier - K: " + k);
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
