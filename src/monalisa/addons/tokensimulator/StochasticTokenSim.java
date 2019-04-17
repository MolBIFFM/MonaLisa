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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.MonaLisaFileChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class simulates a stochastic petri net (PN).
 * Stochastic PNs are timed PNs with stochastic timings. Each transition t has 
 * corresponding fire rate c. A transition t fires after a time delay d is elapsed after
 * this transitions enabling. The time delays are random. In this simulator,
 * this random numbers have exponential distribution, which correlates with the firing rate c.
 * The probability density for the exponential law = c * exp[-c*t], the distribution function is
 * an integral of the probability density =
 * 1 - exp[-c*t]
 * where t is the time interval where the transition can fire.
 * For q-enabled transitions, the probability is
 * q * c * dt
 * @author Pavel Balazki.
 */
public class StochasticTokenSim extends AbstractTokenSim{
    //BEGIN VARIABLES DECLARATION
    //GUI
    //JPanel with custom controls of the simulator
    private StochasticTokenSimPanel tsPanel;
    private StochasticTokenSimPrefPanel prefPanel;
    /**
     * Thread which executes simulation.
     */
    private SimulationSwingWorker simSwingWorker;

    /**
     * Time passed since the begin of simulation.
     */
    private double time = 0.0;
    /**
     * This HashMap links the firing rate to the corresponding transitions ID. The keys are the IDs of
     * the transitions, which are integers; the values are the firing rates, which are doubles.
     */
    private final Map<Integer, Double> firingRates;
    /**
     * This HashMap links possible firing time intervals to the corresponding transition. After a transition becomes enabled,
     * the next firing time is calculated according to the exponential distribution and the transitions firing rate. Every time a transition fires,
     * the corresponding firing time is deleted from the list. The keys are the transitions;
     * the values are the arrays of firing time intervals, which are doubles
     */
    private final Map<Transition, ArrayList<Double>> nextFiringTime;
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(StochasticTokenSim.class);
    //END VARIABLES DECLARATION
    
    //BEGIN INNER CLASSES
    /**
     * Thread which performs the simulation sequence.
     */
    private class SimulationSwingWorker extends SwingWorker {
        /**
         * Number of steps this thread should perform.
         */
        private int stepsLeft;
        /**
         * Indicates whether the simulation is still running. Can be set to
         * "false" upon termination call.
         */
        private boolean isRunning;

        public SimulationSwingWorker(int nrOfStepsN) {
            this.stepsLeft = nrOfStepsN;
        }        
        @Override
        protected Void doInBackground() {
            //time delay between single firings
            int timeDelay = (Integer) tokenSim.preferences.get("Time delay");
            //how often visual output will be updated
            final int updateInterval = (Integer) tokenSim.preferences.get("Update interval");
            tsPanel.progressBar.setMaximum(stepsLeft);
            /*
             * Set the running state of this runnable to "true".
             */
            this.isRunning = true;

            //all active transitions that can be fired right now
            Set<Transition> activeTransitions = tokenSim.getActiveTransitions();

            /*
             * Perform firing until aborted.
             */
            while (isRunning) {
                try {
                    /*
                     * If no transitions are active, abort simulation.
                     */
                    if (activeTransitions.isEmpty() || this.isCancelled()) {
                        LOGGER.debug("No more active transitions, therefore stopping the stochastic token simulation");
                        this.isRunning = false;
                        break;
                    }
                    /*
                     * Perform firing of transitions.
                     */
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            tokenSim.fireTransitions(getTransitionToFire());
                            //If updateInterval is positive, check whether visual output must be updated in current step.
                            if (updateInterval > 0) {
                                if (tokenSim.getSimulatedSteps() % updateInterval == 0) {
                                    tokenSim.updateVisualOutput();
                                }
                            }
                        }
                    });

