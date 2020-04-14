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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import monalisa.addons.tokensimulator.TokenSimulator;
import monalisa.addons.tokensimulator.exceptions.PlaceConstantException;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import monalisa.addons.tokensimulator.utils.Utilities;
import monalisa.util.HighQualityRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This runnable is used to start an exact stochastic simulation. Every reaction
 * occurrence will be simulated. Good for small molecule numbers with observable
 * stochastic effects, but very slow with large molecule numbers. It is possible
 * to start several threads with the instances of this class, so several
 * simulations of the same system will run parallel.
 */
public class ExactSSA implements Runnable {

    // Start variables from StochasticSimulator
    private final StochasticSimulator sts;
    private final double volMol;
    private double maxSimTime;
    private double updateInterval;
    private final int[] nonConstantPlaceIDs;
    private final int[] constantPlaceIDs;
    private final MathematicalExpression[] constantPlacesExp;
    private final MathematicalExpression[] reactionRateConstants;
    private final long[] nonConstantInitialMarking;
    private final String[] nonConstantPlaceNames;
    private final String[] constantPlaceNames;
    private final int[] reactionIDs;
    private final Set<Integer> constantPlacesPostTransitions;
    private final Set<Integer>[] reactionsNonConstantEducts;
    private final int[][] eductStoichMatrix;
    private final Set<Integer>[] compoundsInfluence;
    private final Set<Integer>[] reactionsNonConstantProducts;
    private final int[][] productStoichMatrix;
    private final int[][] reactionOrder;
    private final Set<Integer>[] reactionsConstantEducts;
    private final String[] reactionNames;
    private final GillespieTokenSim gts;
    // End variables from StochasticSimulator

