/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import net.objecthunter.exp4j.tokenizer.UnknownFunctionOrVariableException;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.addons.netviewer.transformer.VertexShapeTransformer;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import org.apache.commons.collections15.Transformer;
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
 * Core class of the token simulator. An instance of this class is created on NetViewer-start.
 * This class handles the basic functions of a simulator including GUI, firing of transitions,
 * history etc. An implementation of AbstractTokenSim provides the specific features 
 * such as calculating active transitions and choosing a transition to fire.
 * @author Pavel Balazki
 */
public class TokenSimulator {
    //START VARIABLES DECLARATION
    //resources
    public static final ResourceManager resources = ResourceManager.instance();
    public static final StringResources strings = resources.getDefaultStrings();
    
    //communication with the rest of MonaLisa
    protected NetViewer netViewer;
    //current project
//    private Project project;
    //petri net of the project
    private PetriNetFacade petriNet;
    //visualization viewer that draws the petri net
    private VisualizationViewer<NetViewerNode, NetViewerEdge> vv;
    /*
     * Saves the icon transformer from NetViewer. When simulator mode starts, new VertexIconTransformer
     * is set, when simulator mode is canceled and NetViewer-mode is active again, oldIconTransformer is returned.
     */
    private Transformer<NetViewerNode, Icon> oldIconTransformer;
    //Renders transitions and places                        
    public VertexIconTransformer vertexIconTransformer;
    //Size of the vertices (places and transitions) on the VisualizationViewer
    protected int vertexSize;
    
    //JPanel with controls of the token simulator
    protected TokenSimPanel tokenSimPanel;
    /*
     * When true, all GUI components should be disabled to prevent user input.
     */
    private boolean lockGUI;
    
    //custom simulator
    //Stores the controls that are provided by the selected simulator.
    private JComponent customTSControls;
    //Instance of the selected simulator mode. implements firing behavior.
    public AbstractTokenSim tokenSim;
    //popup-plugin for the mouse has to be implemented in AbstractTokenSim-object; handles the popups when right mouse button is pressed.
    private AbstractPopupGraphMousePlugin tspMouse;

    //simulation
    //handles the action when a vertex is picked.
    private ItemListener vertexFireListener;
    //Links token numbers to place-id's. Does not contain constant places.
    public Map<Integer, Long> marking;
    /*
     * Constant places have a MathematicalExpression defining the number of tokens.
     */
    private final Map<Integer, MathematicalExpression> constantPlaces = new HashMap<>();
    /**
     * Set of transitions which have constant places as pre-places. Updated every time a place
     * constant status changes.
     */
    protected final Set<Transition> constantPlacesPostTransitions = new HashSet<>();
    /**
     * Set of transitions which are active in current state.
     */
    protected Set<Transition> activeTransitions;
    //history
    //define how many steps are to be stored for the history.
    private static final int MAX_HISTORY = 100;
    //List of an array of Transitions. Each array saves transition that were fired in one step.
    public ArrayList<Transition[]> historyArrayList;
    //ListModel of the historyList; stores names of fired transitions for each step.
    public DefaultListModel historyListModel;
    //saves the index of the last performed step in the historyList
    public int lastHistoryStep;
    
    //snapshots
    //saves state snapshots. A snapshot includes marking, history and statistic for each state
    public ArrayList<Snapshot> snapshots;
    //ListModel of the snapshotsList; stores names of the snapshots, which are represented by the number of performed step.
    public DefaultListModel snapshotsListModel;
    //A snapshot of the state will be created after every saveStateSnapshotInterval steps are performed.
    private int snapshotsInterval;
    
    //statistic
    //statistic of the current state.
    public Statistic currStatistic;
    //saves how much steps were performed at current state.
    public int totalStepNr = 0;
    
    /**
     * A map of custom markings. User is able to save current token numbers on the places 
     * in custom markings. The keys are the names of the marking (string), the values are the markings.
     * In contrast to the global marking-map, which only stores token numbers of non-constant places,
     * the maps in this map store token numbers for all places.
     */
    public Map<String, Map<Integer,Long>>  customMarkingsMap = new HashMap<>();
        
    //log file output
    //name of the log-file of current simulation.
    protected String logName;
    /*
    Writes simulation results into the log-file.
    */
    private PrintWriter logWriter = null;
    
    //START Preferences
    /**
     * Stores the preferences of simulation. Custom simulation mode should also store its preferences here.
     */
    protected HashMap<String, Object> preferences = new HashMap<>();
    
    /**
     * A window where user can change preferences.
     */
    protected TokenSimPreferencesJFrame preferencesJFrame;
    //END Preferences
    //START JFreeChart variables
    /**
     * Maps places to XYSeries which stores the number of tokens on this place
     * at different time points.
     */
    public Map<Place, XYSeries> seriesMap = null;
    JFrame chartFrame = null;
    //END JFreeChart variables
    
    //END VARIABLES DECLARATION
    
    //BEGIN INNER CLASSES
    public class PlaceConstantException extends Exception{
        public PlaceConstantException(){
            super(strings.get("TSPlaceConstantException"));
        }
    }
    
    public class PlaceNonConstantException extends Exception{
        public PlaceNonConstantException(){
            super(strings.get("TSPlaceNonConstantException"));
        }
    }   
    
   
    
    
    
