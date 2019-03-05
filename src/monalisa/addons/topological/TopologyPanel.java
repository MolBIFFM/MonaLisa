/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.topological;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;
import monalisa.addons.AddonPanel;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.MonaLisaFileChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author jens
 * Addon to display the different node degrees of the Petri net
 */
public class TopologyPanel extends AddonPanel {
    
    private Map<Integer, Integer> ndPlacesAll;
    private Map<Integer, Integer> ndPlacesIn;
    private Map<Integer, Integer> ndPlacesOut;
    private Map<Integer, Integer> ndTransitionsAll;
    private Map<Integer, Integer> ndTransitionsIn;
    private Map<Integer, Integer> ndTransitionsOut;
    
    private Map<Integer, Double> freqPlacesAll;
    private Map<Integer, Double> freqPlacesIn;
    private Map<Integer, Double> freqPlacesOut;
    private Map<Integer, Double> freqTransitionsAll;
    private Map<Integer, Double> freqTransitionsIn;
    private Map<Integer, Double> freqTransitionsOut;
    
    private final DefaultTableModel modelPlaces;
    private final DefaultTableModel modelTransitions;
    
    /**
     * Creates new form TopologcialPanel
     */
    public TopologyPanel(final NetViewer netViewer, PetriNetFacade petriNet){
        super(netViewer, petriNet, "Topology");        
        initComponents();
        
        modelPlaces = (DefaultTableModel) placesTable.getModel();                    
        placesTable.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(placesTable.getRowCount() > 0) {
                    //get the selected column   
                    int col = placesTable.columnAtPoint(e.getPoint());   
                    int row = placesTable.rowAtPoint(e.getPoint());   
                    //respond only if fist column is selected + double click
                    if (col == 0) {
                        if(e.getClickCount() == 1) {
                            netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().clear();
                            netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().pick(((NetViewerNode) placesTable.getValueAt(row, col)).getMasterNode() , true);                   
                        }
                    }   
                }
            }
        });   
        
        modelTransitions = (DefaultTableModel) transitionsTabel.getModel();
        transitionsTabel.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(transitionsTabel.getRowCount() > 0) {
                    //get the selected column   
                    int col = transitionsTabel.columnAtPoint(e.getPoint());
                    int row = transitionsTabel.rowAtPoint(e.getPoint());                               
                    //respond only if fist column is selected + double click
                    if (col == 0){
                        if(e.getClickCount() == 1) {
                            netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().clear();
                            netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().pick(((NetViewerNode) transitionsTabel.getValueAt(row, col)).getMasterNode() , true);
                        }
                    }   
                }
            }
        }); 
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
        showPlotsButton = new javax.swing.JButton();
        calcDegrees = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        placesTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        transitionsTabel = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        spacerLeft = new javax.swing.JPanel();
        spacerRight = new javax.swing.JPanel();
        spacerTop = new javax.swing.JPanel();
        spacerBottom = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(397, 662));
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        showPlotsButton.setText("Show Plots");
        showPlotsButton.setEnabled(false);
        showPlotsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPlotsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(showPlotsButton, gridBagConstraints);

        calcDegrees.setText("Compute Degrees");
        calcDegrees.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calcDegreesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(calcDegrees, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(350, 300));

        placesTable.setAutoCreateRowSorter(true);
        placesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "in+out", "in", "out"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        placesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        placesTable.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(placesTable);
        placesTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (placesTable.getColumnModel().getColumnCount() > 0) {
            placesTable.getColumnModel().getColumn(0).setPreferredWidth(250);
            placesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
            placesTable.getColumnModel().getColumn(2).setPreferredWidth(30);
            placesTable.getColumnModel().getColumn(3).setPreferredWidth(30);
        }

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel1.setText("Species");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel3.setText("Transitions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(350, 300));

        transitionsTabel.setAutoCreateRowSorter(true);
        transitionsTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "in+out", "in", "out"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        transitionsTabel.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        transitionsTabel.setColumnSelectionAllowed(true);
        jScrollPane2.setViewportView(transitionsTabel);
        transitionsTabel.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (transitionsTabel.getColumnModel().getColumnCount() > 0) {
            transitionsTabel.getColumnModel().getColumn(0).setPreferredWidth(250);
            transitionsTabel.getColumnModel().getColumn(1).setPreferredWidth(50);
            transitionsTabel.getColumnModel().getColumn(2).setPreferredWidth(30);
            transitionsTabel.getColumnModel().getColumn(3).setPreferredWidth(30);
        }

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane2, gridBagConstraints);

        exportButton.setText("Export CSV");
        exportButton.setEnabled(false);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(exportButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 0.8;
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        add(spacerLeft, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        add(spacerRight, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        add(spacerTop, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        add(spacerBottom, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void showPlotsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPlotsButtonActionPerformed
        TopologicalFrame tpf = new TopologicalFrame();
          
        tpf.mainTP.addTab("Species (in+out)", new ChartFrame("dummy", createChart(freqPlacesAll)).getRootPane());          
        tpf.mainTP.addTab("Species (in)", new ChartFrame("dummy", createChart(freqPlacesIn)).getRootPane());          
        tpf.mainTP.addTab("Species (out)", new ChartFrame("dummy", createChart(freqPlacesOut)).getRootPane());           
        
        tpf.mainTP.addTab("Reactions (in+out)", new ChartFrame("dummy", createChart(freqTransitionsAll)).getRootPane());          
        tpf.mainTP.addTab("Reactions (in)", new ChartFrame("dummy", createChart(freqTransitionsIn)).getRootPane());          
        tpf.mainTP.addTab("Reactions (out)", new ChartFrame("dummy", createChart(freqTransitionsOut)).getRootPane());      
        
        tpf.setVisible(true);
    }//GEN-LAST:event_showPlotsButtonActionPerformed
    
    /**
     * Create a char for a node degree distribution
     * @param mapOfDegrees
     * @param elementCounter
     * @param fraqMap
     * @return 
     */
    private JFreeChart createChart(Map<Integer, Double> fraqMap) {
        List<Integer> listOfDegrees = new ArrayList<>(fraqMap.keySet());
        Collections.sort(listOfDegrees);
        DefaultCategoryDataset datasetPlaces = new DefaultCategoryDataset();
        Integer lastValue = listOfDegrees.get(0);
        for (Integer i : listOfDegrees) {
            // fill gaps
            if(i != lastValue+1) 
                for(Integer j = 0; j < i-lastValue; j++)
                    datasetPlaces.setValue(0 , "p(k)", Integer.toString(lastValue+j+1));
            datasetPlaces.setValue(fraqMap.get(i) , "p(k)", i.toString());
            lastValue = i;
        }            
        return ChartFactory.createBarChart("", "k", "p(k)", datasetPlaces, PlotOrientation.VERTICAL, false,true, false);        
    }
    
    /**
     * Calculates the degrees
     * @param evt 
     */
    private void calcDegreesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcDegreesActionPerformed
        if(modelPlaces.getRowCount() > 0) {
            for (int i = modelPlaces.getRowCount() - 1; i > -1; i--) {
                modelPlaces.removeRow(i);
            }
        }
        
        if(modelTransitions.getRowCount() > 0) {
            for (int i = modelTransitions.getRowCount() - 1; i > -1; i--) {
                modelTransitions.removeRow(i);
            }
        }
        
        ndPlacesAll = new HashMap<>();
        ndPlacesIn = new HashMap<>();
        ndPlacesOut = new HashMap<>();
        ndTransitionsAll = new HashMap<>();
        ndTransitionsIn = new HashMap<>();
        ndTransitionsOut = new HashMap<>();
        
        freqPlacesAll = new HashMap<>();
        freqPlacesIn = new HashMap<>();
        freqPlacesOut = new HashMap<>();
        freqTransitionsAll = new HashMap<>();
        freqTransitionsIn = new HashMap<>();
        freqTransitionsOut = new HashMap<>();        
        
        Integer all, in, out;
        for(Place p : petriNet.places()) {
            in = p.inputs().size();
            out = p.outputs().size();
            all = p.inputs().size() + p.outputs().size();
            
            if(!ndPlacesAll.containsKey(all))
                ndPlacesAll.put(all, 0);
            ndPlacesAll.put(all, ndPlacesAll.get(all)+1);
            
            if(!ndPlacesIn.containsKey(in))
                ndPlacesIn.put(in, 0);
            ndPlacesIn.put(in, ndPlacesIn.get(in)+1);
            
            if(!ndPlacesOut.containsKey(out))
                ndPlacesOut.put(out, 0);
            ndPlacesOut.put(out, ndPlacesOut.get(out)+1);     
            
            modelPlaces.addRow(new Object[]{netViewer.getNodeFromPlaceId(p.id()),all,in,out});
        }
        calcFreq(ndPlacesAll, freqPlacesAll, (double) petriNet.places().size());
        calcFreq(ndPlacesIn, freqPlacesIn, (double) petriNet.places().size());
        calcFreq(ndPlacesOut, freqPlacesOut, (double) petriNet.places().size());
        
        for(Transition t : petriNet.transitions()) {
            in = t.inputs().size();
            out = t.outputs().size();
            all = t.inputs().size() + t.outputs().size();
            
            if(!ndTransitionsAll.containsKey(all))
                ndTransitionsAll.put(all, 0);
            ndTransitionsAll.put(all, ndTransitionsAll.get(all)+1);
            
            if(!ndTransitionsIn.containsKey(in))
                ndTransitionsIn.put(in, 0);
            ndTransitionsIn.put(in, ndTransitionsIn.get(in)+1);
            
            if(!ndTransitionsOut.containsKey(out))
                ndTransitionsOut.put(out, 0);
            ndTransitionsOut.put(out, ndTransitionsOut.get(out)+1);

            modelTransitions.addRow(new Object[]{netViewer.getNodeFromTransitionId(t.id()),all,in,out});
        }        
        calcFreq(ndTransitionsAll, freqTransitionsAll, (double)petriNet.transitions().size());
        calcFreq(ndTransitionsIn, freqTransitionsIn, (double) petriNet.transitions().size());
        calcFreq(ndTransitionsOut, freqTransitionsOut, (double) petriNet.transitions().size());        
        
        if(petriNet.transitions().size() > 0 && petriNet.places().size() > 0) {
            showPlotsButton.setEnabled(true);
            exportButton.setEnabled(true);
        }
    }//GEN-LAST:event_calcDegreesActionPerformed

    /**
     * Calculates the p(k) values
     * @param ndMap
     * @param freqMap
     * @param elementCounter 
     */
    private void calcFreq(Map<Integer, Integer> ndMap, Map<Integer, Double> freqMap, double elementCounter) {
        List<Integer> listOfDegrees = new ArrayList<>(ndMap.keySet());
        Collections.sort(listOfDegrees);       
        
        for (Integer i : listOfDegrees) {
            freqMap.put(i, (double) ndMap.get(i) / elementCounter);
        }        
        
    }
    
    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        MonaLisaFileChooser fc = new MonaLisaFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setApproveButtonText("Export here");
        
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            writeCSVtoFile(freqPlacesAll, new File(fc.getSelectedFile()+"/places_in_out.csv"));
            writeCSVtoFile(freqPlacesIn, new File(fc.getSelectedFile().getAbsolutePath()+"/places_in.csv"));
            writeCSVtoFile(freqPlacesOut, new File(fc.getSelectedFile().getAbsolutePath()+"/places_out.csv"));            
            writeCSVtoFile(freqTransitionsAll, new File(fc.getSelectedFile().getAbsolutePath()+"/transitions_in_out.csv"));
            writeCSVtoFile(freqTransitionsIn, new File(fc.getSelectedFile().getAbsolutePath()+"/transitions_in.csv"));
            writeCSVtoFile(freqTransitionsOut, new File(fc.getSelectedFile().getAbsolutePath()+"/transitions_out.csv"));            
        }    
    }//GEN-LAST:event_exportButtonActionPerformed

    /**
     * Export of the node degree statistics
     */
    private void writeCSVtoFile(Map<Integer, Double> fraqMap, File file) {
        List<Integer> listOfDegrees = new ArrayList<>(fraqMap.keySet());
        Collections.sort(listOfDegrees);
        
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
            writer.write("p\tp(k)\n");            
            for(Integer i : listOfDegrees) {
                writer.write(i+"\t"+fraqMap.get(i)+"\n");
            }
        } catch (IOException ex) {
            System.out.println("IOException");
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }     
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton calcDegrees;
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable placesTable;
    private javax.swing.JButton showPlotsButton;
    private javax.swing.JPanel spacerBottom;
    private javax.swing.JPanel spacerLeft;
    private javax.swing.JPanel spacerRight;
    private javax.swing.JPanel spacerTop;
    private javax.swing.JTable transitionsTabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void netChanged() {
        if(ndPlacesAll != null) {
            ndPlacesAll = new HashMap<>();
            ndPlacesIn = new HashMap<>();
            ndPlacesOut = new HashMap<>();
            ndTransitionsAll = new HashMap<>();
            ndTransitionsIn = new HashMap<>();
            ndTransitionsOut = new HashMap<>();

            freqPlacesAll = new HashMap<>();
            freqPlacesIn = new HashMap<>();
            freqPlacesOut = new HashMap<>();
            freqTransitionsAll = new HashMap<>();
            freqTransitionsIn = new HashMap<>();
            freqTransitionsOut = new HashMap<>();       

            showPlotsButton.setEnabled(false);
            exportButton.setEnabled(false);

            if(modelPlaces.getRowCount() > 0) {
                for (int i = modelPlaces.getRowCount() - 1; i > -1; i--) {
                    modelPlaces.removeRow(i);
                }
            }
            
            if(modelTransitions.getRowCount() > 0) {
                for (int i = modelTransitions.getRowCount() - 1; i > -1; i--) {
                    modelTransitions.removeRow(i);
                }
            }
        }
    }
}
