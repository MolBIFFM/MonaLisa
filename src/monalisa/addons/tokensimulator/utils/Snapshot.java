/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Saves a state-snapshot of the Petri net.
 *
 * @author Pavel Balazki.
 */
public class Snapshot {

    //BEGIN VARIABLES DECLARATION
    //Number of step at which this snapshot was taken
    private final int stepNr;
    //Elements of the ListModel of historyList
    private final String[] historyListModelNames;
    //Transitions that have fired in history
    private final ArrayList<Transition[]> historyArrayList;
    //Current marking; links a number of tokens for each place-id for non-static places
    private final Map<Integer, Long> marking;
    /**
     * Constant places have a MathematicalExpression defining their number of
     * tokens.
     */
    private final Map<Integer, MathematicalExpression> constantPlaces;
    //Statistic of current step
    private Statistic statistic;
    private static final Logger LOGGER = LogManager.getLogger(Snapshot.class);

    //END VARIABLES DECLARATION
    //BEGIN CONSTRUCTORS
    /**
     * Constructor without parameters is not allowed.
     */
    private Snapshot() {
        stepNr = 0;
        historyListModelNames = null;
        historyArrayList = null;
        marking = null;
        constantPlaces = null;
    }

    /**
     * Create a new snapshot
     *
     * @param stepNrN number of the step at which this snapshot is created.
     * @param historyListModelN actual historyListModel
     * @param historyArrayListN actual historyArrayList
     * @param markingN actual marking
     */
    public Snapshot(int stepNrN, Object[] historyListModelN,
            ArrayList<Transition[]> historyArrayListN,
            Map<Integer, Long> markingN,
            Map<Integer, MathematicalExpression> constantPlacesN) {
        LOGGER.info("Creating a new snapshot at stepnumber " + Integer.toString(stepNrN));
        this.stepNr = stepNrN;
        this.historyListModelNames = new String[historyListModelN.length]; // = (String[]) historyListModelN;
        int j = 0;
        for (Object entryName : historyListModelN) {
            historyListModelNames[j] = (String) entryName;
            j++;
        }

        //deep copy of historyArrayList
        this.historyArrayList = new ArrayList<>();
        for (Transition[] tt : historyArrayListN) {
            Transition[] temp = new Transition[tt.length];
            int i = 0;
            for (Transition t : tt) {
                temp[i++] = t;
            }
            historyArrayList.add(temp);
        }

        //deep copy of marking
        this.marking = new HashMap<>(markingN);
        //deep copy of constant places
        this.constantPlaces = new HashMap<>(constantPlacesN);
    }
    //END CONSTRUCTORS

    /**
     * Get the number of step at which the snapshot was created.
     *
     * @return Number of simulation step.
     */
    public int getStepNr() {
        return this.stepNr;
    }

    public String[] getHistoryListModelNames() {
        return this.historyListModelNames;
    }

    public ArrayList<Transition[]> getHistoryArrayList() {
        return this.historyArrayList;
    }

    /**
     * Get the marking which was present as the snapshot was created.
     *
     * @return Map with IDs of non-constant places as keys and numbers of
     * markings on this places as value.
     */
    public Map<Integer, Long> getMarking() {
        return this.marking;
    }

    /**
     * Get the map of constant places and their mathematical expressions.
     *
     * @return
     */
    public Map<Integer, MathematicalExpression> getConstantPlaces() {
        return this.constantPlaces;
    }

    /**
     * Set the statistic for this snapshot.
     *
     * @param statisticN
     */
    public void setStatistic(Statistic statisticN) {
        this.statistic = statisticN;
    }

    /**
     * Get statistics of this snapshot.
     *
     * @return
     */
    public Statistic getStatistic() {
        return this.statistic;
    }
}
