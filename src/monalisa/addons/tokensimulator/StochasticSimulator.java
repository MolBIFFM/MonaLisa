/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.HighQualityRandom;
import monalisa.util.MonaLisaFileChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This class allows a simulation of a chemical system using the Gillespie-algorithm for stochastic simulations of mass-action-kinetic driven
 * reactions. It uses a Petri Net as a mathematical description of a chemical system.
 * @author Pavel Balazki.
 */
public class StochasticSimulator extends javax.swing.JFrame {
    //BEGIN VARIABLES DECLARATION
    //-----------------Places---------------
    /**
     * Array with IDs of all non-constant places of the underlying Petri net.
     */
    private final int[] nonConstantPlaceIDs;
    /**
     * Array with names of all non-constant places of the underlying Petri net.
     */
    private final String[] nonConstantPlaceNames;
    /**
     * Array with number of tokens on all non-constant places of the underlying Petri net at the
     * beginning of the simulation (initial state).
     */
    private final long[] nonConstantInitialMarking;
    /**
     * Array with IDs of all constant places of the underlying Petri net.
     */
    private final int[] constantPlaceIDs;
    /**
     * Array with names of all constant places of the underlying Petri net.
     */
    private final String[] constantPlaceNames;
    /**
     * Array with mathematical expressions which describe
     * the number of tokens on all constant places of the underlying Petri net.
     */
    private final MathematicalExpression[] constantPlacesExp;
    
    //-----------------Transitions---------------
    /**
     * Array with IDs of all transitions of the underlying Petri net.
     */
    private final int[] reactionIDs;
    /**
     * Array with names of all transitions of the underlying Petri net.
     */
    private final String[] reactionNames;
    /**
     * Array with mathematical expressions which describe 
     * reaction rate constants of the reactions (transitions of the underlying Petri net).
     */
    private final MathematicalExpression[] reactionRateConstants;
    /**
     * Order and multiplier of reactions. For each reaction, an array is stored
     * with the order of the reaction on the first place and the multiplier
     * (used for calculating stochastic reaction rate constant) on the second.
     */
    private final int[][] reactionOrder;
    /**
     * Stoichiometric matrix of reactions and their educts. First index is the reaction, second index is
     * the educt of it. The compounds of non-constant places are stored first, followed by the compounds of constant
     * places. The value is the number of educt molecules which are consumed by the reaction.
     */
    private final int[][] eductStoichMatrix;
    /**
     * Stoichiometric matrix of reactions and their products. First index is the reaction, second index is
     * the product of it. The compounds of non-constant places are stored first, followed by the compounds of constant
     * places. The value is the number of product molecules which are produced by the reaction.
     */
    private final int[][] productStoichMatrix;
    /**
     * Array of lists which store indeces of reactions which are influenced by a compound with index i.
     * Used for determining which reaction rates should be re-computed.
     */
    private final Set<Integer>[] compoundsInfluence;
    /**
     * For each reaction, a set of indeces of non-constant places on which the reaction rate depends.
     */
    private final Set<Integer>[] reactionsNonConstantEducts;
    /**
     * For each reaction, a set of indeces of constant places on which the reaction rate depends.
     */
    private final Set<Integer>[] reactionsConstantEducts;
    /**
     * For each reaction, a set of indeces of non-constant places which are the products of this reaction.
     */
    private final Set<Integer>[] reactionsNonConstantProducts;
    /**
     * Set of transitions (reactions) which have constant places as pre-places.
     * The rates of this reactions should be re-calculated every step.
     * However, if the mathematical expression of the constant pre-place is also constant 
     * (has no variables and is not dependent on time), the transition is not in the list.
     * Also contains indeces of transitions which rates have constant places or time as variables.
     */
    protected final Set<Integer> constantPlacesPostTransitions = new HashSet<>();
    /**
     * Volume of the simulated system.
     */
    private final double volume;
    /**
     * Volume multiplied with the Avogadro constant.
     */
    private final double volMol;
    
    /**
     * File where the simulation output is written to (csv formatted).
     * If several parallel simulation runs are started, only the output of the first run is written to this file.
     * For each additional run, a new file with incrementing index is created.
     */
    private File outputFile;
    private List<File> outputFiles;
    
    /**
     * Indicates whether at least one simulation is running.
     */
    protected boolean running = false;
    /**
     * Instance of the token simulator which started this stochastic simulation.
     * Enables access to the Petri net and visual output updating routine.
     */
    private final GillespieTokenSim ts;
    /**
     * Number of simulation threads which can run simulataneously. If more simulation runs are started, they are put in a 
     * queue and executed as soon as capacities are available.
     */
    private int MAX_PARALLEL_THREADS = Runtime.getRuntime().availableProcessors();
    /**
     * Interval in which the updater task is performed. milliseconds.
     */
    private final int UPDATER_TASK_INTERVAL = 3000;
    /**
     * List of SimulationRunnable instances which are currently being executed. Each runnable is a simulation run.
     */
    private final ArrayList<ExactSSA> runnables = new ArrayList<>();
    /**
     * Map of currently running threads, linked to the runnable instances they are executing.
     */
    private final HashMap<Thread, ExactSSA> runningThreads = new HashMap<>();
    /**
     * Runnables of simulation runs which are waiting for their turn.
     */
    private final Queue<ExactSSA> runnablesQueue = new LinkedList<>();
    /**
     * Maximal time to simulate.
     */
    private double maxSimTime = 0.0;
    /**
     * Time interval in which the results will be written into the output file.
     */
    private double updateInterval = 0.0;
    /**
     * Random number generator. Used for generating seeds of new random generators of the simulation runs.
     */
    private HighQualityRandom globalRandom;
    /**
     * Object which processes the output files, e.g. crates a new file with the averages of all simulation runs.
     */
//    private final OutputProcessor outputProcessor = new OutputProcessor();
    //END VARIABLES DECLARATION
    
    //BEGIN INNER CLASSES
    /**
     * This runnable is used to start an exact stochastic simulation. Every reaction occurrence will be simulated.
     * Good for small molecule numbers with observable stochastic effects, but very slow with large molecule numbers.
     * It is possible to start several threads with the instances of this class, so several simulations
     * of the same system will run parallel.
     */
    protected class ExactSSA implements Runnable{
        protected final int[] nonConstantPlaceIDsRun;
        protected final int[] constantPlaceIDsRun;
        protected final MathematicalExpression[] constantPlacesExpRun;
        /**
         * Number of molecules of the compounds which are represented by non-constant places.
         */
        protected final long[] nonConstantMarkingRun;
        /**
         * Number of molecules of the compounds which are represented by the constant places.
         */
        protected final long[] constantMarkingRun;
        /**
         * IDs of places mapped to concentrations of the compounds.
         */
        protected final HashMap<Integer, Double> concentrations;
        /**
        * Array with mathematical expressions which describe 
        * reaction rate constants of the reactions (transitions of the underlying Petri net).
        */
        protected final MathematicalExpression[] reactionRateConstantsRun;
        /**
         * Reaction rates of active transitions. Keys are indeces of reactions, values are corresponding reaction rates.
         * Reactions which rates are 0 are not stored.
         */
        protected final HashMap<Integer, Double> reactionRatesRun;
        /**
         * Sum of reaction rates.
         */
        protected double sumOfRates;
        /**
         * Save the index of reactions rates should be updated. Only update rates of reactions which
         * are dependent of compounds with changed molecule numbers.
         */
        protected final Set<Integer> reactionsToUpdate;
        
        /**
         * Lock object which is used to lock critical areas.
         */
        protected final Lock lock = new ReentrantLock();
        //Output files of the current runnable.
        protected final File outputFileRun;
        /*
         * TextArea with the output information. Displays the number of simulated steps and simulated time.
         */
        protected final JTextArea outTextArea = new JTextArea();
        protected StochasticSimulatorRunPanel outPanel;
        
        /**
         * Counter for the time which was simulated.
         */
        protected double timePassed;
        /**
         * Stores at which time last output update happened.
         */
        protected double lastUpdate = 0.0;
        /**
         * Counter for the simulated steps.
         */
        protected long stepsSimulated;
        /**
         * Indicates whether the thread of this runnable is running.
         */
        protected boolean isRunning;
        /*
         * BufferedWriter used for creating output.
         */
        protected BufferedWriter outputWriter;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        /**
         * Random number generator.
         */
        protected final HighQualityRandom randomRun;

