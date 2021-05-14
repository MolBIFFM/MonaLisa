/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.stochastic;

import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Transition;

/**
 *
 * @author jens
 */
public class SetFiringRatesPanel extends javax.swing.JPanel {

    private AbstractTableModel tableModel;
    private NetViewer netViewer;
    final String[] columnNames = {SimulationManager.strings.get("StochTSFiringRatesTableTransition"), SimulationManager.strings.get("StochTSFiringRatesTableRate")};   //names of columns of the firingRatesTable    

    /**
     * Creates new form SetFiringRatesPanel
     */
    public SetFiringRatesPanel(NetViewer netViewer, final PetriNetFacade petriNet, final Map<Integer, Double> firingRates) {
        this.netViewer = netViewer;
        this.tableModel = tableModel = new AbstractTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col != 0;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == 1) {
                    try {
                        /*
                         * Replace "," with "." first.
                         */
                        double val = Double.parseDouble(value.toString().replaceAll(",", "."));
                        firingRates.put(((Transition) getValueAt(row, 0)).id(), val);
                    } catch (NumberFormatException ex) {
                    }
                }
            }

            @Override
            public String getColumnName(int col) {
                return columnNames[col];
            }

            @Override
            public int getRowCount() {
                return firingRates.size();
            }

            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (col == 0) {
                    return petriNet.findTransition(firingRates.keySet().toArray(new Integer[firingRates.size()])[row]);
                } else {
                    return firingRates.values().toArray(new Double[firingRates.size()])[row];
                }
            }
        };

        initComponents();
    }

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
        jScrollPane1 = new javax.swing.JScrollPane();
        firingRatesTable = new javax.swing.JTable();
        saveButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        firingRatesTable.setAutoCreateRowSorter(true);
        firingRatesTable.setModel(tableModel);
        firingRatesTable.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(firingRatesTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(saveButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        if (firingRatesTable.isEditing()) {
            firingRatesTable.getCellEditor().stopCellEditing();
        }
        for (int i=0; i< firingRatesTable.getModel().getRowCount(); i++) {
            if ((Double) firingRatesTable.getModel().getValueAt(i, 1) < 0.0) {
                JOptionPane.showMessageDialog(null, "Negative inputs are not allowed.");
            }
        }
        this.netViewer.hideMenu();
    }//GEN-LAST:event_saveButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable firingRatesTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
