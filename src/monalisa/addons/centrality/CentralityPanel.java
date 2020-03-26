/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.centrality;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import monalisa.Settings;
import monalisa.addons.AddonPanel;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.data.pn.PetriNetFacade;
import monalisa.util.FileUtils;
import monalisa.util.MonaLisaFileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A panel for the calculation and visualization of centrality measures
 *
 * @author Jens Einloft & Lilya Mirzoyan
 */
public class CentralityPanel extends AddonPanel {

    private static final Logger LOGGER = LogManager.getLogger(CentralityPanel.class);
    private Color heatMapColor;

    private DefaultTableModel modelPlaces;
    private DefaultTableModel modelTransitions;

    private AdjacencyMatrix adjMatrixPlaces;
    private AdjacencyMatrix adjMatrixTransitions;

    private ClosenessCentrality cc;
    private EccentricityCentrality ecc;
    private BetweennessCentrality bc;
    private EigenvectorCentrality ec;

    /**
     * Creates a new form CentralityPanel
     */
    public CentralityPanel(final NetViewer netViewer, final PetriNetFacade petriNet) {
        super(netViewer, petriNet, "Centrality");
        LOGGER.info("Initializing CentralityPanel");
        initComponents();

        placesTable.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                LOGGER.debug("Place selected in CentralityPanel");
                //get the selected column
                int col = placesTable.columnAtPoint(e.getPoint());
                int row = placesTable.rowAtPoint(e.getPoint());
                //respond only if fist column is selected + double click
                if (col == 0){
                    if(e.getClickCount() == 1) {
                        LOGGER.debug("Reflecting place selection in NetViewer");
                        netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().clear();
                        netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().pick(((NetViewerNode) placesTable.getValueAt(row, col)).getMasterNode() , true);
                    }
                }
                LOGGER.debug("Handled place selection in CentralityPanel");
            }
        });

        transitionsTable.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                LOGGER.debug("Transition selected in CentralityPanel");
                //get the selected column
                int col = transitionsTable.columnAtPoint(e.getPoint());
                int row = transitionsTable.rowAtPoint(e.getPoint());
                //respond only if fist column is selected + double click
                if (col == 0){
                    if(e.getClickCount() == 1) {
                        LOGGER.debug("Reflecting transition selection in NetViewer");
                        netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().clear();
                        netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().pick(((NetViewerNode) transitionsTable.getValueAt(row, col)).getMasterNode() , true);
                    }
                }
                LOGGER.debug("Handled transition selection in CentralityPanel");
            }
        });
        LOGGER.info("Successfully initialized CentralityPanel");
    }

    /**
     * A function for labeling nodes due to their centrality ranking
     *
     * @param rankingTransitions
     * @param rankingPlaces
     */
    public void labelNodes(Map<Integer, Double> rankingTransitions, Map<Integer, Double> rankingPlaces) {
        LOGGER.info("Labelling nodes based on centrality ranking");
        heatMapColor = Settings.getAsColor("heatMapColor");
        if (rankingTransitions != null) {
            LOGGER.debug("Labelling transitions based on centrality ranking");
            double min = 0.0, max = 0.0, maxmin;
            float[] hsbvals = Color.RGBtoHSB(heatMapColor.getRed(), heatMapColor.getGreen(), heatMapColor.getBlue(), null);

            double norm;

            this.netViewer.resetColor();

            LOGGER.debug("Finding minimum and maximum values for transitions");
            Iterator<Double> itInt = rankingTransitions.values().iterator();
            double value;
            while (itInt.hasNext()) {
                value = itInt.next();
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
            maxmin = max - min;
            LOGGER.debug("Found minimum and maximum values for transitions");

            Iterator<Map.Entry<Integer, Double>> itEntry = rankingTransitions.entrySet().iterator();
            Map.Entry<Integer, Double> entry;
            LOGGER.debug("Coloring transitions based on centrality ranking");
            while (itEntry.hasNext()) {
                entry = itEntry.next();
                if (maxmin == 0) {
                    norm = (float) 0.05;

                } else {
                    norm = (float) ((entry.getValue() - min) / maxmin);
                }
                if (norm < 0.05) {
                    norm = (float) 0.05;
                }

                this.netViewer.getNodeFromTransitionId(entry.getKey()).setColorForAllNodes(new Color(Color.HSBtoRGB(hsbvals[0], (float) norm, hsbvals[2])));
            }
            this.netViewer.getVisualizationViewer().repaint();
            LOGGER.debug("Finished labelling transitions based on centrality ranking");
        }
        if (rankingPlaces != null) {
            LOGGER.debug("Labelling places based on centrality ranking");
            double min = 0.0, max = 0.0, maxmin;
            float[] hsbvals = Color.RGBtoHSB(heatMapColor.getRed(), heatMapColor.getGreen(), heatMapColor.getBlue(), null);

            double norm;

            LOGGER.debug("Finding minimum and maximum values for places");
            Iterator<Double> itInt = rankingPlaces.values().iterator();
            double value;
            while (itInt.hasNext()) {
                value = itInt.next();
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
            maxmin = max - min;
            LOGGER.debug("Found minimum and maximum value for places");

            Iterator<Map.Entry<Integer, Double>> itEntry = rankingPlaces.entrySet().iterator();
            Map.Entry<Integer, Double> entry;
            LOGGER.debug("Coloring places based on centrality ranking");
            while (itEntry.hasNext()) {
                entry = itEntry.next();
                if (maxmin == 0) {
                    norm = (float) 0.05;
                } else {
                    norm = (float) ((entry.getValue() - min) / maxmin);
                }
                if (norm < 0.05) {
                    norm = (float) 0.05;
                }

                this.netViewer.getNodeFromPlaceId(entry.getKey()).setColorForAllNodes(new Color(Color.HSBtoRGB(hsbvals[0], (float) norm, hsbvals[2])));
            }
            this.netViewer.getVisualizationViewer().repaint();
            LOGGER.debug("Finished labelling places based on centrality value");
        }
        LOGGER.info("Successfully labeled nodes based on centrality ranking");
    }

    /**
     * Returns the selected item of the ComboBox
     *
     * @return selected item
     */
    public Object getComboElement() {
        return centralityList.getSelectedItem();
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

        content = new javax.swing.JPanel();
        computeRankingButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        placesTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        transitionsTable = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        heatMap = new javax.swing.JButton();
        centralityList = new javax.swing.JComboBox();
        spacerLeft = new javax.swing.JPanel();
        spacerRight = new javax.swing.JPanel();
        spacerTop = new javax.swing.JPanel();
        spacerBottom = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(350, 550));
        setLayout(new java.awt.GridBagLayout());

        content.setLayout(new java.awt.GridBagLayout());

        computeRankingButton.setText("Compute Ranking");
        computeRankingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                computeRankingButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        content.add(computeRankingButton, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel1.setText("Species");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        content.add(jLabel1, gridBagConstraints);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(350, 300));

        placesTable.setAutoCreateRowSorter(true);
        placesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Closeness", "Eccentricity", "Betweenness", "Eigenvector"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
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
        placesTable.setEnabled(false);
        placesTable.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(placesTable);
        placesTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (placesTable.getColumnModel().getColumnCount() > 0) {
            placesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            placesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            placesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            placesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
            placesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        }

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        content.add(jScrollPane1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel2.setText("Reactions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        content.add(jLabel2, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(350, 300));

        transitionsTable.setAutoCreateRowSorter(true);
        transitionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Closeness", "Eccentricity", "Betweenness", "Eigenvector"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        transitionsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        transitionsTable.setColumnSelectionAllowed(true);
        transitionsTable.setEnabled(false);
        transitionsTable.setFillsViewportHeight(true);
        jScrollPane2.setViewportView(transitionsTable);
        transitionsTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (transitionsTable.getColumnModel().getColumnCount() > 0) {
            transitionsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            transitionsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            transitionsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            transitionsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
            transitionsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        }

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        content.add(jScrollPane2, gridBagConstraints);

        exportButton.setText("Export Results");
        exportButton.setEnabled(false);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        content.add(exportButton, gridBagConstraints);

        heatMap.setText("Heatmap for:");
        heatMap.setEnabled(false);
        heatMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heatMapActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 5);
        content.add(heatMap, gridBagConstraints);

        centralityList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Closeness", "Eccentricity", "Betweenness", "Eigenvector" }));
        centralityList.setEnabled(false);
        centralityList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                centralityListActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        content.add(centralityList, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 0.8;
        add(content, gridBagConstraints);
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

    /**
     * Calculates centralities for a given graph by pressing the button "Compute
     * Ranking"
     *
     * @param evt
     */
    private void computeRankingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_computeRankingButtonActionPerformed
        LOGGER.info("Computing rankings of nodes");
        adjMatrixPlaces = new AdjacencyMatrix(this.pnf.places(), this.pnf.transitions(), AdjacencyMatrix.PLACES);
        adjMatrixTransitions = new AdjacencyMatrix(this.pnf.places(), this.pnf.transitions(), AdjacencyMatrix.TRANSITIONS);

        cc = new ClosenessCentrality(this.pnf);
        cc.calculate();
        ecc = new EccentricityCentrality(this.pnf);
        ecc.calculate();
        bc = new BetweennessCentrality(this.pnf);
        bc.calculate();
        ec = new EigenvectorCentrality(this.pnf);
        ec.calculate();

        int pL = adjMatrixPlaces.getLength();
        int tL = adjMatrixTransitions.getLength();
        LOGGER.debug("Resetting old rankings");
        modelPlaces = (DefaultTableModel) placesTable.getModel();
        modelTransitions = (DefaultTableModel) transitionsTable.getModel();

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
        LOGGER.debug("Finished resetting old rankings");
        LOGGER.debug("Filling model with new rankings for places");
        for (int i = 0; i < pL; i++) {
            modelPlaces.addRow(new Object[]{this.netViewer.getNodeFromPlaceId(adjMatrixPlaces.getIdForIndex(i)),
                cc.rankingPlaces.get(adjMatrixPlaces.getIdForIndex(i)),
                ecc.rankingPlaces.get(adjMatrixPlaces.getIdForIndex(i)),
                bc.rankingPlaces.get(adjMatrixPlaces.getIdForIndex(i)),
                ec.rankingPlaces.get(adjMatrixPlaces.getIdForIndex(i))});
        }
        LOGGER.debug("Successfully filled model with new rankings for places");
        LOGGER.debug("Filling model with new rankings for transitions");
        for (int i = 0; i < tL; i++) {
            modelTransitions.addRow(new Object[]{this.netViewer.getNodeFromTransitionId(adjMatrixTransitions.getIdForIndex(i)),
                cc.rankingTransitions.get(adjMatrixTransitions.getIdForIndex(i)),
                ecc.rankingTransitions.get(adjMatrixTransitions.getIdForIndex(i)),
                bc.rankingTransitions.get(adjMatrixTransitions.getIdForIndex(i)),
                ec.rankingTransitions.get(adjMatrixTransitions.getIdForIndex(i))});
        }
        LOGGER.debug("Successfully filled model with new rankings for transitions");
        this.exportButton.setEnabled(true);
        this.centralityList.setEnabled(true);
        this.heatMap.setEnabled(true);
        LOGGER.info("Successfully computed rankings of nodes");
    }//GEN-LAST:event_computeRankingButtonActionPerformed

    /**
     * Saves all calculated values by pressing the button "Export Results"
     *
     * @param evt
     */
    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

        LOGGER.info("Exporting centralities");
        MonaLisaFileChooser fileCh = new MonaLisaFileChooser();
        int returnValue = fileCh.showSaveDialog(null);
        File file = fileCh.getSelectedFile();
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {

                if (!"csv".equalsIgnoreCase(FileUtils.getExtension(file))) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }

                if (!file.exists()) {
                    SaveResults.saveResults(file, cc, ecc, bc, ec, pnf);
                } else {
                    int option = JOptionPane.showConfirmDialog(this.netViewer, "The file already exists. Do you want to overwrite this file?", "Save", JOptionPane.OK_CANCEL_OPTION, 2);
                    if (option == JOptionPane.OK_OPTION) {
                        SaveResults.saveResults(file, cc, ecc, bc, ec, pnf);
                    }
                }
                LOGGER.info("Successfully exported centralities");
            } catch (IOException e) {
                LOGGER.error("Issue while exporting centralities: ", e);
            }
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void centralityListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_centralityListActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_centralityListActionPerformed

    /**
     * Labels all nodes due to their ranking by selecting a centrality and
     * pressing the button "Label Nodes"
     *
     * @param evt
     */
    private void heatMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heatMapActionPerformed
        LOGGER.info("New centrality selected for heatmap");
        //Why do we calculate these again on every seletion?
        if (getComboElement().equals("Closeness")) {
            LOGGER.info("New centrality for heatmap will be closeness");
            cc = new ClosenessCentrality(this.pnf);
            cc.calculate();
            Map<Integer, Double> rankingTransitions = cc.getRankingForTransitions();
            Map<Integer, Double> rankingPlaces = cc.getRankingForPlaces();
            labelNodes(rankingTransitions, rankingPlaces);
            LOGGER.info("Successfully changed heatmap centrality to closeness");
        } else if (getComboElement().equals("Eccentricity")) {
            LOGGER.info("New centrality for heatmap will be eccentricity");
            ecc = new EccentricityCentrality(this.pnf);
            ecc.calculate();
            Map<Integer, Double> rankingTransitions = ecc.getRankingForTransitions();
            Map<Integer, Double> rankingPlaces = ecc.getRankingForPlaces();
            labelNodes(rankingTransitions, rankingPlaces);
            LOGGER.info("Successfully changed heatmap centrality to eccentricity");
        } else if (getComboElement().equals("Betweenness")) {
            LOGGER.info("New centrality for heatmap will be betweenness");
            bc = new BetweennessCentrality(this.pnf);
            bc.calculate();
            Map<Integer, Double> rankingTransitions = bc.getRankingForTransitions();
            Map<Integer, Double> rankingPlaces = bc.getRankingForPlaces();
            labelNodes(rankingTransitions, rankingPlaces);
            LOGGER.info("Successfully changed heatmap centrality to betweenness");
        } else if (getComboElement().equals("Eigenvector")) {
            LOGGER.info("New centrality for heatmap is eigenvector");
            ec = new EigenvectorCentrality(this.pnf);
            ec.calculate();
            Map<Integer, Double> rankingTransitions = ec.getRankingForTransitions();
            Map<Integer, Double> rankingPlaces = ec.getRankingForPlaces();
            labelNodes(rankingTransitions, rankingPlaces);
            LOGGER.info("Successfully changed heatmap centrality to eigenvector");
        }
    }//GEN-LAST:event_heatMapActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox centralityList;
    private javax.swing.JButton computeRankingButton;
    private javax.swing.JPanel content;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton heatMap;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable placesTable;
    private javax.swing.JPanel spacerBottom;
    private javax.swing.JPanel spacerLeft;
    private javax.swing.JPanel spacerRight;
    private javax.swing.JPanel spacerTop;
    private javax.swing.JTable transitionsTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void netChanged() {
        LOGGER.debug("Handling net change for CentralityPanel");
        if(cc != null) {
            LOGGER.debug("Resetting model because of net change");
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
            LOGGER.debug("Finished resetting model because of net change");
            this.exportButton.setEnabled(false);
            this.centralityList.setEnabled(false);
            this.heatMap.setEnabled(false);
        }
        LOGGER.debug("Successfully handled net change for CentralityPanel");
    }
}
