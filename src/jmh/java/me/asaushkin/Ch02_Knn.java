package me.asaushkin;

import me.asaushkin.ch02.knn.KnnCoarseGrainedClassifier;
import me.asaushkin.ch02.knn.KnnFineGrainedClassifier;
import me.asaushkin.ch02.knn.KnnSerialClassifier;
import me.asaushkin.ch02.knn.book.BankMarketing;
import me.asaushkin.ch02.knn.book.BankMarketingLoader;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Ch02_Knn {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        List<BankMarketing> train;
        List<BankMarketing> test;
        int k = 10;
        ExecutorService executorService;

        @Setup
        public void initialize() {
            try {
                train = BankMarketingLoader.load("bank.data");
                test  = BankMarketingLoader.load("bank.test");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }

        @TearDown
        public void destroy() {
            executorService.shutdownNow();
        }

        public BankMarketing randomTestSample() {
            int index = ThreadLocalRandom.current().nextInt(0, test.size());
            return test.get(index);
        }
    }

    @Benchmark
    public void coarseGrainedKnnWithoutParallelSort(BenchmarkState state) {
        KnnCoarseGrainedClassifier classifier = new KnnCoarseGrainedClassifier(state.train, state.k,
                false, state.executorService);
        try {
            classifier.classify(state.randomTestSample());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void coarseGrainedKnnWithParallelSort(BenchmarkState state) {
        KnnCoarseGrainedClassifier classifier = new KnnCoarseGrainedClassifier(state.train, state.k,
                true, state.executorService);
        try {
            classifier.classify(state.randomTestSample());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void fineGrainedKnnWithoutParallelSort(BenchmarkState state) {
        KnnFineGrainedClassifier classifier = new KnnFineGrainedClassifier(state.train, state.k,
                false, state.executorService);
        try {
            classifier.classify(state.randomTestSample());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void fineGrainedKnnWithParallelSort(BenchmarkState state) {
        KnnFineGrainedClassifier classifier = new KnnFineGrainedClassifier(state.train, state.k,
                true, state.executorService);
        try {
            classifier.classify(state.randomTestSample());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void serialKnnWithoutParallelSort(BenchmarkState state) {
        KnnSerialClassifier classifier = new KnnSerialClassifier(state.train, state.k, false);
        classifier.classify(state.randomTestSample());
    }

    @Benchmark
    public void serialKnnWithParallelSort(BenchmarkState state) {
        KnnSerialClassifier classifier = new KnnSerialClassifier(state.train, state.k, true);
        classifier.classify(state.randomTestSample());
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + Ch02_Knn.class.getSimpleName() + ".*")
                .warmupIterations(5)
                .measurementIterations(5)
                .mode(Mode.AverageTime)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}