        /**
         * Create a new instance of runnable.
         * @param outF File where the output will be written to.
         * @param runNr Number (index) of simulation run.
         */
        public ExactSSA(File outF, int runNr){
            this.randomRun = new HighQualityRandom(globalRandom.nextLong());
            this.outputFileRun = outF;
            this.timePassed = 0;
            this.stepsSimulated = 0;
            this.reactionsToUpdate = new HashSet<>();
            
            this.nonConstantPlaceIDsRun = new int[nonConstantPlaceIDs.length];
            System.arraycopy(nonConstantPlaceIDs, 0, this.nonConstantPlaceIDsRun, 0, nonConstantPlaceIDs.length);
            this.constantPlaceIDsRun = new int[constantPlaceIDs.length];
            System.arraycopy(constantPlaceIDs, 0, this.constantPlaceIDsRun, 0, constantPlaceIDs.length);
            this.constantPlacesExpRun = new MathematicalExpression[constantPlacesExp.length];
            for (int i = 0; i < constantPlacesExp.length; i++){
                try {
                    this.constantPlacesExpRun[i] = new MathematicalExpression(constantPlacesExp[i]);
                } catch (UnknownFunctionException | UnparsableExpressionException ex) {
                    Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.reactionRateConstantsRun = new MathematicalExpression[reactionRateConstants.length];
            for (int i = 0;  i< reactionRateConstants.length; i++){
                try {
                    this.reactionRateConstantsRun[i] = new MathematicalExpression(reactionRateConstants[i]);
                } catch (UnknownFunctionException | UnparsableExpressionException ex) {
                    Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            /*
            Copy initial marking of non-constant places and calculate corresponding concentrations.
            */
            this.concentrations = new HashMap<>();
            this.nonConstantMarkingRun = new long[nonConstantInitialMarking.length];
            for (int i = 0; i < nonConstantMarkingRun.length; i++){
                this.nonConstantMarkingRun[i] = nonConstantInitialMarking[i];
                this.concentrations.put(this.nonConstantPlaceIDsRun[i], this.nonConstantMarkingRun[i] / volMol);
            }
            /*
            Evaluate initial marking of constant places and calculate corresponding concentrations.
            */
            this.constantMarkingRun = new long[this.constantPlacesExpRun.length];
            for (int i = 0; i < this.constantMarkingRun.length; i++){
                MathematicalExpression exp = this.constantPlacesExpRun[i];
                double val = exp.evaluate(concentrations, this.timePassed);
                this.constantMarkingRun[i] = Math.round(val * volMol);
                this.concentrations.put(constantPlaceIDs[i], val);
            }
            /*
            Compute initial reaction rates.
            */
            this.reactionRatesRun = new HashMap<>();
            this.sumOfRates = 0;
            for (int reactionIdx = 0; reactionIdx < reactionIDs.length; reactionIdx++){
                double reactionRate = this.computeReactionRate(reactionIdx);
                this.sumOfRates += reactionRate;
                /*
                Create an entry in the map only if the reaction rate is greater than 0.
                */
                if(reactionRate > 0){
                    this.reactionRatesRun.put(reactionIdx, reactionRate);
                }
            }
            
            /*
             * Add a tab with a text area which shows the information about this run.
             */
            this.outTextArea.setEditable(false);
            outPanel = new StochasticSimulatorRunPanel(this, nonConstantPlaceNames, constantPlaceNames);
            
            this.outTextArea.setText(TokenSimulator.strings.get("SimStartedAt").concat(dateFormat.format(Calendar.getInstance().getTime())));
            simRunsTabbedPane.addTab("Simulation run "+ runNr, outPanel);
            
            /*
             * Create the header of output file.
             */
            StringBuilder outSB = new StringBuilder();
            outSB.append("Step\tTime[sec]\tReaction");
            /*
             * Write the names of non-constant places.
             */
            for (String name : nonConstantPlaceNames){
                outSB.append("\t").append(name);
            }
            /*
             * Write the names of constant places.
             */
            for (String name : constantPlaceNames){
                outSB.append("\t").append(name);
            }
            outSB.append("\n0\t0\tnone");
            /*
             * Write the initial number of molecules for non-constant places.
             */
            for (long nr : nonConstantMarkingRun){
                outSB.append("\t").append(nr);
            }
            /*
             * Write the initial number of molecules for constant places.
             */
            for (long nr : constantMarkingRun){
                outSB.append("\t").append(nr);
            }
            outSB.append("\n");
            try {
                outputWriter = new BufferedWriter(new FileWriter(outputFileRun));
                outputWriter.write(outSB.toString());                
                outputWriter.close();                
            } catch (IOException ex) {
                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        @Override
        public void run() {
            outPanel.showPlotButton.setEnabled(false);
            try {
                outputWriter = new BufferedWriter(new FileWriter(outputFileRun, true));
            } catch (IOException ex) {
                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.isRunning = true;
            try {
                /*
                 * As long as at least one reaction can take place, execute simulation. A single simulation step consists of calculating reaction rates (which
                 * are dependent on the number of compound molecules), choosing the time of next reaction, choosing next reaction, "executing" the reaction
                 * by adapting compound numbers according to the reaction and writing the results to a file.
                 */
                while(isRunning){
                    /*
                     * Calculate rates of reactions which 
                     educt numbers changed. A rate of a reaction is defined as a product of stochastic reaction constant with
                     the number distinct combinations 
                     * of educt molecules. If the reaction does not have required amount of educts, its reaction rate is set to 0. If at least one reaction
                     * has a rate greater than 0, reactionPossible is set to true.
                     * If multiple identical molecules (represented by an arc weight > 1) are involved,
                     * the number of combinations is a binomial coefficient "n choose k" with n = number of
                     * the molecules in current state and
                     * k = weight of the arc from the pre-place to the transition.
                     */
                    for (int reactionIdx : reactionsToUpdate){
                        double reactionRate = computeReactionRate(reactionIdx);
                        /*
                        Substract old reaction rate from the sum.
                        */
                        Double oldRate = reactionRatesRun.get(reactionIdx);
                        if (oldRate != null){
                            sumOfRates -= oldRate;
                        }                        
                        /*
                        If new reaction rate is greater than 0, put it into the map. Otherwise remove the entry
                        of the reaction from the map.
                        */
                        if(reactionRate > 0){
                            reactionRatesRun.put(reactionIdx, reactionRate);
                            /*
                            Add new reaction rate to the sum.
                            */
                            sumOfRates += reactionRate;
                        }
                        else{
                            reactionRatesRun.remove(reactionIdx);
                        }
                    }
                    /*
                     * Check whether the sum of rates is empty, which indicates that no reaction can occur. If so, abort simulation.
                     */
                    if(sumOfRates <= 0){
                        break;
                    }
                    /*
                    Remove all reactions from the reactionsToUpdate-set except for the reactions with constant pre-places (educts).
                    */
                    reactionsToUpdate.clear();
                    reactionsToUpdate.addAll(constantPlacesPostTransitions);

                    /*
                     * Random number are used for determining next reaction time and the reaction which will occur.
                     */
                    double r1 = randomRun.nextDouble();
                    double r2 = randomRun.nextDouble();

                    /*
                     * Calculate the time at which next reaction will occur.
                     */
                    double nextFiringTime = (Math.log(1 / r1) / sumOfRates);
                    //check if the simulation should go on or the maximal time is reached.
                    if (maxSimTime > 0 && (timePassed + nextFiringTime) > maxSimTime){
                        this.requestStop();
                        break;
                    }

                    /*
                     * Datermine which reaction will occur next.
                     * int reactionIdx is the index of the reaction which will be chosen.
                     * double rateSum is the sum of reaction rates which are previous to the chosen one in the reactionRates list.
                     * Choose the reaction so the sum of reactions which are previous plus the rate of chosen one is greater than or equal to 
                     * the sum of all reaction rates multiplied by r2.
                     */
                    int reaction = 0;
                    double ratesSum = 0;
                    for (Entry<Integer, Double> entr : reactionRatesRun.entrySet()){
                        reaction = entr.getKey();
                        double reactionRate = entr.getValue();
                        ratesSum += reactionRate;
                        if (ratesSum >= sumOfRates * r2){
                            break;
                        }
                    }

                    /*
                     * Update step and time counters.
                     */
                    this.stepsSimulated++;
                    this.timePassed += nextFiringTime;

                    if(this.timePassed >= this.lastUpdate + updateInterval) {
                        writeOutput(reaction);
                    }                    
                    
                    /*
                     * Adapt coumpounds molecule numbers according to the chosen reaction. Iterate through 
                    the non-constant conpounds of the reaction and substract the stoichiometric factor from the number of molecules
                    of this compound.
                     */
                    for (int eductIdx : reactionsNonConstantEducts[reaction]){
                        /*
                        Get the number of molecules of the educt and substract the stoichiometric factor from it.
                        */
                        long nrOfMolecules = nonConstantMarkingRun[eductIdx] - eductStoichMatrix[reaction][eductIdx];
                        /*
                        Update the marking.
                        */
                        nonConstantMarkingRun[eductIdx] = nrOfMolecules;
                        /*
                        Update the concentrations entry.
                        */
                        concentrations.put(this.nonConstantPlaceIDsRun[eductIdx], nrOfMolecules / volMol);
                        /*
                        Mark all reactions which are dependent from this educt so the reaction rates will be re-calculated.
                        */
                        reactionsToUpdate.addAll(compoundsInfluence[eductIdx]);
                    }
                    for (int productIdx : reactionsNonConstantProducts[reaction]){
                        /*
                        Get the number of molecules of the educt and add the stoichiometric factor from it.
                        */
                        long nrOfMolecules = nonConstantMarkingRun[productIdx] + productStoichMatrix[reaction][productIdx];
                        /*
                        Update the marking.
                        */
                        nonConstantMarkingRun[productIdx] = nrOfMolecules;
                        /*
                        Update the concentrations entry.
                        */
                        concentrations.put(this.nonConstantPlaceIDsRun[productIdx], nrOfMolecules / volMol);
                        /*
                        Mark all reactions which are dependent from this educt so the reaction rates will be re-calculated.
                        */
                        reactionsToUpdate.addAll(compoundsInfluence[productIdx]);
                    }
                    /*
                    Update marking and concentration for constant places
                    */
                    for (int i = 0; i < this.constantMarkingRun.length; i++){
                        MathematicalExpression exp = this.constantPlacesExpRun[i];
                        double val = exp.evaluate(concentrations, this.timePassed);
                        this.constantMarkingRun[i] = Math.round(val * volMol);
                        this.concentrations.put(this.constantPlaceIDsRun[i], val);
                    }

                    //Write updated molecule numbers into output file.
                    if (updateInterval == 0 || timePassed - lastUpdate >= updateInterval){
                        writeOutput(reaction);
                    }
                }
                //close writers
                outputWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.outTextArea.append(("\n").concat(TokenSimulator.strings.get("SimFinished")).concat(dateFormat.format(Calendar.getInstance().getTime())));
            this.updateOutput();
            outPanel.showPlotButton.setEnabled(true);
            ts.checkOutRunningThread();
        }
        
        /**
         * This method is used to write the current state of simulation run (number of simulated steps and simulated time) 
         * to the outTextArea.
         */
        public void updateOutput(){
            lock.lock();
            try {
                this.outTextArea.append("\nSteps: " + this.stepsSimulated + "; time passed: " + Utilities.secondsToTime(timePassed));
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * Stop execution of the simulation run.
         */
        public void requestStop(){
            lock.lock();
            try {
                isRunning = false;
                this.outTextArea.append("\n".concat(TokenSimulator.strings.get("SimStopping")));
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * Export current state of simulation (nr. of compound molecules) to the Petri net and update the visual output of the PN.
         * Also update the time counter of TokenSimulator.
         */
        public void exportMarking(){
            lock.lock();
            try {
                /*
                Iterate through non-constant places.
                */
                for (int i = 0; i < nonConstantPlaceIDs.length; i++){
                    ts.tokenSim.setTokens(nonConstantPlaceIDs[i], nonConstantMarkingRun[i]);
                }
                ((GillespieTokenSim) ts).setSimulatedTime(timePassed);
            } catch (TokenSimulator.PlaceConstantException ex) {
                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * Creates a window with results of selected places plotted with JFreeChart.
         * Results are read from the output file.
         * @param nonConstantPlaces List of indexes of non-constant places.
         * @param constantPlaces List of indexes of constant places.
         */
        public void showPlot(List<Integer> nonConstantPlaces, List<Integer> constantPlaces){
            final List<Integer> nonConstantPlacesF = nonConstantPlaces;
            final List<Integer> constantPlacesF = constantPlaces;
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    //CHECK FOR THE SIZE OF THE OUTPUT FILE AND SHOW A WARNING
                    //IF IT IS TOO BIG (> 5 MB)
                    int proceed = JOptionPane.OK_OPTION;
                    if (outputFileRun.length() > 1024*1024*5){
                        proceed = JOptionPane.showConfirmDialog(null, "The number of timepoints is too large. This could result in long plotting time and even make the program unresponsive." +
                                " Are you sure you wat to continue? (Hint: You can limit the number of timepoints by increasing the update interval.)", "Warning!", JOptionPane.OK_CANCEL_OPTION);
                    }
                    try {
                        if (proceed == JOptionPane.OK_OPTION){
                            //Create an array of XYSeries for places.
                            XYSeries[] nonConstantPlotSeries = new XYSeries[nonConstantPlaceNames.length];
                            for (int i = 0; i < nonConstantPlaceNames.length; i++){
                                String name  = nonConstantPlaceNames[i];
                                nonConstantPlotSeries[i] = new XYSeries(name, false);
                            }
                            XYSeries[] constantPlotSeries = new XYSeries[constantPlaceNames.length];
                            for (int i = 0; i < constantPlaceNames.length; i++){
                                String name  = constantPlaceNames[i];
                                constantPlotSeries[i] = new XYSeries(name, false);
                            }
                            BufferedReader in = new BufferedReader(new FileReader(outputFileRun));
                            String line;
                            in.readLine();
                            while((line = in.readLine()) != null){
                                String[] entr = line.split("\t");
                                try{
                                    double time = Double.parseDouble(entr[1]);
                                    for (int i = 0; i < nonConstantMarkingRun.length; i++){
                                        nonConstantPlotSeries[i].add(time, Long.parseLong(entr[i+3]), false);
                                    }
                                    for (int i = 0; i < constantMarkingRun.length; i++){
                                        constantPlotSeries[i].add(time, Long.parseLong(entr[i+3+nonConstantPlotSeries.length]), false);
                                    }
                                }
                                catch(Exception ex){
                                    Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            XYSeriesCollection chartDataset = new XYSeriesCollection();
                            for (int idx : nonConstantPlacesF){
                                chartDataset.addSeries(nonConstantPlotSeries[idx]);
                            }
                            for (int idx : constantPlacesF){
                                chartDataset.addSeries(constantPlotSeries[idx]);
                            }

                            /*
                            Generate graph.
                            */
                            final JFreeChart chart = ChartFactory.createScatterPlot("Simulation results",
                                    "Passed time [sec]", "Nr. of molecules", chartDataset,
                                    PlotOrientation.VERTICAL, true, false, false);
                                    ChartFrame frame = new ChartFrame("Simulation results", chart);

                            frame.setSize(800,600);
                            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            //                chartFrame.getContentPane().add(cp);
                            frame.setVisible(true);
                        }
                        
                    } catch (IOException ex) {
                        Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });
        }
        
        /**
         * Compute the rate of the reaction with index idx at current state. If the reaction does not have required amount of educts, its reaction rate is set to 0.
         * @param idx Index of the reaction for which the rate will be computed.
         * @return Reaction rate (depends on rate constant, current marking and volume).
         */
        protected double computeReactionRate(int idx){
                /*
                * Evaluate deterministic reaction rate constant
                */
                double detReactionRateConst = reactionRateConstantsRun[idx].evaluate(concentrations, timePassed);
                /*
                * Convert deterministic reaction rate constant to stochastic one. Get the order and the multiplier of the reaction.
                */
                int order = reactionOrder[idx][0];
                int multiplier = reactionOrder[idx][1];
                double stochReactionRateConst;
                switch(order){
                    case 0:
                        stochReactionRateConst = detReactionRateConst * volMol;
                        break;
                    case 1:
                        stochReactionRateConst = detReactionRateConst;
                        break;
                    case 2:
                        stochReactionRateConst = (detReactionRateConst * multiplier) / volMol;
                        break;
                    default:
                        double volMolPow = 1;
                        for (int i = 0; i < order-1; i++){
                            volMolPow *= volMol;
                        }
                        stochReactionRateConst = (detReactionRateConst * multiplier) / volMolPow;
                        break;
                }
                //number if distinct combinations of educt molecules.
                long h = 1;
                
                /*
                Iterate through all non-constant educts of the reaction. Get the number of molecules of the educt and the
                stoichiometric factor and calculate the number of distinct molecular combinations.
                */
                for (int eductIdx : reactionsNonConstantEducts[idx]){
                    //stoichiometric factor
                    int weight = eductStoichMatrix[idx][eductIdx];
                    long tokens = this.nonConstantMarkingRun[eductIdx];
                    if (weight > tokens){
                        return 0;
                    }
                    //update the number of distinct combinations.
                    h *= Utilities.binomialCoefficient(tokens, weight);
                }
                /*
                Iterate through all constant educts of the reaction. Get the number of molecules of the educt and the
                stoichiometric factor and calculate the number of distinct molecular combinations.
                */
                for (int eductIdx : reactionsConstantEducts[idx]){
                    //stoichiometric factor
                    int weight = eductStoichMatrix[idx][eductIdx + nonConstantPlaceIDs.length];
                    long tokens = this.constantMarkingRun[eductIdx];
                    if (weight > tokens){
                        return 0;
                    }
                    //update the number of distinct combinations.
                    h *= Utilities.binomialCoefficient(tokens, weight);
                }
                return h * stochReactionRateConst;
        }
        
        protected void writeOutput(Integer... reactions){
            lastUpdate += updateInterval;
            double timeForLog;
            if(updateInterval == 0.0) {
                timeForLog = timePassed;
            } else {
                timeForLog = lastUpdate;
            }
            StringBuilder outSB = new StringBuilder();
            outSB.append(stepsSimulated).append("\t").append(timeForLog).append("\t");
            for (int idx : reactions){
                outSB.append(reactionNames[idx]).append(";");
            }
            outSB.deleteCharAt(outSB.length()-1);
            
            /*
             * Write molecule numbers for non-constant places.
             */
            for (long nrOfMolecules : nonConstantMarkingRun){
                outSB.append("\t").append(nrOfMolecules);
            }
            /*
             * Write molecule numbers for constant places.
             */
            for (long nrOfMolecules : constantMarkingRun){
                outSB.append("\t").append(nrOfMolecules);
            }
            outSB.append("\n");
            try {
                outputWriter.write(outSB.toString());
            } catch (IOException ex) {
                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
    
    private class TauLeapingSSA extends ExactSSA{
        /**
         * A set containing indeces of reactions that are critical.
         */
        private final Set<Integer> criticalReactions = new HashSet<>();
        /**
         * Minimal number of reaction occurrences that must be possible before reaction
         * is regarded as critical.
         */
        private final static int CRITICAL_THRESHOLD = 20;
        private final static double EPS = 0.03;
        
        public TauLeapingSSA(File outF, int runNr) {
            super(outF, runNr);
            
            /*
            Check which reactions are critical at the beginning of the simulation.
            */
            for (int reactionIdx : reactionRatesRun.keySet()){
                /*
                minL is the number of times this reaction can occur before one of its reactants depletes.
                */
                int minL = CRITICAL_THRESHOLD;
                /*
                Iterate through non-constant educts of this reaction.
                */
                for (int eductIdx : reactionsNonConstantEducts[reactionIdx]){
                    //Number of molecules of the educt.
                    long eductNr = this.nonConstantMarkingRun[eductIdx];
                    //Stoichiometric factor of the reaction
                    int factor = eductStoichMatrix[reactionIdx][eductIdx];
                    /*
                    If the factor is 0
                    */
                    minL = Math.min(minL, Math.round(eductNr / factor));
                }
                for (int eductIdx : reactionsConstantEducts[reactionIdx]){
                    //Number of molecules of the educt.
                    long eductNr = this.constantMarkingRun[eductIdx];
                    //Stoichiometric factor of the reaction
                    int factor = eductStoichMatrix[reactionIdx][eductIdx + nonConstantPlaceIDs.length];
                    minL = Math.min(minL, Math.round(eductNr / factor));
                }
                /*
                If the number of executable reactions is less than the critical threshold, the reaction
                is considered as critical.
                */
                if (minL < CRITICAL_THRESHOLD){
                    this.criticalReactions.add(reactionIdx);
                }
            }
        }
        @Override
        public void run() {
            outPanel.showPlotButton.setEnabled(false);
            /*
            List of reactions which fired in this step. Used for output.
            */
            Set<Integer> firedReactions = new HashSet<>();
            try {
                outputWriter = new BufferedWriter(new FileWriter(outputFileRun, true));
            } catch (IOException ex) {
                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.isRunning = true;
            try {
                /*
                 * As long as at least one reaction can take place, execute simulation. A single simulation step consists of calculating reaction rates (which
                 * are dependent on the number of compound molecules), choosing the time of next reaction, choosing next reaction, "executing" the reaction
                 * by adapting compound numbers according to the reaction and writing the results to a file.
                 */
                while(isRunning){
                    /*
                     * Check whether the sum of rates is empty, which indicates that no reaction can occur. If so, abort simulation.
                     */
                    if(sumOfRates <= 0){
                        break;
                    }
                    firedReactions.clear();
                    updateReactions();
                    /*
                    Get a set of all non-critical reaction indeces.
                    */
                    Set<Integer> nonCriticalReactions = new HashSet<>();
                    for (int reactionIdx : this.reactionRatesRun.keySet()){
                        if (!this.criticalReactions.contains(reactionIdx)){
                            nonCriticalReactions.add(reactionIdx);
                        }
                    }
                    /*
                    Compute next firing time of non critical reactions
                    */
                    double nextFiringTimeNonCritical;
                    if (maxSimTime > 0){
                        if (maxSimTime - timePassed <= 0){
                            this.requestStop();
                            break;
                        }
                        nextFiringTimeNonCritical = maxSimTime - timePassed;
                    }
                    else{
                        nextFiringTimeNonCritical = Double.MAX_VALUE;
                    }
                    if (!nonCriticalReactions.isEmpty()){
                        for (int compoundIdx = 0; compoundIdx < compoundsInfluence.length; compoundIdx++){
                            Set<Integer> reactions = compoundsInfluence[compoundIdx];
                            if (reactions.isEmpty()){
                                continue;
                            }
                            double my = 0;
                            double sigma = 0;
                            for (int reactionIdx : reactions){
                                Double tmpRate = reactionRatesRun.get(reactionIdx);
                                if (tmpRate != null){
                                    int stoichFactor = eductStoichMatrix[reactionIdx][compoundIdx];
                                    my += -stoichFactor * reactionRatesRun.get(reactionIdx);
                                    sigma += stoichFactor* stoichFactor * tmpRate;
                                }
                            }
                            double scaledNr = Math.max(EPS * nonConstantMarkingRun[compoundIdx], 1);

                            nextFiringTimeNonCritical = Math.min(nextFiringTimeNonCritical, (scaledNr / Math.abs(my)));
                            nextFiringTimeNonCritical = Math.min(nextFiringTimeNonCritical, ((scaledNr*scaledNr) / sigma));
                        }
                    }
                    if (nextFiringTimeNonCritical < (1 / sumOfRates)*10){
                        for (short i = 0; i < 100; i++){
                            int reactionIdx = exactStep();
                            if (reactionIdx == -1){
                                break;
                            }
                        }
                        continue;
                    }
                    /*
                    Compute the sum of critical reaction rates
                    */
                    double criticalSumOfRates = 0;
                    for (int idx : criticalReactions){
                        criticalSumOfRates += reactionRatesRun.get(idx);
                    }

                    /*
                     * Calculate the time at which next critical reaction will occur.
                     */
                    double nextFiringTimeCritical = Double.MAX_VALUE;
                    if (criticalSumOfRates > 0){
                        nextFiringTimeCritical = (Math.log(1 / randomRun.nextDouble()) / criticalSumOfRates);
                    }

                    //only non-critical reactions will occur
                    if (nextFiringTimeNonCritical < nextFiringTimeCritical){
                        //check if the simulation should go on or the maximal time is reached.
                        if (maxSimTime > 0 && (timePassed + nextFiringTimeNonCritical) > maxSimTime){
                            this.requestStop();
                            break;
                        }
                        for (int reactionIdx : nonCriticalReactions){
                            int nrOccu = randomRun.nextPoisson(reactionRatesRun.get(reactionIdx) * nextFiringTimeNonCritical);
                            for (int i = 0; i < nrOccu; i++){
                                firedReactions.add(reactionIdx);                                    
                                fireReaction(reactionIdx);
                            }
                        }
                        /*
                         * Update step and time counters.
                         */
                        this.stepsSimulated++;
                        this.timePassed += nextFiringTimeNonCritical;
                    }
                    else{
                        //check if the simulation should go on or the maximal time is reached.
                        if (maxSimTime > 0 && (timePassed + nextFiringTimeCritical) > maxSimTime){
                            this.requestStop();
                            break;
                        }
                        /*
                        * Datermine which reaction will occur next.
                        * int reactionIdx is the index of the reaction which will be chosen.
                        * double rateSum is the sum of reaction rates which are previous to the chosen one in the reactionRates list.
                        * Choose the reaction so the sum of reactions which are previous plus the rate of chosen one is greater than or equal to 
                        * the sum of all reaction rates multiplied by r2.
                        */
                        int reaction = 0;
                        double ratesSum = 0;
                        for (int reactionIdx : criticalReactions){
                            reaction = reactionIdx;
                            double reactionRate = reactionRatesRun.get(reactionIdx);
                            ratesSum += reactionRate;
                            if (ratesSum >= criticalSumOfRates * randomRun.nextDouble()){
                                break;
                            }
                        }
                        firedReactions.add(reaction);
                        fireReaction(reaction);
                        for (int reactionIdx : nonCriticalReactions){
                            int nrOccu = randomRun.nextPoisson(reactionRatesRun.get(reactionIdx) * nextFiringTimeCritical);
                            for (int i = 0; i < nrOccu; i++){
                                firedReactions.add(reactionIdx);
                                fireReaction(reactionIdx);
                            }
                        }

                        /*
                         * Update step and time counters.
                         */
                        this.stepsSimulated++;
                        this.timePassed += nextFiringTimeCritical;
                    }
                    //Write updated molecule numbers into output file.
                    if (updateInterval == 0 || timePassed - lastUpdate >= updateInterval){
                        writeOutput(firedReactions.toArray(new Integer[firedReactions.size()]));
                    }
                }
                //close writers
                outputWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.outTextArea.append(("\n").concat(TokenSimulator.strings.get("SimFinished")).concat(dateFormat.format(Calendar.getInstance().getTime())));
            this.updateOutput();
            outPanel.showPlotButton.setEnabled(true);
            ts.checkOutRunningThread();
        }
        
        private void updateReactions(){
            for (int reactionIdx : reactionsToUpdate){
                double reactionRate = computeReactionRate(reactionIdx);
                /*
                Substract old reaction rate from the sum.
                */
                Double oldRate = reactionRatesRun.get(reactionIdx);
                if (oldRate != null){
                    sumOfRates -= oldRate;
                }
                /*
                If new reaction rate is greater than 0, put it into the map. Otherwise remove the entry
                of the reaction from the map.
                */
                if(reactionRate > 0){
                    //Check if reaction became critical.
                    /*
                    minL is the number of times this reaction can occur before one of its reactants depletes.
                    */
                    int minL = CRITICAL_THRESHOLD;
                    /*
                    Iterate through non-constant educts of this reaction.
                    */
                    for (int eductIdx : reactionsNonConstantEducts[reactionIdx]){
                        //Number of molecules of the educt.
                        long eductNr = this.nonConstantMarkingRun[eductIdx];
                        //Stoichiometric factor of the reaction
                        int factor = eductStoichMatrix[reactionIdx][eductIdx];
                        minL = Math.min(minL, Math.round(eductNr / factor));
                    }
                    for (int eductIdx : reactionsConstantEducts[reactionIdx]){
                        //Number of molecules of the educt.
                        long eductNr = this.constantMarkingRun[eductIdx];
                        //Stoichiometric factor of the reaction
                        int factor = eductStoichMatrix[reactionIdx][eductIdx + nonConstantPlaceIDs.length];
                        minL = Math.min(minL, Math.round(eductNr / factor));
                    }
                    /*
                    If the number of executable reactions is less than the critical threshold, the reaction
                    is considered as critical.
                    */
                    if (minL < CRITICAL_THRESHOLD){
                        this.criticalReactions.add(reactionIdx);
                    }
                    else{
                        this.criticalReactions.remove(reactionIdx);
                    }

                    reactionRatesRun.put(reactionIdx, reactionRate);
                    /*
                    Add new reaction rate to the sum.
                    */
                    sumOfRates += reactionRate;
                }
                else{
                    reactionRatesRun.remove(reactionIdx);
                    this.criticalReactions.remove(reactionIdx);
                }
            }
            /*
            Remove all reactions from the reactionsToUpdate-set except for the reactions with constant pre-places (educts).
            */
            reactionsToUpdate.clear();
            reactionsToUpdate.addAll(constantPlacesPostTransitions);
        }
        
        /**
         * Performs an exact step of SSA.
         * @return index of occurred reaction.
         */
        private int exactStep(){
            updateReactions();
            /*
             * Check whether the sum of rates is empty, which indicates that no reaction can occur. If so, abort simulation.
             */
            if(sumOfRates <= 0){
                return -1;
            }
            /*
            * Random number are used for determining next reaction time and the reaction which will occur.
            */
            double r2 = randomRun.nextDouble();

            /*
            * Calculate the time at which next reaction will occur.
            */
            double nextFiringTime = (Math.log(1 / randomRun.nextDouble()) / sumOfRates);
            //check if the simulation should go on or the maximal time is reached.
            if (maxSimTime > 0 && (timePassed + nextFiringTime) > maxSimTime){
                this.requestStop();
                return -1;
            }

            /*
            * Datermine which reaction will occur next.
            * int reactionIdx is the index of the reaction which will be chosen.
            * double rateSum is the sum of reaction rates which are previous to the chosen one in the reactionRates list.
            * Choose the reaction so the sum of reactions which are previous plus the rate of chosen one is greater than or equal to
            * the sum of all reaction rates multiplied by r2.
            */
            int reaction = 0;
            double ratesSum = 0;
            for (int reactionIdx : reactionRatesRun.keySet()){
                reaction = reactionIdx;
                double reactionRate = reactionRatesRun.get(reactionIdx);
                ratesSum += reactionRate;
                if (ratesSum >= sumOfRates * r2){
                    break;
                }
            }

            /*
            * Update step and time counters.
            */
            this.stepsSimulated++;
            this.timePassed += nextFiringTime;
            
            if(this.timePassed >= this.lastUpdate + updateInterval) {
                writeOutput(reaction);
            }
                        
            //fire selected reaction
            fireReaction(reaction);

            if (updateInterval == 0 || timePassed - lastUpdate >= updateInterval){
                writeOutput(reaction);
            }
            return reaction;
        }
        
        private void fireReaction(int reactionIdx){
            /*
            * Adapt coumpounds molecule numbers according to the chosen reaction. Iterate through
            the non-constant conpounds of the reaction and substract the stoichiometric factor from the number of molecules
            of this compound.
            */
            for (int eductIdx : reactionsNonConstantEducts[reactionIdx]){
                /*
                Get the number of molecules of the educt and remove the stoichiometric factor from it.
                */
                long nrOfMolecules = nonConstantMarkingRun[eductIdx] - eductStoichMatrix[reactionIdx][eductIdx];
                //Ensure that no negative values are produced
                if(nrOfMolecules < 0){
                    System.out.println(nrOfMolecules);
                    nrOfMolecules = Math.max(nrOfMolecules, 0);
                }
                /*
                Update the marking.
                */
                nonConstantMarkingRun[eductIdx] = nrOfMolecules;
                /*
                Update the concentrations entry.
                */
                concentrations.put(this.nonConstantPlaceIDsRun[eductIdx], nrOfMolecules / volMol);
                /*
                Mark all reactions which are dependent from this educt so the reaction rates will be re-calculated.
                */
                reactionsToUpdate.addAll(compoundsInfluence[eductIdx]);
            }
            for (int productIdx : reactionsNonConstantProducts[reactionIdx]){
                /*
                Get the number of molecules of the educt and add the stoichiometric factor from it.
                */
                long nrOfMolecules = nonConstantMarkingRun[productIdx] + productStoichMatrix[reactionIdx][productIdx];                    
                /*
                Update the marking.
                */
                nonConstantMarkingRun[productIdx] = nrOfMolecules;
                /*
                Update the concentrations entry.
                */
                concentrations.put(this.nonConstantPlaceIDsRun[productIdx], nrOfMolecules / volMol);
                /*
                Mark all reactions which are dependent from this educt so the reaction rates will be re-calculated.
                */
                reactionsToUpdate.addAll(compoundsInfluence[productIdx]);
            }
            /*
            Update marking and concentration for constant places
            */
            for (int i = 0; i < this.constantMarkingRun.length; i++){
                MathematicalExpression exp = this.constantPlacesExpRun[i];
                double val = exp.evaluate(concentrations, this.timePassed);
                this.constantMarkingRun[i] = Math.round(val * volMol);
                this.concentrations.put(this.constantPlaceIDsRun[i], val);
            }
        }
    }
    
    /**
     * After UpdaterTask is started, it requests each running runnable to update its output in equidistant intervals.
     * It also checks, whether a run is still running. If not, it removes the thread which corresponds to that run.
     */
    private class UpdaterTask extends TimerTask{
        Lock updaterLock = new ReentrantLock();
        @Override
        public void run() {
            Boolean alreadyLogged = false;
            updaterLock.lock();
            MAX_PARALLEL_THREADS = Runtime.getRuntime().availableProcessors();
            try {
                /*
                If no simulation is running, cancel the task.
                */
                if (!running){
                    this.cancel();
                }
                /*
                * Update status while at least one simulation is running.
                */
                //Iterate through all thread. Store dead threads.
                ArrayList<Thread> deadThreads = new ArrayList<>(runningThreads.size());
                for (Thread thread : runningThreads.keySet()){
                    /*
                    * If a thread is alive, request its runnable to update output.
                    */
                    if (thread.isAlive()){
                        runningThreads.get(thread).updateOutput();
                    }
                    /*
                    * If a thread is dead, remove it from the threads-HashMap.
                    */
                    else{
                        deadThreads.add(thread);
                    }
                }
                /*
                * remove dead threads
                */
                for (Thread th : deadThreads){
                    runningThreads.remove(th);
                }
                /*
                If runnables are waiting in the queue, create as much threads as were removed.
                */
                while (!runnablesQueue.isEmpty() && runningThreads.size() < MAX_PARALLEL_THREADS && ts.isNewThreadAllowed()){
                    ExactSSA run = runnablesQueue.remove();
                    ts.registerNewThread();
                    Thread thread = new Thread(run);
                    runningThreads.put(thread, run);
                    thread.start();
                }
                
                /*
                * If no thread is running, set the running-status to false and update the runButton.
                */
                if (runningThreads.isEmpty() && runnablesQueue.isEmpty()){
                    running = false;
                    runButton.setText(TokenSimulator.strings.get("ATSFireTransitionsB"));
                    runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/run_tools.png")));
                    //enable computation of averages.
                    // averagesButton.setEnabled(true);                    
                    
                    if(createLogOfAllRuns.isSelected() && !alreadyLogged) {                           
                        alreadyLogged = true;
                        File sumFile = new File(outputFile.getParentFile().getAbsolutePath().concat("/summary.csv"));
                        sumFile.createNewFile();
                        Boolean firstLine;
                        Boolean firstFile = true;
                        BufferedReader in;
                        PrintWriter pWriter = new PrintWriter(new BufferedWriter(new FileWriter(sumFile)));
                        try {
                            for(File file : outputFiles) {
                                try {
                                    if(!firstFile) {
                                        pWriter.println("\t ----- run "+outputFiles.indexOf(file)+" -----"); 
                                    }
                                    in = new BufferedReader(new FileReader(file));                                    
                                    firstLine = true;
                                    String line;
                                    while ((line = in.readLine()) != null) {
                                        if(firstLine && firstFile) {
                                            pWriter.println(line); 
                                            pWriter.println("\t ----- run "+0+" -----"); 
                                        }
                                        if(firstLine) {
                                            firstLine = false;                                            
                                        } else if(!firstLine || firstFile) {
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
                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                updaterLock.unlock();
            }
        }
    }
    
    /**
     * A class which performs operations with the output files. Can compute the averages of all simulation runs.
     */
//    private class OutputProcessor{
//        protected boolean computingAverages = false;
//        Thread runningJob;
//        File processorOutFile;
//        
//        public Map<Double, List<Long>> processFile(File inFile, double updateInterval, double maxTime) {
//            Map<Double, List<Long>> output = new TreeMap<>();
//            double processedTime = 0.0;
//            try {
//                try (BufferedReader reader = new BufferedReader(new FileReader(inFile))) {
//                    String currLine;
//                    currLine = reader.readLine();
//                    String[] currLineSplitted = currLine.split("\t");
//                    int nrOfPlaces = currLineSplitted.length - 3;
//
//                    double oldTime = 0;
//                    List<Long> oldVals = new ArrayList<>(nrOfPlaces+1);
//                    //Read the first line and fill the values.
//                    if ((currLine = reader.readLine()) != null) {
//                        currLineSplitted = currLine.split("\t");
//                        //Iterate through all places
//                        for (int i = 3; i < nrOfPlaces + 3; i++) {
//                            oldVals.add(Long.parseLong((currLineSplitted[i])));
//                        }
//                        output.put(oldTime, new ArrayList<>(oldVals));
//                        processedTime += updateInterval;
//                    }
//                    //Read line by line
//                    currLine = reader.readLine();
//                    currLineSplitted = currLine.split("\t");
//                    while (processedTime <= maxTime) {
//                        //Time value of the current line.
//                        double time = Double.parseDouble(currLineSplitted[1]);
//                        //The time in the current line is the one we are looking for. Perfect!
//                        if (time == processedTime) {
//                            List<Long> currVals = new ArrayList<>(nrOfPlaces+1);
//                            oldTime = time;
//                            for (int i = 3; i < nrOfPlaces + 3; i++) {
//                                currVals.add(Long.parseLong(currLineSplitted[i]));
//                                oldVals.set(i - 3, Long.parseLong(currLineSplitted[i]));
//                            }
//                            output.put(processedTime, new ArrayList<>(currVals));
//                            processedTime += updateInterval;
//                            if ((currLine = reader.readLine()) == null) {
//                                break;
//                            }
//                            currLineSplitted = currLine.split("\t");
//                            continue;
//                        }
//                        //The time in the current line is before the one we are looking for. It is then
//                        //definitely closer than the oldTime, but the next time could be better.
//                        if (time < processedTime) {
//                            oldTime = time;
//                            for (int i = 3; i < nrOfPlaces + 3; i++) {
//                                oldVals.set(i - 3, Long.parseLong(currLineSplitted[i]));
//                            }
//                            if ((currLine = reader.readLine()) == null) {
//                                break;
//                            }
//                            currLineSplitted = currLine.split("\t");
//                            continue;
//                        }
//                        //The time in the current line is after the one we are looking for. Check which time is closer - 
//                        //the actual one or the oldTime.
//                        if (Math.abs(processedTime - oldTime) < Math.abs(processedTime - time)) {
//                            output.put(processedTime, new ArrayList<>(oldVals));
//                            processedTime += updateInterval;
//                            continue;
//                        }
//                        oldTime = time;
//                        List<Long> currVals = new ArrayList<>();
//                        for (int i = 3; i < nrOfPlaces + 3; i++) {
//                            try{
//                                currVals.add(Long.parseLong(currLineSplitted[i]));
//                            }
//                            catch(Exception ex){
//                                System.out.println(currLineSplitted[0]);
//                            }
//                            oldVals.set(i - 3, Long.parseLong(currLineSplitted[i]));
//                        }
//                        if ((currLine = reader.readLine()) == null) {
//                            break;
//                        }
//                        currLineSplitted = currLine.split("\t");
//                        output.put(processedTime, new ArrayList<>(currVals));
//                        processedTime += updateInterval;
//                    }
//                }
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IOException ex) {
//                Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            return output;
//        }
//        
//        public void computeAverages(){
//            runningJob = new Thread(new Runnable(){
//                @Override
//                public void run() {
//                    int nrOfFiles = runnables.size();
//                    double updateInter = Math.max(1, updateInterval);
//                    averagesProgressBar.setMaximum(nrOfFiles);
//                    //Path of the source file.
//                    String path =outPathField.getText();
//                    String ext;                    
//                    //cut off the extension
//                    if(path.contains(".")){
//                        ext = path.substring(path.lastIndexOf('.'));
//                        path = path.substring(0, path.lastIndexOf('.'));
//                    }else{
//                        ext = "csv";
//                    }
//                    //Output file.
//                    processorOutFile = new File(path + "_averages.csv");
//                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(processorOutFile))) {
//                        Map<Double, List<Long>> sumOfTimeVals = new TreeMap<>();
//                        Map<Double, List<Long>> minVals = new TreeMap<>();
//                        Map<Double, List<Long>> maxVals = new TreeMap<>();
//                        Map<Double, Integer> nrOfOccurs = new TreeMap<>();
//                        for (int i = 0; i < nrOfFiles; i++) {
//                            averagesProgressBar.setValue(i+1);
//                            File inputF;
//                            if (i == 0) {
//                                inputF = new File(path  + ext);
//                                //Write labels
//                                writer.write("Time");
//                                String[] vals = new BufferedReader(new FileReader(inputF)).readLine().split("\t");
//                                for (int k = 3; k < vals.length; k++) {
//                                    writer.write("\t" + vals[k]);
//                                    writer.write("\t" + vals[k] + "_max");
//                                    writer.write("\t" + vals[k] + "_min");
//                                }
//                                writer.write("\n");
//                            } else {
//                                inputF = new File(path + "_" + i + ext);
//                            }
//                            for (Entry<Double, List<Long>> entr : processFile(inputF, updateInter, maxSimTime).entrySet()) {
//                                double timeVal = entr.getKey();                                
//                                if (sumOfTimeVals.containsKey(timeVal)) {
//                                    for (int j = 0; j < entr.getValue().size(); j++) {
//                                        sumOfTimeVals.get(timeVal).set(j, sumOfTimeVals.get(timeVal).get(j) + entr.getValue().get(j));
//                                        maxVals.get(timeVal).set(j, Math.max(maxVals.get(timeVal).get(j), entr.getValue().get(j)));
//                                        minVals.get(timeVal).set(j, Math.min(minVals.get(timeVal).get(j), entr.getValue().get(j)));
//                                    }
//                                    nrOfOccurs.put(timeVal, nrOfOccurs.get(timeVal) + 1);
//                                } else {
//                                    sumOfTimeVals.put(timeVal, new ArrayList<Long>());
//                                    maxVals.put(timeVal, new ArrayList<Long>());
//                                    minVals.put(timeVal, new ArrayList<Long>());
//                                    for (int j = 0; j < entr.getValue().size(); j++) {
//                                        sumOfTimeVals.get(timeVal).add(entr.getValue().get(j));
//                                        maxVals.get(timeVal).add(entr.getValue().get(j));
//                                        minVals.get(timeVal).add(entr.getValue().get(j));
//                                    }
//                                    nrOfOccurs.put(timeVal, 1);
//                                }
//                            }
//                        }
//                        //Write averages into file
//                        for (double timeVal : nrOfOccurs.keySet()) {
//                            int occurs = nrOfOccurs.get(timeVal);
//                            writer.write(String.valueOf(timeVal));
//                            for (int i = 0; i < sumOfTimeVals.get(timeVal).size(); i++){
//                                long val = sumOfTimeVals.get(timeVal).get(i);
//                                long max = maxVals.get(timeVal).get(i);
//                                long min = minVals.get(timeVal).get(i);
//                                long avg = Math.round(val / occurs);
//                                writer.write("\t" + avg);
//                                writer.write("\t" + max);
//                                writer.write("\t" + min);
//                            }
//                            writer.write("\n");
//                        }
//                    } catch (IOException ex) {
//                        JOptionPane.showMessageDialog(StochasticSimulator.this, TokenSimulator.strings.get("CannotCreateFiles"),
//                        TokenSimulator.strings.get("Error"), JOptionPane.ERROR_MESSAGE);
//                    }
//                    averagesProgressBar.setValue(0);
//                }
//            });
//            runningJob.start();
//        }
//        
//        public void abortComputeAverages(){
//            runningJob.interrupt();
//            processorOutFile.delete();
//        }
//    }
    //END INNER CLASSES
    
    //BEGIN CONSTRUCTORS
    /**
     * Creates new form StochasticSimulator and extracts the information of the chemical system from given data (Petrinet, reaction constants etc.).
     * @param tsN
     * @param detReactionConstants
     * @param nonConstantPlaces
     * @param vol
     * @param ran
     */
    public StochasticSimulator(GillespieTokenSim tsN, Map<Integer, MathematicalExpression> detReactionConstants, Map<Integer, Long> nonConstantPlaces,
            double vol, HighQualityRandom ran) {
        this.volume = vol;
        this.volMol = this.volume * 6E23;
        /*
         * Create the default output file.
         */
        this.outputFile = new File(System.getProperty("user.home").concat(File.separator).concat("StochasticSimulation").concat(File.separator).
                concat("StochasticSim.csv"));
        this.outputFiles = new ArrayList<>();
        this.ts = tsN;
        PetriNetFacade petriNet = ts.petriNet;
        /*
        Iterate through places of the net and create entries of compounds.
        */
        //Keys are IDs, values are indeces.
        Map<Integer, Integer> constantPlacesIDsMap = new HashMap<>();
        ArrayList<String> constantPlacesNamesList = new ArrayList<>();
        ArrayList<MathematicalExpression> constantPlacesMathExpList = new ArrayList<>();
        //Keys are IDs, values are indeces.
        Map<Integer, Integer> nonConstantPlacesIDsMap = new HashMap<>();
        ArrayList<String> nonConstantPlacesNamesList = new ArrayList<>();
        ArrayList<Long> nonConstantPlacesMarkingList = new ArrayList<>();
        int nonConstantIdx = 0;
        int constantIdx = 0;
        for (Place place : petriNet.places()){
            int id = place.id();
            String name = place.getProperty("name");
            if (!place.isConstant()){
                nonConstantPlacesIDsMap.put(id, nonConstantIdx++);
                nonConstantPlacesNamesList.add(name);
                nonConstantPlacesMarkingList.add(this.ts.getTokens(id));
            }
            else{
                constantPlacesIDsMap.put(id, constantIdx++);
                constantPlacesNamesList.add(name);
                constantPlacesMathExpList.add(this.ts.tokenSim.getMathematicalExpression(id));
            }
        }
        //non-constant places
        this.nonConstantPlaceIDs = new int[nonConstantPlacesIDsMap.size()];
        for (Map.Entry<Integer, Integer> entr : nonConstantPlacesIDsMap.entrySet()){
            this.nonConstantPlaceIDs[entr.getValue()] = entr.getKey();
        }
        this.nonConstantPlaceNames = new String[nonConstantPlacesNamesList.size()];
        for (int i = 0; i < nonConstantPlacesNamesList.size(); i++){
            this.nonConstantPlaceNames[i] = nonConstantPlacesNamesList.get(i);
        }
        this.nonConstantInitialMarking = new long[nonConstantPlacesMarkingList.size()];
        for (int i = 0; i < nonConstantPlacesMarkingList.size(); i++){
            this.nonConstantInitialMarking[i] = nonConstantPlacesMarkingList.get(i);
        }
        //constant places
        this.constantPlaceIDs = new int[constantPlacesIDsMap.size()];
        for (Map.Entry<Integer, Integer> entr : constantPlacesIDsMap.entrySet()){
            this.constantPlaceIDs[entr.getValue()] = entr.getKey();
        }
        this.constantPlaceNames = new String[constantPlacesNamesList.size()];
        for (int i = 0; i < constantPlacesNamesList.size(); i++){
            this.constantPlaceNames[i] = constantPlacesNamesList.get(i);
        }
        this.constantPlacesExp = new MathematicalExpression[constantPlacesMathExpList.size()];
        for (int i = 0; i < constantPlacesMathExpList.size(); i++){
            this.constantPlacesExp[i] = constantPlacesMathExpList.get(i);
        }
        this.compoundsInfluence = new Set[nonConstantPlaceIDs.length];
        for (int i = 0; i < compoundsInfluence.length; i++){
            this.compoundsInfluence[i] = new HashSet<>();
        }
        
        /*
        Iterate through transitions and create entries of reactions.
        */
        int reactionIdx = -1;
        this.reactionIDs = new int[petriNet.transitions().size()];
        this.reactionNames = new String[petriNet.transitions().size()];
        this.reactionRateConstants = new MathematicalExpression[petriNet.transitions().size()];
        this.reactionOrder = new int[petriNet.transitions().size()][2];
        this.eductStoichMatrix = new int[petriNet.transitions().size()][petriNet.places().size()];
        this.productStoichMatrix = new int[petriNet.transitions().size()][petriNet.places().size()];
        this.reactionsConstantEducts = new Set[petriNet.transitions().size()];
        for (int i = 0; i < reactionsConstantEducts.length; i++){
            this.reactionsConstantEducts[i] = new HashSet<>();
        }
        this.reactionsNonConstantEducts = new Set[petriNet.transitions().size()];
        for (int i = 0; i < reactionsNonConstantEducts.length; i++){
            this.reactionsNonConstantEducts[i] = new HashSet<>();
        }
        this.reactionsNonConstantProducts = new Set[petriNet.transitions().size()];
        for (int i = 0; i < reactionsNonConstantProducts.length; i++){
            this.reactionsNonConstantProducts[i] = new HashSet<>();
        }
        for (Transition transition : petriNet.transitions()){
            reactionIdx++;
            int id = transition.id();
            String name = transition.getProperty("name");
            
            this.reactionIDs[reactionIdx] = id;
            this.reactionNames[reactionIdx] = name;
            this.reactionRateConstants[reactionIdx] = detReactionConstants.get(id);
            /*
            * In reactions with order greater than or equal to 2, the reaction rate constant k is multiplied with the
            * factorial of each arc weight
            */
            int multiplier = 1;
            /*
             * Determine which order the reaction has.
             */
            int order = 0;
            for (Place p : petriNet.getInputPlacesFor(transition)){
                int weight = petriNet.getArc(p, transition).weight();
                try {
                    multiplier *= Utilities.factorial((long) weight);
                } catch (Utilities.FactorialTooBigException ex) {
                    try {
                        multiplier *= Utilities.factorial(20L);             
                    } catch(Utilities.FactorialTooBigException ex2) { 
                        System.out.println("This should never ever happen");
                    }                    
                }
                order += weight;
            }
            int[] inf = {order,multiplier};
            this.reactionOrder[reactionIdx] = inf;
            
            /*
            Iterate through pre- and post-places and fill the stoichiometric map.
            */
            for (Place p : transition.inputs()){
                int pId = p.id();
                int weight = petriNet.getArc(p, transition).weight();
                if (!p.isConstant()){
                    //Index of the compound
                    int pIdx = nonConstantPlacesIDsMap.get(pId);
                    eductStoichMatrix[reactionIdx][pIdx] = weight;
                    /*
                    Transition with index reactionIdx is influenced by the place p.
                    */
                    compoundsInfluence[pIdx].add(reactionIdx);
                    
                    /*
                    Place p is an educt of transition with index reactionIdx.
                    */
                    reactionsNonConstantEducts[reactionIdx].add(pIdx);
                }
                else{
                    /*
                    If a transition has a constant pre-place, its reaction rate must be recalculated at each step.
                    */
                    if (!ts.tokenSim.getMathematicalExpression(pId).isConstant()){
                        constantPlacesPostTransitions.add(reactionIdx);
                    }
                    //Index of the compound
                    int pIdx = constantPlacesIDsMap.get(pId);
                    eductStoichMatrix[reactionIdx][constantPlacesIDsMap.get(pId) + nonConstantPlaceIDs.length] = weight;
                    /*
                    Place p is an educt of transition with index reactionIdx.
                    */
                    reactionsConstantEducts[reactionIdx].add(pIdx);
                }
            }
            for (Place p : transition.outputs()){
                int pId= p.id();
                int weight = petriNet.getArc(transition, p).weight();
                if (!p.isConstant()){
                    //Index of the compound
                    int pIdx = nonConstantPlacesIDsMap.get(pId);
                    productStoichMatrix[reactionIdx][pIdx] = weight;
                    
                    /*
                    Place p is a product of transition with index reactionIdx.
                    */
                    reactionsNonConstantProducts[reactionIdx].add(pIdx);
                }
                else{
                    //Index of the compound
                    int pIdx = constantPlacesIDsMap.get(pId);
                    productStoichMatrix[reactionIdx][pIdx + nonConstantPlaceIDs.length] = weight;
                }
            }
            /*
            Also check, what variables are used in the mathematical expressions describing the reaction
            rate constant. The transition also depends on the places which represent the variables.
            If the rate constant depends on time, the transitions rate must be recalculated at each step.
            */
            MathematicalExpression rateConstant = detReactionConstants.get(id);
            //First, check whether time is used in the expression.
            if (rateConstant.toString().contains(MathematicalExpression.TIME_VAR)){
                constantPlacesPostTransitions.add(reactionIdx);
            }
            else{
                for (int placeID : rateConstant.getVariables().values()){
                    //get the index of this compound
                    int placeIdx;
                    if (nonConstantPlacesIDsMap.containsKey(placeID)){
                        placeIdx = nonConstantPlacesIDsMap.get(placeID);
                        compoundsInfluence[placeIdx].add(reactionIdx);
                    }
                    else if (constantPlacesIDsMap.containsKey(placeID)){
                        constantPlacesPostTransitions.add(reactionIdx);
                    }
                }
            }
            
        }

        initComponents();
        this.globalRandom = new HighQualityRandom(ran.getSeed());
        outPathField.setText(outputFile.getAbsolutePath());
        /*
         * Add listener for close operation - all running threads should be ended before exiting
         */
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                int result = JOptionPane.OK_OPTION;
                if (running){
                    result = JOptionPane.showConfirmDialog(StochasticSimulator.this, "Stop all running simulations and close?", "Terminate simulations", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                }
                if (result == JOptionPane.OK_OPTION){
                    ts.fastSimFrame.removeFastSim(StochasticSimulator.this);
                }
            }
        });
        setVisible(false);
        
        for (int i = 1; i < 501; i++){
            this.nrOfSimsBox.addItem(i);
        }
    }
    //END CONSTRUCTORS

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        options = new javax.swing.JPanel();
        runTime = new javax.swing.JLabel();
        updateTime = new javax.swing.JLabel();
        timeSpanField = new javax.swing.JFormattedTextField();
        updateIntervalField = new javax.swing.JFormattedTextField();
        algorithmType = new javax.swing.JLabel();
        selectAlgoComboBox = new javax.swing.JComboBox();
        nbrThreads = new javax.swing.JLabel();
        nrOfSimsBox = new javax.swing.JComboBox();
        newRandomB = new javax.swing.JButton();
        runButton = new javax.swing.JButton();
        outPathField = new javax.swing.JTextField();
        browseOutFileButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        createLogOfAllRuns = new javax.swing.JCheckBox();
        simLog = new javax.swing.JPanel();
        simRunsTabbedPane = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Stochastic simulation");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        options.setMaximumSize(new java.awt.Dimension(300, 400));
        options.setMinimumSize(new java.awt.Dimension(300, 115));
        options.setLayout(new java.awt.GridBagLayout());

        runTime.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        runTime.setText(TokenSimulator.strings.get("SimTimeSpan"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 1;
        options.add(runTime, gridBagConstraints);

        updateTime.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        updateTime.setText(TokenSimulator.strings.get("OutputInterval"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        options.add(updateTime, gridBagConstraints);

        try {
            timeSpanField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###:##:##:##")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        timeSpanField.setText("000:00:00:00  ");
        timeSpanField.setToolTipText(TokenSimulator.strings.get("SimTimeSpanTT"));
        timeSpanField.setMaximumSize(new java.awt.Dimension(75, 2147483647));
        timeSpanField.setMinimumSize(new java.awt.Dimension(75, 25));
        timeSpanField.setPreferredSize(new java.awt.Dimension(75, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        options.add(timeSpanField, gridBagConstraints);

        try {
            updateIntervalField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##:##:##:##")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        updateIntervalField.setText("00:00:00:00");
        updateIntervalField.setToolTipText(TokenSimulator.strings.get("OutputIntervalTT"));
        updateIntervalField.setMaximumSize(new java.awt.Dimension(175, 2147483647));
        updateIntervalField.setMinimumSize(new java.awt.Dimension(175, 25));
        updateIntervalField.setPreferredSize(new java.awt.Dimension(175, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        options.add(updateIntervalField, gridBagConstraints);

        algorithmType.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        algorithmType.setText(TokenSimulator.strings.get("SelectAlgorithm"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        options.add(algorithmType, gridBagConstraints);

        selectAlgoComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Exact SSA", "Approximate SSA" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        options.add(selectAlgoComboBox, gridBagConstraints);

        nbrThreads.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        nbrThreads.setText(TokenSimulator.strings.get("NrOfSims"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        options.add(nbrThreads, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        options.add(nrOfSimsBox, gridBagConstraints);

        newRandomB.setText(TokenSimulator.strings.get("NewRandomB"));
        newRandomB.setToolTipText(TokenSimulator.strings.get("NewRandomBT"));
        newRandomB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newRandomBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        options.add(newRandomB, gridBagConstraints);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/run_tools.png"))); // NOI18N
        runButton.setText(TokenSimulator.strings.get("ATSFireTransitionsB"));
        runButton.setToolTipText(TokenSimulator.strings.get("RunStochSimTT"));
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        options.add(runButton, gridBagConstraints);

        outPathField.setMinimumSize(new java.awt.Dimension(75, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        options.add(outPathField, gridBagConstraints);

        browseOutFileButton.setText(TokenSimulator.strings.get("Browse"));
        browseOutFileButton.setToolTipText(TokenSimulator.strings.get("OutputPathTT"));
        browseOutFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseOutFileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        options.add(browseOutFileButton, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel2.setText("Location of Output");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        options.add(jLabel2, gridBagConstraints);

        jLabel1.setText("[days:hours:minutes:seconds]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        options.add(jLabel1, gridBagConstraints);

        jLabel3.setText("[days:hours:minutes:seconds]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        options.add(jLabel3, gridBagConstraints);

        createLogOfAllRuns.setText("Create an additional log file of all runs combined");
        createLogOfAllRuns.setToolTipText("<html>If selected, the Simulator will create a addition log file, which contains all runs.<br/>\nInside the log file, each run is sperated</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        options.add(createLogOfAllRuns, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.5;
        jPanel1.add(options, gridBagConstraints);

        simLog.setLayout(new java.awt.GridBagLayout());

        simRunsTabbedPane.setPreferredSize(new java.awt.Dimension(600, 300));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        simLog.add(simRunsTabbedPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(simLog, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void browseOutFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseOutFileButtonActionPerformed
        /*
         * Create a dialog for selecting output file.
         */
        MonaLisaFileChooser fc = new MonaLisaFileChooser(outputFile);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.showOpenDialog(this);
        File file = fc.getSelectedFile();
        if (file.isFile()){
            this.outputFile = file;
        }
        /*
         * Write the file path to the text field.
         */
        this.outPathField.setText(file.getAbsolutePath());
    }//GEN-LAST:event_browseOutFileButtonActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        try {
            //parse the time span and update interval.
            this.timeSpanField.commitEdit();
            this.updateIntervalField.commitEdit();
        } catch (ParseException ex) {
            Logger.getLogger(StochasticSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] timeInput = this.timeSpanField.getValue().toString().split(":");
        try{
            double timeSpanD = (((Double.parseDouble(timeInput[0]) * 24 + Double.parseDouble(timeInput[1])) * 60) + 
                Double.parseDouble(timeInput[2])) * 60 + Double.parseDouble(timeInput[3]);
            this.maxSimTime = timeSpanD;
        }
        catch(NumberFormatException e){
            
        }
        timeInput = this.updateIntervalField.getValue().toString().split(":");
        try{
            double interval = (((Double.parseDouble(timeInput[0]) * 24 + Double.parseDouble(timeInput[1])) * 60) + 
                Double.parseDouble(timeInput[2])) * 60 + Double.parseDouble(timeInput[3]);
            this.updateInterval = interval;
        }
        catch(NumberFormatException e){
            
        }
        
        /*
         * If no simulation is running, start the selected number of simulation runs. Disable all controlls. Create output files.
         */
        if (!running){    
            //Handle averages controls
//            this.averagesButton.setEnabled(false);
//            this.averagesProgressBar.setValue(0);
            
            runButton.setText(TokenSimulator.strings.get("ATSStopFiringB"));
            runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/stop_tools.png")));
            browseOutFileButton.setEnabled(false);
            outPathField.setEditable(false);
            nrOfSimsBox.setEnabled(false);
            selectAlgoComboBox.setEnabled(false);
            running = true;
            /*
             * If runnable SimulationRunnable instances allready exist, create threads and start this runnables. Otherwise, create new simulation runs.
             */
            if (!this.runnables.isEmpty()){
                for (ExactSSA run : this.runnables){
                    /*
                    If maximum parallel running threads limit is not exceeded, start a new thread with the runnable.
                    Otherwise put it into the queue.
                    */
                    if (this.runningThreads.size() < MAX_PARALLEL_THREADS && this.ts.isNewThreadAllowed()){
                        this.ts.registerNewThread();
                        Thread thread = new Thread(run);
                        this.runningThreads.put(thread, run);
                        thread.start();
                    }
                    else{
                        this.runnablesQueue.offer(run);
                    }
                }
            }
            else{
                //Handle averages controls
//                this.averagesButton.setEnabled(false);
                
                //number of parallel simulation runs of the system.
                int nrOfRuns = (int)  nrOfSimsBox.getSelectedItem();
                try {
                    //create output files
                    outputFile = new File(outPathField.getText());
                    if (!outputFile.isFile()){
                        File dir = outputFile.getParentFile();
                        if (!dir.isDirectory()){
                            dir.mkdir();
                        }
                    }
                    String outFileName = outputFile.getAbsolutePath();
                    String outFileExt;
                    if(outFileName.contains(".")){
                        outFileExt = outFileName.substring(outFileName.lastIndexOf('.'));
                        outFileName = outFileName.substring(0, outFileName.lastIndexOf('.'));
                    }else{
                        outFileExt = "csv";
                    }
                    outputFiles.add(outputFile);
                    
                    for (int i = 0; i < nrOfRuns; i++){                        
                        if(i > 0){
                            outputFile = new File(outFileName.concat("_").concat(String.valueOf(i).concat(outFileExt)));
                            outputFiles.add(outputFile);
                        }                        
                        outputFile.createNewFile();

                        /*
                         * Create a SimulationRunnable, create a thread which will execute the runnable and start it.
                         */
                        ExactSSA runnable = null;
                        if(selectAlgoComboBox.getSelectedItem().toString().equals("Exact SSA")){
                            runnable = new ExactSSA(outputFile, i);
                        }
                        if(selectAlgoComboBox.getSelectedItem().toString().equals("Approximate SSA")){
                            runnable = new TauLeapingSSA(outputFile, i);
                        }                        
                        this.runnables.add(runnable);
                        /*
                        If maximum parallel running threads limit is not exceeded, start a new thread with the runnable.
                        Otherwise put it into the queue.
                        */
                        if (i < MAX_PARALLEL_THREADS && this.ts.isNewThreadAllowed()){
                            this.ts.registerNewThread();
                            Thread thread = new Thread(runnable);
                            this.runningThreads.put(thread, runnable);
                            thread.start();
                        }
                        else{
                            this.runnablesQueue.offer(runnable);
                        }
                    }
                }
                catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, TokenSimulator.strings.get("CannotCreateFiles"),
                            TokenSimulator.strings.get("Error"), JOptionPane.ERROR_MESSAGE);
                    this.outPathField.setEnabled(true);
                    this.browseOutFileButton.setEnabled(true);
                }
            }
            /*
             * Create and start the updater task.
             */
            Timer timer = new Timer();
            timer.schedule(new UpdaterTask(), 0, UPDATER_TASK_INTERVAL);
        }
        /*
         * If the simulation is running, stop it. The corresponding runnables, however, will not be destroyed and can be started afterwards.
         */
        else{
            running = false;
            stopSimulation();
        }
    }//GEN-LAST:event_runButtonActionPerformed

    private void newRandomBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newRandomBActionPerformed
        this.globalRandom = new HighQualityRandom();
    }//GEN-LAST:event_newRandomBActionPerformed

    /**
     * Invoke the stop of simulation runs. All executed runnables will be stopped, threads executing them destroyed.
     * The runnables themselves remain, so simulation can be continued from the point where it was interrupted
     * by creating threads with the runnables.
     */
    public void stopSimulation(){
        running = false;
        for (ExactSSA runnable : this.runningThreads.values()){
            runnable.requestStop();
        }
        this.runningThreads.clear();
        this.runnablesQueue.clear();
        runButton.setText(TokenSimulator.strings.get("ATSFireTransitionsB"));
        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/run_tools.png")));
    }
    
    public void closeSimulator() {
        int result = JOptionPane.OK_OPTION;
        if (running){
            result = JOptionPane.showConfirmDialog(StochasticSimulator.this, "Stop all running simulations and close?", "Terminate simulations", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        }
        if (result == JOptionPane.OK_OPTION){
            ts.fastSimFrame.removeFastSim(StochasticSimulator.this);
        }        
    }
//        //Stop the computation of the averages
//        if (outputProcessor.computingAverages){
//            outputProcessor.abortComputeAverages();
//            averagesButton.setText(TokenSimulator.strings.get("StopComputeAveragesB"));
//            averagesButton.setToolTipText(TokenSimulator.strings.get("StopComputeAveragesTT"));
//            averagesProgressBar.setValue(0);
//            runButton.setEnabled(true);
//        }
//        //Start the computation of the averages
//        else{
//            outputProcessor.computeAverages();
//            averagesButton.setText(TokenSimulator.strings.get("ComputeAveragesB"));
//            averagesButton.setToolTipText(TokenSimulator.strings.get("ComputeAveragesTT"));
//            runButton.setEnabled(false);
//        }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel algorithmType;
    private javax.swing.JButton browseOutFileButton;
    private javax.swing.JCheckBox createLogOfAllRuns;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel nbrThreads;
    private javax.swing.JButton newRandomB;
    private javax.swing.JComboBox nrOfSimsBox;
    private javax.swing.JPanel options;
    private javax.swing.JTextField outPathField;
    private javax.swing.JButton runButton;
    private javax.swing.JLabel runTime;
    private javax.swing.JComboBox selectAlgoComboBox;
    private javax.swing.JPanel simLog;
    private javax.swing.JTabbedPane simRunsTabbedPane;
    private javax.swing.JFormattedTextField timeSpanField;
    private javax.swing.JFormattedTextField updateIntervalField;
    private javax.swing.JLabel updateTime;
    // End of variables declaration//GEN-END:variables
}