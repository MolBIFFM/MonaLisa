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

import java.util.HashMap;
import java.util.Map;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.data.pn.Transition;

/**
 * Saves the statistics of firings.
 *
 * @author Pavel Balazki.
 */
public class Statistic {

    //BEGIN VARIABLES DECLARATION
    private static final String[] columnNames = {SimulationManager.strings.get("STATTName"), SimulationManager.strings.get("STATFired")};
    //GUI
    //table representation of statistic data
    //table model of statisticsTable
    private final StatisticsTableModel tableModel;
    //TokenSimulator that fires the transitions
    private final SimulationManager simulationMan;
    /**
     * Maps the name of the transition to the number of how often it did fire
     */
    //Counts how often a single transition has fired.
    protected Map<String, Integer> countTransitions;
    protected int stepsFired, transitionsFired, tokensTotal;
    //END VARIABLES DECLARATION

    //BEGIN CONSTRUCTORS
    /**
     * Constructor without parameters is not allowed.
     */
    private Statistic() {
        tableModel = null;
        simulationMan = null;
    }

    public Statistic(SimulationManager simulationManager) {
        this.simulationMan = simulationManager;
        this.stepsFired = 0;                                                    //counts how many steps were fired
        this.tokensTotal = 0;                                                   //counts how many tokens are on the places
        this.transitionsFired = 0;                                              //counts how many transitions have fired

        /*
         * For every transition in petri-net, an entry in countTransitions saves how often the transition has fired.
         */
        this.countTransitions = new HashMap<>();
        for (Transition transition : this.simulationMan.getPetriNet().transitions()) {
            this.countTransitions.put((String) transition.getProperty("name"), 0);
        }

        this.tableModel = new StatisticsTableModel(columnNames, countTransitions);
    }

    /**
     * Create a copy of existing statistic.
     */
    public Statistic(Statistic statN) {
        this.simulationMan = statN.simulationMan;
        this.stepsFired = statN.stepsFired;
        this.tokensTotal = statN.tokensTotal;
        this.transitionsFired = statN.transitionsFired;
        //deep copy of countTransitions
        this.countTransitions = new HashMap<>(statN.countTransitions);
        this.tableModel = new StatisticsTableModel(columnNames, countTransitions);
    }
    //END CONSTRUCTORS

    public void incrementSteps() {
        this.stepsFired++;
    }

    public void decrementSteps() {
        this.stepsFired--;
    }

    /**
     * If transition t has fired, increase the count of firings for this
     * transition and the global amount of fired transitions transitionsFired.
     *
     * @param t Transition which fired in last step.
     */
    public void transitionFired(Transition t) {
        this.countTransitions.put(t.toString(), this.countTransitions.get(t.toString()) + 1);
        this.transitionsFired++;
    }

    /**
     * If transition t has reverse-fired, decrease the count of firings for this
     * transition and the global amount of fired transitions transitionsFired.
     *
     * @param t
     */
    public void transitionReverseFired(Transition t) {
        this.countTransitions.put(t.toString(), this.countTransitions.get(t.toString()) - 1);
        this.transitionsFired--;
    }

    /**
     * @return the tableModel
     */
    public StatisticsTableModel getTableModel() {
        return tableModel;
    }
}