    protected final int[] nonConstantPlaceIDsRun;
    protected final int[] constantPlaceIDsRun;
    protected final MathematicalExpression[] constantPlacesExpRun;
    /**
     * Number of molecules of the compounds which are represented by
     * non-constant places.
     */
    protected final long[] nonConstantMarkingRun;
    /**
     * Number of molecules of the compounds which are represented by the
     * constant places.
     */
    protected final long[] constantMarkingRun;
    /**
     * IDs of places mapped to concentrations of the compounds.
     */
    protected final HashMap<Integer, Double> concentrations;
    /**
     * Array with mathematical expressions which describe reaction rate
     * constants of the reactions (transitions of the underlying Petri net).
     */
    protected final MathematicalExpression[] reactionRateConstantsRun;
    /**
     * Reaction rates of active transitions. Keys are indeces of reactions,
     * values are corresponding reaction rates. Reactions which rates are 0 are
     * not stored.
     */
    protected final HashMap<Integer, Double> reactionRatesRun;
    /**
     * Sum of reaction rates.
     */
    protected double sumOfRates;
    /**
     * Save the index of reactions rates should be updated. Only update rates of
     * reactions which are dependent of compounds with changed molecule numbers.
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
    private static final Logger LOGGER = LogManager.getLogger(ExactSSA.class);

    /**
     * Create a new instance of runnable.
     *
     * @param outF File where the output will be written to.
     * @param runNr Number (index) of simulation run.
     */
    public ExactSSA(File outF, int runNr, Long seed, StochasticSimulator sts, GillespieTokenSim gts) {
        this.sts = sts;
        this.gts = gts;
        this.volMol = sts.getVolMol();
        this.maxSimTime = sts.getMaxSimTime();
        this.updateInterval = sts.getUpdateInterval();
        this.randomRun = new HighQualityRandom(seed);

        this.nonConstantPlaceIDs = sts.getNonConstantPlaceIDs();
        this.nonConstantPlaceNames = sts.getNonConstantPlaceNames();
        this.constantPlaceNames = sts.getConstantPlaceNames();
        this.constantPlaceIDs = sts.getConstantPlaceIDs();
        this.constantPlacesExp = sts.getConstantPlacesExp();
        this.constantPlacesPostTransitions = sts.getConstantPlacesPostTransitions();
        this.reactionIDs = sts.getReactionIDs();
        this.reactionNames = sts.getReactionNames();
        this.reactionRateConstants = sts.getReactionRateConstants();
        this.reactionOrder = sts.getReactionOrder();
        this.reactionsConstantEducts = sts.getReactionsConstantEducts();
        this.reactionsNonConstantEducts = sts.getReactionsNonConstantEducts();
        this.reactionsNonConstantProducts = sts.getReactionsNonConstantProducts();
        this.nonConstantInitialMarking = sts.getNonConstantInitialMarking();
        this.eductStoichMatrix = sts.getEductStoichMatrix();
        this.productStoichMatrix = sts.getProductStoichMatrix();
        this.compoundsInfluence = sts.getCompoundsInfluence();

        this.outputFileRun = outF;
        this.timePassed = 0;
        this.stepsSimulated = 0;
        this.reactionsToUpdate = new HashSet<>();

        this.nonConstantPlaceIDsRun = new int[nonConstantPlaceIDs.length];
        System.arraycopy(nonConstantPlaceIDs, 0, this.nonConstantPlaceIDsRun, 0, nonConstantPlaceIDs.length);
        this.constantPlaceIDsRun = new int[constantPlaceIDs.length];
        System.arraycopy(constantPlaceIDs, 0, this.constantPlaceIDsRun, 0, constantPlaceIDs.length);
        this.constantPlacesExpRun = new MathematicalExpression[constantPlacesExp.length];
        for (int i = 0; i < constantPlacesExp.length; i++) {
            try {
                this.constantPlacesExpRun[i] = new MathematicalExpression(constantPlacesExp[i]);
            } catch (RuntimeException ex) {
                LOGGER.error("Unknown function or unparsable expression while trying to initiate a runnable and trying to express a constantplace property" + ex);
            }
        }
        this.reactionRateConstantsRun = new MathematicalExpression[reactionRateConstants.length];
        for (int i = 0; i < reactionRateConstants.length; i++) {
            try {
                this.reactionRateConstantsRun[i] = new MathematicalExpression(reactionRateConstants[i]);
            } catch (RuntimeException ex) {
                LOGGER.error("Unknown function or unparsable expression while trying to initiate a runnable and trying to express a reaction rate property" + ex);
            }
        }
        /*
        Copy initial marking of non-constant places and calculate corresponding concentrations.
         */
        LOGGER.debug("Copying initial marking of constant places and calculating the corrsesponding concentrations");
        this.concentrations = new HashMap<>();
        this.nonConstantMarkingRun = new long[nonConstantInitialMarking.length];
        for (int i = 0; i < nonConstantMarkingRun.length; i++) {
            this.nonConstantMarkingRun[i] = nonConstantInitialMarking[i];
            this.concentrations.put(this.nonConstantPlaceIDsRun[i], this.nonConstantMarkingRun[i] / volMol);
        }
        /*
        Evaluate initial marking of constant places and calculate corresponding concentrations.
         */
        this.constantMarkingRun = new long[this.constantPlacesExpRun.length];
        for (int i = 0; i < this.constantMarkingRun.length; i++) {
            MathematicalExpression exp = this.constantPlacesExpRun[i];
            double val = exp.evaluateML(concentrations, this.timePassed);
            this.constantMarkingRun[i] = Math.round(val * volMol);
            this.concentrations.put(constantPlaceIDs[i], val);
        }
        /*
        Compute initial reaction rates.
         */
        LOGGER.debug("Computing initial reaction rates");
        this.reactionRatesRun = new HashMap<>();
        this.sumOfRates = 0;
        for (int reactionIdx = 0; reactionIdx < reactionIDs.length; reactionIdx++) {
            double reactionRate = this.computeReactionRate(reactionIdx);
            this.sumOfRates += reactionRate;
            /*
            Create an entry in the map only if the reaction rate is greater than 0.
             */
            if (reactionRate > 0) {
                this.reactionRatesRun.put(reactionIdx, reactionRate);
            }
        }

        /*
         * Add a tab with a text area which shows the information about this run.
         */
        LOGGER.debug("Initializing tab with text area for information about this run");
        this.outTextArea.setEditable(false);
        outPanel = new StochasticSimulatorRunPanel(this, nonConstantPlaceNames, constantPlaceNames);

        this.outTextArea.setText(TokenSimulator.strings.get("SimStartedAt").concat(dateFormat.format(Calendar.getInstance().getTime())));
        sts.getSimRunsTabbedPane().addTab("Simulation run " + runNr, outPanel);
        LOGGER.debug("Start creation of output file");
        /*
         * Create the header of output file.
         */
        StringBuilder outSB = new StringBuilder();
        outSB.append("Step\tTime[sec]\tReaction");
        /*
         * Write the names of non-constant places.
         */
        for (String name : nonConstantPlaceNames) {
            outSB.append("\t").append(name);
        }
        /*
         * Write the names of constant places.
         */
        for (String name : constantPlaceNames) {
            outSB.append("\t").append(name);
        }
        outSB.append("\n0\t0\tnone");
        /*
         * Write the initial number of molecules for non-constant places.
         */
        for (long nr : nonConstantMarkingRun) {
            outSB.append("\t").append(nr);
        }
        /*
         * Write the initial number of molecules for constant places.
         */
        for (long nr : constantMarkingRun) {
            outSB.append("\t").append(nr);
        }
        outSB.append("\n");
        try {
            outputWriter = new BufferedWriter(new FileWriter(outputFileRun));
            outputWriter.write(outSB.toString());
            outputWriter.close();
        } catch (IOException ex) {
            LOGGER.error("IO Exception while trying to write the String into a buffered Writer and in the according output File " + ex);
        }
    }

