package me.asaushkin.ch02.knn;

import me.asaushkin.ch02.knn.book.*;

import java.net.URISyntaxException;
import java.util.*;

public class KnnClassifier {

    private List<? extends Sample> dataSet;

    private int k;

    public KnnClassifier(List<? extends Sample> dataSet, int k) {
        Objects.requireNonNull(dataSet);

        this.dataSet = dataSet;
        this.k = k;
    }

    public String classify(Sample example) {
        Distance []distances = new Distance[dataSet.size()];

        int i = 0;
        for (Sample localExample : dataSet) {
            distances[i] = new Distance();
            distances[i].setIndex(i);
            distances[i].setDistance(EuclideanDistanceCalculator.calculate(localExample, example));
            i++;
        }

        Arrays.sort(distances);

        Map<String, Integer> result = new HashMap<>();

        for (i = 0; i < k; i++) {
            Sample localExample = dataSet.get(distances[i].getIndex());
            result.merge(localExample.getTag(), 1, (a, b) -> a + b);
        }

        return Collections.max(result.entrySet(), Map.Entry.comparingByValue()).getKey();
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

        KnnClassifier classifier = new KnnClassifier(train, k);
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
        System.out.println("Success: " + success);
        System.out.println("Mistakes: " + mistakes);
        System.out.println("Execution Time: " + (currentTime / 1000)
                + " seconds.");
        System.out.println("******************************************");
    }
}
