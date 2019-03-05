/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import monalisa.data.pn.Transition;

/**
 * Saves the statistics of firings.
 * @author Pavel Balazki.
 */
public class Statistic {
    //BEGIN VARIABLES DECLARATION
    private static final String[] columnNames = {TokenSimulator.strings.get("STATTName"), TokenSimulator.strings.get("STATFired")};    
    //GUI
    //table representation of statistic data
    private JTable statisticTable;
    //table model of statisticsTable
    private final Statistic.StatisticsTableModel tableModel;
    //TokenSimulator that fires the transitions
    private final TokenSimulator tokenSim;
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
    private Statistic(){
        tableModel = null;
        tokenSim = null;
    }
    
    Statistic(TokenSimulator tsN){
        this.tokenSim = tsN;
        this.stepsFired = 0;                                                    //counts how many steps were fired
        this.tokensTotal = 0;                                                   //counts how many tokens are on the places
        this.transitionsFired = 0;                                              //counts how many transitions have fired
        
        /*
         * For every transition in petri-net, an entry in countTransitions saves how often the transition has fired.
         */
        this.countTransitions = new HashMap<>();
        for (Transition transition : this.tokenSim.getPetriNet().transitions()){
            this.countTransitions.put((String)transition.getProperty("name"), 0);
        }
        
        this.tableModel = new Statistic.StatisticsTableModel();
    }
    
    /**
     * Create a copy of existing statistic.
     */
    Statistic(Statistic statN){
        this.tokenSim = statN.tokenSim;
        this.stepsFired = statN.stepsFired;
        this.tokensTotal = statN.tokensTotal;
        this.transitionsFired = statN.transitionsFired;
        //deep copy of countTransitions
        this.countTransitions = new HashMap<>(statN.countTransitions);        
        this.tableModel = new Statistic.StatisticsTableModel();
    }
    //END CONSTRUCTORS
    
    /**
     * TableModel for statisticsTable
     */
    private class StatisticsTableModel extends AbstractTableModel{
        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        @Override
        public Class<?> getColumnClass(int column){
            switch (column){
                case 0: return String.class;
                case 1: return Integer.class;
                default: return Object.class;
            }
        }

        @Override
        public int getRowCount() {
            return countTransitions.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0){
                return countTransitions.keySet().toArray()[row];
            }
            else
                return countTransitions.values().toArray()[row];
        }    
    }
    
    /**
     * If transition t has fired, increase the count of firings for this transition
     * and the global amount of fired transitions transitionsFired.
     * @param t Transition which fired in last step.
     */
    public void transitionFired(Transition t){
        this.countTransitions.put(t.toString(), this.countTransitions.get(t.toString())+1);
        this.transitionsFired++;
    }
    
    /**
     * If transition t has reverse-fired, decrease the count of firings for this transition
     * and the global amount of fired transitions transitionsFired.
     * @param t 
     */
    public void transitionReverseFired(Transition t){
        this.countTransitions.put(t.toString(), this.countTransitions.get(t.toString())-1);
        this.transitionsFired--;
    }
    
    /**
     * return a table with statistics
     * @return 
     */
    public JTable getStatisticTable(){
        this.statisticTable = new JTable();
        this.statisticTable.setModel(this.tableModel);
        this.statisticTable.setFillsViewportHeight(true);
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(this.tableModel);
        this.statisticTable.setRowSorter(rowSorter);
        List sortKeys = new ArrayList();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));        
        rowSorter.setComparator(1, new Comparator<Integer>(){
            @Override
            public int compare(Integer t, Integer t1) {
                return (t-t1);
            }
        });
        rowSorter.setSortKeys(sortKeys);
        
        return this.statisticTable;
    }
}
