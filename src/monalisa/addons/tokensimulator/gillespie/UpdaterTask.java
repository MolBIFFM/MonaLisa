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
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import monalisa.addons.tokensimulator.TokenSimulator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * After UpdaterTask is started, it requests each running runnable to update
 * its output in equidistant intervals. It also checks, whether a run is
 * still running. If not, it removes the thread which corresponds to that
 * run.
 */
public class UpdaterTask extends TimerTask {

    Lock updaterLock = new ReentrantLock();
    private int MAX_PARALLEL_THREADS;
    private boolean running;
    private final GillespieTokenSim gts;
    private final StochasticSimulator sts;

    /**
     * Map of currently running threads, linked to the runnable instances they
     * are executing.
     */
    private final HashMap<Thread, ExactSSA> runningThreads;
    /**
     * Runnables of simulation runs which are waiting for their turn.
     */
    private final Queue<ExactSSA> runnablesQueue;
    
    private static final Logger LOGGER = LogManager.getLogger(UpdaterTask.class);

    public UpdaterTask(HashMap<Thread, ExactSSA> running, Queue<ExactSSA> queue, GillespieTokenSim gts, StochasticSimulator sts) {
        this.runningThreads = running;
        this.runnablesQueue = queue;
        this.gts = gts;
        this.sts = sts;
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
            while (!runnablesQueue.isEmpty() && runningThreads.size() < MAX_PARALLEL_THREADS && gts.isNewThreadAllowed()) {
                ExactSSA run = runnablesQueue.remove();
                gts.registerNewThread();
                Thread thread = new Thread(run);
                runningThreads.put(thread, run);
                thread.start();
            }

            /*
            * If no thread is running, set the running-status to false and update the runButton.
             */
            if (runningThreads.isEmpty() && runnablesQueue.isEmpty()) {
                running = false;
                sts.getRunButton().setText(TokenSimulator.strings.get("ATSFireTransitionsB"));
                sts.getRunButton().setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/run_tools.png")));
                //enable computation of averages.
                // averagesButton.setEnabled(true);                    

                if (sts.isLogAll() && !alreadyLogged) {
                    alreadyLogged = true;
                    File sumFile = new File(sts.getOutputFile().getParentFile().getAbsolutePath().concat("/summary.csv"));
                    sumFile.createNewFile();
                    Boolean firstLine;
                    Boolean firstFile = true;
                    BufferedReader in;
                    PrintWriter pWriter = new PrintWriter(new BufferedWriter(new FileWriter(sumFile)));
                    try {
                        for (File file : sts.getOutputFiles()) {
                            try {
                                if (!firstFile) {
                                    pWriter.println("\t ----- run " + sts.getOutputFiles().indexOf(file) + " -----");
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
            LOGGER.error("IOException while trying to coordinate the multiple threads while performing a stochastic simulation");
        } finally {
            updaterLock.unlock();
        }
    }
}

