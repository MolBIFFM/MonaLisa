/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Molekulare Bioinformatik, Goethe University Frankfurt, Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import monalisa.addons.tokensimulator.utils.StatisticFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import monalisa.addons.AddonPanel;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.addons.netviewer.transformer.VertexShapeTransformer;
import monalisa.addons.tokensimulator.asynchronous.AsynchronousTokenSim;
import monalisa.addons.tokensimulator.asynchronous.AsynchronousTokenSimPanel;
import monalisa.addons.tokensimulator.asynchronous.AsynchronousTokenSimPrefPanel;
import monalisa.addons.tokensimulator.gillespie.GillespieTokenSim;
import monalisa.addons.tokensimulator.gillespie.GillespieTokenSimPanel;
import monalisa.addons.tokensimulator.gillespie.GillespieTokenSimPrefPanel;
import monalisa.addons.tokensimulator.utils.HistoryCellRenderer;
import monalisa.addons.tokensimulator.listeners.CustomMarkingsComboBoxPopupListener;
import monalisa.addons.tokensimulator.listeners.GuiEvent;
import monalisa.addons.tokensimulator.listeners.GuiListener;
import monalisa.addons.tokensimulator.listeners.HistorySelectionListener;
import monalisa.addons.tokensimulator.listeners.SnapshotsListSelectionListener;
import monalisa.addons.tokensimulator.listeners.VertexFireListener;
import monalisa.addons.tokensimulator.stochastic.StochasticTokenSim;
import monalisa.addons.tokensimulator.stochastic.StochasticTokenSimPanel;
import monalisa.addons.tokensimulator.stochastic.StochasticTokenSimPrefPanel;
import monalisa.addons.tokensimulator.synchronous.SynchronousTokenSim;
import monalisa.addons.tokensimulator.synchronous.SynchronousTokenSimPanel;
import monalisa.addons.tokensimulator.synchronous.SynchronousTokenSimPrefPanel;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import monalisa.addons.tokensimulator.utils.Snapshot;
import monalisa.addons.tokensimulator.utils.Statistic;
import monalisa.addons.tokensimulator.utils.TokenSimPopupMousePlugin;
import monalisa.addons.tokensimulator.utils.VertexIconTransformer;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.MonaLisaFileChooser;
import org.apache.commons.collections15.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Pavel Balazki.
 */
public class SimulationPanel extends AddonPanel implements GuiListener {

    private final SimulationManager simulationMan;
    private static final Logger LOGGER = LogManager.getLogger(SimulationPanel.class);

    //Stores the controls that are provided by the selected simulator.
    private AbstractTokenSimPanel customTSControls;
    //Stores the preferences that are provided by the selected simulator.
    private AbstractTokenSimPrefPanel customTSPrefs;

    private final VisualizationViewer<NetViewerNode, NetViewerEdge> vv;
    //popup-plugin for the mouse has to be implemented in AbstractTokenSim-object; handles the popups when right mouse button is pressed.
    private AbstractPopupGraphMousePlugin tspMouse;
    //Renders transitions and places
    public VertexIconTransformer vertexIconTransformer;
    //Size of the vertices (places and transitions) on the VisualizationViewer
    protected int vertexSize;
    /*
     * Saves the icon transformer from NetViewer. When simulator mode starts, new VertexIconTransformer
     * is set, when simulator mode is canceled and NetViewer-mode is active again, oldIconTransformer is returned.
     */
    private Transformer<NetViewerNode, Icon> oldIconTransformer;
    private VertexFireListener vfl;
    /**
     * A window where user can change preferences.
     */
    private SimulationPrefFrame preferencesJFrame;
    private JFrame chartFrame = null;
    //END JFreeChart variables
    //END Preferences
    //START JFreeChart variables
    /**
     * Maps places to XYSeries which stores the number of tokens on this place
     * at different time points.
     */
    public Map<Place, XYSeries> seriesMap = null;
    //snapshots
    //saves state snapshots. A snapshot includes marking, history and statistic for each state
    private ArrayList<Snapshot> snapshots;
    //ListModel of the snapshotsList; stores names of the snapshots, which are represented by the number of performed step.
    public DefaultListModel snapshotsListModel;

