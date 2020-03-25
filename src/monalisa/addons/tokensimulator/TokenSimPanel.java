/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Molekulare Bioinformatik, Goethe University Frankfurt, Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import monalisa.addons.AddonPanel;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.transformer.VertexShapeTransformer;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author Pavel Balazki.
 */
public class TokenSimPanel extends AddonPanel {

    private final TokenSimulator ts;
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(TokenSimPanel.class);

    // START INNER CLASSES
/**
     * Handles navigation through the history. When a history entry in historyList is picked, all steps between the current state and picked state
     * will be performed (if picked state is later than current state) or reverse-fired (if picked state is earlier than current state); i.e.
     * the picked state will be the last performed state.
     */
    public class HistorySelectionListener implements ListSelectionListener{
        @Override
        public void valueChanged(ListSelectionEvent e) {
            LOGGER.info("New Entry chosen from history list");
            if(e.getValueIsAdjusting() == false){
                int selectedVar = historyJList.getSelectedIndex();
                if(selectedVar > -1){
                    historyJList.clearSelection();
                    //if the last step was performed later than the picked state, reverse-fire all steps between the last performer step and the picked
                    historyJList.setEnabled(false);
                    while(selectedVar < ts.lastHistoryStep){
                        ts.reverseFireTransitions(ts.historyArrayList.get(ts.lastHistoryStep--));
                    }
                    //if the selected step was performed after the curren step, perform steps between selected and lastHistoryStep and update the visual output after the last firing
                    while(selectedVar > ts.lastHistoryStep){
                        ts.fireTransitions(false, ts.historyArrayList.get(++ts.lastHistoryStep));
                    }
                    historyBackJButton.setEnabled(ts.lastHistoryStep > -1);
                    historyForwardJButton.setEnabled(ts.lastHistoryStep < ts.historyArrayList.size()-1);
                    ts.updateVisualOutput();
                    
                    historyJList.repaint();
                    historyJList.ensureIndexIsVisible(ts.lastHistoryStep);
                    historyJList.setEnabled(true);
                }
            }
        }
    }