    @Override
    public void run() {
        outPanel.showPlotButton.setEnabled(false);
        try {
            outputWriter = new BufferedWriter(new FileWriter(outputFileRun, true));
        } catch (IOException ex) {
            LOGGER.error("IOException while creating a new buffered writer for the outputfile");
        }
        this.isRunning = true;
        try {
            /*
             * As long as at least one reaction can take place, execute simulation. A single simulation step consists of calculating reaction rates (which
             * are dependent on the number of compound molecules), choosing the time of next reaction, choosing next reaction, "executing" the reaction
             * by adapting compound numbers according to the reaction and writing the results to a file.
             */
            while (isRunning) {
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
                for (int reactionIdx : reactionsToUpdate) {
                    double reactionRate = computeReactionRate(reactionIdx);
                    /*
                    Substract old reaction rate from the sum.
                     */
                    Double oldRate = reactionRatesRun.get(reactionIdx);
                    if (oldRate != null) {
                        sumOfRates -= oldRate;
                    }
                    /*
                    If new reaction rate is greater than 0, put it into the map. Otherwise remove the entry
                    of the reaction from the map.
                     */
                    if (reactionRate > 0) {
                        reactionRatesRun.put(reactionIdx, reactionRate);
                        /*
                        Add new reaction rate to the sum.
                         */
                        sumOfRates += reactionRate;
                    } else {
                        reactionRatesRun.remove(reactionIdx);
                    }
                }
                /*
                 * Check whether the sum of rates is empty, which indicates that no reaction can occur. If so, abort simulation.
                 */
                if (sumOfRates <= 0) {
                    LOGGER.debug("SumOfRates is zero, therefore stop the stochastic simulation");
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
                if (maxSimTime > 0 && (timePassed + nextFiringTime) > maxSimTime) {
                    this.requestStop();
                    LOGGER.debug("Maximum Time has been reached, therefore stopping the stochastic simulation");
                    break;
                }

                /*
                 * Datermine which reaction will occur next.
                 * int reactionIdx is the index of the reaction which will be chosen.
                 * double rateSum is the sum of reaction rates which are previous to the chosen one in the reactionRates list.
                 * Choose the reaction so the sum of reactions which are previous plus the rate of chosen one is greater than or equal to 
                 * the sum of all reaction rates multiplied by r2.
                 */
                LOGGER.debug("Calculating the reaction which will occur next");
                int reaction = 0;
                double ratesSum = 0;
                for (Map.Entry<Integer, Double> entr : reactionRatesRun.entrySet()) {
                    reaction = entr.getKey();
                    double reactionRate = entr.getValue();
                    ratesSum += reactionRate;
                    if (ratesSum >= sumOfRates * r2) {
                        break;
                    }
                }

                /*
                 * Update step and time counters.
                 */
                this.stepsSimulated++;
                this.timePassed += nextFiringTime;

                if (this.timePassed >= this.lastUpdate + updateInterval) {
                    LOGGER.debug("Time passed is bigger than time since LastUpdate and current interval, therefore writing the output");
                    writeOutput(reaction);
                }

                /*
                 * Adapt coumpounds molecule numbers according to the chosen reaction. Iterate through 
                the non-constant conpounds of the reaction and substract the stoichiometric factor from the number of molecules
                of this compound.
                 */
                LOGGER.debug("Calculating new marking for all educts");
                for (int eductIdx : reactionsNonConstantEducts[reaction]) {
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
                LOGGER.debug("Calculating new marking for all products");
                for (int productIdx : reactionsNonConstantProducts[reaction]) {
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
                LOGGER.debug("Updating marking and concentration for all constant places");
                for (int i = 0; i < this.constantMarkingRun.length; i++) {
                    MathematicalExpression exp = this.constantPlacesExpRun[i];
                    double val = exp.evaluateML(concentrations, this.timePassed);
                    this.constantMarkingRun[i] = Math.round(val * volMol);
                    this.concentrations.put(this.constantPlaceIDsRun[i], val);
                }

                //Write updated molecule numbers into output file.
                if (updateInterval == 0 || timePassed - lastUpdate >= updateInterval) {
                    LOGGER.debug("New Update Intervall is zero or left time is not enough, therefore writing the output");
                    writeOutput(reaction);
                }
            }
            LOGGER.info("Simulation has stopped running, therefore finishing the Simulator");
            //close writers
            outputWriter.close();
        } catch (IOException ex) {
            LOGGER.error("IOException while trying to fill the output writer with data", ex);
        }
        this.outTextArea.append(("\n").concat(TokenSimulator.strings.get("SimFinished")).concat(dateFormat.format(Calendar.getInstance().getTime())));
        this.updateOutput();
        outPanel.showPlotButton.setEnabled(true);
        gts.checkOutRunningThread();
    }

    /**
     * This method is used to write the current state of simulation run (number
     * of simulated steps and simulated time) to the outTextArea.
     */
    public void updateOutput() {
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
    public void requestStop() {
        lock.lock();
        try {
            isRunning = false;
            this.outTextArea.append("\n".concat(TokenSimulator.strings.get("SimStopping")));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Export current state of simulation (nr. of compound molecules) to the
     * Petri net and update the visual output of the PN. Also update the time
     * counter of TokenSimulator.
     */
    public void exportMarking() {
        LOGGER.info("Exporting the current state of the stochastic simulation to the Petri net and updating the visual output");
        lock.lock();
        try {
            /*
            Iterate through non-constant places.
             */
            for (int i = 0; i < sts.getNonConstantPlaceIDs().length; i++) {
                gts.getTokenSim().setTokens(sts.getNonConstantPlaceIDs()[i], nonConstantMarkingRun[i]);
            }
            gts.setSimulatedTime(timePassed);
        } catch (PlaceConstantException ex) {
            LOGGER.error("Constant place found at unexpected place while exporting the Marking in the Stochastic Simulator", ex);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates a window with results of selected places plotted with JFreeChart.
     * Results are read from the output file.
     *
     * @param nonConstantPlaces List of indexes of non-constant places.
     * @param constantPlaces List of indexes of constant places.
     */
    public void showPlot(List<Integer> nonConstantPlaces, List<Integer> constantPlaces) {
        final List<Integer> nonConstantPlacesF = nonConstantPlaces;
        final List<Integer> constantPlacesF = constantPlaces;
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //CHECK FOR THE SIZE OF THE OUTPUT FILE AND SHOW A WARNING
                //IF IT IS TOO BIG (> 5 MB)
                LOGGER.info("Creating a plot out of the current stochastic simulation data");
                int proceed = JOptionPane.OK_OPTION;
                if (outputFileRun.length() > 1024 * 1024 * 5) {
                    proceed = JOptionPane.showConfirmDialog(null, "The number of timepoints is too large. This could result in long plotting time and even make the program unresponsive."
                            + " Are you sure you wat to continue? (Hint: You can limit the number of timepoints by increasing the update interval.)", "Warning!", JOptionPane.OK_CANCEL_OPTION);
                }
                try {
                    if (proceed == JOptionPane.OK_OPTION) {
                        //Create an array of XYSeries for places.
                        LOGGER.debug("Putting the information in usable XYSeries datatype while trying to create a plot out of the current stochastic simulation data");
                        XYSeries[] nonConstantPlotSeries = new XYSeries[nonConstantPlaceNames.length];
                        for (int i = 0; i < nonConstantPlaceNames.length; i++) {
                            String name = nonConstantPlaceNames[i];
                            nonConstantPlotSeries[i] = new XYSeries(name, false);
                        }
                        XYSeries[] constantPlotSeries = new XYSeries[constantPlaceNames.length];
                        for (int i = 0; i < constantPlaceNames.length; i++) {
                            String name = constantPlaceNames[i];
                            constantPlotSeries[i] = new XYSeries(name, false);
                        }
                        BufferedReader in = new BufferedReader(new FileReader(outputFileRun));
                        String line;
                        in.readLine();
                        LOGGER.debug("Trying to parse the data out of the XYSeries datatypes into readers for chart creation");
                        while ((line = in.readLine()) != null) {
                            String[] entr = line.split("\t");
                            try {
                                double time = Double.parseDouble(entr[1]);
                                for (int i = 0; i < nonConstantMarkingRun.length; i++) {
                                    nonConstantPlotSeries[i].add(time, Long.parseLong(entr[i + 3]), false);
                                }
                                for (int i = 0; i < constantMarkingRun.length; i++) {
                                    constantPlotSeries[i].add(time, Long.parseLong(entr[i + 3 + nonConstantPlotSeries.length]), false);
                                }
                            } catch (Exception ex) {
                                LOGGER.error("General exception while trying to create a plot of the stochastic simulation", ex);
                            }
                        }

                        XYSeriesCollection chartDataset = new XYSeriesCollection();
                        for (int idx : nonConstantPlacesF) {
                            chartDataset.addSeries(nonConstantPlotSeries[idx]);
                        }
                        for (int idx : constantPlacesF) {
                            chartDataset.addSeries(constantPlotSeries[idx]);
                        }

                        /*
                        Generate graph.
                         */
                        LOGGER.debug("Generating the graph for the stochastic simulation data");
                        final JFreeChart chart = ChartFactory.createScatterPlot("Simulation results",
                                "Passed time [sec]", "Nr. of molecules", chartDataset,
                                PlotOrientation.VERTICAL, true, false, false);
                        ChartFrame frame = new ChartFrame("Simulation results", chart);

                        frame.setSize(800, 600);
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        //                chartFrame.getContentPane().add(cp);
                        frame.setVisible(true);
                    }

                } catch (IOException ex) {
                    LOGGER.error("IOException while trying to create a graph out of the stochastic simulation data", ex);
                }

            }
        });
    }

    /**
     * Compute the rate of the reaction with index idx at current state. If the
     * reaction does not have required amount of educts, its reaction rate is
     * set to 0.
     *
     * @param idx Index of the reaction for which the rate will be computed.
     * @return Reaction rate (depends on rate constant, current marking and
     * volume).
     */
    protected double computeReactionRate(int idx) {
        /*
            * Evaluate deterministic reaction rate constant
         */
        LOGGER.debug("Computing the reaction rate of the reaction with the ID: " + Integer.toString(idx));
        double detReactionRateConst = reactionRateConstantsRun[idx].evaluateML(concentrations, timePassed);
        /*
            * Convert deterministic reaction rate constant to stochastic one. Get the order and the multiplier of the reaction.
         */
        int order = reactionOrder[idx][0];
        int multiplier = reactionOrder[idx][1];
        double stochReactionRateConst;
        switch (order) {
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
                for (int i = 0; i < order - 1; i++) {
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
        for (int eductIdx : reactionsNonConstantEducts[idx]) {
            //stoichiometric factor
            int weight = eductStoichMatrix[idx][eductIdx];
            long tokens = this.nonConstantMarkingRun[eductIdx];
            if (weight > tokens) {
                return 0;
            }
            //update the number of distinct combinations.
            h *= Utilities.binomialCoefficient(tokens, weight);
        }
        /*
            Iterate through all constant educts of the reaction. Get the number of molecules of the educt and the
            stoichiometric factor and calculate the number of distinct molecular combinations.
         */
        for (int eductIdx : reactionsConstantEducts[idx]) {
            //stoichiometric factor
            int weight = eductStoichMatrix[idx][eductIdx + nonConstantPlaceIDs.length];
            long tokens = this.constantMarkingRun[eductIdx];
            if (weight > tokens) {
                return 0;
            }
            //update the number of distinct combinations.
            h *= Utilities.binomialCoefficient(tokens, weight);
        }
        LOGGER.debug("Done with computing the reaction rate for the reaction with the ID: " + Integer.toString(idx) + " with the reacton rate: " + Double.toString(h * stochReactionRateConst));
        return h * stochReactionRateConst;
    }

    protected void writeOutput(Integer... reactions) {
        LOGGER.info("Starting to write the output for the stochastic simulation");
        LOGGER.debug("Writing general data into the StringBuilder");
        lastUpdate += updateInterval;
        double timeForLog;
        if (updateInterval == 0.0) {
            timeForLog = timePassed;
        } else {
            timeForLog = lastUpdate;
        }
        StringBuilder outSB = new StringBuilder();
        outSB.append(stepsSimulated).append("\t").append(timeForLog).append("\t");
        for (int idx : reactions) {
            outSB.append(reactionNames[idx]).append(";");
        }
        outSB.deleteCharAt(outSB.length() - 1);

        /*
         * Write molecule numbers for non-constant places.
         */
        LOGGER.debug("Filling the molecule numbers for non-constant places into the output");
        for (long nrOfMolecules : nonConstantMarkingRun) {
            outSB.append("\t").append(nrOfMolecules);
        }
        /*
         * Write molecule numbers for constant places.
         */
        LOGGER.debug("Filling the molecule numbers for constant places into the output");
        for (long nrOfMolecules : constantMarkingRun) {
            outSB.append("\t").append(nrOfMolecules);
        }
        outSB.append("\n");
        LOGGER.debug("Trying to write the finished data into the output");
        try {
            outputWriter.write(outSB.toString());
        } catch (IOException ex) {
            LOGGER.error("IOException while trying to write the data out of the stringbuilder into the outputWriter", ex);
        }
        LOGGER.info("Finished writing the data into the output Writer");
    }
};
