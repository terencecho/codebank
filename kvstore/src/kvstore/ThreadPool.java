package kvstore;

import java.util.*;


public class ThreadPool {

    /* Array of threads in the threadpool */
    public Thread threads[];
    volatile boolean started;
    Queue<Runnable> jobQueue;

    /**
     * Constructs a Threadpool with a certain number of threads.
     *
     * @param size number of threads in the thread pool
     */
    public ThreadPool(int size) {
        threads = new Thread[size];
        /* begin */
        jobQueue = new LinkedList<Runnable>();
        started = true;
        for (int i = 0; i < size; i++) {
            threads[i] = new WorkerThread(this);
            threads[i].start();
        }
        /* end */
    }

    /**
     * Add a job to the queue of jobs that have to be executed. As soon as a
     * thread is available, the thread will retrieve a job from this queue if
     * if one exists and start processing it.
     *
     * @param r job that has to be executed
     * @throws InterruptedException if thread is interrupted while in blocked
     *         state. Your implementation may or may not actually throw this.
     */
    public void addJob(Runnable r) throws InterruptedException {
        synchronized (jobQueue) {
            jobQueue.add(r);
            jobQueue.notify();
        }
    }

    /**
     * Block until a job is present in the queue and retrieve the job
     * @return A runnable task that has to be executed
     * @throws InterruptedException if thread is interrupted while in blocked
     *         state. Your implementation may or may not actually throw this.
     */
    public Runnable getJob() throws InterruptedException {
        synchronized (jobQueue) {
            while (jobQueue.peek() == null) {
                jobQueue.wait();
            }
            return jobQueue.remove();            
        }
    }
    
    /**
     * Stop all worker thread execution such that server terminates cleanly.
 	 * @see ServerClientHandler, SocketServer
     */
    public void stop() {
    	this.started = false;
    }

    /**
     * A thread in the thread pool.
     */
    public class WorkerThread extends Thread {

        public ThreadPool threadPool;

        /**
         * Constructs a thread for this particular ThreadPool.
         *
         * @param pool the ThreadPool containing this thread
         */
        public WorkerThread(ThreadPool pool) {
            threadPool = pool;
        }

        /**
         * Scan for and execute tasks.
         */
        @Override
        public void run() {
        	/* begin */
            while (threadPool.started == true) {
                try {
                    threadPool.getJob().run();
                } catch (Exception e) {
                    continue;
                }
            }
            /* end */
        }
    }
}
