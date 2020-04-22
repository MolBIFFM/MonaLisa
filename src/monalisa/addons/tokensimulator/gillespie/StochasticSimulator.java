/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.gillespie;

import monalisa.addons.tokensimulator.gillespie.GillespieTokenSim;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import monalisa.addons.tokensimulator.utils.Utilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.exceptions.FactorialTooBigException;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.tools.BooleanChangeEvent;
import monalisa.tools.BooleanChangeListener;
import monalisa.util.HighQualityRandom;
import monalisa.util.MonaLisaFileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class allows a simulation of a chemical system using the
 * Gillespie-algorithm for stochastic simulations of mass-action-kinetic driven
 * reactions. It uses a Petri Net as a mathematical description of a chemical
 * system.
 *
 * @author Pavel Balazki.
 */
public class StochasticSimulator extends javax.swing.JFrame implements BooleanChangeListener {

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
     * Array with number of tokens on all non-constant places of the underlying
     * Petri net at the beginning of the simulation (initial state).
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
     * Array with mathematical expressions which describe the number of tokens
     * on all constant places of the underlying Petri net.
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
     * Array with mathematical expressions which describe reaction rate
     * constants of the reactions (transitions of the underlying Petri net).
     */
    private final MathematicalExpression[] reactionRateConstants;
    /**
     * Order and multiplier of reactions. For each reaction, an array is stored
     * with the order of the reaction on the first place and the multiplier
     * (used for calculating stochastic reaction rate constant) on the second.
     */
    private final int[][] reactionOrder;
    /**
     * Stoichiometric matrix of reactions and their educts. First index is the
     * reaction, second index is the educt of it. The compounds of non-constant
     * places are stored first, followed by the compounds of constant places.
     * The value is the number of educt molecules which are consumed by the
     * reaction.
     */
    private final int[][] eductStoichMatrix;
    /**
     * Stoichiometric matrix of reactions and their products. First index is the
     * reaction, second index is the product of it. The compounds of
     * non-constant places are stored first, followed by the compounds of
     * constant places. The value is the number of product molecules which are
     * produced by the reaction.
     */
    private final int[][] productStoichMatrix;
    /**
     * Array of lists which store indeces of reactions which are influenced by a
     * compound with index i. Used for determining which reaction rates should
     * be re-computed.
     */
    private final Set<Integer>[] compoundsInfluence;
    /**
     * For each reaction, a set of indeces of non-constant places on which the
     * reaction rate depends.
     */
    private final Set<Integer>[] reactionsNonConstantEducts;
    /**
     * For each reaction, a set of indeces of constant places on which the
     * reaction rate depends.
     */
    private final Set<Integer>[] reactionsConstantEducts;
    /**
     * For each reaction, a set of indeces of non-constant places which are the
     * products of this reaction.
     */
    private final Set<Integer>[] reactionsNonConstantProducts;
    /**
     * Set of transitions (reactions) which have constant places as pre-places.
     * The rates of this reactions should be re-calculated every step. However,
     * if the mathematical expression of the constant pre-place is also constant
     * (has no variables and is not dependent on time), the transition is not in
     * the list. Also contains indeces of transitions which rates have constant
     * places or time as variables.
     */
    private final Set<Integer> constantPlacesPostTransitions = new HashSet<>();
    /**
     * Volume of the simulated system.
     */
    private final double volume;
    /**
     * Volume multiplied with the Avogadro constant.
     */
    private final double volMol;

    /**
     * File where the simulation output is written to (csv formatted). If
     * several parallel simulation runs are started, only the output of the
     * first run is written to this file. For each additional run, a new file
     * with incrementing index is created.
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
    private final GillespieTokenSim gillTS;
    /**
     * Number of simulation threads which can run simulataneously. If more
     * simulation runs are started, they are put in a queue and executed as soon
     * as capacities are available.
     */
    private int MAX_PARALLEL_THREADS = Runtime.getRuntime().availableProcessors();
    /**
     * Interval in which the updater task is performed. milliseconds.
     */
    private final int UPDATER_TASK_INTERVAL = 3000;
    /**
     * List of SimulationRunnable instances which are currently being executed.
     * Each runnable is a simulation run.
     */
    private final ArrayList<ExactSSA> runnables = new ArrayList<>();
    /**
     * List of StochasticSimulatorRunPanels associated with runnables.
     */
    private final ArrayList<StochasticSimulatorRunPanel> runPanels = new ArrayList<>();
    /**
     * Map of currently running threads, linked to the runnable instances they
     * are executing.
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
     * Random number generator. Used for generating seeds of new random
     * generators of the simulation runs.
     */
    private HighQualityRandom globalRandom;

