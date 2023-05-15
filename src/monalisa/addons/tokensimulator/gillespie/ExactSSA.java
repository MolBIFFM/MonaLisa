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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.exceptions.PlaceConstantException;
import monalisa.addons.tokensimulator.listeners.SimulationEvent;
import monalisa.addons.tokensimulator.listeners.SimulationListener;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import monalisa.addons.tokensimulator.utils.Utilities;
import monalisa.util.HighQualityRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final GillespieTokenSim gillTS;
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
    private final File outputFileRun;

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

    protected DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    /**
     * Random number generator.
     */
    protected final HighQualityRandom randomRun;

    private List<SimulationListener> listeners = new ArrayList<>();

    private static final Logger LOGGER = LogManager.getLogger(ExactSSA.class);
    private final SimulationStorage simStor;

    /**
     * Create a new instance of an ExactSSA runnable.
     *
     * @param outF File where the output will be written to.
     * @param runNr Number (index) of simulation run.
     * @param seed Seed for randomization
     * @param gillTS Associated GillespieTokenSim
     * @param simStor Storage with data for simulation
     */
    public ExactSSA(File outF, int runNr, Long seed, GillespieTokenSim gillTS, SimulationStorage simStor) {
        this.gillTS = gillTS;
        this.simStor = simStor;
        this.outputFileRun = outF;
        this.randomRun = new HighQualityRandom(seed);
        this.timePassed = 0;
        this.stepsSimulated = 0;
        this.reactionsToUpdate = new HashSet<>();

        this.nonConstantPlaceIDsRun = new int[simStor.getNonConstantPlaceIDs().length];
        System.arraycopy(simStor.getNonConstantPlaceIDs(), 0, this.nonConstantPlaceIDsRun, 0, simStor.getNonConstantPlaceIDs().length);
        this.constantPlaceIDsRun = new int[simStor.getConstantPlaceIDs().length];
        System.arraycopy(simStor.getConstantPlaceIDs(), 0, this.constantPlaceIDsRun, 0, simStor.getConstantPlaceIDs().length);
        this.constantPlacesExpRun = new MathematicalExpression[simStor.getConstantPlacesExp().length];
        for (int i = 0; i < simStor.getConstantPlacesExp().length; i++) {
            try {
                this.constantPlacesExpRun[i] = new MathematicalExpression(simStor.getConstantPlacesExp()[i]);
            } catch (RuntimeException ex) {
                LOGGER.error("Unknown function or unparsable expression while trying to initiate a runnable and trying to express a constantplace property" + ex);
            }
        }
        this.reactionRateConstantsRun = new MathematicalExpression[simStor.getReactionRateConstants().length];
        for (int i = 0; i < simStor.getReactionRateConstants().length; i++) {
            try {
                this.reactionRateConstantsRun[i] = new MathematicalExpression(simStor.getReactionRateConstants()[i]);
            } catch (RuntimeException ex) {
                LOGGER.error("Unknown function or unparsable expression while trying to initiate a runnable and trying to express a reaction rate property" + ex);
            }
        }
        /*
        Copy initial marking of non-constant places and calculate corresponding concentrations.
         */
        LOGGER.debug("Copying initial marking of constant places and calculating the corrsesponding concentrations");
        this.concentrations = new HashMap<>();
        this.nonConstantMarkingRun = new long[simStor.getNonConstantInitialMarking().length];
        for (int i = 0; i < nonConstantMarkingRun.length; i++) {
            this.nonConstantMarkingRun[i] = simStor.getNonConstantInitialMarking()[i];
            this.concentrations.put(this.nonConstantPlaceIDsRun[i], this.nonConstantMarkingRun[i] / simStor.getVolMol());
        }
        /*
        Evaluate initial marking of constant places and calculate corresponding concentrations.
         */
        this.constantMarkingRun = new long[this.constantPlacesExpRun.length];
        for (int i = 0; i < this.constantMarkingRun.length; i++) {
            MathematicalExpression exp = this.constantPlacesExpRun[i];
            double val = exp.evaluateML(concentrations, this.timePassed);
            this.constantMarkingRun[i] = Math.round(val * simStor.getVolMol());
            this.concentrations.put(simStor.getConstantPlaceIDs()[i], val);
        }
        /*
        Compute initial reaction rates.
         */
        LOGGER.debug("Computing initial reaction rates");
        this.reactionRatesRun = new HashMap<>();
        this.sumOfRates = 0;
        for (int reactionIdx = 0; reactionIdx < simStor.getReactionIDs().length; reactionIdx++) {
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

        LOGGER.debug("Start creation of output file");
        /*
         * Create the header of output file.
         */
        StringBuilder outSB = new StringBuilder();
        outSB.append("Number of steps");
        /*
         * Write the names of non-constant places.
         */
        for (String name : simStor.getNonConstantPlaceNames()) {
            outSB.append("\t").append(name);
        }
        /*
         * Write the names of constant places.
         */
        for (String name : simStor.getConstantPlaceNames()) {
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
        fireSimulationEvent(SimulationEvent.INIT, -1);
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
                reactionsToUpdate.addAll(simStor.getConstantPlacesPostTransitions());

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
                if (simStor.getMaxSimTime() > 0 && (timePassed + nextFiringTime) > simStor.getMaxSimTime()) {
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

                if (this.timePassed >= this.lastUpdate + simStor.getUpdateInterval()) {
                    LOGGER.debug("Time passed is bigger than time since LastUpdate and current interval, therefore writing the output");
                    writeOutput(reaction);
                }

                /*
                 * Adapt coumpounds molecule numbers according to the chosen reaction. Iterate through 
                the non-constant conpounds of the reaction and substract the stoichiometric factor from the number of molecules
                of this compound.
                 */
                LOGGER.debug("Calculating new marking for all educts");
                for (int eductIdx : simStor.getReactionsNonConstantEducts()[reaction]) {
                    /*
                    Get the number of molecules of the educt and substract the stoichiometric factor from it.
                     */
                    long nrOfMolecules = nonConstantMarkingRun[eductIdx] - simStor.getEductStoichMatrix()[reaction][eductIdx];
                    /*
                    Update the marking.
                     */
                    nonConstantMarkingRun[eductIdx] = nrOfMolecules;
                    /*
                    Update the concentrations entry.
                     */
                    concentrations.put(this.nonConstantPlaceIDsRun[eductIdx], nrOfMolecules / simStor.getVolMol());
                    /*
                    Mark all reactions which are dependent from this educt so the reaction rates will be re-calculated.
                     */
                    reactionsToUpdate.addAll(simStor.getCompoundsInfluence()[eductIdx]);
                }
                LOGGER.debug("Calculating new marking for all products");
                for (int productIdx : simStor.getReactionsNonConstantProducts()[reaction]) {
                    /*
                    Get the number of molecules of the educt and add the stoichiometric factor from it.
                     */
                    long nrOfMolecules = nonConstantMarkingRun[productIdx] + simStor.getProductStoichMatrix()[reaction][productIdx];
                    /*
                    Update the marking.
                     */
                    nonConstantMarkingRun[productIdx] = nrOfMolecules;
                    /*
                    Update the concentrations entry.
                     */
                    concentrations.put(this.nonConstantPlaceIDsRun[productIdx], nrOfMolecules / simStor.getVolMol());
                    /*
                    Mark all reactions which are dependent from this educt so the reaction rates will be re-calculated.
                     */
                    reactionsToUpdate.addAll(simStor.getCompoundsInfluence()[productIdx]);
                }
                /*
                Update marking and concentration for constant places
                 */
                LOGGER.debug("Updating marking and concentration for all constant places");
                for (int i = 0; i < this.constantMarkingRun.length; i++) {
                    MathematicalExpression exp = this.constantPlacesExpRun[i];
                    double val = exp.evaluateML(concentrations, this.timePassed);
                    this.constantMarkingRun[i] = Math.round(val * simStor.getVolMol());
                    this.concentrations.put(this.constantPlaceIDsRun[i], val);
                }

                //Write updated molecule numbers into output file.
                /*if (simStor.getUpdateInterval() == 0 || timePassed - lastUpdate >= simStor.getUpdateInterval()) {
                    LOGGER.debug("New Update Intervall is zero or left time is not enough, therefore writing the output");
                    writeOutput(reaction);
                }*/
            }
            LOGGER.info("Simulation has stopped running, therefore finishing the Simulator");
            //close writers
            outputWriter.close();
        } catch (IOException ex) {
            LOGGER.error("IOException while trying to fill the output writer with data", ex);
        }
        this.updateOutput();
        fireSimulationEvent(SimulationEvent.DONE, ("\n").concat(SimulationManager.strings.get("SimFinished")).concat(dateFormat.format(Calendar.getInstance().getTime())));
        gillTS.checkOutRunningThread();
    }

    /**
     * This method is used to write the current state of simulation run (number
     * of simulated steps and simulated time) to the outTextArea.
     */
    public void updateOutput() {
        lock.lock();
        try {
            fireSimulationEvent(SimulationEvent.UPDATE_PROGRESS, "\nSteps: " + this.stepsSimulated + "; time passed: " + Utilities.secondsToTime(timePassed));
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
            fireSimulationEvent(SimulationEvent.STOPPED, "\n".concat(SimulationManager.strings.get("SimStopping")));
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
            for (int i = 0; i < simStor.getNonConstantPlaceIDs().length; i++) {
                gillTS.getSimulationMan().setTokens(simStor.getNonConstantPlaceIDs()[i], nonConstantMarkingRun[i]);
            }
            gillTS.setSimulatedTime(timePassed);
        } catch (PlaceConstantException ex) {
            LOGGER.error("Constant place found at unexpected place while exporting the Marking in the Stochastic Simulator", ex);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Prepares the dataset of the simulation for display.
     *
     * @param nonConstantPlaces List of non-constant places.
     * @param constantPlaces List of constant places.
     * @return XYSeries for all places.
     * @throws IOException
     */
    public XYSeriesCollection prepDataset(List<Integer> nonConstantPlaces, List<Integer> constantPlaces) throws IOException {
        //Create an array of XYSeries for places.
        final List<Integer> nonConstantPlacesF = nonConstantPlaces;
        final List<Integer> constantPlacesF = constantPlaces;
        LOGGER.debug("Putting the information in usable XYSeries datatype while trying to create a plot out of the current stochastic simulation data");
        XYSeries[] nonConstantPlotSeries = new XYSeries[simStor.getNonConstantPlaceNames().length];
        for (int i = 0; i < simStor.getNonConstantPlaceNames().length; i++) {
            String name = simStor.getNonConstantPlaceNames()[i];
            nonConstantPlotSeries[i] = new XYSeries(name, false);
        }
        XYSeries[] constantPlotSeries = new XYSeries[simStor.getConstantPlaceNames().length];
        for (int i = 0; i < simStor.getConstantPlaceNames().length; i++) {
            String name = simStor.getConstantPlaceNames()[i];
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
            } catch (NumberFormatException ex) {
                LOGGER.error("NumberFormatException while trying to create a plot of the stochastic simulation", ex);
            }
        }
        XYSeriesCollection chartDataset = new XYSeriesCollection();
        for (int idx : nonConstantPlacesF) {
            chartDataset.addSeries(nonConstantPlotSeries[idx]);
        }
        for (int idx : constantPlacesF) {
            chartDataset.addSeries(constantPlotSeries[idx]);
        }
        return chartDataset;
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
        int order = simStor.getReactionOrder()[idx][0];
        int multiplier = simStor.getReactionOrder()[idx][1];
        double stochReactionRateConst;
        switch (order) {
            case 0:
                stochReactionRateConst = detReactionRateConst * simStor.getVolMol();
                break;
            case 1:
                stochReactionRateConst = detReactionRateConst;
                break;
            case 2:
                stochReactionRateConst = (detReactionRateConst * multiplier) / simStor.getVolMol();
                break;
            default:
                double volMolPow = 1;
                for (int i = 0; i < order - 1; i++) {
                    volMolPow *= simStor.getVolMol();
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
        for (int eductIdx : simStor.getReactionsNonConstantEducts()[idx]) {
            //stoichiometric factor
            int weight = simStor.getEductStoichMatrix()[idx][eductIdx];
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
        for (int eductIdx : simStor.getReactionsConstantEducts()[idx]) {
            //stoichiometric factor
            int weight = simStor.getEductStoichMatrix()[idx][eductIdx + simStor.getNonConstantPlaceIDs().length];
            long tokens = this.constantMarkingRun[eductIdx];
            if (weight > tokens) {
                return 0;
            }
            //update the number of distinct combinations.
            h *= Utilities.binomialCoefficient(tokens, weight);
        }
        LOGGER.debug("Done with computing the reaction rate for the reaction with the ID: " + Integer.toString(idx)
                + " with the reacton rate: " + Double.toString(h * stochReactionRateConst));
        return h * stochReactionRateConst;
    }

    /**
     * Writes a simulation log.
     *
     * @param reactions Reactions that occurred.
     */
    protected void writeOutput(Integer... reactions) {
        LOGGER.debug("Writing general data into the StringBuilder");
        lastUpdate += simStor.getUpdateInterval();
        double timeForLog;
        if (simStor.getUpdateInterval() == 0.0) {
            timeForLog = timePassed;
        } else {
            timeForLog = lastUpdate;
        }
        StringBuilder outSB = new StringBuilder();
        outSB.append(stepsSimulated).append("\t").append(timeForLog).append("\t");
        for (int idx : reactions) {
            outSB.append(simStor.getReactionNames()[idx]).append(";");
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
    }

    /**
     * @return the outputFileRun
     */
    public File getOutputFileRun() {
        return outputFileRun;
    }

    /**
     * Adds a SimulationListener sl to the simulation.
     *
     * @param sl SimulationListener to be added.
     */
    public synchronized void addSimulationListener(SimulationListener sl) {
        if (!listeners.contains(sl)) {
            listeners.add(sl);
        }
    }

    /**
     * Removes a SimulationListener sl from the simulation.
     *
     * @param sl SimulationListener to be removed.
     */
    public synchronized void removeSimulationListener(SimulationListener sl) {
        listeners.remove(sl);
    }

    /**
     * Fires a SimulationEvent to all SimulationListeners.
     *
     * @param type Type of SimulationEvent to be fired.
     * @param val Value for SimulationEvent, if needed. -1, if not.
     */
    protected void fireSimulationEvent(String type, Object val) {
        SimulationEvent e = new SimulationEvent(this, type, val);
        for (SimulationListener sl : listeners) {
            sl.simulationUpdated(e);
        }
    }
}