    /**
     * Creates new form TopologcialPanel
     */
    public SimulationPanel(final NetViewer netViewer, PetriNetFacade petriNet) {
        super(netViewer, petriNet, "Simulator");
        LOGGER.info("Initiating TokenSimPanel");
        initComponents();
        LOGGER.info("Successfully initiated TokenSimPanel");
        this.vv = netViewer.getVisualizationViewer();
        this.simulationMan = new SimulationManager();
        modifyComponents();
        this.simulationMan.initTokenSimulator(petriNet);

        this.repaint();
    }

    private void modifyComponents() {
        simModeJComboBox.addItem(strings.get("ATSName"));
        simModeJComboBox.addItem(strings.get("STSName"));
        simModeJComboBox.addItem(strings.get("StochTSName"));
        simModeJComboBox.addItem(strings.get("GilTSName"));
        simModeJComboBox.setSelectedIndex(3);

        //history
        this.simulationMan.historyListModel = new DefaultListModel();
        historyJList.setModel(this.simulationMan.historyListModel);
        historyJList.setCellRenderer(new HistoryCellRenderer(simulationMan));
        historyJList.addListSelectionListener(new HistorySelectionListener(historyBackJButton, historyForwardJButton, historyJList, simulationMan));

        //snapshots
        snapshotsListModel = new DefaultListModel();
        snapshotsJList.setModel(snapshotsListModel);
        snapshotsJList.addListSelectionListener(new SnapshotsListSelectionListener(this, snapshotsJList, vv));

        //create new selection listener selecting custom markings. If an entry is selected in customMarkingsComboBox, load the marking
        customMarkingsJComboBox.addPopupMenuListener(new CustomMarkingsComboBoxPopupListener(this, simulationMan, customMarkingsJComboBox));

        //Increase the size of vertices (places and transitions)
        iconSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newIconSize = ((Integer) iconSizeSpinner.getValue());
                netViewer.getVisualizationViewer().getRenderContext().setVertexShapeTransformer(new VertexShapeTransformer(newIconSize));
                vertexIconTransformer.setVertexSize(newIconSize);
                netViewer.getVisualizationViewer().repaint();
            }
        });

        //Go one step back in the history
        historyBackJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Backwards button in the history has been pressed, going one step back");
                historyForwardJButton.setEnabled(true);
                //cancel the last performed step
                simulationMan.reverseFireTransitions(simulationMan.historyArrayList.get(simulationMan.lastHistoryStep--));
                historyJList.repaint();
                historyJList.ensureIndexIsVisible(simulationMan.lastHistoryStep);
                //if no steps were performed before, disable stepBackButton
                if (simulationMan.lastHistoryStep < 0) {
                    historyBackJButton.setEnabled(false);
                }
                simulationMan.updateVisualOutput();
            }
        });

        //Perform next step in the history
        historyForwardJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Forwards button in the history has been pressed, going one step forward");
                historyBackJButton.setEnabled(true);
                //perform the next step after last performed
                simulationMan.fireTransitions(false, simulationMan.historyArrayList.get(++simulationMan.lastHistoryStep));
                historyJList.repaint();
                historyJList.ensureIndexIsVisible(simulationMan.lastHistoryStep);
                //if no steps in history left, disable stepForwardButton
                if (simulationMan.lastHistoryStep == simulationMan.historyArrayList.size() - 1) {
                    historyForwardJButton.setEnabled(false);
                }
                simulationMan.updateVisualOutput();
            }
        });

        //Shows a frame with statistics for current state
        showStatisticsJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //Create a new snapshot of current state
                saveSnapshot();
                //show a windows with statistics
                new StatisticFrame(SimulationPanel.this);
            }
        });

        //Button for saving current marking
        saveMarkingJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //save current marking to a new entry in customMarkingsMap
                //name of current marking
                LOGGER.info("SaveMarkingButton has been pressed, saving the current Marking");
                String markingName = JOptionPane.showInputDialog(SimulationPanel.this, strings.get("TSNameOfMarkingOptionPane"),
                        ("Marking " + (simulationMan.customMarkingsMap.size() + 1)));
                if (markingName != null) {
                    //if a marking with given name already exists, promt to give a new name
                    if (simulationMan.customMarkingsMap.containsKey(markingName)) {
                        JOptionPane.showMessageDialog(null, strings.get("TSMarkingNameAlreadyExists"));
                    } else {
                        Map<Integer, Long> tmpMarking = new HashMap<>(pnf.places().size());
                        for (Place place : pnf.places()) {
                            tmpMarking.put(place.id(), simulationMan.getTokenSim().getTokens(place.id()));
                        }
                        simulationMan.customMarkingsMap.put(markingName, tmpMarking);
                        //insert new marking entry to the top of the ComboBox
                        customMarkingsJComboBox.addItem(markingName);
                        customMarkingsJComboBox.setSelectedIndex(customMarkingsJComboBox.getItemCount() - 1);
                        customMarkingsJComboBox.setEnabled(true);
                        deleteMarkingJButton.setEnabled(true);
                    }
                }
            }
        });

        //Button to delete selected marking
        deleteMarkingJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Button to delete the currently selected marking has been pressed - deleting current marking");
                //delete the marking that is currently selected in customMarkingsComboBox
                String markingName = customMarkingsJComboBox.getSelectedItem().toString();
                simulationMan.customMarkingsMap.remove(markingName);
                customMarkingsJComboBox.removeItemAt(customMarkingsJComboBox.getSelectedIndex());
                //disable custmMarkingsComboBox and the delete-button if no markings are saved
                if (simulationMan.customMarkingsMap.isEmpty()) {
                    customMarkingsJComboBox.setEnabled(false);
                    deleteMarkingJButton.setEnabled(false);
                }
            }
        });
    }

    public void disableSetup() {
        loadSetupButton.setEnabled(false);
        saveSetupButton.setEnabled(false);
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
        startSimulationJButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSstartSimulationJButton"));
        startSimulationJButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSstartSimulationJButtonT"));
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

        simModeJLabel.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSsimModeJLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        jPanel1.add(simModeJLabel, gridBagConstraints);

        endSimulationJButton.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        endSimulationJButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSendSimulationJButton"));
        endSimulationJButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSendSimulationJButtonT"));
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
        snapshotsJLabel.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSsnapshotsJLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        jPanel3.add(snapshotsJLabel, gridBagConstraints);

        loadSetupButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSLoadSetup"));
        loadSetupButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSLoadSetupTT"));
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

        saveSetupButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSSaveSetup"));
        saveSetupButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSSaveSetupTT"));
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

        historyBackJButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TShistoryBackJButton"));
        historyBackJButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TShistoryBackJButtonT"));
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

        historyForwardJButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TShistoryForwardJButton"));
        historyForwardJButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TShistoryForwardJButtonT"));
        historyForwardJButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 3, 0, 0);
        jPanel4.add(historyForwardJButton, gridBagConstraints);

        historyJLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        historyJLabel.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TShistoryJLabel"));
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

        deleteMarkingJButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSdeleteMarkingJButton"));
        deleteMarkingJButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSdeleteMarkingJButtonT"));
        deleteMarkingJButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        jPanel5.add(deleteMarkingJButton, gridBagConstraints);

        saveMarkingJButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSsaveMarkingJButton"));
        saveMarkingJButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSsaveMarkingJButtonT"));
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

        showPlotButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSShowPlotB"));
        showPlotButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSShowPlotTT"));
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

        showStatisticsJButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSshowStatisticsJButton"));
        showStatisticsJButton.setToolTipText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSshowStatisticsJButtonT"));
        showStatisticsJButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 3);
        jPanel6.add(showStatisticsJButton, gridBagConstraints);

        preferencesJButton.setText(monalisa.addons.tokensimulator.SimulationManager.strings.get("TSpreferencesJButton"));
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
        String simType = simModeJComboBox.getSelectedItem().toString();
        initTS();
        createTokenSim(simType);
        startSim();
        this.simulationMan.startSimulator();
    }//GEN-LAST:event_startSimulationJButtonActionPerformed

    private void endSimulationJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endSimulationJButtonActionPerformed
        endSim();
        this.simulationMan.endSimulator();
        snapshots = null;
    }//GEN-LAST:event_endSimulationJButtonActionPerformed

    /**
     * Is called from the Project class to get all data, which should be saved
     * for the AddOn. All kind of objects can be stored here, if the class
     * implements the "Serializable" interface. The string is an identifier if
     * more than one object should be saved.
     *
     * @return A map with the data to store.
     */
    @Override
    public Map<String, Object> getObjectsForStorage() {
        Map<String, Object> storage = new HashMap<>();

        storage.put("customMarking", simulationMan.customMarkingsMap);

        return storage;
    }

    /**
     * Is called to send the stored data to the AddOn. It will get the map which
     * is saved with getObjectsForStorage() method.
     *
     * @param storage
     */
    @Override
    public void receiveStoredObjects(Map<String, Object> storage) {
        simulationMan.customMarkingsMap = (Map<String, Map<Integer, Long>>) storage.get("customMarking");
    }

    /**
     * Show the preferences-frame
     *
     * @param evt
     */
    private void preferencesJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesJButtonActionPerformed
        this.preferencesJFrame.logPathJTextField.setText((String) this.simulationMan.getPreferences().get("LogPath"));
        this.preferencesJFrame.createLogJCheckBox.setSelected((Boolean) this.simulationMan.getPreferences().get("LogEnabled"));
        this.preferencesJFrame.logPathJTextField.setEnabled((Boolean) this.simulationMan.getPreferences().get("LogEnabled"));
        this.preferencesJFrame.logPathBrowseJButton.setEnabled((Boolean) this.simulationMan.getPreferences().get("LogEnabled"));
        this.customTSPrefs.loadPreferences();
        this.netViewer.displayMenu(preferencesJFrame.getContentPane(), "TokenSimPreferences");
    }//GEN-LAST:event_preferencesJButtonActionPerformed

    private void saveSetupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSetupButtonActionPerformed
        File outFile;
        MonaLisaFileChooser fc = new MonaLisaFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        outFile = fc.getSelectedFile();
        try {
            LOGGER.info("Import of setup initiated");
            this.simulationMan.getTokenSim().exportSetup(outFile);
        } catch (ParserConfigurationException | TransformerException ex) {
            LOGGER.error("Parser or Transformer exception while handling the setupexport in the asynchronous token simulator", ex);
            JOptionPane.showMessageDialog(null, "Error during Export!",
                    SimulationManager.strings.get("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveSetupButtonActionPerformed

    private void loadSetupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSetupButtonActionPerformed
        File inFile;
        MonaLisaFileChooser fc = new MonaLisaFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        inFile = fc.getSelectedFile();
        try {
            LOGGER.info("Import of setup initiated");
            this.simulationMan.getTokenSim().importSetup(inFile);
        } catch (FileNotFoundException | XMLStreamException ex) {
            JOptionPane.showMessageDialog(null, "Invalid XML file!",
                    SimulationManager.strings.get("Error"), JOptionPane.ERROR_MESSAGE);
            LOGGER.error("File for import was not found or was invalid", ex);
        }
    }//GEN-LAST:event_loadSetupButtonActionPerformed

    private void showPlotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPlotButtonActionPerformed
        showPlot();
    }//GEN-LAST:event_showPlotButtonActionPerformed

    private void makePicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makePicActionPerformed
        try {
            this.netViewer.makePic();
        } catch (IOException ex) {
            LOGGER.error(ex);
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

    private void createTokenSim(String simType) {
        //create an instance of choosen token simulator
        LOGGER.info("simType: " + simType);
        if (simType.equalsIgnoreCase(strings.get("ATSName"))) {
            simulationMan.setTokenSim(new AsynchronousTokenSim(simulationMan));
            this.customTSControls = new AsynchronousTokenSimPanel((AsynchronousTokenSim) simulationMan.getTokenSim(), this);
            this.customTSControls.setSimName((strings.get("ATSNAME")));
            this.customTSPrefs = new AsynchronousTokenSimPrefPanel((AsynchronousTokenSim) simulationMan.getTokenSim());
        } else if (simType.equalsIgnoreCase(strings.get("STSName"))) {
            simulationMan.setTokenSim(new SynchronousTokenSim(simulationMan));
            this.customTSControls = new SynchronousTokenSimPanel((SynchronousTokenSim) simulationMan.getTokenSim(), this);
            this.customTSControls.setSimName((strings.get("STSNAME")));
            this.customTSPrefs = new SynchronousTokenSimPrefPanel((SynchronousTokenSim) simulationMan.getTokenSim());
        } else if (simType.equalsIgnoreCase(strings.get("StochTSName"))) {
            simulationMan.setTokenSim(new StochasticTokenSim(simulationMan));
            this.customTSControls = new StochasticTokenSimPanel((StochasticTokenSim) simulationMan.getTokenSim(), this);
            this.customTSControls.setSimName((strings.get("StochTSNAME")));
            this.customTSPrefs = new StochasticTokenSimPrefPanel((StochasticTokenSim) simulationMan.getTokenSim());
        } else if (simType.equalsIgnoreCase(strings.get("GilTSName"))) {
            simulationMan.setTokenSim(new GillespieTokenSim(simulationMan));
            this.customTSControls = new GillespieTokenSimPanel((GillespieTokenSim) simulationMan.getTokenSim(), this);
            this.customTSControls.setSimName((strings.get("GilTSNAME")));
            this.customTSPrefs = new GillespieTokenSimPrefPanel((GillespieTokenSim) simulationMan.getTokenSim());
        }
        simulationMan.getTokenSim().addTransitionsToCheck(simulationMan.getPetriNet().transitions().toArray(new Transition[0]));
        tspMouse = new TokenSimPopupMousePlugin(vv, simulationMan.getPetriNet(), simulationMan.getTokenSim());

        /*
         * Set the name of choosen mode for customSimulatorJPanel in preferences frame
         */
        this.customTSPrefs.setBorder(BorderFactory.createTitledBorder(simType));

        //Preferences frame
        preferencesJFrame = new SimulationPrefFrame(simulationMan, customTSPrefs, this);
        preferencesJFrame.logPathJTextField.setText((String) simulationMan.getPreferences().get("LogPath"));
        preferencesJFrame.createLogJCheckBox.setSelected((Boolean) simulationMan.getPreferences().get("LogEnabled"));
        preferencesJFrame.pack();
        preferencesJFrame.setVisible(false);

        preferencesJFrame.customSimulatorJScrollPane.setViewportView(this.customTSPrefs);
        preferencesJFrame.repaint();
        this.customControlScrollPane.setViewportView(this.customTSControls);
    }

    private void startSim() {
        netViewer.startTokenSimulator(tspMouse);

        customTSControls.startSim();

        //add customMarking names to customMarkingComboBix
        customMarkingsJComboBox.removeAllItems();
        for (String markingName : simulationMan.customMarkingsMap.keySet()) {
            customMarkingsJComboBox.addItem(markingName);
        }

        //save icon transformer from the vv before changing it to custom VertexIconTransformer
        oldIconTransformer = vv.getRenderContext().getVertexIconTransformer();
        //assign new VertexIconTransformer
        vertexIconTransformer = new VertexIconTransformer(simulationMan, vertexSize);
        vv.getRenderContext().setVertexIconTransformer(vertexIconTransformer);
        vv.getRenderContext().setVertexShapeTransformer(new VertexShapeTransformer(vertexSize));
        //assign ItemListener for firing picked transitions
        vfl = new VertexFireListener(simulationMan, vv);
        vv.getRenderContext().getPickedVertexState().addItemListener(vfl);
        //assign correct values for the spinner
        fontSizeSpinner.setValue(netViewer.getFontSize());
        arrowSizeSpinner.setValue(netViewer.getArrowSize());
        edgeSizeSpinner.setValue(netViewer.getEdgeSize());
        if (netViewer.getIconSize() > 25) {
            iconSizeSpinner.setValue(netViewer.getIconSize());
        }
        //enables/disables GUI-components
        startSimulationJButton.setEnabled(false);
        endSimulationJButton.setEnabled(true);
        iconSizeSpinner.setEnabled(true);
        arrowSizeSpinner.setEnabled(true);
        fontSizeSpinner.setEnabled(true);
        edgeSizeSpinner.setEnabled(true);
        showStatisticsJButton.setEnabled(true);
        saveMarkingJButton.setEnabled(true);
        deleteMarkingJButton.setEnabled(true);
        historyJList.setEnabled(true);
        snapshotsJList.setEnabled(true);
        simModeJComboBox.setEnabled(false);
        customMarkingsJComboBox.setEnabled(true);
        preferencesJButton.setEnabled(true);
        saveSetupButton.setEnabled(true);
        loadSetupButton.setEnabled(true);
        showPlotButton.setEnabled(true);
        vv.repaint();
        historyJList.repaint();
    }

    /**
     * This function is called every time a simulator is initialized. It handles
     * the assigning of data structures to current project etc.
     */
    private void initTS() {
        //By default, logging is disabled
        LOGGER.info("Initializing the token simulator");
        simulationMan.addGuiListener(this);
        simulationMan.getPreferences().put("LogEnabled", false);
        simulationMan.getPreferences().put("LogPath", System.getProperty("user.home") + File.separator + "MonaLisa_Simulation_log");
        simulationMan.getPreferences().put("SaveSnapshots", true);
        simulationMan.getPreferences().put("EnablePlotting", true);
        Map<Place, Boolean> placesToPlot = new HashMap<>();
        simulationMan.getPreferences().put("PlacesToPlot", placesToPlot);
        //get the size of vertices in NetViewer
        vertexSize = (int) iconSizeSpinner.getValue();
        //create clear history
        simulationMan.lastHistoryStep = -1;
        simulationMan.historyArrayList = new ArrayList<>();
        simulationMan.historyListModel.clear();
        //create clear snapshot list
        snapshots = new ArrayList<>();
        snapshotsListModel.clear();
        simulationMan.totalStepNr = 0;
        //create initial marking. For constant places, create an entry in the constantPlaces-map.
        LOGGER.info("Create initial marking");
        simulationMan.marking = new HashMap<>();
        for (Place place : simulationMan.getPetriNet().places()) {
            placesToPlot.put(place, true);
            /*
            Get the number of tokens on this place which is stored in the Petri net structure.
             */
            long tokens = simulationMan.getPetriNet().getTokens(place);
            if (!place.isConstant()) {
                simulationMan.marking.put(place.id(), tokens);
            } else {
                try {
                    MathematicalExpression mathExp = new MathematicalExpression(String.valueOf(tokens));
                    simulationMan.getConstantPlaces().put(place.id(), mathExp);
                    /*
                    Add post-transitions of a constant place to a corresponding map.
                     */
                    for (Transition t : place.outputs()) {
                        simulationMan.constantPlacesPostTransitions.add(t);
                    }
                } catch (RuntimeException ex) {
                    LOGGER.error("Issue while initializing marking for constant places: ", ex);
                }
            }
        }
        //create new empty activeTransitions list
        simulationMan.activeTransitions = new HashSet<>();
        //create clear statistic
        simulationMan.currStatistic = new Statistic(simulationMan);
        //if no markings were saved, create a new, initial marking with no tokens on all places.
        if (simulationMan.customMarkingsMap.isEmpty()) {
            LOGGER.debug("No custom marking was found, therefore create an empty marking");
            //name of current marking
            String markingName = "Empty marking";
            Map<Integer, Long> tmpMarking = new HashMap<>(simulationMan.getPetriNet().places().size());
            for (Place place : simulationMan.getPetriNet().places()) {
                tmpMarking.put(place.id(), 0L);
            }
            simulationMan.customMarkingsMap.put(markingName, tmpMarking);
        } else {
            LOGGER.debug("Custom marking was found, so it is used in the simulator");
            customMarkingsJComboBox.removeAllItems();
            for (String markingName : simulationMan.customMarkingsMap.keySet()) {
                customMarkingsJComboBox.addItem(markingName);
            }
        }
        LOGGER.info("Initial marking was created based on custom/default marking");
        /*
        Map for plotting functionality.
         */
        LOGGER.debug("Map for plotting places is created");
        seriesMap = new HashMap<>();
        for (Place place : simulationMan.getPetriNet().places()) {
            if (placesToPlot.get(place)) {
                XYSeries series = new XYSeries(place.id(), false);
                series.setDescription(place.getProperty("name").toString());
                seriesMap.put(place, series);
            }
        }
        LOGGER.debug("Map for plotting places has been created");
    }

    private void endSim() {
        if (simulationMan.getTokenSim() == null) {
            return;
        }
        customTSControls.endSim();
        customControlScrollPane.setViewportView(null);
        netViewer.endTokenSimulator(tspMouse);

        //assign old VertexIconTransformer
        vv.getRenderContext().setVertexIconTransformer(oldIconTransformer);
        //remove ItemListener for firing picked transitions
        vv.getRenderContext().getPickedVertexState().removeItemListener(vfl);
        vv.repaint();
        preferencesJFrame.dispose();
        //reset the spinner values to the NetViewer
        netViewer.setArrowSize((double) arrowSizeSpinner.getValue());
        netViewer.setFontSize((int) fontSizeSpinner.getValue());
        netViewer.setIconSize((int) iconSizeSpinner.getValue());
        netViewer.setEdgeSize((int) edgeSizeSpinner.getValue());
        //enables/disables GUI-components
        startSimulationJButton.setEnabled(true);
        endSimulationJButton.setEnabled(false);
        iconSizeSpinner.setEnabled(false);
        arrowSizeSpinner.setEnabled(false);
        fontSizeSpinner.setEnabled(false);
        edgeSizeSpinner.setEnabled(false);
        historyBackJButton.setEnabled(false);
        historyForwardJButton.setEnabled(false);
        showStatisticsJButton.setEnabled(false);
        saveMarkingJButton.setEnabled(false);
        deleteMarkingJButton.setEnabled(false);
        historyJList.setEnabled(false);
        snapshotsJList.setEnabled(false);
        simModeJComboBox.setEnabled(true);
        customMarkingsJComboBox.setEnabled(false);
        preferencesJButton.setEnabled(false);
        saveSetupButton.setEnabled(false);
        loadSetupButton.setEnabled(false);
        showPlotButton.setEnabled(false);
        preferencesJFrame.setVisible(false);

        seriesMap = null;
        if (chartFrame != null) {
            chartFrame.dispose();
        }
        simulationMan.removeGuiListener(this);
    }

    private void updatePlot() {
        LOGGER.debug("Updating the plot for all places to plot");
        Map<Place, Boolean> placesToPlot = (Map<Place, Boolean>) simulationMan.getPreferences().get("PlacesToPlot");
        /*
        Add current number of tokens to the chart series.
         */
        for (Place p : simulationMan.getPetriNet().places()) {
            if (placesToPlot.get(p)) {
                //Get the series object which corresponds to place p.
                XYSeries series = seriesMap.get(p);
                //Add the current time and number of tokens in the place p.
                series.add(simulationMan.getTokenSim().getSimulatedTime(), simulationMan.getTokenSim().getTokens(p.id()), false);
            }
        }
    }

    /**
     * Opens a window with simulation results plotted with JFreeChart.
     */
    protected void showPlot() {
        LOGGER.info("Plotting the simulation results and putting them into a window for the user to see");
        if (chartFrame != null) {
            chartFrame.dispose();
        }
        updatePlot();
        /**
         * Dataset for a XY chart.
         */
        XYSeriesCollection chartDataset = new XYSeriesCollection();
        Map<Place, Boolean> placesToPlot = (Map<Place, Boolean>) simulationMan.getPreferences().get("PlacesToPlot");
        for (Map.Entry<Place, XYSeries> entr : seriesMap.entrySet()) {
            if (placesToPlot.get(entr.getKey())) {
                chartDataset.addSeries(entr.getValue());
            }
        }
        /*
        Generate graph.
         */
        LOGGER.debug("Generating the graph for the output visualization");
        JFreeChart chart = ChartFactory.createXYLineChart("Simulation results", "Passed time [sec]", "Nr. of tokens", chartDataset,
                PlotOrientation.VERTICAL, true, false, false);
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < chartDataset.getSeries().size(); i++) {
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
        }
        renderer.setLegendItemLabelGenerator(new StandardXYSeriesLabelGenerator() {
            @Override
            public String generateLabel(XYDataset dataset, int series) {
                String label = dataset.toString();
                if (dataset instanceof XYSeriesCollection) {
                    label = ((XYSeriesCollection) dataset).getSeries(series).getDescription();
                }
                return label;
            }
        });
        chart.getXYPlot().setRenderer(renderer);
        chart.getXYPlot().getRangeAxis(0).setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        chart.getXYPlot().getRangeAxis(0).setLowerBound(0);
        chart.getXYPlot().getDomainAxis(0).setLowerBound(0);
        ChartPanel cp = new ChartPanel(chart);
        cp.setMouseWheelEnabled(true);
        /*
        Create the frame for the chart panel
         */
        chartFrame = new JFrame("Results plot");
        chartFrame.setIconImage(SimulationManager.resources.getImage("icon-16.png"));
        chartFrame.setSize(800, 600);
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.getContentPane().add(cp);
        chartFrame.setVisible(true);
    }

    public NetViewer getNetViewer() {
        return netViewer;
    }

    public void loadSnap(int selected) {
        simulationMan.loadSnapshot(snapshots.get(selected));
        historyForwardJButton.setEnabled(false);
        historyBackJButton.setEnabled(!simulationMan.historyArrayList.isEmpty());
    }

    @Override
    public void guiUpdateCall(GuiEvent e) {
        String type = e.getType();
        switch (type) {
            case GuiEvent.UPDATE_PLOT:
                updatePlot();
                break;
            case GuiEvent.UPDATE_VISUAL:
                vv.repaint();
                vv.paintComponents(vv.getGraphics());
                break;
            case GuiEvent.REPAINT:
                vv.repaint();
                break;
            case GuiEvent.LOCK:
                historyJList.setEnabled(false);
                snapshotsJList.setEnabled(false);
                historyBackJButton.setEnabled(false);
                historyForwardJButton.setEnabled(false);
                customMarkingsJComboBox.setEnabled(false);
                saveMarkingJButton.setEnabled(false);
                deleteMarkingJButton.setEnabled(false);
                customMarkingsJComboBox.setEnabled(false);
                preferencesJButton.setEnabled(false);
                saveSetupButton.setEnabled(false);
                loadSetupButton.setEnabled(false);
                break;
            case GuiEvent.UNLOCK:
                historyJList.setEnabled(true);
                snapshotsJList.setEnabled(true);
                if (simulationMan.lastHistoryStep > -1) {
                    historyBackJButton.setEnabled(true);
                }
                if (simulationMan.lastHistoryStep < simulationMan.historyArrayList.size() - 1) {
                    historyForwardJButton.setEnabled(true);
                }
                customMarkingsJComboBox.setEnabled(true);
                saveMarkingJButton.setEnabled(true);
                deleteMarkingJButton.setEnabled(true);
                customMarkingsJComboBox.setEnabled(true);
                preferencesJButton.setEnabled(true);
                saveSetupButton.setEnabled(true);
                loadSetupButton.setEnabled(true);
                break;
            case GuiEvent.SNAPSHOT:
                saveSnapshot();
                break;
            case GuiEvent.HISTORY:
                historyJList.repaint();
                historyJList.ensureIndexIsVisible(simulationMan.lastHistoryStep);
                if (simulationMan.lastHistoryStep < simulationMan.historyArrayList.size() - 1) {
                    historyForwardJButton.setEnabled(false);
                }
                if (!simulationMan.isLockGUI()) {
                    historyBackJButton.setEnabled(true);
                }
            default:
                break;
        }
    }

    /**
     * @return the preferencesJFrame
     */
    public SimulationPrefFrame getPreferencesJFrame() {
        return preferencesJFrame;
    }

    /**
     * @return the snapshots
     */
    public ArrayList<Snapshot> getSnapshots() {
        return snapshots;
    }

    /**
     * Saves a snapshot of current state. It includes the marking,
     * historyListModel, historyArrayList, activeTransitions and statistic for
     * the current state
     */
    public void saveSnapshot() {
        /*
         * Remove all snapshots that are older than current step
         */
        LOGGER.debug("Saving a snapshot of the current state of the simulation");
        while (!snapshots.isEmpty() && snapshots.get(snapshots.size() - 1).getStepNr() >= simulationMan.totalStepNr) {
            snapshots.remove(snapshots.size() - 1);
            snapshotsListModel.remove(snapshotsListModel.size() - 1);
        }
        Snapshot currSnapshot = new Snapshot(simulationMan.totalStepNr,
                simulationMan.historyListModel.toArray(),
                simulationMan.historyArrayList, simulationMan.marking,
                simulationMan.getConstantPlaces());
        currSnapshot.setStatistic(new Statistic(simulationMan.currStatistic));
        snapshots.add(currSnapshot);
        snapshots.trimToSize();
        snapshotsListModel.addElement("Step " + simulationMan.totalStepNr);
        snapshotsJList.ensureIndexIsVisible(snapshotsListModel.size() - 1);
    }
}
