package me.asaushkin.ch02.knn;

import me.asaushkin.ch02.knn.book.*;

import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.LongAccumulator;

public class KnnSerialClassifier {

    private List<? extends Sample> dataSet;

    private int k;
    private boolean parallelSort;

    LongAccumulator execAccumulator = new LongAccumulator((a, b) -> a + b, 0);
    LongAccumulator sortAccumulator = new LongAccumulator((a, b) -> a + b, 0);
    LongAccumulator collectAccumulator = new LongAccumulator((a, b) -> a + b, 0);

    public KnnSerialClassifier(List<? extends Sample> dataSet, int k, boolean parallelSort) {
        Objects.requireNonNull(dataSet);

        this.dataSet = dataSet;
        this.k = k;
        this.parallelSort = parallelSort;
    }

    public String classify(Sample example) {
        Distance []distances = new Distance[dataSet.size()];

        Instant start = Instant.now();

        int i = 0;
        for (Sample localExample : dataSet) {
            distances[i] = new Distance();
            distances[i].setIndex(i);
            distances[i].setDistance(EuclideanDistanceCalculator.calculate(localExample, example));
            i++;
        }

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

        KnnSerialClassifier classifier = new KnnSerialClassifier(train, k, false);
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
        }
        System.out.println("******************************************");
        System.out.println("Serial Classifier - K: " + k);
        System.out.println("Is parallel sort: " + classifier.parallelSort);
        System.out.println("Success: " + success);
        System.out.println("Mistakes: " + mistakes);
        System.out.println("Execution Time: " + (currentTime / 1000) + " seconds.");

        System.out.println("Task execution time: " + classifier.execAccumulator.doubleValue()/1_000_000_000 + " seconds.");
        System.out.println("Sort time: " + classifier.sortAccumulator.doubleValue()/1_000_000_000 + " seconds.");
        System.out.println("Collect time: " + classifier.collectAccumulator.doubleValue()/1_000_000_000 + " seconds.");

        System.out.println("******************************************");
    }
}