    /**
     * If a transitions is picked and is active, it will be fired and visual output will be updated.
     */
    public class VertexFireListener implements ItemListener{
        @Override
        public void itemStateChanged(ItemEvent ie) {
            if(ie.getStateChange() == ItemEvent.SELECTED) {
                Set<NetViewerNode> pickedVertices = vv.getPickedVertexState().getPicked();
                if(pickedVertices.size() == 1){
                    final NetViewerNode node = pickedVertices.iterator().next();
                    if(node.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)){
                        Transition t = petriNet.findTransition(node.getMasterNode().getId());
                        if(activeTransitions.contains(t)){
                            fireTransitions(t);
                            updateVisualOutput();
                        }
                        vv.getPickedVertexState().clear();
                    }
                }
            }
        }
    }
    
   
    //END INNER CLASSES

    /**
     * Creates new instance of an implementation of the AbstractTokenSim.
     * This method should be used for choosing specific simulator, mousePopupPlugin and customToolBar.
     */
    private void newTokenSim(){
        /*
         * Check what simulator mode is selected.
         */
        String selectedSimulator = this.tokenSimPanel.simModeJComboBox.getSelectedItem().toString();        
        if(selectedSimulator.equalsIgnoreCase(strings.get("ATSName"))){
            this.tokenSim = new AsynchronousTokenSim(this);
        }
        else
            if(selectedSimulator.equalsIgnoreCase(strings.get("STSName"))){
                this.tokenSim = new SynchronousTokenSim(this);
            }
            else
                if(selectedSimulator.equalsIgnoreCase(strings.get("StochTSName"))){
                    this.tokenSim = new StochasticTokenSim(this);
                }
                else
                    if(selectedSimulator.equalsIgnoreCase(strings.get("GilTSName"))){
                        this.tokenSim = new GillespieTokenSim(this);
                    }
        
        //for this time, all transitions have to be checked for the active state.
        this.tokenSim.addTransitionsToCheck(petriNet.transitions().toArray(new Transition[0]));
        
        this.tspMouse = this.tokenSim.getMousePopupPlugin();
        this.customTSControls = this.tokenSim.getControlComponent();
        
        /*
         * Set the name of choosen mode for customSimulatorJPanel in preferences frame
         */
        JPanel customTSPrefs = this.tokenSim.getPreferencesPanel();
        customTSPrefs.setBorder(BorderFactory.createTitledBorder(selectedSimulator));
        this.preferencesJFrame.customSimulatorJScrollPane.setViewportView(customTSPrefs);
        this.preferencesJFrame.repaint();
        this.tokenSimPanel.customControlScrollPane.setViewportView(this.customTSControls);
    }
    
    /**
     * This function is called every time a simulator is initialized. It handles the assigning of data structures to current project etc.
     */
    private void init(){
        //By default, logging is disabled
        this.preferences.put("LogEnabled", false);
        this.preferences.put("LogPath", System.getProperty("user.home") + File.separator + "MonaLisa_Simulation_log");
        this.preferences.put("SaveSnapshots", true);
        this.preferences.put(("EnablePlotting"), true);
        Map<Place, Boolean> placesToPlot = new HashMap<>();
        this.preferences.put("PlacesToPlot", placesToPlot);
        
        //get actual visualization viewer from NetViewer
        this.vv = this.netViewer.getVisualizationViewer();
        //get the size of vertices in NetViewer
        
        this.vertexSize = (int) this.tokenSimPanel.iconSizeSpinner.getValue();
        
        //create clear history
        this.lastHistoryStep = -1;
        this.historyArrayList = new ArrayList<>();
        this.historyListModel.clear();
        
        //create clear snapshot list
        this.snapshots = new ArrayList<>();
        this.snapshotsListModel.clear();
        this.totalStepNr = 0;
        
        //create initial marking. For constant places, create an entry in the constantPlaces-map.
        this.marking = new HashMap<>();
        for (Place place : this.petriNet.places()){
            placesToPlot.put(place, true);
            /*
            Get the number of tokens on this place which is stored in the Petri net structure.
            */
            long tokens = petriNet.getTokens(place);
            if (!place.isConstant()){
                this.marking.put(place.id(), tokens);
            }
            else{
                try {
                    MathematicalExpression mathExp = new MathematicalExpression(String.valueOf(tokens));
                    this.constantPlaces.put(place.id(), mathExp);
                    /*
                    Add post-transitions of a constant place to a corresponding map.
                    */
                    for (Transition t : place.outputs()){
                        this.constantPlacesPostTransitions.add(t);
                    }
                } catch (UnknownFunctionOrVariableException ex) {
                    Logger.getLogger(TokenSimulator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        //create new empty activeTransitions list
        this.activeTransitions = new HashSet<>();
        
        //create clear statistic
        this.currStatistic = new Statistic(this);
        
        //if no markings were saved, create a new, initial marking with no tokens on all places.
        if(this.customMarkingsMap.isEmpty()){
            //name of current marking
            String markingName = "Empty marking";
            Map<Integer, Long> tmpMarking = new HashMap<>(petriNet.places().size());
            for (Place place : petriNet.places()){
                tmpMarking.put(place.id(), 0L);
            }
            this.customMarkingsMap.put(markingName, tmpMarking);
        } else {
            this.tokenSimPanel.customMarkingsJComboBox.removeAllItems();
            for(String markingName : this.customMarkingsMap.keySet()) {
                this.tokenSimPanel.customMarkingsJComboBox.addItem(markingName);
            }
        }
        //Preferences frame
        this.preferencesJFrame = new TokenSimPreferencesJFrame(this);
        
        this.preferencesJFrame.logPathJTextField.setText((String) this.preferences.get("LogPath"));
        this.preferencesJFrame.createLogJCheckBox.setSelected((Boolean) this.preferences.get("LogEnabled"));
        
        this.preferencesJFrame.pack();
        this.preferencesJFrame.setVisible(false);

        /*
        Map for plotting functionality.
        */
        this.seriesMap = new HashMap<>();
        for (Place place : this.petriNet.places()){
            if (placesToPlot.get(place)){
                XYSeries series = new XYSeries(place.id(), false);
                series.setDescription(place.getProperty("name").toString());
                this.seriesMap.put(place, series);
            }
        }
    }
    
    /**
     * creates the GUI
     */
    private void createGUI(){        
//        //START MODIFY CONTROL ELEMENTS IN TOKENSIMPANEL
//        //choose simulator mode
//        this.tokenSimPanel.simModeJComboBox.addItem(strings.get("ATSName"));
//        this.tokenSimPanel.simModeJComboBox.addItem(strings.get("STSName"));
//        this.tokenSimPanel.simModeJComboBox.addItem(strings.get("StochTSName"));
//        this.tokenSimPanel.simModeJComboBox.addItem(strings.get("GilTSName"));
//        this.tokenSimPanel.simModeJComboBox.setSelectedIndex(3);
//        
//        //history
//        this.historyListModel = new DefaultListModel();
//        this.tokenSimPanel.historyJList.setModel(this.historyListModel);
//        this.tokenSimPanel.historyJList.setCellRenderer(new TokenSimulator.HistoryCellRenderer());
//        this.tokenSimPanel.historyJList.addListSelectionListener(new TokenSimulator.HistorySelectionListener());
//        
//        //snapshots
//        this.snapshotsListModel = new DefaultListModel();
//        this.tokenSimPanel.snapshotsJList.setModel(snapshotsListModel);
//        this.tokenSimPanel.snapshotsJList.addListSelectionListener(new TokenSimulator.snapshotsListSelectionListener());
//
//        //create new selection listener selecting custom markings. If an entry is selected in customMarkingsComboBox, load the marking
//        this.tokenSimPanel.customMarkingsJComboBox.addPopupMenuListener(new TokenSimulator.customMarkingsComboBoxPopupListener());
//        
//        //Increase the size of vertices (places and transitions)
//        this.tokenSimPanel.iconSizeSpinner.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                int newIconSize = ((Integer)tokenSimPanel.iconSizeSpinner.getValue());
//                vv.getRenderContext().setVertexShapeTransformer(new VertexShapeTransformer(newIconSize));
//                vertexIconTransformer.setVertexSize(newIconSize);
//                vv.repaint();
//            }
//        }); 
//                
//        //Go one step back in the history
//        this.tokenSimPanel.historyBackJButton.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                tokenSimPanel.historyForwardJButton.setEnabled(true);
//                //cancel the last performed step
//                reverseFireTransitions(historyArrayList.get(lastHistoryStep--));
//                tokenSimPanel.historyJList.repaint();
//                tokenSimPanel.historyJList.ensureIndexIsVisible(lastHistoryStep);
//                //if no steps were performed before, disable stepBackButton
//                if(lastHistoryStep < 0){
//                    tokenSimPanel.historyBackJButton.setEnabled(false);
//                }
//                updateVisualOutput();
//            }
//        });
//        
//        //Perform next step in the history
//        this.tokenSimPanel.historyForwardJButton.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                tokenSimPanel.historyBackJButton.setEnabled(true);
//                //perform the next step after last performed
//                fireTransitions(false, historyArrayList.get(++lastHistoryStep));
//                tokenSimPanel.historyJList.repaint();
//                tokenSimPanel.historyJList.ensureIndexIsVisible(lastHistoryStep);
//                //if no steps in history left, disable stepForwardButton
//                if(lastHistoryStep == historyArrayList.size()-1){
//                    tokenSimPanel.historyForwardJButton.setEnabled(false);
//                }
//                updateVisualOutput();
//            }
//        });
//        
//        //Shows a frame with statistics for current state
//        this.tokenSimPanel.showStatisticsJButton.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                //Create a new snapshot of current state
//                saveSnapshot();
//                //show a windows with statistics
//                new StatisticFrame(TokenSimulator.this);
//            }
//        });
//        
//        //Button for saving current marking
//        this.tokenSimPanel.saveMarkingJButton.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                //save current marking to a new entry in customMarkingsMap
//                //name of current marking
//                String markingName = JOptionPane.showInputDialog(tokenSimPanel, strings.get("TSNameOfMarkingOptionPane"), ("Marking " + (customMarkingsMap.size() + 1)));
//                if(markingName != null){
//                    //if a marking with given name already exists, promt to give a new name
//                    if(customMarkingsMap.containsKey(markingName)){
//                        JOptionPane.showMessageDialog(null, strings.get("TSMarkingNameAlreadyExists"));
//                    }
//                    else{
//                        Map<Integer, Long> tmpMarking = new HashMap<>(petriNet.places().size());
//                        for (Place place : petriNet.places()){
//                            tmpMarking.put(place.id(), tokenSim.getTokens(place.id()));
//                        }
//                        customMarkingsMap.put(markingName, tmpMarking);
//                        //insert new marking entry to the top of the ComboBox
//                        tokenSimPanel.customMarkingsJComboBox.addItem(markingName);
//                        tokenSimPanel.customMarkingsJComboBox.setSelectedIndex(tokenSimPanel.customMarkingsJComboBox.getItemCount()-1);
//                        tokenSimPanel.customMarkingsJComboBox.setEnabled(true);
//                        tokenSimPanel.deleteMarkingJButton.setEnabled(true);
//                    }
//                }
//            }
//        });
//        
//        //Button to delete selected marking
//        this.tokenSimPanel.deleteMarkingJButton.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                //delete the marking that is currently selected in customMarkingsComboBox
//                String markingName = tokenSimPanel.customMarkingsJComboBox.getSelectedItem().toString();
//                customMarkingsMap.remove(markingName);
//                tokenSimPanel.customMarkingsJComboBox.removeItemAt(tokenSimPanel.customMarkingsJComboBox.getSelectedIndex());
//                //disable custmMarkingsComboBox and the delete-button if no markings are saved
//                if(customMarkingsMap.isEmpty()){
//                    tokenSimPanel.customMarkingsJComboBox.setEnabled(false);
//                    tokenSimPanel.deleteMarkingJButton.setEnabled(false);
//                }
//            }
//        });
//        //END MODIFY CONTROL ELEMENTS IN TOKENSIMPANEL
        
//        JScrollPane tokenSimPanelJScrollPane = new JScrollPane(this.tokenSimPanel);
//        tokenSimPanelJScrollPane.getVerticalScrollBar().setUnitIncrement(16);              
//        this.netViewer.addTabToMenuBar(strings.get("TSSimulator"), tokenSimPanelJScrollPane);
        
        this.lockGUI = false;
    }
    
    /**
     * Implements the firing of given transitions. All transitions will be fired in proper sequence; every firing computes the new marking.
     * @param transitions Transition to be fired
     * @param history Whether the fired step (can contain multiple transitions) should be added to the history or not. If the step is fired 
     * by picking an entry from the history or by activating stepForwardButton/stepBackButton, this parameter should be set to false
     */
    public void fireTransitions(boolean history, Transition... transitions){
        /*
        Ensure that die initial marking is in the plot series.
        */
        /*
        Add values to plot series if plotting is enabled
        */
        if (this.totalStepNr == 0 && (boolean) this.preferences.get("EnablePlotting")){
            this.updatePlot();
        }
        for(Transition transition: transitions){
            //tells the currStatistic that transition was fired
            this.currStatistic.transitionFired(transition);
            
//            //animate tokens movement
//            if(this.tokenSimPanel.animateTokensCheckBox.isSelected()){
//                final NetViewerNode transitionNode = this.netViewer.getTransitionMap().get(transition.id());
//                final ArrayList<NetViewerNode> prePlaceNodes = new ArrayList<NetViewerNode>();
//                for (Place place : this.petriNet.getInputPlacesFor(transition)){
//                    prePlaceNodes.add(this.netViewer.getPlaceMap().get(place.id()));
//                }
//                final ArrayList<NetViewerNode> postPlaceNodes = new ArrayList<NetViewerNode>();
//                for (Place place : this.petriNet.getPlacesFor(transition)){
//                    postPlaceNodes.add(this.netViewer.getPlaceMap().get(place.id()));
//                }
//                this.moveTokens(transitionNode, prePlaceNodes.toArray(new NetViewerNode[0]), postPlaceNodes.toArray(new NetViewerNode[0]));
//            }
            
            
            //START computing new state
            //Saves the token number for the place before firing a transition.
            long newToken;
            //Substract tokens from all input places. If a place is constant, skip this step.
            for (Place place : this.petriNet.getInputPlacesFor(transition)){
                if (!place.isConstant()){
                    //copmute tokens for preplace
                    newToken = this.marking.get(place.id()) - this.petriNet.getArc(place, transition).weight();
                    this.marking.put(place.id(), newToken);
                    //add all post-transitions of the place to checkTransitions
                    this.tokenSim.addTransitionsToCheck(this.petriNet.getTransitionsFor(place).toArray(new Transition[0]));
                }
            }
            //add tokens to all output places
            for (Place place : this.petriNet.getPlacesFor(transition)){                
                //compute tokens for preplace. If a place is constant, skip this step.
                if (!place.isConstant()){
                    newToken = this.marking.get(place.id()) + this.petriNet.getArc(transition, place).weight();
                    this.marking.put(place.id(), newToken);
                    //add all post-transitions of the place to checkTransitions
                    this.tokenSim.addTransitionsToCheck(this.petriNet.getTransitionsFor(place).toArray(new Transition[0]));
                }
            }
            //END computing new state
        }
        
        //compute active transitions after firing complete step
        this.tokenSim.computeActiveTransitions();

        if(history){
            this.addHistoryEntry(transitions);
        }
        
        this.totalStepNr++;
        this.currStatistic.stepsFired++;
        //check whether to save snapshot of current state
        try{
            int k = MAX_HISTORY;
            this.snapshotsInterval = (0 > k) ? 0 : k;
        }
        catch (NumberFormatException E){}
        if (this.snapshotsInterval > 0){
            if((boolean) this.preferences.get("SaveSnapshots") && this.totalStepNr % this.snapshotsInterval == 0){
                this.saveSnapshot();
            }
        }
        
        /*
        Actualize the log file if loggin is enabled.
        */
        if ((boolean) this.preferences.get("LogEnabled")){
            this.writeLog(transitions);
        }
        /*
        Add values to plot series if plotting is enabled
        */
        if ((boolean) this.preferences.get("EnablePlotting")){
            this.updatePlot();
        }
    }
    
    /**
     * Fires given transitions backwards, i.e. for each transition, tokens will be added to its pre-places and subtracted from the post-places.
     * Transitions will be fired from the last transition to first. This method is used for navigation through the history.
     * @param transitions 
     */
    public void reverseFireTransitions(Transition... transitions){
        this.totalStepNr--;
        this.currStatistic.stepsFired--;
        for(Transition transition : transitions){
            this.currStatistic.transitionReverseFired(transition);              //tells the currStatistic object that transition-firing was canceled
            long newToken;
            //add tokens to all input places
            for (Place place : this.petriNet.getInputPlacesFor(transition)){
                if (!place.isConstant()){
                    newToken = this.marking.get(place.id()) + this.petriNet.getArc(place, transition).weight();
                    this.marking.put(place.id(), newToken );
                    //add all post-transitions of the place to checkTransitions
                    this.tokenSim.addTransitionsToCheck(this.petriNet.getTransitionsFor(place).toArray(new Transition[0]));
                }
            }
            //remove tokens from all output places
            for (Place place : this.petriNet.getPlacesFor(transition)){
                if (!place.isConstant()){
                    newToken = this.marking.get(place.id()) - this.petriNet.getArc(transition, place).weight();
                    this.marking.put(place.id(), newToken);
                    //add all post-transitions of the place to checkTransitions
                    this.tokenSim.addTransitionsToCheck(this.petriNet.getTransitionsFor(place).toArray(new Transition[0]));
                }
            }
            //compute active transitions after firing complete step
            this.tokenSim.computeActiveTransitions();
        }
    }
    
    /**
     * Given transitions are considered to be fired in one step and will be added to historyArrayList and historyListModel as one step
     * @param transitions 
     */
    private void addHistoryEntry(Transition... transitions){
        /*
         * If the new step is not a part of the history, and the last fired step is not 
         * the last in the historyArrayList, so perform the step and delete all history entries after.
         * I.e., if the history is interrupted with a new step, the newer history becomes not actual.
        */
        if(this.lastHistoryStep < this.historyArrayList.size() - 1){
            this.historyListModel.removeRange(this.lastHistoryStep + 1, this.historyArrayList.size() - 1);
            while (this.lastHistoryStep < this.historyArrayList.size()-1){
                this.historyArrayList.remove(this.historyArrayList.size()-1);
            }
            this.tokenSimPanel.historyForwardJButton.setEnabled(false);
        }
        
        //check whether the limit size of history is reached. If it is, remove the first history entry
        if(this.historyListModel.getSize() >= TokenSimulator.MAX_HISTORY){
            this.historyListModel.remove(0);
            this.historyArrayList.remove(0);
            this.lastHistoryStep--;
        }
        StringBuilder historyOutSB = new StringBuilder();
        Arrays.sort(transitions);
        for(Transition t : transitions){
            historyOutSB.append(", ").append(t.toString());
        }
        //add the fired transition to the history list
        this.historyListModel.addElement(historyOutSB.substring(2));
        this.historyArrayList.add(transitions);
        this.historyArrayList.trimToSize();
        
        this.lastHistoryStep++;
        this.tokenSimPanel.historyJList.repaint();
        this.tokenSimPanel.historyJList.ensureIndexIsVisible(this.lastHistoryStep);
        if(!this.lockGUI)
            this.tokenSimPanel.historyBackJButton.setEnabled(true);
    }
    
    /**
     * Saves a snapshot of current state. It includes the marking, historyListModel, historyArrayList,
     * activeTransitions and statistic for the current state
     */
    public void saveSnapshot(){
        /*
         * Remove all snapshots that are older than current step
         */
        while (!this.snapshots.isEmpty() && this.snapshots.get(this.snapshots.size()-1).getStepNr() >= this.totalStepNr){
            this.snapshots.remove(this.snapshots.size()-1);
            this.snapshotsListModel.remove(this.snapshotsListModel.size()-1);
        }
        Snapshot currSnapshot = new Snapshot(this.totalStepNr, this.historyListModel, this.historyArrayList, this.marking, this.constantPlaces);
        currSnapshot.setStatistic(new Statistic(this.currStatistic));
        this.snapshots.add(currSnapshot);
        this.snapshots.trimToSize();
        this.snapshotsListModel.addElement("Step " + this.totalStepNr);
        this.tokenSimPanel.snapshotsJList.ensureIndexIsVisible(this.snapshotsListModel.size()-1);
    }
    
    /**
     * Loads the given snapshot and makes it to current state
     * @param snapshot 
     */
    public void loadSnapshot(Snapshot snapshot){
        this.totalStepNr = snapshot.getStepNr();
        
        //deep copy of historyListModel
        this.historyListModel.clear();
        for (Object obj : snapshot.getHistoryListModel()){
            this.historyListModel.addElement(obj);
        }
        
        //deep copy of historyArrayList
        this.historyArrayList.clear();
        for(Transition[] tt : snapshot.getHistoryArrayList()){
            Transition[] temp = new Transition[tt.length];
            int i = 0;
            for (Transition t : tt){
                temp[i++] = t;
            }
            this.historyArrayList.add(temp);
        }
        
        //Copy the marking of the snapshot.
        this.marking.putAll(snapshot.getMarking());
        //Copy values of constant places
        this.constantPlaces.putAll(snapshot.getConstantPlaces());
        //after loading a snapshot, active transitions must be calculated
        this.tokenSim.addTransitionsToCheck(this.petriNet.transitions().toArray(new Transition[0]));        
        this.tokenSim.computeActiveTransitions();
        this.vv.repaint();
        
        this.lastHistoryStep = this.historyArrayList.size()-1;
        this.tokenSimPanel.historyForwardJButton.setEnabled(false);
        this.tokenSimPanel.historyBackJButton.setEnabled(!historyArrayList.isEmpty());
    }
    
    /**
     * Draw token movement. NOT READY!
     */
    private void moveTokens(NetViewerNode transition, NetViewerNode[] prePlaces, NetViewerNode[] postPlaces){
//        /*
//         * Number of drawing steps
//         */
//        int steps = 1000;
//        
//        //get the size of a token
//        int tokenSize = this.vertexIconTransformer.getTokenSize()+5;
//
//        //get the vv-graphics to draw on
//        Graphics2D g2d = (Graphics2D) this.vv.getGraphics();
//        g2d.setPaint(Color.RED);
//        
//        //co-ordinates of the transition
//        Point2D transitionCO = vv.getGraphLayout().transform(transition);
//        Point2D transitionCOTransformed = vv.getRenderContext().getMultiLayerTransformer().transform(transitionCO);
//        double transitionX = transitionCOTransformed.getX();
//        double transitionY = transitionCOTransformed.getY();
//
//        //move tokens from pre-places to transition
//        for (int i = 0; i< steps; i++){
//            for(NetViewerNode prePlace : prePlaces){
//                /*
//                 * Check whether the edge between pre-place and node is a direct edge or has bends. It is possible that the edges are not sorted properly
//                 */
//                List<NetViewerEdge> bendEdges = this.netViewer.getEdge(prePlace, transition).getBendEdges();
//                
//                /*
//                 * If the direct edge has bend edges, move tokens along each bend edge
//                 */
//                if (bendEdges.size() > 0){
//                    /*
//                    * sort the edges properly
//                    * boolean sorted indicates whether the list is sorted or not
//                    */
//                    boolean sorted = false;
//                    /*
//                    * while not sorted, repeat
//                    */
//                    while(!sorted){
//                        sorted = true;
//                        /*
//                        * go through the list and invert edge on position i with the edge on position i+1, if the sink of position i
//                        * is not the source of position i+1.
//                        */
//                        for (int j = 0; j < bendEdges.size()-1; j++){
//                            NetViewerEdge edge = bendEdges.get(j);
//                            NetViewerEdge edgeFollower = bendEdges.get(j+1);
//                            if (!edge.getAim().equals(edgeFollower.getSource())){
//                                bendEdges.set(j, edgeFollower);
//                                bendEdges.set(j+1, edge);
//                                /*
//                                * if an inversion was made, the list in not sorted and the another iteration should occur
//                                */
//                                sorted = false;
//                            }
//                        }
//                    }
//                
//                    /*
//                     * There are int steps steps total, so each bend-edge has (steps/number of bends) steps
//                     */
//                    int stepsPerBend = steps/bendEdges.size();
//                    /*
//                     * The bend-edge where tokens move at the moment
//                     */
//                    int currentBend = (i/stepsPerBend);
//                    NetViewerEdge bendEdge = bendEdges.get(currentBend);
//                    /*
//                     * 
//                     */
//                    int relativeStep = i - currentBend*stepsPerBend;
//                    
//                    /*
//                    * get the co-ordinates of the source of the bendEdge
//                    */
//                    Point2D bendEdgeSourceCO = vv.getGraphLayout().transform(bendEdge.getSource());
//                    Point2D bendEdgeSourceCOTransformed = vv.getRenderContext().getMultiLayerTransformer().transform(bendEdgeSourceCO);
//                    double bendEdgeSourceX = bendEdgeSourceCOTransformed.getX();
//                    double bendEdgeSourceY = bendEdgeSourceCOTransformed.getY();
//
//                    /*
//                    * get the co-ordinates of the aim of the bendEdge
//                    */
//                    Point2D bendEdgeAimCO = vv.getGraphLayout().transform(bendEdge.getAim());
//                    Point2D bendEdgeAimCOTransformed = vv.getRenderContext().getMultiLayerTransformer().transform(bendEdgeAimCO);
//                    double bendEdgeAimX = bendEdgeAimCOTransformed.getX();
//                    double bendEdgeAimY = bendEdgeAimCOTransformed.getY();
//
//                    //determine the length of one moving step
//                    double xStep = (bendEdgeSourceX - bendEdgeAimX)/stepsPerBend;
//                    double yStep = (bendEdgeSourceY - bendEdgeAimY)/stepsPerBend;
//
//                    //repaint previous area
//                    this.vv.paintImmediately(new Float(bendEdgeSourceX - (relativeStep-1)*xStep).intValue(), new Float(bendEdgeSourceY - (relativeStep-1)*yStep).intValue(), tokenSize+10, tokenSize+10);
//
//                    //create a token shape and draw it on the graphics object
//                    Shape s = new Ellipse2D.Float(new Float(bendEdgeSourceX - relativeStep*xStep), new Float(bendEdgeSourceY - relativeStep*yStep), tokenSize, tokenSize);
//                    g2d.fill(s);
//                }
//                
//                else{
//                    //get the co-ordinates of the pre-place
//                    Point2D prePlaceCO = vv.getGraphLayout().transform(prePlace);
//                    Point2D prePlaceCOTransformed = vv.getRenderContext().getMultiLayerTransformer().transform(prePlaceCO);
//                    double prePlaceX = prePlaceCOTransformed.getX();
//                    double prePlaceY = prePlaceCOTransformed.getY();
//
//                    //determine the length of one moving step
//                    double xStep = (prePlaceX - transitionX)/steps;
//                    double yStep = (prePlaceY - transitionY)/steps;
//
//                    //repaint previous area
//                    this.vv.paintImmediately(new Float(prePlaceX - (i-1)*xStep).intValue(), new Float(prePlaceY - (i-1)*yStep).intValue(), tokenSize+10, tokenSize+10);
//
//                    //create a token shape and draw it on the graphics object
//                    Shape s = new Ellipse2D.Float(new Float(prePlaceX - i*xStep), new Float(prePlaceY - i*yStep), tokenSize, tokenSize);
//                    g2d.fill(s);
//                }
//            }
//        }
//        
//         //move tokens from transition to post-places
//        for (int i = 0; i< steps; i++){
//            for(NetViewerNode postPlace : postPlaces){
//                /*
//                 * Check whether the edge between transition and post-place is a direct edge or has bends. It is possible that the edges are not sorted properly
//                 */
//                List<NetViewerEdge> bendEdges = this.netViewer.getEdge(transition, postPlace).getBendEdges();
//                
//                /*
//                 * If the direct edge has bend edges, move tokens along each bend edge
//                 */
//                if (bendEdges.size() > 0){
//                     /*
//                    * sort the edges properly
//                    * boolean sorted indicates whether the list is sorted or not
//                    */
//                    boolean sorted = false;
//                    /*
//                    * while not sorted, repeat
//                    */
//                    while(!sorted){
//                        sorted = true;
//                        /*
//                        * go through the list and invert edge on position i with the edge on position i+1, if the sink of position i
//                        * is not the source of position i+1.
//                        */
//                        for (int j = 0; j < bendEdges.size()-1; j++){
//                            NetViewerEdge edge = bendEdges.get(j);
//                            NetViewerEdge edgeFollower = bendEdges.get(j+1);
//                            if (!edge.getAim().equals(edgeFollower.getSource())){
//                                bendEdges.set(j, edgeFollower);
//                                bendEdges.set(j+1, edge);
//                                /*
//                                * if an inversion was made, the list in not sorted and the another iteration should occur
//                                */
//                                sorted = false;
//                            }
//                        }
//                    }
//                    /*
//                     * There are int steps steps total, so each bend-edge has (steps/number of bends) steps
//                     */
//                    int stepsPerBend = steps/bendEdges.size();
//                    /*
//                     * The bend-edge where tokens move at the moment
//                     */
//                    int currentBend = (i/stepsPerBend);
//                    NetViewerEdge bendEdge = bendEdges.get(currentBend);
//                    /*
//                     * 
//                     */
//                    int relativeStep = i - currentBend*stepsPerBend;
//                    
//                    /*
//                    * get the co-ordinates of the source of the bendEdge
//                    */
//                    Point2D bendEdgeSourceCO = vv.getGraphLayout().transform(bendEdge.getSource());
//                    Point2D bendEdgeSourceCOTransformed = vv.getRenderContext().getMultiLayerTransformer().transform(bendEdgeSourceCO);
//                    double bendEdgeSourceX = bendEdgeSourceCOTransformed.getX();
//                    double bendEdgeSourceY = bendEdgeSourceCOTransformed.getY();
//
//                    /*
//                    * get the co-ordinates of the aim of the bendEdge
//                    */
//                    Point2D bendEdgeAimCO = vv.getGraphLayout().transform(bendEdge.getAim());
//                    Point2D bendEdgeAimCOTransformed = vv.getRenderContext().getMultiLayerTransformer().transform(bendEdgeAimCO);
//                    double bendEdgeAimX = bendEdgeAimCOTransformed.getX();
//                    double bendEdgeAimY = bendEdgeAimCOTransformed.getY();
//
//                    //determine the length of one moving step
//                    double xStep = (bendEdgeSourceX - bendEdgeAimX)/stepsPerBend;
//                    double yStep = (bendEdgeSourceY - bendEdgeAimY)/stepsPerBend;
//
//                    //repaint previous area
//                    this.vv.paintImmediately(new Float(bendEdgeSourceX - (relativeStep-1)*xStep).intValue(), new Float(bendEdgeSourceY - (relativeStep-1)*yStep).intValue(), tokenSize+10, tokenSize+10);
//
//                    //create a token shape and draw it on the graphics object
//                    Shape s = new Ellipse2D.Float(new Float(bendEdgeSourceX - relativeStep*xStep), new Float(bendEdgeSourceY - relativeStep*yStep), tokenSize, tokenSize);
//                    g2d.fill(s);
//                }
//                
//                else{
//                    //get the co-ordinates of the post-place
//                    Point2D postPlaceCO = vv.getGraphLayout().transform(postPlace);
//                    Point2D postPlaceCOTransformed = vv.getRenderContext().getMultiLayerTransformer().transform(postPlaceCO);
//                    double postPlaceX = postPlaceCOTransformed.getX();
//                    double postPlaceY = postPlaceCOTransformed.getY();
//
//                    //determine the length of one moving step
//                    double xStep = (postPlaceX - transitionX)/steps;
//                    double yStep = (postPlaceY - transitionY)/steps;
//
//                    //repaint previous area
//                    this.vv.paintImmediately(new Float(transitionX + (i-1)*xStep).intValue(), new Float(transitionY + (i-1)*yStep).intValue(), tokenSize+10, tokenSize+10);
//
//                    //create a token shape and draw it on the graphics object
//                    Shape s = new Ellipse2D.Float(new Float(transitionX + i*xStep), new Float(transitionY + i*yStep), tokenSize, tokenSize);
//                    g2d.fill(s);
//                }
//            }
//        }
//
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(TokenSimulator.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    private void updatePlot(){
        Map<Place, Boolean> placesToPlot = (Map<Place, Boolean>) this.preferences.get("PlacesToPlot");
        /*
        Add current number of tokens to the chart series.
        */
        for (Place p : this.petriNet.places()){
            if (placesToPlot.get(p)){
                //Get the series object which corresponds to place p.
                XYSeries series = this.seriesMap.get(p);
                //Add the current time and number of tokens in the place p.
                series.add(this.tokenSim.getSimulatedTime(), this.tokenSim.getTokens(p.id()), false);
            }
        }
    }
    
    /**
     * Opens a window with simulation results plotted with JFreeChart.
     */
    protected void showPlot(){
        if(chartFrame != null){
            chartFrame.dispose();
        }
        updatePlot();
        /**
         * Dataset for a XY chart.
        */
        XYSeriesCollection chartDataset = new XYSeriesCollection();
        Map<Place, Boolean> placesToPlot = (Map<Place, Boolean>) this.preferences.get("PlacesToPlot");
        for (Entry<Place, XYSeries> entr : this.seriesMap.entrySet()){
            if (placesToPlot.get(entr.getKey())){
                chartDataset.addSeries(entr.getValue());
            }
        }
        /*
        Generate graph.
        */
        JFreeChart chart = ChartFactory.createXYLineChart("Simulation results",
                "Passed time [sec]", "Nr. of tokens", chartDataset,
                PlotOrientation.VERTICAL, true, false, false);
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < chartDataset.getSeries().size(); i++){
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
        }
        renderer.setLegendItemLabelGenerator(new StandardXYSeriesLabelGenerator(){
            @Override
            public String generateLabel( XYDataset dataset, int series)  {
                String label = dataset.toString();
                if (dataset instanceof XYSeriesCollection){
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
        chartFrame.setIconImage(resources.getImage("icon-16.png"));
        chartFrame.setSize(800,600);
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.getContentPane().add(cp);
        chartFrame.setVisible(true);
    }
    
    /**
     * Write the simulation step to the log file.
     */
    private void writeLog(Transition... transitions){
        StringBuilder logSB = new StringBuilder();
        /*
        If the method is called for the first time, create new log writer.
        */
        if (this.logWriter == null){
            /*
             * Create default directory for log-files
             */
            new File((String) this.preferences.get("LogPath")).mkdir();
            //create a name of the log-file based on current date
            Calendar cal = Calendar.getInstance();
            this.logName = "Sim_log_" + cal.getTime().toString().replaceAll(" ", "_");

            /*
            Write the header of the log.
            */
            try{
                this.logWriter = new PrintWriter(new BufferedWriter(
                        new FileWriter((String) this.preferences.get("LogPath") + File.separator + this.logName +".csv")));
                logSB.append("PlaceName\t\t\t");
                for (Place place : this.petriNet.places()){
                    logSB.append((String) place.getProperty("name")).append("\t");
                }
                logSB.append("\nPlaceID\t\t\t");
                for (Place place : this.petriNet.places()){
                    logSB.append(place.id()).append("\t");
                }
                logSB.append("\n----------------\nStep\tTime\tFired transition(s)");
            }
            catch(IOException ex){
                Logger.getLogger(TokenSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
        logSB.append("\n").append(this.totalStepNr).append("\t").append(this.tokenSim.getSimulatedTime()).append("\t");
        //Append names of fired transitions
        for (Transition t : transitions){
            logSB.append((String) t.getProperty("name")).append(";");
        }
        logSB.deleteCharAt(logSB.length()-1);
        logSB.append("\t");
        for (Place place : this.petriNet.places()){
            logSB.append(this.tokenSim.getTokens(place.id())).append("\t");
        }
        logWriter.print(logSB);
    }
    
    /**
     * Forces the data in the log-output stream to be written to file.
     */
    public void flushLog(){
        if (logWriter != null){
            logWriter.flush();
        }
    }
    
    /**
     * Switches from NetViewer-mode to Simulator-mode
     */
    protected void startSimulator(){
        this.init();
        //create an instance of choosen token simulator
        this.newTokenSim();
        
        this.netViewer.startTokenSimulator(this.tspMouse);
        this.tokenSim.startSim();

        //add customMarking names to customMarkingComboBix
        this.tokenSimPanel.customMarkingsJComboBox.removeAllItems();
        for (String markingName : this.customMarkingsMap.keySet()){
            this.tokenSimPanel.customMarkingsJComboBox.addItem(markingName);
        }
        
        //load current tokens from Petri net
        for(Place p : this.petriNet.places()) {
            try {
                if(!p.isConstant()) {
                    this.setTokens(p.id(), this.petriNet.getTokens(p));
                }
            } catch (PlaceConstantException ex) {
                Logger.getLogger(TokenSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //save icon transformer from the vv before changing it to custom VertexIconTransformer
        this.oldIconTransformer = this.vv.getRenderContext().getVertexIconTransformer();
        //assign new VertexIconTransformer
        this.vertexIconTransformer = new VertexIconTransformer(this);
        this.vv.getRenderContext().setVertexIconTransformer(this.vertexIconTransformer);
        this.vv.getRenderContext().setVertexShapeTransformer(new VertexShapeTransformer(this.vertexSize));
        //assign ItemListener for firing picked transitions
        this.vv.getRenderContext().getPickedVertexState().addItemListener(this.vertexFireListener);
        //assign correct values for the spinner
        this.tokenSimPanel.fontSizeSpinner.setValue(this.netViewer.getFontSize());
        this.tokenSimPanel.arrowSizeSpinner.setValue(this.netViewer.getArrowSize());
        this.tokenSimPanel.edgeSizeSpinner.setValue(this.netViewer.getEdgeSize());
        if(this.netViewer.getIconSize() > 25) {
            this.tokenSimPanel.iconSizeSpinner.setValue(this.netViewer.getIconSize());
        }        
        //enables/disables GUI-components
        this.tokenSimPanel.startSimulationJButton.setEnabled(false);
        this.tokenSimPanel.endSimulationJButton.setEnabled(true);
        this.tokenSimPanel.iconSizeSpinner.setEnabled(true);
        this.tokenSimPanel.arrowSizeSpinner.setEnabled(true);
        this.tokenSimPanel.fontSizeSpinner.setEnabled(true);        
        this.tokenSimPanel.edgeSizeSpinner.setEnabled(true);
        this.tokenSimPanel.showStatisticsJButton.setEnabled(true);
        this.tokenSimPanel.saveMarkingJButton.setEnabled(true);
        this.tokenSimPanel.deleteMarkingJButton.setEnabled(true);
        this.tokenSimPanel.historyJList.setEnabled(true);
        this.tokenSimPanel.snapshotsJList.setEnabled(true);
        this.tokenSimPanel.simModeJComboBox.setEnabled(false);
        this.tokenSimPanel.customMarkingsJComboBox.setEnabled(true);
        this.tokenSimPanel.preferencesJButton.setEnabled(true);
        this.tokenSimPanel.saveSetupButton.setEnabled(true);
        this.tokenSimPanel.loadSetupButton.setEnabled(true);
        this.tokenSimPanel.showPlotButton.setEnabled(true);
        this.vv.repaint();
        this.tokenSimPanel.historyJList.repaint();
    }
    
    /**
     * returns the visualization viewer of the simulator
     * @return 
     */
    protected VisualizationViewer<NetViewerNode, NetViewerEdge> getVisualizationViewer(){
        return this.vv;
    }
    
    /**
     * Returns the number of steps simulated so far.
     * @return Number of simulated steps.
     */
    protected int getSimulatedSteps(){
        return this.totalStepNr;
    }
    
    /**
     * Switches from Simulator-mode to NetViewer-mode
     */
    public void endSimulator(){
        if(this.tokenSim == null)
            return;
        this.tokenSim.endSim();
        this.tokenSimPanel.customControlScrollPane.setViewportView(null);
        this.netViewer.endTokenSimulator(tspMouse);
        
        //assign old VertexIconTransformer
        this.vv.getRenderContext().setVertexIconTransformer(oldIconTransformer);
        //remove ItemListener for firing picked transitions
        vv.getRenderContext().getPickedVertexState().removeItemListener(vertexFireListener);
        this.vv.repaint();
        this.preferencesJFrame.dispose();
        //reset the spinner values to the NetViewer
        this.netViewer.setArrowSize((double) this.tokenSimPanel.arrowSizeSpinner.getValue());
        this.netViewer.setFontSize((int) this.tokenSimPanel.fontSizeSpinner.getValue());
        this.netViewer.setIconSize((int) this.tokenSimPanel.iconSizeSpinner.getValue());
        this.netViewer.setEdgeSize((int) this.tokenSimPanel.edgeSizeSpinner.getValue());
        //enables/disables GUI-components
        this.tokenSimPanel.startSimulationJButton.setEnabled(true);
        this.tokenSimPanel.endSimulationJButton.setEnabled(false);
        this.tokenSimPanel.iconSizeSpinner.setEnabled(false);
        this.tokenSimPanel.arrowSizeSpinner.setEnabled(false);
        this.tokenSimPanel.fontSizeSpinner.setEnabled(false);        
        this.tokenSimPanel.edgeSizeSpinner.setEnabled(false);
        this.tokenSimPanel.historyBackJButton.setEnabled(false);
        this.tokenSimPanel.historyForwardJButton.setEnabled(false);
        this.tokenSimPanel.showStatisticsJButton.setEnabled(false);
        this.tokenSimPanel.saveMarkingJButton.setEnabled(false);
        this.tokenSimPanel.deleteMarkingJButton.setEnabled(false);
        this.tokenSimPanel.historyJList.setEnabled(false);
        this.tokenSimPanel.snapshotsJList.setEnabled(false);
        this.tokenSimPanel.simModeJComboBox.setEnabled(true);
        this.tokenSimPanel.customMarkingsJComboBox.setEnabled(false);
        this.tokenSimPanel.preferencesJButton.setEnabled(false);
        this.tokenSimPanel.saveSetupButton.setEnabled(false);
        this.tokenSimPanel.loadSetupButton.setEnabled(false);
        this.tokenSimPanel.showPlotButton.setEnabled(false);
        this.preferencesJFrame.setVisible(false);
        /*
        Close writer.
        */
        try{
            this.logWriter.close();
        }
        catch(Exception ex){
        }
        this.logWriter = null;
        //clear plot
        this.snapshots = null;
        this.seriesMap = null;
        if (this.chartFrame != null){
            this.chartFrame.dispose();
        }
    }
    
    //START PUBLIC METHODS
    /**
     * The visual state of the simulator will be updated, i.e. all vertices and edges are
     * repainted, showing the actual marking. It should be called every time the new state should
     * be painted, e.g. after firing a transition, a set of transitions of firing multiple transitions in one step. Updating visual output slows 
     * the firing frequence down, so sometimes it should be better not to update after every firing.
     */
    public void updateVisualOutput(){
        this.vv.repaint();
        this.vv.paintComponents(this.vv.getGraphics());
    }
    
    /**
     * Fires given transitions in one step and adds this step to the history
     * @param transitions
     */
    public void fireTransitions(Transition... transitions){
        fireTransitions(true, transitions);
    }
    
    /**
     * Enables to lock GUI elements such as history or snapshots list by calling lockGUI(true). The GUI should be locked before performing a sequence
     * of firing to avoid firing sequence being disrupt by user input. After firing task is complete the GUI should be unlocked by calling lockGUI(false)
     * @param lock 
     */
    public void lockGUI(Boolean lock){
        this.lockGUI = lock;
        if(lock){
            this.tokenSimPanel.historyJList.setEnabled(false);
            this.tokenSimPanel.snapshotsJList.setEnabled(false);
            this.tokenSimPanel.historyBackJButton.setEnabled(false);
            this.tokenSimPanel.historyForwardJButton.setEnabled(false);
            this.tokenSimPanel.customMarkingsJComboBox.setEnabled(false);
            this.tokenSimPanel.saveMarkingJButton.setEnabled(false);
            this.tokenSimPanel.deleteMarkingJButton.setEnabled(false);
            this.tokenSimPanel.customMarkingsJComboBox.setEnabled(false);
            this.tokenSimPanel.preferencesJButton.setEnabled(false);
            this.tokenSimPanel.saveSetupButton.setEnabled(false);
            this.tokenSimPanel.loadSetupButton.setEnabled(false);
        }
        else{
            this.tokenSimPanel.historyJList.setEnabled(true);
            this.tokenSimPanel.snapshotsJList.setEnabled(true);
            if(this.lastHistoryStep > -1)
                this.tokenSimPanel.historyBackJButton.setEnabled(true);
            if(this.lastHistoryStep < this.historyArrayList.size() - 1)
                this.tokenSimPanel.historyForwardJButton.setEnabled(true);
            this.tokenSimPanel.customMarkingsJComboBox.setEnabled(true);
            this.tokenSimPanel.saveMarkingJButton.setEnabled(true);
            this.tokenSimPanel.deleteMarkingJButton.setEnabled(true);
            this.tokenSimPanel.customMarkingsJComboBox.setEnabled(true);
            this.tokenSimPanel.preferencesJButton.setEnabled(true);
            this.tokenSimPanel.saveSetupButton.setEnabled(true);
            this.tokenSimPanel.loadSetupButton.setEnabled(true);
        }       
    }
    
    /**
     * Assign the given token number to a non-constant place with given id
     * @param id ID of a non-constant place.
     * @param tokens 
     * @throws monalisa.addons.tokensimulator.TokenSimulator.PlaceConstantException 
     */
    public void setTokens(int id, long tokens) throws PlaceConstantException{
        /*
        Get the place with passed ID.
        */
        Place p = petriNet.findPlace(id);
        if (!petriNet.findPlace(id).isConstant()){
            this.marking.put(id, tokens);
            //add all post-transitions of the place to checkTransitions and compute active transitions
            this.tokenSim.addTransitionsToCheck(this.petriNet.getTransitionsFor(p).toArray(new Transition[0]));
            this.tokenSim.computeActiveTransitions();
            this.vv.repaint();
        }
        //if the place is constant, trow an exception.
        else{
            throw new PlaceConstantException();
        }
    }
    
    /**
     * Assign the mathematical expression to a constant place with given id. Usualy it should describe concentration in M.
     * @param id
     * @param mathExp 
     * @throws monalisa.addons.tokensimulator.TokenSimulator.PlaceNonConstantException 
     */
    public void setMathExpression(int id, MathematicalExpression mathExp) throws PlaceNonConstantException{
        /*
        Get the place with passed ID.
        */
        Place p = petriNet.findPlace(id);
        if (p.isConstant()){
            this.constantPlaces.put(id, mathExp);
            //add all post-transitions of the place to checkTransitions and compute active transitions
            this.tokenSim.addTransitionsToCheck(this.petriNet.getTransitionsFor(p).toArray(new Transition[0]));
            this.tokenSim.computeActiveTransitions();
            this.vv.repaint();
        }
        //if the place is non-constant, trow an exception.
        else{
            throw new PlaceNonConstantException();
        }
    }
    
    /**
     * Return the mathematical expression which describes the number of tokens on the place with id.
     * @param id ID of the place.
     * @return If the place is constant, its MathematicalExpression.
     */
    public MathematicalExpression getMathematicalExpression(int id){
        if (petriNet.findPlace(id).isConstant()){
            return this.constantPlaces.get(id);
        }
        return null;
    }
    
    /**
     * Return the map with non-constant places and the number of tokens on them. Does not contain constant places.
     * @return Copy of the marking-Map.
     */
    public Map <Integer, Long> getMarking(){
        return new HashMap<>(marking);
    }
    
    /**
     * Get the map of constant places with their mathematical expressions of token number.
     * @return Copy of the constantPlaces-Map.
     */
    public Map<Integer, MathematicalExpression> getConstantPlaces(){
        return new HashMap(this.constantPlaces);
    }
    
    /**
     * Set the places status as constant or non-constant.
     * If isConstant == true:
     * -sets the places property to constant.
     * -removes the place from marking-map.
     * -adds the place to constantPlaces-map with the mathematical expression build from the
     *  number of tokens of the place while it was non-constant.
     * If isConstant == false:
     * -set the places property to non-constant.
     * -removes the place from the constantPlaces-map.
     * -adds the place to the marking-map and tries to evaluate the mathematical expression without variables. This <b>will</b> fail 
     * if the expression has variables. In this case, 0 will be set as the number of tokens for the place.
     * @param id ID of the place.
     * @param isConstant Whether the place is constant or not.
     */
    public void setPlaceConstant(int id, boolean isConstant){
        //get the place.
        Place p = this.petriNet.findPlace(id);
        if (isConstant){
            //if the place is allready constant, do nothing.
            if (!p.isConstant()){
                try {
                    p.setConstant(true);
                    long oldVal = this.marking.get(id);
                    this.marking.remove(id);
                    this.constantPlaces.put(id, new MathematicalExpression(String.valueOf(oldVal)));
                    /*
                    Add post-transitions of a constant place to a corresponding map.
                    */
                    for (Transition t : p.outputs()){
                        this.constantPlacesPostTransitions.add(t);
                    }
                    } catch (UnknownFunctionOrVariableException ex) {
                        Logger.getLogger(TokenSimulator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else{
            //if the place is allready non-constant, do nothing.
            if (p.isConstant()){
                long value = tokenSim.getTokens(p.id());
                p.setConstant(false);
                this.constantPlaces.remove(id);
                /*
                Remove post-transitions of a constant place from the corresponding map.
                */
                for (Transition t : p.outputs()){
                    this.constantPlacesPostTransitions.remove(t);
                }
                this.marking.put(id, value);
            }
        }
    }    
    
    /**
     * 
     * @return PetriNetFacade of the current project
     */
    public PetriNetFacade getPetriNet(){
        return this.petriNet;
    }
   
    /**
     * A set with active transitions.
     * @return Copy of activeTransitions - set of this TokenSimulator.
     */
    public Set<Transition> getActiveTransitions(){
        return new HashSet(this.activeTransitions);
    }
    
    /**
     * 
     * @return statistic of the current state
     */
    public Statistic getStatistic(){
        return this.currStatistic;
    }

    public Map<String, Map<Integer, Long>> getCustomMarkingsMap() {
        return customMarkingsMap;
    }

    public void setCustomMarkingsMap(Map<String, Map<Integer, Long>> customMarkingsMap) {
        this.customMarkingsMap = customMarkingsMap;
    }
    
    /**
     * This method is called on initialization of NetViewer.
     * @param netViewerN
     * @param projectN 
     */
    public void initTokenSimulator(NetViewer netViewer, PetriNetFacade petriNet, TokenSimPanel tokenSimPanel){
        //netViewer are initialized on creating TokenSimulator and will not be changed.
        this.netViewer = netViewer;
        this.tokenSimPanel = tokenSimPanel;
        this.vertexFireListener = new TokenSimulator.VertexFireListener();
        this.petriNet = petriNet;
        
        this.createGUI();
    }
    //END PUBLIC METHODS
}