                    /*
                     * If continuous mode is not selected, reduce the number of steps left by one.
                     */
                    if (!tsPanel.continuousModeCheckBox.isSelected()) {
                        tsPanel.progressBar.setValue(tsPanel.progressBar.getMaximum() - --stepsLeft);
                        /*
                         * Abort simulation if no more steps are left.
                         */
                        if (stepsLeft <= 0) {
                            this.isRunning = false;
                        }
                    }
                    Thread.sleep(timeDelay);
                } catch (InterruptedException | InvocationTargetException ex) {
                    LOGGER.error("Simulation interupted or InvocationTargetException", ex);
                }
            }
            return null;
        }

        @Override
        protected void done() {
            /*
             * Reset the progress bar.
             */
            tsPanel.progressBar.setMaximum(0);
            tsPanel.progressBar.setValue(0);

            //unlock GUI
            tsPanel.fireTransitionsButton.setText(TokenSimulator.strings.get("ATSFireTransitionsB"));
            tsPanel.fireTransitionsButton.setToolTipText(TokenSimulator.strings.get("ATSFireTransitionsBT"));
            
            if(!tsPanel.continuousModeCheckBox.isSelected()){
                tsPanel.stepField.setEnabled(true);
            }
            tsPanel.continuousModeCheckBox.setEnabled(true);
            tsPanel.firingRateButton.setEnabled(true);
            tokenSim.lockGUI(false);
            tokenSim.flushLog();
            tsPanel.simTimeLabel.setText("Simulated time: " + getSimulatedTime());
        }

        /**
         * Signal to stop simulation.
         */
        public void stopSequence() {
            LOGGER.debug("Stopping the sequence and updating the visual output");
            this.isRunning = false;
            //update visual output
            tokenSim.updateVisualOutput();
        }
    }
    //END INNER CLASSES
    
    //BEGIN CONSTRUCTORS
    /**
     * Creates a new instance of the stochastic token simulator.
     * @param tsN 
     */
    StochasticTokenSim(TokenSimulator tsN){
        super(tsN);
        /*
         * Initialize the firing rates. If nothing specified, a rate of 1 is concidered
         * Initialize the HashMap for firing times.
         */
        this.firingRates = new HashMap<>();
        this.nextFiringTime = new HashMap<>();
        
        for (Transition t : this.tokenSim.getPetriNet().transitions()){
            this.firingRates.put(t.id(), 1.0);
            this.nextFiringTime.put(t, new ArrayList<Double>());
        }
    }
    //END CONSTRUCTORS

    /**
     * The init()-function is called in the super-constructor of the AbstractTokenSim,
     * which is called in the constructor of StochasticTokenSim.
     * A JPanel with controls will be created, this panel in coded in StochasticTokenSimPanel. This panel will be given 
     * to the NetViewer and will be built in the control-toolbar.
     */
    @Override
    protected void init() {
        //START CREATING GUI ELEMENTS
        /*
         * create new GUI-Panel
         */
        this.tsPanel = new StochasticTokenSimPanel(this);
        this.prefPanel = new StochasticTokenSimPrefPanel(this);
        /*
         * Set the name of the Stochastic simulator as a text for the JLabel simName
         */
        this.tsPanel.simName.setText(TokenSimulator.strings.get("StochTSName"));
        //END CREATING GUI ELEMENTS
        this.tokenSim.preferences.put("Time delay", 0);
        this.tokenSim.preferences.put("Update interval", 1);
        this.tokenSim.preferences.put("Marking dependent rates", false);
    }
    
    /**
     * 
     * @return the tsPanel, a JPanel with custom controls. tsPanel is an instance of
     * StochasticTokenSimPanel
     */
    @Override
    protected JComponent getControlComponent() {
        return this.tsPanel;
    }
    
    @Override
    protected JPanel getPreferencesPanel(){
        return this.prefPanel;
    }
    
     @Override
    protected void updatePreferences() {
        /*
         * Update time delay.
         */
        LOGGER.debug("Updating time delay, update interval and marking checkbox depending on the preferences");
        int timeDelay = Integer.parseInt(this.prefPanel.timeDelayJFormattedTextField.getText());
        if (timeDelay >= 0)
            this.tokenSim.preferences.put("Time delay", timeDelay);
        /*
         * Update update interval
         */
        int updateInterval = Integer.parseInt(this.prefPanel.updateIntervalFormattedTextField.getText());
        if (updateInterval >= 0)
            this.tokenSim.preferences.put("Update interval", updateInterval);
        /*
         * Update marking dependent firing rates check box
         */
        this.tokenSim.preferences.put("Marking dependent rates", this.prefPanel.qEnablingCheckBox.isSelected());
    }

    @Override
    protected void loadPreferences() {
        this.prefPanel.timeDelayJFormattedTextField.setText(((Integer) this.tokenSim.preferences.get("Time delay")).toString());
        this.prefPanel.updateIntervalFormattedTextField.setText(((Integer) this.tokenSim.preferences.get("Update interval")).toString());
        this.prefPanel.qEnablingCheckBox.setSelected((Boolean) this.tokenSim.preferences.get("Marking dependent rates"));
    }

    @Override
    protected void startSim() {
        this.tsPanel.stepField.setEnabled(true);
        this.tsPanel.fireTransitionsButton.setEnabled(true);
        this.tsPanel.firingRateButton.setEnabled(true);
        this.computeActiveTransitions();
    }

    @Override
    protected void endSim() {
        LOGGER.debug("Ending the stochastic token simulator");
        try{
            this.simSwingWorker.cancel(true);
        }
        catch(NullPointerException ex){
            LOGGER.error("Nullpointer exception while trying to cancel the simSwingWorker in the stochastic token simulator", ex);
        }
        this.tsPanel.stepField.setEnabled(false);
        this.tsPanel.fireTransitionsButton.setEnabled(false);
        this.tsPanel.continuousModeCheckBox.setEnabled(false);
        this.tsPanel.firingRateButton.setEnabled(false);
        this.tokenSim.tokenSimPanel.saveSetupButton.setEnabled(false);
        this.tokenSim.tokenSimPanel.loadSetupButton.setEnabled(false);
        this.tokenSim.lockGUI(true);
    }

    /**
     * This method calculates, what transition will fire next and when it will fire.
     * A random time of firing will be drawn for each enabled transition, according to its firing rate and enabling degree q.
     * Transition with shortest time will be fired. If multiple transitions have the same (shortest) firing time, choose randomly.
     * @return 
     */
    @Override
    protected Transition getTransitionToFire() {
        LOGGER.debug("Getting all transitions that will fire next in the stochastic token simulator");
        Set<Transition> activeTransitions = this.tokenSim.getActiveTransitions();
        /*
         * calculate the next firing time for every active transition
         */
        for (Transition t : activeTransitions){
            this.updateNextFiringTime(t);
        }
        /*
         * Find a transition with the lowest firing time. Go through all active transitions
         * and check the next firing time for every transition. If a transition with lower firing time than the actual transition's
         * firing time is found, put it in the transitionsToFire - Set after clearing the set. If a transition is found with the
         * same firing time as the actual transition, put in in the transitionsToFire - set. This collects all transitions with the same (lowest)
         * firing time in an set. Then choose randomly which transition from transitionsToFire.
         */
        LOGGER.debug("Search through all transitions and choose those, that have the lowest firing time");
        Set<Transition> transitionsToFire = new HashSet<>();
        //Add any transition to the transitionsToFire-set and save its firing time.
        Transition firstT = activeTransitions.iterator().next();
        double lowestTime = nextFiringTime.get(firstT).get(0);
        transitionsToFire.add(firstT);
        for (Transition t : activeTransitions){
            /*
            If the next transition has the same firing time as the transitions in transitionsToFire-set, add it to the set.
            */
            double nextTransTime = nextFiringTime.get(t).get(0);
            if (nextTransTime == lowestTime){
                transitionsToFire.add(t);
            }
            else{
                /*
                If the next transition has lower next firing time as transitions in transitionsToFire set,
                clear the set and add next transition to it. Update the lowest time.
                */
                if (nextTransTime < lowestTime){
                    transitionsToFire.clear();
                    transitionsToFire.add(t);
                    lowestTime = nextTransTime;
                }
            }
        }
        /*
         * Draw random transition out of transitionsToFire.
         * Update passed time.
         */
        Transition chosenT = transitionsToFire.toArray(new Transition[transitionsToFire.size()])[random.nextInt(transitionsToFire.size())];
        time += this.nextFiringTime.get(chosenT).get(0);
        return chosenT;
    }
    
    /**
     * Open a frame where user can input firing rates for transitions.
     */
    void setFiringRates(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tokenSim.netViewer.displayMenu(new SetFiringRatesPanel(tokenSim.netViewer, petriNet, firingRates), "Transition firing rates");
            }
        });
    }
    
    /**
     * Compute the next time interval in which transition t will fire.
     * The time intervals are distributed according to the exponential distribution,
     * the firing rate of the transition and the enabling-degree q. The enabling-degree
     * is an integer which describes how many times the transition could fire at once according to the current
     * marking.
     * @param t Transition that becomes enabled
     */
   void updateNextFiringTime(Transition t){
        /*
         * Calculate the enabling-degree of the transition t.
         * For every pre-place p of t, divide the number of tokens on p through the
         * weight of the arc between p and t; the minimal value is the q-value
         */
        //Enabling-degree q of the transition t.
        LOGGER.debug("Updating the next firing time for a given transition");
        long q;
        
        if ((Boolean) this.tokenSim.preferences.get("Marking dependent rates")){
            /*
            Preplaces of transition t.
            */
            List<Place> prePlaces = this.petriNet.getInputPlacesFor(t);
            /*
             * If the transition has no pre-places, i.e. is an input transition, let q be 1
             */
            if (prePlaces.isEmpty()){
                q = 1;
            }
            else{
                q = Long.MAX_VALUE;
                for (Place prePlace : prePlaces){
                    long tmp = this.getTokens(prePlace.id()) / this.petriNet.getArc(prePlace, t).weight();
                    q = (q < tmp ? q : tmp);
                }
            }
        }
        /*
         * if marking-dependent firing-rate is disabled, let q be 1
         */
        else
            q = 1;
        
        /*
         * generate a random number r in the interval [0,1]
         */
        double r = this.random.nextDouble();
        /*
         * calculate the next firing time time. time = -(ln(1-r)/(q*firingRate))
         */
        double nextTime = -(Math.log(1 - r) / (q * this.firingRates.get(t.id())));
        /*
         * put the calculated next firing time for transition t in the nextFiringTime-Map.
         * Because of resampling-method, the next firing time is indipendent from the enabling-time,
         * put the next firing time on the first place in the array.
         */
        this.nextFiringTime.get(t).add(0, nextTime);
    }
    
    /**
     * Start firing sequence.
     */
    protected void startFiring(){
        //lock GUI, so the user cannot interrupt the firing sequence by alterating the settings.
        this.tsPanel.stepField.setEnabled(false);
        this.tsPanel.continuousModeCheckBox.setEnabled(false);
        this.tsPanel.firingRateButton.setEnabled(false);
        //tells the token simulator to lock the GUI, too.
        this.tokenSim.lockGUI(true);

        //try to parse number of steps to perform from stepField. If no integer is entered, create a warning popup and do nothing
        try{
            //number of steps that will be performed
            int steps = Integer.parseInt(tsPanel.stepField.getText());
            if (steps < 1) {
                steps = 1;
                tsPanel.stepField.setText("1");
            }
            //Create new thread that will perform all firing steps.
            simSwingWorker = new SimulationSwingWorker(steps);
            simSwingWorker.execute();
        }        
        catch(NumberFormatException nfe){
            LOGGER.error("NumberFormatException while trying to start firing, therefore stopping the firing", nfe);
            stopFiring();
            JOptionPane.showMessageDialog(null, TokenSimulator.strings.get("TSNumberFormatExceptionM"));
        }
    }
    
    /**
     * Stop actual firing sequence.
     */
    protected void stopFiring(){
        LOGGER.debug("Stopping the current firing sequence");
        if (this.simSwingWorker != null){
            this.simSwingWorker.stopSequence();
        }
    }

    @Override
    protected void exportSetup() {
        /*
         * Ask user where he wants to have the setup file and how to name it.
         */
        LOGGER.info("Exporting the setup of the stochastic token simulator");
        File outFile;
        MonaLisaFileChooser fc = new MonaLisaFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION){
            return;
        }
        outFile = fc.getSelectedFile();
        try {
            /*
             * Create a XML-document
             */
            LOGGER.debug("Creating a .XML-document and the needed builders");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            //Create root element and append it to the document.
            Element root = doc.createElement("SimulationSetup");
            doc.appendChild(root);
            
            //Create element for places and append it to the root.
            Element placesEl = doc.createElement("places");
            root.appendChild(placesEl);
            
            //Iterate through all places and create an element for each one.
            LOGGER.debug("Iterating through all places and putting the data into the builders");
            for (Place place : this.petriNet.places()){
                //Create place element and append it to places element
                Element placeEl = doc.createElement("place");
                placesEl.appendChild(placeEl);
                //Attributes - id, name, contant/not constant, nr. of tokens.
                placeEl.setAttribute("id", String.valueOf(place.id()));
                placeEl.setAttribute("name", (String) place.getProperty("name"));
                placeEl.setAttribute("isConstant", String.valueOf(place.isConstant()));
                //if the place is non-constant, store the number of tokens on this place. Otherwise, store the corresponding
                //mathematical expression
                if (!place.isConstant()){
                    placeEl.setAttribute("nrOfTokens", String.valueOf(this.tokenSim.getMarking().get(place.id())));
                }
                else{
                    //Create a mathematical expression element for the place
                    Element mathExpressionEl = doc.createElement("mathematicalExpression");
                    placeEl.appendChild(mathExpressionEl);
                    MathematicalExpression placeMathExp = this.tokenSim.getConstantPlaces().get(place.id());
                    //Text of the expression
                    String expText = placeMathExp.toString();
                    Element expTextEl = doc.createElement("expressionText");
                    expTextEl.appendChild(doc.createTextNode(expText));
                    mathExpressionEl.appendChild(expTextEl);
                    /*
                     * Iterate through variables and create an element for each one.
                     */
                    Map<String, Integer> variables = placeMathExp.getVariables();
                    for (String variable : variables.keySet()){
                        Element varEl = doc.createElement("variable");
                        mathExpressionEl.appendChild(varEl);
                        varEl.setAttribute("name", variable);
                        varEl.setAttribute("placeId", String.valueOf(variables.get(variable)));
                    }
                }
            }
            
            //Create an element for transitions and append it to the root.
            Element transitionsEl = doc.createElement("transitions");
            root.appendChild(transitionsEl);
            
            //Iterate through all transitions and create an element for each one.
            LOGGER.debug("Iterating through all transitions and putting the data into the builders");
            for (Transition transition : this.petriNet.transitions()){
                //Create a transition element.
                Element transitionEl = doc.createElement("transition");
                transitionsEl.appendChild(transitionEl);
                //Attributes - id, name, firing rate.
                transitionEl.setAttribute("id", String.valueOf(transition.id()));
                transitionEl.setAttribute("name", (String) transition.getProperty("name"));
                transitionEl.setAttribute("firingRate", String.valueOf(firingRates.get(transition.id())));
            }
            
            //Write the document into a file.
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outFile);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException ex) {
            LOGGER.error("ParserConfigurationException or TransformerException while trying to create an export file for the stochastic token sim");
        }
    }

    @Override
    protected void importSetup() {
        LOGGER.info("Imporing the setup for the stochastic token simulator");
        try {
            /*
             * Get the setup file.
             */
            LOGGER.debug("Creating the needed file, factory and parsers for extracting the information out of the input file");
            File inFile;
            MonaLisaFileChooser fc = new MonaLisaFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if(fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION){
                return;
            }
            inFile = fc.getSelectedFile();
            //Create a SAX-parser.
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader parser = factory.createXMLStreamReader( new FileInputStream(inFile));

            int id = 0;
            String name = "";
            boolean placeConstant = false;
            Long placeTokens = 0L;
            MathematicalExpression mathExp = null;
            Map<String, Integer> variables = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            LOGGER.debug("Parsing through the input file");
            while (parser.hasNext()){
                switch ( parser.getEventType() ){
                    case XMLStreamConstants.START_DOCUMENT:
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        parser.close();
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        switch (parser.getLocalName()){
                            case "place":
                                //iterate through attributes
                                for (int i = 0; i < parser.getAttributeCount(); i++){
                                    switch (parser.getAttributeLocalName(i)){
                                        case "id":
                                            id = Integer.parseInt(parser.getAttributeValue(i));
                                            break;
                                        case "isConstant":
                                            placeConstant = Boolean.parseBoolean(parser.getAttributeValue(i));
                                            break;
                                        case "name":
                                            name = parser.getAttributeValue(i);
                                            break;
                                        case "nrOfTokens":
                                            placeTokens = Long.parseLong(parser.getAttributeValue(i));
                                            break;
                                    }
                                }
                                //find a place with the id.
                                Place p = petriNet.findPlace(id);
                                if (p == null){
                                    break;
                                }
                                //set the properties of the place.
                                tokenSim.setPlaceConstant(id, placeConstant);
                                if (!placeConstant){
                                    tokenSim.setTokens(id, placeTokens);
                                }
                                break;
                            //If a "transition"-element is found, try to apply the settings to a transitino in Petri net.
                            case "transition":
                                double firingRate = 0;
                                for (int i = 0; i < parser.getAttributeCount(); i++){
                                    switch (parser.getAttributeLocalName(i)){
                                        case "id":
                                            id = Integer.parseInt(parser.getAttributeValue(i));
                                            break;
                                        case "name":
                                            name = parser.getAttributeValue(i);
                                            break;
                                        case "firingRate":
                                            firingRate = Double.parseDouble(parser.getAttributeValue(i));
                                            break;
                                    }
                                }
                                //find a transition with the id.
                                Transition t = petriNet.findTransition(id);
                                if (t == null){
                                    break;
                                }
                                //set the properties of the transition
                                firingRates.put(id, firingRate);
                                break;
                            case "mathematicalExpression":
                                sb = new StringBuilder();
                                variables.clear();
                                break;
                            case "variable":
                                String varName = "";
                                String placeId = "";
                                for (int i = 0; i < parser.getAttributeCount(); i++){
                                    switch (parser.getAttributeLocalName(i)){
                                        case "name":
                                            varName = parser.getAttributeValue(i);
                                            break;
                                        case "placeId":
                                            placeId = parser.getAttributeValue(i);
                                            break;
                                    }
                                }
                                variables.put(varName, Integer.parseInt(placeId));
                                break;
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if (!parser.isWhiteSpace()){
                            sb.append(parser.getText());
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        switch (parser.getLocalName()){
                            case "mathematicalExpression":
                                mathExp = new MathematicalExpression(sb.toString(), variables);
                                sb = new StringBuilder();
                                break;
                            case "place":
                                if (petriNet.findPlace(id) == null){
                                    break;
                                }
                                if (placeConstant){
                                    tokenSim.setMathExpression(id, mathExp);
                                }
                                break;
                        }
                        break;
                    default:
                        break;
                }
                parser.next();
            }
            
        } catch (FileNotFoundException | XMLStreamException ex) {
            JOptionPane.showMessageDialog(null, "Invalid XML file!",
                    TokenSimulator.strings.get("Error"), JOptionPane.ERROR_MESSAGE);
            LOGGER.error("Importfile was not found or the .XML-Stream was interrupted unexpectedly", ex);
        } catch (Exception ex) {
            LOGGER.error("General exception while trying to import a setupfile");
        }
    }

    /**
     * Simulated time is the number of steps.
     * @return Number of simulated steps.
     */
    @Override
    public double getSimulatedTime() {
        return this.time;
    }
}