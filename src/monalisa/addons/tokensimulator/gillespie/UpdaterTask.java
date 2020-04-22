/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.gillespie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import monalisa.tools.BooleanChangeEvent;
import monalisa.tools.BooleanChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * After UpdaterTask is started, it requests each running runnable to update its
 * output in equidistant intervals. It also checks, whether a run is still
 * running. If not, it removes the thread which corresponds to that run.
 */
public class UpdaterTask extends TimerTask {

    Lock updaterLock = new ReentrantLock();
    private int MAX_PARALLEL_THREADS;
    private boolean running = true;
    private final GillespieTokenSim gillTS;

    /**
     * Map of currently running threads, linked to the runnable instances they
     * are executing.
     */
    private final HashMap<Thread, ExactSSA> runningThreads;
    /**
     * Runnables of simulation runs which are waiting for their turn.
     */
    private final Queue<ExactSSA> runnablesQueue;

    private final List<BooleanChangeListener> boolListeners = new ArrayList<>();

    private static final Logger LOGGER = LogManager.getLogger(UpdaterTask.class);
    private final File outFile;
    private final List<File> outFiles;
    private final boolean logAll;

    public UpdaterTask(HashMap<Thread, ExactSSA> running, Queue<ExactSSA> queue,
            GillespieTokenSim gillTS, File outputFile, List<File> outputFiles,
            boolean logAll) {
        this.runningThreads = running;
        this.runnablesQueue = queue;
        this.gillTS = gillTS;
        this.logAll = logAll;
        this.outFile = outputFile;
        this.outFiles = outputFiles;
    }

    @Override
    public void run() {
        Boolean alreadyLogged = false;
        updaterLock.lock();
        MAX_PARALLEL_THREADS = Runtime.getRuntime().availableProcessors();
        try {
            /*
            If no simulation is running, cancel the task.
             */
            if (!running) {
                this.cancel();
            }
            /*
            * Update status while at least one simulation is running.
             */
            //Iterate through all thread. Store dead threads.
            ArrayList<Thread> deadThreads = new ArrayList<>(runningThreads.size());
            for (Thread thread : runningThreads.keySet()) {
                /*
                * If a thread is alive, request its runnable to update output.
                 */
                if (thread.isAlive()) {
                    runningThreads.get(thread).updateOutput();
                } /*
                * If a thread is dead, remove it from the threads-HashMap.
                 */ else {
                    deadThreads.add(thread);
                }
            }
            /*
            * remove dead threads
             */
            for (Thread th : deadThreads) {
                runningThreads.remove(th);
            }
            /*
            If runnables are waiting in the queue, create as much threads as were removed.
             */
            while (!runnablesQueue.isEmpty() && runningThreads.size() < MAX_PARALLEL_THREADS && gillTS.isNewThreadAllowed()) {
                ExactSSA run = runnablesQueue.remove();
                gillTS.registerNewThread();
                Thread thread = new Thread(run);
                runningThreads.put(thread, run);
                thread.start();
            }

            /*
            * If no thread is running, set the running-status to false and update the runButton.
             */
            if (runningThreads.isEmpty() && runnablesQueue.isEmpty()) {
                running = false;
                // Fire #DONE
                fireBoolChange(false);
                //enable computation of averages.
                // averagesButton.setEnabled(true);                    

                if (logAll && !alreadyLogged) {
                    alreadyLogged = true;
                    File sumFile = new File(outFile.getParentFile().getAbsolutePath().concat("/summary.csv"));
                    sumFile.createNewFile();
                    Boolean firstLine;
                    Boolean firstFile = true;
                    BufferedReader in;
                    PrintWriter pWriter = new PrintWriter(new BufferedWriter(new FileWriter(sumFile)));
                    try {
                        for (File file : outFiles) {
                            try {
                                if (!firstFile) {
                                    pWriter.println("\t ----- run " + outFiles.indexOf(file) + " -----");
                                }
                                in = new BufferedReader(new FileReader(file));
                                firstLine = true;
                                String line;
                                while ((line = in.readLine()) != null) {
                                    if (firstLine && firstFile) {
                                        pWriter.println(line);
                                        pWriter.println("\t ----- run " + 0 + " -----");
                                    }
                                    if (firstLine) {
                                        firstLine = false;
                                    } else if (!firstLine || firstFile) {
                                        pWriter.println(line);
                                    }
                                }
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            firstFile = false;
                        }
                    } finally {
                        pWriter.flush();
                        pWriter.close();
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.error("IOException while trying to coordinate the multiple threads while performing a stochastic simulation: ", ex);
        } finally {
            updaterLock.unlock();
        }
    }

    public void addBooleanChangeListener(BooleanChangeListener bl) {
        if (!boolListeners.contains(bl)) {
            boolListeners.add(bl);
        }
    }

    private void fireBoolChange(boolean b) {
        BooleanChangeEvent e = new BooleanChangeEvent(this, b);
        for (BooleanChangeListener bl : boolListeners) {
            bl.changed(e);
        }
    }
}
