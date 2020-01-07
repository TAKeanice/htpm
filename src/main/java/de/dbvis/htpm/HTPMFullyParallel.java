package de.dbvis.htpm;

import de.dbvis.htpm.constraints.HTPMConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;

import java.util.*;
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

    /* Parallelization overhead does not justify this. Additionally, to fully parallelize
       we would have to call ORAlign in parallel

    protected void calculateBranch(List<PatternOccurrence> m, int depth,
                                   List<List<PatternOccurrence>> partitions, int index) {

        PatternOccurrence first = m.get(index);

        List<Callable<List<Map<HybridTemporalPattern, PatternOccurrence>>>> joinTasks = new ArrayList<>();

        for (int j = index; j < m.size(); j++) {
            PatternOccurrence second = m.get(j);

            joinTasks.add(() -> {
                if (!constraint.patternsQualifyForJoin(first.prefix, first.pattern, second.pattern, depth)) {
                    return Arrays.asList(Collections.emptyMap(), Collections.emptyMap());
                }
                return join(first, second, depth);
            });
        }

        List<Future<List<Map<HybridTemporalPattern, PatternOccurrence>>>> results = miningExecutor.invokeAll(joinTasks);

        for (int j = index; j < m.size(); j++) {
            //parse into pattern occurrences

            List<Map<HybridTemporalPattern, PatternOccurrence>> joined;
            try {
                joined = results.get(j - index).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("could not retrieve result from join", e);
            }

            List<PatternOccurrence> parentFirst = new ArrayList<>(joined.get(0).values());
            partitions.get(index).addAll(parentFirst);

            if (index != j) {
                List<PatternOccurrence> parentSecond = new ArrayList<>(joined.get(1).values());
                partitions.get(j).addAll(parentSecond);
            }
        }
    }*/

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
