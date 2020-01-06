package de.dbvis.htpm;

import de.dbvis.htpm.constraints.HTPMConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class HTPMFullyParallel extends HTPMDFS {

    private final int numThreads;
    private ExecutorService outputExecutor;
    private ForkJoinPool miningExecutor;

    /**
     * Creates a new HTPM-Algorithm-Object.
     *
     * @param d          - The Database containing the series.
     * @param constraint - The constraint determining the pre- and post-joining pruning behavior.
     */
    public HTPMFullyParallel(HybridEventSequenceDatabase d, HTPMConstraint constraint, int numThreads) {
        super(d, constraint);
        this.numThreads = numThreads;
    }

    /**
     * The method that actually runs the algorithm.
     */
    @Override
    public void start() {

        miningExecutor = new ForkJoinPool(numThreads);
        outputExecutor = Executors.newSingleThreadExecutor();

        if (!constraint.shouldGeneratePatternsOfLength(1)) {
            return;
        }

        List<PatternOccurrence> patterns = this.genL1().get(0);

        output(Collections.singletonList(new ArrayList<>(patterns)), 1);

        try {
            int depth = 1;
            if (constraint.shouldGeneratePatternsOfLength(depth + 1)) {
                MiningChunk newChunk = new MiningChunk(patterns, depth, this);
                miningExecutor.invoke(newChunk);
            }
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        miningExecutor.shutdown();
        outputExecutor.shutdown();
        try {
            miningExecutor.awaitTermination(10, TimeUnit.HOURS);
            outputExecutor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Something interrupted the shutdown of the mining and output executors");
            e.printStackTrace();
        }
    }

    @Override
    void output(List<List<PatternOccurrence>> patterns, int depth) {
        outputExecutor.submit(() -> super.output(patterns, depth));
    }

    private static class MiningChunk extends RecursiveAction {

        private final List<PatternOccurrence> m;
        private final int depth;
        private final HTPMFullyParallel htpm;

        MiningChunk(List<PatternOccurrence> m, int depth, HTPMFullyParallel htpm) {
            this.m = m;
            this.depth = depth;
            this.htpm = htpm;
        }

        @Override
        protected void compute() {
            if (!htpm.constraint.branchCanProduceResults(m)) {
                return;
            }

            List<List<PatternOccurrence>> partitions = new ArrayList<>();
            List<MiningChunk> startedTasks = new ArrayList<>();

            for (int i = 0; i < m.size(); i++) {
                partitions.add(new ArrayList<>());
            }

            for (int i = 0; i < m.size(); i++) {
                htpm.calculateBranch(m, depth, partitions, i);
                List<PatternOccurrence> finishedPartition = partitions.get(i);

                //release current pattern, we will not use it any more
                // also removes it from the partitions stored by the calling subroutine
                m.set(i, null);

                //continuously output found patterns
                htpm.output(Collections.singletonList(new ArrayList<>(finishedPartition)), depth);

                if (htpm.constraint.shouldGeneratePatternsOfLength(depth + 1)) {
                    MiningChunk newChunk = new MiningChunk(finishedPartition, depth, htpm);
                    newChunk.fork();
                    startedTasks.add(newChunk);
                }
            }

            startedTasks.forEach(ForkJoinTask::join);
        }
    }
}
