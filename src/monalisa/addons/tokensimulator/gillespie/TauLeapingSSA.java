/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.gillespie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.listeners.SimulationEvent;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TauLeapingSSA extends ExactSSA {

    /**
     * A set containing indeces of reactions that are critical.
     */
    private final Set<Integer> criticalReactions = new HashSet<>();
    /**
     * Minimal number of reaction occurrences that must be possible before
     * reaction is regarded as critical.
     */
    private final static int CRITICAL_THRESHOLD = 20;
    private final static double EPS = 0.03;
    private final static Logger LOGGER = LogManager.getLogger(TauLeapingSSA.class);
    private final GillespieTokenSim gillTS;
    private final SimulationStorage simStor;

    /**
     * Create a new instance of a TauLeapingSSA runnable.
     *
     * @param outF File where the output will be written to.
     * @param runNr Number (index) of simulation run.
     * @param seed Seed for randomization
     * @param gillTS Associated GillespieTokenSim
     * @param simStor Storage with data for simulation
     */
    public TauLeapingSSA(File outF, int runNr, Long seed, GillespieTokenSim gillTS, SimulationStorage simStor) {
        super(outF, runNr, seed, gillTS, simStor);
        this.gillTS = gillTS;
        this.simStor = simStor;

        /*
        Check which reactions are critical at the beginning of the simulation.
         */
        LOGGER.debug("Checking for critical reactions in the stochastic simulation");
        for (int reactionIdx : reactionRatesRun.keySet()) {
            /*
            minL is the number of times this reaction can occur before one of its reactants depletes.
             */
            int minL = CRITICAL_THRESHOLD;
            /*
            Iterate through non-constant educts of this reaction.
             */
            for (int eductIdx : simStor.getReactionsNonConstantEducts()[reactionIdx]) {
                //Number of molecules of the educt.
                long eductNr = this.nonConstantMarkingRun[eductIdx];
                //Stoichiometric factor of the reaction
                int factor = simStor.getEductStoichMatrix()[reactionIdx][eductIdx];
                /*
                If the factor is 0
                 */
                minL = Math.min(minL, Math.round(eductNr / factor));
            }
            for (int eductIdx : simStor.getReactionsConstantEducts()[reactionIdx]) {
                //Number of molecules of the educt.
                long eductNr = this.constantMarkingRun[eductIdx];
                //Stoichiometric factor of the reaction
                int factor = simStor.getEductStoichMatrix()[reactionIdx][eductIdx + simStor.getNonConstantPlaceIDs().length];
                minL = Math.min(minL, Math.round(eductNr / factor));
            }
            /*
            If the number of executable reactions is less than the critical threshold, the reaction
            is considered as critical.
             */
            if (minL < CRITICAL_THRESHOLD) {
                this.criticalReactions.add(reactionIdx);
            }
        }
    }

    @Override
    public void run() {
        fireSimulationEvent(SimulationEvent.INIT, -1);
        /*
        List of reactions which fired in this step. Used for output.
         */
        Set<Integer> firedReactions = new HashSet<>();
        try {
            outputWriter = new BufferedWriter(new FileWriter(getOutputFileRun(), true));
        } catch (IOException ex) {
            LOGGER.error("IOException while trying to create a new buffered file writer for the output in the stochastic simulation", ex);
        }
        this.isRunning = true;
        LOGGER.info("Starting the stochastic simulation");
        try {
            /*
             * As long as at least one reaction can take place, execute simulation. A single simulation step consists of calculating reaction rates (which
             * are dependent on the number of compound molecules), choosing the time of next reaction, choosing next reaction, "executing" the reaction
             * by adapting compound numbers according to the reaction and writing the results to a file.
             */
            while (isRunning) {
                /*
                 * Check whether the sum of rates is empty, which indicates that no reaction can occur. If so, abort simulation.
                 */
                if (sumOfRates <= 0) {
                    LOGGER.info("Sum of rates is less or equal to zero, therefore no more reaction can occur and the simulation is stopped");
                    break;
                }
                firedReactions.clear();
                updateReactions();
                /*
                Get a set of all non-critical reaction indeces.
                 */
                LOGGER.debug("Go through non-critical reactions and compute their next firing time");
                Set<Integer> nonCriticalReactions = new HashSet<>();
                for (int reactionIdx : this.reactionRatesRun.keySet()) {
                    if (!this.criticalReactions.contains(reactionIdx)) {
                        nonCriticalReactions.add(reactionIdx);
                    }
                }
                /*
                Compute next firing time of non critical reactions
                 */
                double nextFiringTimeNonCritical;
                if (simStor.getMaxSimTime() > 0) {
                    if (simStor.getMaxSimTime() - timePassed <= 0) {
                        this.requestStop();
                        break;
                    }
                    nextFiringTimeNonCritical = simStor.getMaxSimTime() - timePassed;
                } else {
                    nextFiringTimeNonCritical = Double.MAX_VALUE;
                }
                if (!nonCriticalReactions.isEmpty()) {
                    for (int compoundIdx = 0; compoundIdx < simStor.getCompoundsInfluence().length; compoundIdx++) {
                        Set<Integer> reactions = simStor.getCompoundsInfluence()[compoundIdx];
                        if (reactions.isEmpty()) {
                            continue;
                        }
                        double my = 0;
                        double sigma = 0;
                        for (int reactionIdx : reactions) {
                            Double tmpRate = reactionRatesRun.get(reactionIdx);
                            if (tmpRate != null) {
                                int stoichFactor = simStor.getEductStoichMatrix()[reactionIdx][compoundIdx];
                                my += -stoichFactor * reactionRatesRun.get(reactionIdx);
                                sigma += stoichFactor * stoichFactor * tmpRate;
                            }
                        }
                        double scaledNr = Math.max(EPS * nonConstantMarkingRun[compoundIdx], 1);

                        nextFiringTimeNonCritical = Math.min(nextFiringTimeNonCritical, (scaledNr / Math.abs(my)));
                        nextFiringTimeNonCritical = Math.min(nextFiringTimeNonCritical, ((scaledNr * scaledNr) / sigma));
                    }
                }
                if (nextFiringTimeNonCritical < (1 / sumOfRates) * 10) {
                    for (short i = 0; i < 100; i++) {
                        int reactionIdx = exactStep();
                        if (reactionIdx == -1) {
                            break;
                        }
                    }
                    continue;
                }
                LOGGER.debug("Computing the sum of critical reaction rates and calculating the time at which the next critical reaction will occur");
                /*
                Compute the sum of critical reaction rates
                 */
                double criticalSumOfRates = 0;
                for (int idx : criticalReactions) {
                    criticalSumOfRates += reactionRatesRun.get(idx);
                }

                /*
                 * Calculate the time at which next critical reaction will occur.
                 */
                double nextFiringTimeCritical = Double.MAX_VALUE;
                if (criticalSumOfRates > 0) {
                    nextFiringTimeCritical = (Math.log(1 / randomRun.nextDouble()) / criticalSumOfRates);
                }

                //only non-critical reactions will occur
                if (nextFiringTimeNonCritical < nextFiringTimeCritical) {
                    LOGGER.debug("Next firing time is from a non critical reaction, therefore trying to fire it");
                    //check if the simulation should go on or the maximal time is reached.
                    if (simStor.getMaxSimTime() > 0 && (timePassed + nextFiringTimeNonCritical) > simStor.getMaxSimTime()) {
                        LOGGER.debug("Maximum simulation time has been reached, therefore stopping the simulation before firing it");
                        this.requestStop();
                        break;
                    }
                    LOGGER.debug("Firing the next non critical Reaction");
                    for (int reactionIdx : nonCriticalReactions) {
                        int nrOccu = randomRun.nextPoisson(reactionRatesRun.get(reactionIdx) * nextFiringTimeNonCritical);
                        for (int i = 0; i < nrOccu; i++) {
                            firedReactions.add(reactionIdx);
                            fireReaction(reactionIdx);
                        }
                    }
                    /*
                     * Update step and time counters.
                     */
                    this.stepsSimulated++;
                    this.timePassed += nextFiringTimeNonCritical;
                } else {
                    LOGGER.debug("Next firing reaction is critical");
                    //check if the simulation should go on or the maximal time is reached.
                    if (simStor.getMaxSimTime() > 0 && (timePassed + nextFiringTimeCritical) > simStor.getMaxSimTime()) {
                        LOGGER.debug("Maximum simulation time has been reached, therefore stopping the simulation before firing it");
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
                    LOGGER.debug("Firing the next critical reaction");
                    int reaction = 0;
                    double ratesSum = 0;
                    for (int reactionIdx : criticalReactions) {
                        reaction = reactionIdx;
                        double reactionRate = reactionRatesRun.get(reactionIdx);
                        ratesSum += reactionRate;
                        if (ratesSum >= criticalSumOfRates * randomRun.nextDouble()) {
                            break;
                        }
                    }
                    firedReactions.add(reaction);
                    fireReaction(reaction);
                    for (int reactionIdx : nonCriticalReactions) {
                        int nrOccu = randomRun.nextPoisson(reactionRatesRun.get(reactionIdx) * nextFiringTimeCritical);
                        for (int i = 0; i < nrOccu; i++) {
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
                if (simStor.getUpdateInterval() == 0 || timePassed - lastUpdate >= simStor.getUpdateInterval()) {
                    LOGGER.debug("Writing the new molecule numbers into the output file");
                    writeOutput(firedReactions.toArray(new Integer[firedReactions.size()]));
                }
            }
            //close writers
            outputWriter.close();
        } catch (IOException ex) {
            LOGGER.error("IOException while trying to calculate the next firing step or writing its results in the output", ex);
        }
        this.updateOutput();
        fireSimulationEvent(SimulationEvent.DONE, ("\n").concat(SimulationManager.strings.get("SimFinished")).concat(dateFormat.format(Calendar.getInstance().getTime())));
        gillTS.checkOutRunningThread();
    }

    private void updateReactions() {
        LOGGER.info("Updating the reactions with the new reaction rates");
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
                //Check if reaction became critical.
                /*
                minL is the number of times this reaction can occur before one of its reactants depletes.
                 */
                int minL = CRITICAL_THRESHOLD;
                /*
                Iterate through non-constant educts of this reaction.
                 */
                for (int eductIdx : simStor.getReactionsNonConstantEducts()[reactionIdx]) {
                    //Number of molecules of the educt.
                    long eductNr = this.nonConstantMarkingRun[eductIdx];
                    //Stoichiometric factor of the reaction
                    int factor = simStor.getEductStoichMatrix()[reactionIdx][eductIdx];
                    minL = Math.min(minL, Math.round(eductNr / factor));
                }
                for (int eductIdx : simStor.getReactionsConstantEducts()[reactionIdx]) {
                    //Number of molecules of the educt.
                    long eductNr = this.constantMarkingRun[eductIdx];
                    //Stoichiometric factor of the reaction
                    int factor = simStor.getEductStoichMatrix()[reactionIdx][eductIdx + simStor.getNonConstantPlaceIDs().length];
                    minL = Math.min(minL, Math.round(eductNr / factor));
                }
                /*
                If the number of executable reactions is less than the critical threshold, the reaction
                is considered as critical.
                 */
                if (minL < CRITICAL_THRESHOLD) {
                    this.criticalReactions.add(reactionIdx);
                } else {
                    this.criticalReactions.remove(reactionIdx);
                }

                reactionRatesRun.put(reactionIdx, reactionRate);
                /*
                Add new reaction rate to the sum.
                 */
                sumOfRates += reactionRate;
            } else {
                reactionRatesRun.remove(reactionIdx);
                this.criticalReactions.remove(reactionIdx);
            }
        }
        /*
        Remove all reactions from the reactionsToUpdate-set except for the reactions with constant pre-places (educts).
         */
        reactionsToUpdate.clear();
        reactionsToUpdate.addAll(simStor.getConstantPlacesPostTransitions());
    }

    /**
     * Performs an exact step of SSA.
     *
     * @return index of occurred reaction.
     */
    private int exactStep() {
        LOGGER.info("Performing one step of the stochastic simulation");
        updateReactions();
        /*
         * Check whether the sum of rates is empty, which indicates that no reaction can occur. If so, abort simulation.
         */
        if (sumOfRates <= 0) {
            LOGGER.debug("Sum of rates is less than or equal to zero, therefore stopping the simulation");
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
        if (simStor.getMaxSimTime() > 0 && (timePassed + nextFiringTime) > simStor.getMaxSimTime()) {
            LOGGER.debug("Maximum simulation time has been reached, therefore stopping the simulation");
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
        LOGGER.debug("Calculating the reaction to occur next");
        int reaction = 0;
        double ratesSum = 0;
        for (int reactionIdx : reactionRatesRun.keySet()) {
            reaction = reactionIdx;
            double reactionRate = reactionRatesRun.get(reactionIdx);
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
            writeOutput(reaction);
        }

        //fire selected reaction
        LOGGER.debug("Firing the reaction that has been determined to occur next");
        fireReaction(reaction);

        if (simStor.getUpdateInterval() == 0 || timePassed - lastUpdate >= simStor.getUpdateInterval()) {
            LOGGER.debug("Writing the resulting output after firing the reaction");
            writeOutput(reaction);
        }
        return reaction;
    }

    private void fireReaction(int reactionIdx) {
        /*
        * Adapt coumpounds molecule numbers according to the chosen reaction. Iterate through
        the non-constant conpounds of the reaction and substract the stoichiometric factor from the number of molecules
        of this compound.
         */
        LOGGER.debug("Firing the reaction with the ID: " + Integer.toString(reactionIdx));
        LOGGER.debug("Calculating the new values for all educts");
        for (int eductIdx : simStor.getReactionsNonConstantEducts()[reactionIdx]) {
            /*
            Get the number of molecules of the educt and remove the stoichiometric factor from it.
             */
            long nrOfMolecules = nonConstantMarkingRun[eductIdx] - simStor.getEductStoichMatrix()[reactionIdx][eductIdx];
            //Ensure that no negative values are produced
            if (nrOfMolecules < 0) {
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
            concentrations.put(this.nonConstantPlaceIDsRun[eductIdx], nrOfMolecules / simStor.getVolMol());
            /*
            Mark all reactions which are dependent from this educt so the reaction rates will be re-calculated.
             */
            reactionsToUpdate.addAll(simStor.getCompoundsInfluence()[eductIdx]);
        }
        LOGGER.debug("Calculating the new values for all products");
        for (int productIdx : simStor.getReactionsNonConstantProducts()[reactionIdx]) {
            /*
            Get the number of molecules of the educt and add the stoichiometric factor from it.
             */
            long nrOfMolecules = nonConstantMarkingRun[productIdx] + simStor.getProductStoichMatrix()[reactionIdx][productIdx];
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
        LOGGER.debug("Updating the marking and concentration for all constant places");
        for (int i = 0; i < this.constantMarkingRun.length; i++) {
            MathematicalExpression exp = this.constantPlacesExpRun[i];
            double val = exp.evaluateML(concentrations, this.timePassed);
            this.constantMarkingRun[i] = Math.round(val * simStor.getVolMol());
            this.concentrations.put(this.constantPlaceIDsRun[i], val);
        }
    }
}