     /**
     * Handles the coloring of entries in historyList. The last performed step has red background.
     */
    public class HistoryCellRenderer implements ListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
            JLabel renderer = (JLabel)defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if(index == ts.lastHistoryStep){
                renderer.setBackground(Color.red);
            }
            else
                renderer.setBackground(Color.white);
            return renderer;
        }
    }
    
    /**
     * Handles the navigation through snapshots. When a snapshot is picked, the snapshots marking and history are assigned to current state
     */
    public class SnapshotsListSelectionListener implements ListSelectionListener{
        @Override
        public void valueChanged(ListSelectionEvent lse) {
            if(lse.getValueIsAdjusting() == false){
                int selectedVar = snapshotsJList.getSelectedIndex();
                if (selectedVar > -1){
                    snapshotsJList.clearSelection();
                    ts.loadSnapshot(ts.snapshots.get(selectedVar));
                }
            }
        }
    }    
    
    /**
     * Handles selection of a marking from the customMarkingsComboBox.
     */
    public class CustomMarkingsComboBoxPopupListener implements PopupMenuListener{
        private boolean canceled = false;
        
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
            canceled = false;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
            //only if the popup becomes invisible in the cause of selecting an item by user.
            if(!canceled){
                //get the name of selected marking
                String markingName = (String)customMarkingsJComboBox.getSelectedItem();
                /*
                Put the values from the selected marking into current marking.
                */
                ts.marking.putAll(ts.customMarkingsMap.get(markingName));
                //re-compute active transitions
                ts.tokenSim.addTransitionsToCheck(pnf.transitions().toArray(new Transition[0]));
                ts.tokenSim.computeActiveTransitions();
                //clear history
                ts.historyListModel.clear();
                ts.historyArrayList.clear();
                ts.lastHistoryStep = -1;
                //clear snapshots
                ts.snapshots.clear();
                ts.snapshotsListModel.clear();
                //clear statistic
                ts.totalStepNr = 0;
                ts.currStatistic = new Statistic(ts);
                for (Map.Entry<Place, XYSeries> entr : ts.seriesMap.entrySet()){
                    entr.getValue().clear();
                    entr.getValue().add(ts.tokenSim.getSimulatedTime(), ts.tokenSim.getTokens(entr.getKey().id()), false);
                }

                ts.updateVisualOutput();
            }
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent pme) {
            canceled = true;
        }
    }    
    
    // END INNER CLASSES
    
    /**
     * Creates new form TopologcialPanel
     */
    public TokenSimPanel(final NetViewer netViewer, PetriNetFacade petriNet) {      
        super(netViewer, petriNet, "Simulator"); 
        LOGGER.info("Initiating TokenSimPanel");
        initComponents();
        
        this.ts = new TokenSimulator();
        modifyComponents();                
        this.ts.initTokenSimulator(netViewer, petriNet, this);       
        
        this.repaint();
    }  

    private void modifyComponents() {
        simModeJComboBox.addItem(strings.get("ATSName"));
        simModeJComboBox.addItem(strings.get("STSName"));
        simModeJComboBox.addItem(strings.get("StochTSName"));
        simModeJComboBox.addItem(strings.get("GilTSName"));
        simModeJComboBox.setSelectedIndex(3);
        
        //history
        this.ts.historyListModel = new DefaultListModel();
        historyJList.setModel(this.ts.historyListModel);
        historyJList.setCellRenderer(new HistoryCellRenderer());
        historyJList.addListSelectionListener(new HistorySelectionListener());
        
        //snapshots
        this.ts.snapshotsListModel = new DefaultListModel();
        snapshotsJList.setModel(this.ts.snapshotsListModel);
        snapshotsJList.addListSelectionListener(new SnapshotsListSelectionListener());

        //create new selection listener selecting custom markings. If an entry is selected in customMarkingsComboBox, load the marking
        customMarkingsJComboBox.addPopupMenuListener(new CustomMarkingsComboBoxPopupListener());
        
        //Increase the size of vertices (places and transitions)
        iconSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newIconSize = ((Integer)ts.tokenSimPanel.iconSizeSpinner.getValue());
                netViewer.getVisualizationViewer().getRenderContext().setVertexShapeTransformer(new VertexShapeTransformer(newIconSize));
                ts.vertexIconTransformer.setVertexSize(newIconSize);
                netViewer.getVisualizationViewer().repaint();
            }
        }); 
                
        //Go one step back in the history
        historyBackJButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Backwards button in the history has been pressed, going one step back");
                historyForwardJButton.setEnabled(true);
                //cancel the last performed step
                ts.reverseFireTransitions(ts.historyArrayList.get(ts.lastHistoryStep--));
                historyJList.repaint();
                historyJList.ensureIndexIsVisible(ts.lastHistoryStep);
                //if no steps were performed before, disable stepBackButton
                if(ts.lastHistoryStep < 0){
                    historyBackJButton.setEnabled(false);
                }
                ts.updateVisualOutput();
            }
        });
        
        //Perform next step in the history
        historyForwardJButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Forwards button in the history has been pressed, going one step forward");
                historyBackJButton.setEnabled(true);
                //perform the next step after last performed
                ts.fireTransitions(false, ts.historyArrayList.get(++ts.lastHistoryStep));
                historyJList.repaint();
                historyJList.ensureIndexIsVisible(ts.lastHistoryStep);
                //if no steps in history left, disable stepForwardButton
                if(ts.lastHistoryStep == ts.historyArrayList.size()-1){
                    historyForwardJButton.setEnabled(false);
                }
                ts.updateVisualOutput();
            }
        });
        
        //Shows a frame with statistics for current state
        showStatisticsJButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                //Create a new snapshot of current state
                ts.saveSnapshot();
                //show a windows with statistics
                new StatisticFrame(ts);
            }
        });
        
        //Button for saving current marking
        saveMarkingJButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                //save current marking to a new entry in customMarkingsMap
                //name of current marking
                LOGGER.info("SaveMarkingButton has been pressed, saving the current Marking");
                String markingName = JOptionPane.showInputDialog(TokenSimPanel.this, strings.get("TSNameOfMarkingOptionPane"), ("Marking " + (ts.customMarkingsMap.size() + 1)));
                if(markingName != null){
                    //if a marking with given name already exists, promt to give a new name
                    if(ts.customMarkingsMap.containsKey(markingName)){
                        JOptionPane.showMessageDialog(null, strings.get("TSMarkingNameAlreadyExists"));
                    }
                    else{
                        Map<Integer, Long> tmpMarking = new HashMap<>(pnf.places().size());
                        for (Place place : pnf.places()){
                            tmpMarking.put(place.id(), ts.tokenSim.getTokens(place.id()));
                        }
                        ts.customMarkingsMap.put(markingName, tmpMarking);
                        //insert new marking entry to the top of the ComboBox
                        customMarkingsJComboBox.addItem(markingName);
                        customMarkingsJComboBox.setSelectedIndex(customMarkingsJComboBox.getItemCount()-1);
                        customMarkingsJComboBox.setEnabled(true);
                        deleteMarkingJButton.setEnabled(true);
                    }
                }
            }
        });
        
        //Button to delete selected marking
        deleteMarkingJButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Button to delete the currently selected marking has been pressed - deleting current marking");
                //delete the marking that is currently selected in customMarkingsComboBox
                String markingName = customMarkingsJComboBox.getSelectedItem().toString();
                ts.customMarkingsMap.remove(markingName);
                customMarkingsJComboBox.removeItemAt(customMarkingsJComboBox.getSelectedIndex());
                //disable custmMarkingsComboBox and the delete-button if no markings are saved
                if(ts.customMarkingsMap.isEmpty()){
                    customMarkingsJComboBox.setEnabled(false);
                    deleteMarkingJButton.setEnabled(false);
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
        startSimulationJButton = new javax.swing.JButton();
        simModeJComboBox = new javax.swing.JComboBox();
        simModeJLabel = new javax.swing.JLabel();
        endSimulationJButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        edgeSizeLabel = new javax.swing.JLabel();
        fontSizeLabel = new javax.swing.JLabel();
        arrowSizeLabel = new javax.swing.JLabel();
        iconSizeLabel = new javax.swing.JLabel();
        edgeSizeSpinner = new javax.swing.JSpinner();
        fontSizeSpinner = new javax.swing.JSpinner();
        iconSizeSpinner = new javax.swing.JSpinner();
        arrowSizeSpinner = new javax.swing.JSpinner();
        makePic = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        snapshotsJLabel = new javax.swing.JLabel();
        loadSetupButton = new javax.swing.JButton();
        snapshotsScrollPane = new javax.swing.JScrollPane();
        this.snapshotsScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        snapshotsJList = new javax.swing.JList();
        saveSetupButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        historyBackJButton = new javax.swing.JButton();
        historyScrollPane = new javax.swing.JScrollPane();
        historyJList = new javax.swing.JList();
        historyForwardJButton = new javax.swing.JButton();
        historyJLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        deleteMarkingJButton = new javax.swing.JButton();
        saveMarkingJButton = new javax.swing.JButton();
        customMarkingsJComboBox = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        showPlotButton = new javax.swing.JButton();
        showStatisticsJButton = new javax.swing.JButton();
        preferencesJButton = new javax.swing.JButton();
        customControlScrollPane = new javax.swing.JScrollPane();
        this.customControlScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 5));
        setFocusCycleRoot(true);
        setPreferredSize(new java.awt.Dimension(250, 1089));
        setRequestFocusEnabled(false);
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        startSimulationJButton.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        startSimulationJButton.setText(TokenSimulator.strings.get("TSstartSimulationJButton"));
        startSimulationJButton.setToolTipText(TokenSimulator.strings.get("TSstartSimulationJButtonT"));
        startSimulationJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startSimulationJButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 3);
        jPanel1.add(startSimulationJButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel1.add(simModeJComboBox, gridBagConstraints);

        simModeJLabel.setText(TokenSimulator.strings.get("TSsimModeJLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        jPanel1.add(simModeJLabel, gridBagConstraints);

        endSimulationJButton.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        endSimulationJButton.setText(TokenSimulator.strings.get("TSendSimulationJButton"));
        endSimulationJButton.setToolTipText(TokenSimulator.strings.get("TSendSimulationJButtonT"));
        endSimulationJButton.setEnabled(false);
        endSimulationJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endSimulationJButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel1.add(endSimulationJButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        edgeSizeLabel.setText("Edge size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel2.add(edgeSizeLabel, gridBagConstraints);

        fontSizeLabel.setText("Font size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        jPanel2.add(fontSizeLabel, gridBagConstraints);

        arrowSizeLabel.setText("Arrow size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        jPanel2.add(arrowSizeLabel, gridBagConstraints);

        iconSizeLabel.setText("Icon size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        jPanel2.add(iconSizeLabel, gridBagConstraints);

        edgeSizeSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        edgeSizeSpinner.setEnabled(false);
        edgeSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                netViewer.setEdgeSize(((Integer)edgeSizeSpinner.getValue()).intValue());
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel2.add(edgeSizeSpinner, gridBagConstraints);

        fontSizeSpinner.setModel(new SpinnerNumberModel(12, 5, 100, 1));
        fontSizeSpinner.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        jPanel2.add(fontSizeSpinner, gridBagConstraints);
        fontSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                netViewer.setFontSize(((Integer)fontSizeSpinner.getValue()).intValue());
            }
        });

        iconSizeSpinner.setModel(new SpinnerNumberModel(25, 5, 150, 1));
        iconSizeSpinner.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        jPanel2.add(iconSizeSpinner, gridBagConstraints);

        arrowSizeSpinner.setModel(new SpinnerNumberModel(1.0, 1.0, 100.0, 0.5));
        arrowSizeSpinner.setEnabled(false);
        arrowSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                netViewer.setArrowSize(((Double)arrowSizeSpinner.getValue()));
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        jPanel2.add(arrowSizeSpinner, gridBagConstraints);

        makePic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/save_picture.png"))); // NOI18N
        makePic.setText("Save as image");
        makePic.setToolTipText("Saves the Petri net as an image");
        makePic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makePicActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        jPanel2.add(makePic, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        snapshotsJLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        snapshotsJLabel.setText(TokenSimulator.strings.get("TSsnapshotsJLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        jPanel3.add(snapshotsJLabel, gridBagConstraints);

        loadSetupButton.setText(TokenSimulator.strings.get("TSLoadSetup"));
        loadSetupButton.setToolTipText(TokenSimulator.strings.get("TSLoadSetupTT"));
        loadSetupButton.setEnabled(false);
        loadSetupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSetupButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(20, 3, 0, 0);
        jPanel3.add(loadSetupButton, gridBagConstraints);

        snapshotsJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        snapshotsJList.setEnabled(false);
        snapshotsScrollPane.setViewportView(snapshotsJList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        jPanel3.add(snapshotsScrollPane, gridBagConstraints);

        saveSetupButton.setText(TokenSimulator.strings.get("TSSaveSetup"));
        saveSetupButton.setToolTipText(TokenSimulator.strings.get("TSSaveSetupTT"));
        saveSetupButton.setEnabled(false);
        saveSetupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSetupButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 3);
        jPanel3.add(saveSetupButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel3, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        historyBackJButton.setText(TokenSimulator.strings.get("TShistoryBackJButton"));
        historyBackJButton.setToolTipText(TokenSimulator.strings.get("TShistoryBackJButtonT"));
        historyBackJButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 3);
        jPanel4.add(historyBackJButton, gridBagConstraints);

        historyJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        historyJList.setEnabled(false);
        historyScrollPane.setViewportView(historyJList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        jPanel4.add(historyScrollPane, gridBagConstraints);

        historyForwardJButton.setText(TokenSimulator.strings.get("TShistoryForwardJButton"));
        historyForwardJButton.setToolTipText(TokenSimulator.strings.get("TShistoryForwardJButtonT"));
        historyForwardJButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 3, 0, 0);
        jPanel4.add(historyForwardJButton, gridBagConstraints);

        historyJLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        historyJLabel.setText(TokenSimulator.strings.get("TShistoryJLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        jPanel4.add(historyJLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel4, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        deleteMarkingJButton.setText(TokenSimulator.strings.get("TSdeleteMarkingJButton"));
        deleteMarkingJButton.setToolTipText(TokenSimulator.strings.get("TSdeleteMarkingJButtonT"));
        deleteMarkingJButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        jPanel5.add(deleteMarkingJButton, gridBagConstraints);

        saveMarkingJButton.setText(TokenSimulator.strings.get("TSsaveMarkingJButton"));
        saveMarkingJButton.setToolTipText(TokenSimulator.strings.get("TSsaveMarkingJButtonT"));
        saveMarkingJButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        jPanel5.add(saveMarkingJButton, gridBagConstraints);

        customMarkingsJComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel5.add(customMarkingsJComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel5, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        showPlotButton.setText(TokenSimulator.strings.get("TSShowPlotB"));
        showPlotButton.setToolTipText(TokenSimulator.strings.get("TSShowPlotTT"));
        showPlotButton.setEnabled(false);
        showPlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPlotButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 3, 0, 0);
        jPanel6.add(showPlotButton, gridBagConstraints);

        showStatisticsJButton.setText(TokenSimulator.strings.get("TSshowStatisticsJButton"));
        showStatisticsJButton.setToolTipText(TokenSimulator.strings.get("TSshowStatisticsJButtonT"));
        showStatisticsJButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 3);
        jPanel6.add(showStatisticsJButton, gridBagConstraints);

        preferencesJButton.setText(TokenSimulator.strings.get("TSpreferencesJButton"));
        preferencesJButton.setEnabled(false);
        preferencesJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferencesJButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        jPanel6.add(preferencesJButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel6, gridBagConstraints);

        customControlScrollPane.setBorder(null);
        customControlScrollPane.setPreferredSize(new java.awt.Dimension(250, 250));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        add(customControlScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void startSimulationJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startSimulationJButtonActionPerformed
        this.ts.startSimulator();
    }//GEN-LAST:event_startSimulationJButtonActionPerformed

    private void endSimulationJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endSimulationJButtonActionPerformed
        this.ts.endSimulator();
    }//GEN-LAST:event_endSimulationJButtonActionPerformed
    
    /**
     * Is called from the Project class to get all data, which should be saved for the AddOn.
     * All kind of objects can be stored here, if the class implements the "Serializable" interface. 
     * The string is an identifier if more than one object should be saved.
     * @return A map with the data to store.
     */
    @Override
    public Map<String, Object> getObjectsForStorage() {
        Map<String, Object> storage = new HashMap<>();
        
        storage.put("customMarking", ts.customMarkingsMap);
        
        return storage;
    }
    
    /**
     * Is called from the Project class to send the stored data to the AddOn.
     * It will get the map which is saved with getObjectsForStorage() method.
     * @param storage 
     */
    @Override
    public void reciveStoredObjects(Map<String, Object> storage) {
        ts.customMarkingsMap = (Map<String, Map<Integer, Long>>) storage.get("customMarking");
    }    
    
    /**
     * Show the preferences-frame
     * @param evt 
     */
    private void preferencesJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesJButtonActionPerformed
        this.ts.preferencesJFrame.logPathJTextField.setText((String) this.ts.preferences.get("LogPath"));
        this.ts.preferencesJFrame.createLogJCheckBox.setSelected((Boolean) this.ts.preferences.get("LogEnabled"));
        this.ts.preferencesJFrame.logPathJTextField.setEnabled((Boolean) this.ts.preferences.get("LogEnabled"));
        this.ts.preferencesJFrame.logPathBrowseJButton.setEnabled((Boolean) this.ts.preferences.get("LogEnabled"));
        this.ts.tokenSim.loadPreferences();
        this.ts.netViewer.displayMenu(this.ts.preferencesJFrame.getContentPane(), "TokenSimPreferences");
    }//GEN-LAST:event_preferencesJButtonActionPerformed

    private void saveSetupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSetupButtonActionPerformed
        this.ts.tokenSim.exportSetup();
    }//GEN-LAST:event_saveSetupButtonActionPerformed

    private void loadSetupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSetupButtonActionPerformed
        this.ts.tokenSim.importSetup();
    }//GEN-LAST:event_loadSetupButtonActionPerformed

    private void showPlotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPlotButtonActionPerformed
        this.ts.showPlot();
    }//GEN-LAST:event_showPlotButtonActionPerformed

    private void makePicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makePicActionPerformed
        try {
            this.netViewer.makePic();
        } catch (IOException ex) {
            Logger.getLogger(TokenSimPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_makePicActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel arrowSizeLabel;
    protected javax.swing.JSpinner arrowSizeSpinner;
    protected javax.swing.JScrollPane customControlScrollPane;
    protected javax.swing.JComboBox customMarkingsJComboBox;
    protected javax.swing.JButton deleteMarkingJButton;
    private javax.swing.JLabel edgeSizeLabel;
    protected javax.swing.JSpinner edgeSizeSpinner;
    protected javax.swing.JButton endSimulationJButton;
    private javax.swing.JLabel fontSizeLabel;
    protected javax.swing.JSpinner fontSizeSpinner;
    protected javax.swing.JButton historyBackJButton;
    protected javax.swing.JButton historyForwardJButton;
    private javax.swing.JLabel historyJLabel;
    protected javax.swing.JList historyJList;
    protected javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JLabel iconSizeLabel;
    protected javax.swing.JSpinner iconSizeSpinner;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    protected javax.swing.JButton loadSetupButton;
    private javax.swing.JButton makePic;
    protected javax.swing.JButton preferencesJButton;
    protected javax.swing.JButton saveMarkingJButton;
    protected javax.swing.JButton saveSetupButton;
    protected javax.swing.JButton showPlotButton;
    protected javax.swing.JButton showStatisticsJButton;
    protected javax.swing.JComboBox simModeJComboBox;
    private javax.swing.JLabel simModeJLabel;
    private javax.swing.JLabel snapshotsJLabel;
    protected javax.swing.JList snapshotsJList;
    private javax.swing.JScrollPane snapshotsScrollPane;
    protected javax.swing.JButton startSimulationJButton;
    // End of variables declaration//GEN-END:variables

}