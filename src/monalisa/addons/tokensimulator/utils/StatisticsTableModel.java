/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.utils;

import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 * TableModel for statisticsTable
 */
public class StatisticsTableModel extends AbstractTableModel {

    private final String[] columnNames;
    private final Map<String, Integer> countTransitions;

    public StatisticsTableModel(String[] columnNames, Map<String, Integer> countTransitions) {
        this.columnNames = columnNames;
        this.countTransitions = countTransitions;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
                return Integer.class;
            default:
                return Object.class;
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
        if (col == 0) {
            return countTransitions.keySet().toArray()[row];
        } else {
            return countTransitions.values().toArray()[row];
        }
    }
}