    private final SimulationStorage simStor;
    /**
     * Object which processes the output files, e.g. crates a new file with the
     * averages of all simulation runs.
     */
//    private final OutputProcessor outputProcessor = new OutputProcessor();
    private static final Logger LOGGER = LogManager.getLogger(StochasticSimulator.class);
    //END VARIABLES DECLARATION
    private final FastSimulationModes simOwner;

    //BEGIN INNER CLASSES
    /**
     * A class which performs operations with the output files. Can compute the
     * averages of all simulation runs.
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
     * Creates new form StochasticSimulator and extracts the information of the
     * chemical system from given data (Petrinet, reaction constants etc.).
     *
     * @param tsN
     * @param detReactionConstants
     * @param nonConstantPlaces
     * @param vol
     * @param ran
     */
    public StochasticSimulator(GillespieTokenSim tsN, Map<Integer, MathematicalExpression> detReactionConstants, Map<Integer, Long> nonConstantPlaces,
            double vol, HighQualityRandom ran, FastSimulationModes simOwner) {
        LOGGER.info("Instantiating a stochastic simulator");
        this.volume = vol;
        this.volMol = this.volume * 6E23;
        /*
         * Create the default output file.
         */
        this.outputFile = new File(System.getProperty("user.home").concat(File.separator).concat("StochasticSimulation").concat(File.separator).
                concat("StochasticSim.csv"));
        this.outputFiles = new ArrayList<>();
        this.simOwner = simOwner;
        this.gillTS = tsN;
        PetriNetFacade petriNet = gillTS.getPetriNet();
        /*
        Iterate through places of the net and create entries of compounds.
         */
        //Keys are IDs, values are indeces.
        LOGGER.debug("Filling the simulator with data out of the current petri net");
        Map<Integer, Integer> constantPlacesIDsMap = new HashMap<>();
        ArrayList<String> constantPlacesNamesList = new ArrayList<>();
        ArrayList<MathematicalExpression> constantPlacesMathExpList = new ArrayList<>();
        //Keys are IDs, values are indeces.
        Map<Integer, Integer> nonConstantPlacesIDsMap = new HashMap<>();
        ArrayList<String> nonConstantPlacesNamesList = new ArrayList<>();
        ArrayList<Long> nonConstantPlacesMarkingList = new ArrayList<>();
        int nonConstantIdx = 0;
        int constantIdx = 0;
        for (Place place : petriNet.places()) {
            int id = place.id();
            String name = place.getProperty("name");
            if (!place.isConstant()) {
                nonConstantPlacesIDsMap.put(id, nonConstantIdx++);
                nonConstantPlacesNamesList.add(name);
                nonConstantPlacesMarkingList.add(this.gillTS.getTokens(id));
            } else {
                constantPlacesIDsMap.put(id, constantIdx++);
                constantPlacesNamesList.add(name);
                constantPlacesMathExpList.add(this.gillTS.getSimulationMan().getMathematicalExpression(id));
            }
        }
        //non-constant places
        this.nonConstantPlaceIDs = new int[nonConstantPlacesIDsMap.size()];
        for (Map.Entry<Integer, Integer> entr : nonConstantPlacesIDsMap.entrySet()) {
            this.nonConstantPlaceIDs[entr.getValue()] = entr.getKey();
        }
        this.nonConstantPlaceNames = new String[nonConstantPlacesNamesList.size()];
        for (int i = 0; i < nonConstantPlacesNamesList.size(); i++) {
            this.nonConstantPlaceNames[i] = nonConstantPlacesNamesList.get(i);
        }
        this.nonConstantInitialMarking = new long[nonConstantPlacesMarkingList.size()];
        for (int i = 0; i < nonConstantPlacesMarkingList.size(); i++) {
            this.nonConstantInitialMarking[i] = nonConstantPlacesMarkingList.get(i);
        }
        //constant places
        this.constantPlaceIDs = new int[constantPlacesIDsMap.size()];
        for (Map.Entry<Integer, Integer> entr : constantPlacesIDsMap.entrySet()) {
            this.constantPlaceIDs[entr.getValue()] = entr.getKey();
        }
        this.constantPlaceNames = new String[constantPlacesNamesList.size()];
        for (int i = 0; i < constantPlacesNamesList.size(); i++) {
            this.constantPlaceNames[i] = constantPlacesNamesList.get(i);
        }
        this.constantPlacesExp = new MathematicalExpression[constantPlacesMathExpList.size()];
        for (int i = 0; i < constantPlacesMathExpList.size(); i++) {
            this.constantPlacesExp[i] = constantPlacesMathExpList.get(i);
        }
        this.compoundsInfluence = new Set[nonConstantPlaceIDs.length];
        for (int i = 0; i < compoundsInfluence.length; i++) {
            this.compoundsInfluence[i] = new HashSet<>();
        }

        /*
        Iterate through transitions and create entries of reactions.
         */
        LOGGER.debug("Create entries of reactions out of the transitions of the petri net");
        int reactionIdx = -1;
        this.reactionIDs = new int[petriNet.transitions().size()];
        this.reactionNames = new String[petriNet.transitions().size()];
        this.reactionRateConstants = new MathematicalExpression[petriNet.transitions().size()];
        this.reactionOrder = new int[petriNet.transitions().size()][2];
        this.eductStoichMatrix = new int[petriNet.transitions().size()][petriNet.places().size()];
        this.productStoichMatrix = new int[petriNet.transitions().size()][petriNet.places().size()];
        this.reactionsConstantEducts = new Set[petriNet.transitions().size()];
        for (int i = 0; i < reactionsConstantEducts.length; i++) {
            this.reactionsConstantEducts[i] = new HashSet<>();
        }
        this.reactionsNonConstantEducts = new Set[petriNet.transitions().size()];
        for (int i = 0; i < reactionsNonConstantEducts.length; i++) {
            this.reactionsNonConstantEducts[i] = new HashSet<>();
        }
        this.reactionsNonConstantProducts = new Set[petriNet.transitions().size()];
        for (int i = 0; i < reactionsNonConstantProducts.length; i++) {
            this.reactionsNonConstantProducts[i] = new HashSet<>();
        }
        for (Transition transition : petriNet.transitions()) {
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
            for (Place p : petriNet.getInputPlacesFor(transition)) {
                int weight = petriNet.getArc(p, transition).weight();
                try {
                    multiplier *= Utilities.factorial((long) weight);
                } catch (FactorialTooBigException ex) {
                    try {
                        multiplier *= Utilities.factorial(20L);
                    } catch (FactorialTooBigException ex2) {
                        LOGGER.error("Factorial after multiple changes still too big, should not happen", ex2);
                    }
                }
                order += weight;
            }
            int[] inf = {order, multiplier};
            this.reactionOrder[reactionIdx] = inf;

            /*
            Iterate through pre- and post-places and fill the stoichiometric map.
             */
            LOGGER.debug("Fill the stoichiometric map with data out of the pre- and postplaces");
            for (Place p : transition.inputs()) {
                int pId = p.id();
                int weight = petriNet.getArc(p, transition).weight();
                if (!p.isConstant()) {
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
                } else {
                    /*
                    If a transition has a constant pre-place, its reaction rate must be recalculated at each step.
                     */
                    if (!gillTS.getSimulationMan().getMathematicalExpression(pId).isConstant()) {
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
            for (Place p : transition.outputs()) {
                int pId = p.id();
                int weight = petriNet.getArc(transition, p).weight();
                if (!p.isConstant()) {
                    //Index of the compound
                    int pIdx = nonConstantPlacesIDsMap.get(pId);
                    productStoichMatrix[reactionIdx][pIdx] = weight;

                    /*
                    Place p is a product of transition with index reactionIdx.
                     */
                    reactionsNonConstantProducts[reactionIdx].add(pIdx);
                } else {
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
            if (rateConstant.toString().contains(MathematicalExpression.TIME_VAR)) {
                constantPlacesPostTransitions.add(reactionIdx);
            } else {
                for (int placeID : rateConstant.getVariables().values()) {
                    //get the index of this compound
                    int placeIdx;
                    if (nonConstantPlacesIDsMap.containsKey(placeID)) {
                        placeIdx = nonConstantPlacesIDsMap.get(placeID);
                        compoundsInfluence[placeIdx].add(reactionIdx);
                    } else if (constantPlacesIDsMap.containsKey(placeID)) {
                        constantPlacesPostTransitions.add(reactionIdx);
                    }
                }
            }

        }
        this.simStor = new SimulationStorage(
                volMol,
                maxSimTime,
                updateInterval,
                nonConstantPlaceIDs,
                constantPlaceIDs,
                constantPlacesExp,
                reactionRateConstants,
                nonConstantInitialMarking,
                nonConstantPlaceNames,
                constantPlaceNames,
                reactionIDs,
                constantPlacesPostTransitions,
                reactionsNonConstantEducts,
                eductStoichMatrix,
                compoundsInfluence,
                reactionsNonConstantProducts,
                productStoichMatrix,
                reactionOrder,
                reactionsConstantEducts,
                reactionNames);

        initComponents();
        this.globalRandom = new HighQualityRandom(ran.getSeed());
        outPathField.setText(outputFile.getAbsolutePath());
        /*
         * Add listener for close operation - all running threads should be ended before exiting
         */
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.OK_OPTION;
                if (running) {
                    result = JOptionPane.showConfirmDialog(StochasticSimulator.this, "Stop all running simulations and close?", "Terminate simulations",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                }
                if (result == JOptionPane.OK_OPTION) {
                    simOwner.removeFastSim(StochasticSimulator.this);
                }
            }
        });
        setVisible(false);

        for (int i = 1; i < 501; i++) {
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
        runTime.setText(SimulationManager.strings.get("SimTimeSpan"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 1;
        options.add(runTime, gridBagConstraints);

        updateTime.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        updateTime.setText(SimulationManager.strings.get("OutputInterval"));
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
        timeSpanField.setToolTipText(SimulationManager.strings.get("SimTimeSpanTT"));
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
        updateIntervalField.setToolTipText(SimulationManager.strings.get("OutputIntervalTT"));
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
        algorithmType.setText(SimulationManager.strings.get("SelectAlgorithm"));
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
        nbrThreads.setText(SimulationManager.strings.get("NrOfSims"));
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

        newRandomB.setText(SimulationManager.strings.get("NewRandomB"));
        newRandomB.setToolTipText(SimulationManager.strings.get("NewRandomBT"));
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
        runButton.setText(SimulationManager.strings.get("ATSFireTransitionsB"));
        runButton.setToolTipText(SimulationManager.strings.get("RunStochSimTT"));
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

        browseOutFileButton.setText(SimulationManager.strings.get("Browse"));
        browseOutFileButton.setToolTipText(SimulationManager.strings.get("OutputPathTT"));
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
        MonaLisaFileChooser fc = new MonaLisaFileChooser(getOutputFile());
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.showOpenDialog(this);
        File file = fc.getSelectedFile();
        if (file.isFile()) {
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
            LOGGER.error("Exception while trying to parse the time span and the update interval", ex);
        }
        String[] timeInput = this.timeSpanField.getValue().toString().split(":");
        try {
            double timeSpanD = (((Double.parseDouble(timeInput[0]) * 24 + Double.parseDouble(timeInput[1])) * 60)
                    + Double.parseDouble(timeInput[2])) * 60 + Double.parseDouble(timeInput[3]);
            this.maxSimTime = timeSpanD;
        } catch (NumberFormatException e) {
            LOGGER.error("NumberFormatException while trying to parse the timeinput", e);
        }
        timeInput = this.updateIntervalField.getValue().toString().split(":");
        try {
            double interval = (((Double.parseDouble(timeInput[0]) * 24 + Double.parseDouble(timeInput[1])) * 60)
                    + Double.parseDouble(timeInput[2])) * 60 + Double.parseDouble(timeInput[3]);
            this.updateInterval = interval;
        } catch (NumberFormatException e) {
            LOGGER.error("NumberFormatException while trying to pase the update interval", e);
        }

        /*
         * If no simulation is running, start the selected number of simulation runs. Disable all controlls. Create output files.
         */
        if (!running) {
            //Handle averages controls
//            this.averagesButton.setEnabled(false);
//            this.averagesProgressBar.setValue(0);

            getRunButton().setText(SimulationManager.strings.get("ATSStopFiringB"));
            getRunButton().setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/stop_tools.png")));
            browseOutFileButton.setEnabled(false);
            outPathField.setEditable(false);
            nrOfSimsBox.setEnabled(false);
            selectAlgoComboBox.setEnabled(false);
            running = true;
            /*
             * If runnable SimulationRunnable instances allready exist, create threads and start this runnables. Otherwise, create new simulation runs.
             */
            if (!this.runnables.isEmpty()) {
                for (ExactSSA run : this.runnables) {
                    /*
                    If maximum parallel running threads limit is not exceeded, start a new thread with the runnable.
                    Otherwise put it into the queue.
                     */
                    if (this.runningThreads.size() < MAX_PARALLEL_THREADS && this.gillTS.isNewThreadAllowed()) {
                        this.gillTS.registerNewThread();
                        Thread thread = new Thread(run);
                        this.runningThreads.put(thread, run);
                        thread.start();
                    } else {
                        this.runnablesQueue.offer(run);
                    }
                }
            } else {
                //Handle averages controls
//                this.averagesButton.setEnabled(false);

                //number of parallel simulation runs of the system.
                int nrOfRuns = (int) nrOfSimsBox.getSelectedItem();
                try {
                    //create output files
                    outputFile = new File(outPathField.getText());
                    if (!outputFile.isFile()) {
                        File dir = getOutputFile().getParentFile();
                        if (!dir.isDirectory()) {
                            dir.mkdir();
                        }
                    }
                    String outFileName = getOutputFile().getAbsolutePath();
                    String outFileExt;
                    if (outFileName.contains(".")) {
                        outFileExt = outFileName.substring(outFileName.lastIndexOf('.'));
                        outFileName = outFileName.substring(0, outFileName.lastIndexOf('.'));
                    } else {
                        outFileExt = "csv";
                    }
                    getOutputFiles().add(getOutputFile());

                    for (int i = 0; i < nrOfRuns; i++) {
                        if (i > 0) {
                            outputFile = new File(outFileName.concat("_").concat(String.valueOf(i).concat(outFileExt)));
                            getOutputFiles().add(getOutputFile());
                        }
                        getOutputFile().createNewFile();

                        /*
                         * Create a SimulationRunnable, create a thread which will execute the runnable and start it.
                         */
                        ExactSSA runnable = null;
                        if (selectAlgoComboBox.getSelectedItem().toString().equals("Exact SSA")) {
                            runnable = new ExactSSA(getOutputFile(), i, globalRandom.nextLong(), this.gillTS, simStor);
                        }
                        if (selectAlgoComboBox.getSelectedItem().toString().equals("Approximate SSA")) {
                            runnable = new TauLeapingSSA(getOutputFile(), i, globalRandom.nextLong(), this.gillTS, simStor);
                        }
                        StochasticSimulatorRunPanel runPanel = new StochasticSimulatorRunPanel(runnable, nonConstantPlaceNames, constantPlaceNames);
                        this.runPanels.add(runPanel);
                        runnable.addSimulationListener(runPanel);
                        getSimRunsTabbedPane().addTab("Simulation run " + i, runPanel);
                        this.runnables.add(runnable);
                        /*
                        If maximum parallel running threads limit is not exceeded, start a new thread with the runnable.
                        Otherwise put it into the queue.
                         */
                        if (i < MAX_PARALLEL_THREADS && this.gillTS.isNewThreadAllowed()) {
                            this.gillTS.registerNewThread();
                            Thread thread = new Thread(runnable);
                            this.runningThreads.put(thread, runnable);
                            thread.start();
                        } else {
                            this.runnablesQueue.offer(runnable);
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.error("IOException while trying to create files for coordinating threads in the stochastic simulation");
                    JOptionPane.showMessageDialog(this, SimulationManager.strings.get("CannotCreateFiles"),
                            SimulationManager.strings.get("Error"), JOptionPane.ERROR_MESSAGE);
                    this.outPathField.setEnabled(true);
                    this.browseOutFileButton.setEnabled(true);
                }
            }
            /*
             * Create and start the updater task.
             */
            Timer timer = new Timer();
            UpdaterTask ut = new UpdaterTask(runningThreads, runnablesQueue, gillTS, outputFile, outputFiles, isLogAll());
            ut.addBooleanChangeListener(this);
            timer.schedule(ut, 0, UPDATER_TASK_INTERVAL);
        } /*
         * If the simulation is running, stop it. The corresponding runnables, however, will not be destroyed and can be started afterwards.
         */ else {
            running = false;
            stopSimulation();
        }
    }//GEN-LAST:event_runButtonActionPerformed

    private void newRandomBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newRandomBActionPerformed
        this.globalRandom = new HighQualityRandom();
    }//GEN-LAST:event_newRandomBActionPerformed

    /**
     * Invoke the stop of simulation runs. All executed runnables will be
     * stopped, threads executing them destroyed. The runnables themselves
     * remain, so simulation can be continued from the point where it was
     * interrupted by creating threads with the runnables.
     */
    public void stopSimulation() {
        LOGGER.info("Stopping all simulations");
        running = false;
        for (ExactSSA runnable : this.runningThreads.values()) {
            runnable.requestStop();
        }
        this.runningThreads.clear();
        this.runnablesQueue.clear();
        getRunButton().setText(SimulationManager.strings.get("ATSFireTransitionsB"));
        getRunButton().setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/run_tools.png")));
    }

    public void closeSimulator() {
        int result = JOptionPane.OK_OPTION;
        if (running) {
            result = JOptionPane.showConfirmDialog(StochasticSimulator.this, "Stop all running simulations and close?", "Terminate simulations",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        }
        if (result == JOptionPane.OK_OPTION) {
            simOwner.removeFastSim(StochasticSimulator.this);
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

    /**
     * @return the nonConstantPlaceIDs
     */
    public int[] getNonConstantPlaceIDs() {
        return nonConstantPlaceIDs;
    }

    /**
     * @return the nonConstantPlaceNames
     */
    public String[] getNonConstantPlaceNames() {
        return nonConstantPlaceNames;
    }

    /**
     * @return the nonConstantInitialMarking
     */
    public long[] getNonConstantInitialMarking() {
        return nonConstantInitialMarking;
    }

    /**
     * @return the constantPlaceIDs
     */
    public int[] getConstantPlaceIDs() {
        return constantPlaceIDs;
    }

    /**
     * @return the constantPlaceNames
     */
    public String[] getConstantPlaceNames() {
        return constantPlaceNames;
    }

    /**
     * @return the constantPlacesExp
     */
    public MathematicalExpression[] getConstantPlacesExp() {
        return constantPlacesExp;
    }

    /**
     * @return the reactionIDs
     */
    public int[] getReactionIDs() {
        return reactionIDs;
    }

    /**
     * @return the reactionNames
     */
    public String[] getReactionNames() {
        return reactionNames;
    }

    /**
     * @return the reactionRateConstants
     */
    public MathematicalExpression[] getReactionRateConstants() {
        return reactionRateConstants;
    }

    /**
     * @return the reactionOrder
     */
    public int[][] getReactionOrder() {
        return reactionOrder;
    }

    /**
     * @return the eductStoichMatrix
     */
    public int[][] getEductStoichMatrix() {
        return eductStoichMatrix;
    }

    /**
     * @return the productStoichMatrix
     */
    public int[][] getProductStoichMatrix() {
        return productStoichMatrix;
    }

    /**
     * @return the compoundsInfluence
     */
    public Set<Integer>[] getCompoundsInfluence() {
        return compoundsInfluence;
    }

    /**
     * @return the reactionsNonConstantEducts
     */
    public Set<Integer>[] getReactionsNonConstantEducts() {
        return reactionsNonConstantEducts;
    }

    /**
     * @return the reactionsConstantEducts
     */
    public Set<Integer>[] getReactionsConstantEducts() {
        return reactionsConstantEducts;
    }

    /**
     * @return the reactionsNonConstantProducts
     */
    public Set<Integer>[] getReactionsNonConstantProducts() {
        return reactionsNonConstantProducts;
    }

    /**
     * @return the constantPlacesPostTransitions
     */
    public Set<Integer> getConstantPlacesPostTransitions() {
        return constantPlacesPostTransitions;
    }

    /**
     * @return the volMol
     */
    public double getVolMol() {
        return volMol;
    }

    /**
     * @return the maxSimTime
     */
    public double getMaxSimTime() {
        return maxSimTime;
    }

    /**
     * @return the updateInterval
     */
    public double getUpdateInterval() {
        return updateInterval;
    }

    /**
     * @return the simRunsTabbedPane
     */
    public javax.swing.JTabbedPane getSimRunsTabbedPane() {
        return simRunsTabbedPane;
    }

    /**
     * @return the runButton
     */
    public javax.swing.JButton getRunButton() {
        return runButton;
    }

    boolean isLogAll() {
        return createLogOfAllRuns.isSelected();
    }

    /**
     * @return the outputFile
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * @return the outputFiles
     */
    public List<File> getOutputFiles() {
        return outputFiles;
    }

    @Override
    public void changed(BooleanChangeEvent e) {
        if (e.getNewValue() == false) {
        getRunButton().setText(SimulationManager.strings.get("ATSFireTransitionsB"));
        getRunButton().setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/run_tools.png")));
        running = false;
        }
    }
}
