package com.workday.java;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public class JobRunnerImpl implements JobRunner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Executor threadExecutor;
    private final Map<Long, Integer> customerPriorityMap;

    private volatile boolean shouldContinue = true;
    private volatile boolean shutdownFinished = false;

    public JobRunnerImpl(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final int queueSize) {
        customerPriorityMap = new ConcurrentHashMap<>();
        final BlockingQueue<Runnable> jobQueue = new PriorityBlockingQueue<>(queueSize);
        threadExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, jobQueue);
    }

    @Override
    public void run(final JobQueue jobQueue) {
        try{
            while (shouldContinue) {
                final Job oldJob = jobQueue.pop();
                // calculate priority by summing the job duration
                final int priority = this.customerPriorityMap.merge(oldJob.customerId(), Math.abs(oldJob.duration()), Integer::sum);
                // create a new job wrapper with the calculated priority and sorted priority
                final JobRunnable jobRunnable = new JobRunnable(oldJob, priority);
                System.out.println(jobRunnable);
                this.threadExecutor.execute(jobRunnable);
            }
            logger.info("shutting down");
            shutdownFinished = true;
        } catch (Throwable throwable) {
            logger.error("error occurred", throwable);
        }
    }

    @Override
    public void shutdown() {
        shouldContinue = false;
        while (!shutdownFinished) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                logger.error("Error while shutting down", e);
                throw new RuntimeException(e);
            }
        }
    }
}

/*
 * Wrapper class defines a job with its priority, that implements the Runnable and Comparable interface.
 * This class is used to execute the job and sort the jobs based on the priority.
 */
class JobRunnable implements Runnable, Comparable<JobRunnable> {
    private final Job job;
    private int priority;

    public JobRunnable(final Job job, final int priority) {
        this.job = job;
        this.priority = priority;
    }

    @Override
    public void run() {
        job.execute();
    }

    public long customerId() {
        return job.customerId();
    }

    public int duration() {
        return job.duration();
    }

    public long uniqueId() {
        return job.uniqueId();
    }

    public int priority() {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(final JobRunnable nextJob) {
        return Integer.compare(this.priority(), nextJob.priority());
    }

    @Override
    public String toString() {
        return "JobRunnable{" +
                "job=" + job +
                ", priority=" + priority +
                '}';
    }
}


