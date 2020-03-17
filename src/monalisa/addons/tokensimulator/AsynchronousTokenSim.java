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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
 * This simulation mode allows to fire selected amount of steps with selected delay between firings.
 * In each step only one active transition is fired, i.e. the firing of transitions is asynchronous.
 * @author Pavel Balazki
 */
public class AsynchronousTokenSim extends AbstractTokenSim {
    //BEGIN VARIABLES DECLARATION
    //GUI
    private AsynchronousTokenSimPanel tsPanel;
    private AsynchronousTokenSimPrefPanel prefPanel;
    /**
     * Thread which executes simulation.
     */
    private SimulationSwingWorker simSwingWorker;
    private static final Logger LOGGER = LogManager.getLogger(AsynchronousTokenSim.class);
    //END VARIABLES DECLARATION
    
    //BEGIN INNER CLASSES
    /**
     * Thread which performs the simulation sequence.
     */
    private class SimulationSwingWorker extends SwingWorker{
        /**
         * Number of steps this thread should perform.
         */
        private int stepsLeft;
        /**
         * Indicates whether the simulation is still running. Can be set to "false" upon termination call.
         */
        private boolean isRunning;
        
        public SimulationSwingWorker(int nrOfStepsN){
            this.stepsLeft = nrOfStepsN;
        }
        @Override
        protected Void doInBackground(){
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
            Set<Transition> activeTransitions;

            /*
             * Perform firing until aborted.
             */
            while(isRunning){
                try {
                    activeTransitions = tokenSim.getActiveTransitions();
                    /*
                     * If no transitions are active, abort simulation.
                     */
                    if (activeTransitions.isEmpty() || this.isCancelled()){
                        LOGGER.info("Simulation stopped since no more transitions are active");
                        this.isRunning = false;
                        break;
                    }
                    /*
                     * Perform firing of transitions.
                     */
                    SwingUtilities.invokeAndWait(new Runnable(){
                        @Override
                        public void run() {
                            tokenSim.fireTransitions(getTransitionToFire());
                            //If updateInterval is positive, check whether visual output must be updated in current step.
                            if (updateInterval > 0){
                                if(tokenSim.getSimulatedSteps() % updateInterval == 0){
                                    tokenSim.updateVisualOutput();
                                }
                            }
                        }
                    });
                    
                    /*
                     * If continuous mode is not selected, reduce the number of steps left by one.
                     */
                    if (!tsPanel.continuousModeCheckBox.isSelected()){
                        tsPanel.progressBar.setValue(tsPanel.progressBar.getMaximum() - --stepsLeft);
                        /*
                         * Abort simulation if no more steps are left.
                         */
                        if (stepsLeft <= 0){
                            this.isRunning = false;
                        }
                    }
                    Thread.sleep(timeDelay);
                } catch (InterruptedException | InvocationTargetException ex) {
                    LOGGER.error("Background process got interrupted or broke because of an invalid target", ex);
                }
            }
            return null;
        }

        @Override
        protected void done(){
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
            tokenSim.lockGUI(false);
            tokenSim.flushLog();
        }

        /**
         * Signal to stop simulation.
         */
        public void stopSequence(){
            this.isRunning = false;
            //update visual output
            tokenSim.updateVisualOutput();
        }
    }
    //END INNER CLASSES
    
    //BEGIN CONSTRUCTORS    
    public AsynchronousTokenSim(TokenSimulator tsN){
        super(tsN);
    }
    //END CONSTRUCTORS
    
    /**
     * Create GUI and etc, so classes that extend this class can use it too.
     */
    @Override
    protected void init(){
        //START CREATING GUI ELEMENTS
        LOGGER.debug("GUI for Asynchronous Token Simulator initialized");
        this.tsPanel = new AsynchronousTokenSimPanel(this);
        this.prefPanel = new AsynchronousTokenSimPrefPanel(this);
        this.tsPanel.simName.setText(TokenSimulator.strings.get("ATSName"));
        //END CREATING GUI ELEMENTS
        this.tokenSim.preferences.put("Time delay", 0);
        this.tokenSim.preferences.put("Update interval", 1);
    }

    @Override
    protected JPanel getControlComponent() {        
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
        int timeDelay = Integer.parseInt(this.prefPanel.timeDelayJFormattedTextField.getText());
        if (timeDelay >= 0)
            this.tokenSim.preferences.put("Time delay", timeDelay);
        /*
         * Update update interval
         */
        int updateInterval = Integer.parseInt(this.prefPanel.updateIntervalFormattedTextField.getText());
        if (updateInterval >= 0){
            this.tokenSim.preferences.put("Update interval", updateInterval);
        }
    }

    @Override
    protected void loadPreferences() {
        this.prefPanel.timeDelayJFormattedTextField.setText(((Integer) this.tokenSim.preferences.get("Time delay")).toString());
        this.prefPanel.updateIntervalFormattedTextField.setText(((Integer) this.tokenSim.preferences.get("Update interval")).toString());
    }
    
    /**
     * Start firing sequence.
     */
    protected void startFiring(){
        //Lock GUI
        LOGGER.info("Firing of Asynchronous Token Simulator started");
        this.tsPanel.stepField.setEnabled(false);
        this.tsPanel.continuousModeCheckBox.setEnabled(false);
        this.tokenSim.lockGUI(true);

        //Try to parse the number of steps to perform from stepField. If no integer is entered, create a warning popup and do nothing.
        try{
            //number of steps that will be performed
            int steps = Integer.parseInt(tsPanel.stepField.getText());
            if (steps < 1){
                steps = 1;
                tsPanel.stepField.setText("1");
            }
            //Create new thread that will perform all firing steps.
            simSwingWorker = new SimulationSwingWorker(steps);
            simSwingWorker.execute();
        } 
        catch(NumberFormatException nfe){
            stopFiring();
            LOGGER.error("NumberFormatException while checking the number of firing steps in the asynchronous token simulator", nfe);
            JOptionPane.showMessageDialog(null, TokenSimulator.strings.get("TSNumberFormatExceptionM"));
        }
    } 
    
    /**
     * Stop actual firing sequence.
     */
    protected void stopFiring(){
        if (this.simSwingWorker != null){
            this.simSwingWorker.stopSequence();
        }
    }
    
    /**
     * The transition to fire will be chosen randomly from all active transitions.
     * @return randomly chosen active transition.
     */
    @Override
    protected Transition getTransitionToFire() {
        Transition[] activeTransitions = this.tokenSim.getActiveTransitions().toArray(new Transition[0]);
        int transitionIndex = random.nextInt(activeTransitions.length);
        LOGGER.debug("Random transition to fire has been chosen in asynchronous token simulator");
        return activeTransitions[transitionIndex];
    }
    
    @Override
    protected void startSim(){
        LOGGER.info("Asynchronous token simulation started");
        this.tsPanel.stepField.setEnabled(true);
        this.tsPanel.fireTransitionsButton.setEnabled(true);
        this.computeActiveTransitions();  
    }
    
    @Override
    protected void endSim(){
        try{
            this.simSwingWorker.cancel(true);
        }
        catch(NullPointerException ex){
            LOGGER.error("Nullpointer exception while trying to cancel simSwingWorker in asynchronous token simulator", ex);
        }
        this.tsPanel.stepField.setEnabled(false);
        this.tsPanel.fireTransitionsButton.setEnabled(false);
        this.tsPanel.continuousModeCheckBox.setEnabled(false);
        this.tokenSim.tokenSimPanel.saveSetupButton.setEnabled(false);
        this.tokenSim.tokenSimPanel.loadSetupButton.setEnabled(false);
        tokenSim.lockGUI(true);
        LOGGER.info("Asynchronous token simulator ended");
    }

    @Override
    protected void exportSetup() {
        /*
         * Ask user where he wants to have the setup file and how to name it.
         */
        LOGGER.debug("Export of setup started");
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
            LOGGER.debug("Creating a new Builder for Setupexport in the asynchronous token simulator");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            //Create root element and append it to the document.
            Element root = doc.createElement("SimulationSetup");
            doc.appendChild(root);
            
            //Create element for places and append it to the root.
            Element placesEl = doc.createElement("places");
            root.appendChild(placesEl);
            
            //Iterate through all places and create an element for each one.
            for (Place place : this.petriNet.places()){
                LOGGER.debug("New place is found and processed for export");
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
                    LOGGER.debug("Place is constant and gets the according properties");
                    placeEl.setAttribute("nrOfTokens", String.valueOf(this.tokenSim.getMarking().get(place.id())));
                }
                else{
                    LOGGER.debug("Creating a new mathematical expression element for this place since it is not constant");
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
            LOGGER.debug("Writing the created document into a file");
            //Write the document into a file.
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outFile);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException ex) {
            LOGGER.error("Parser or Transformer exception while handling the setupexport in the asynchronous token simulator", ex);
        }
    }
    
    @Override
    protected void importSetup() {
        LOGGER.info("Import of setup initiated");
        try {
            /*
             * Get the setup file.
             */
            File inFile;
            MonaLisaFileChooser fc = new MonaLisaFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if(fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION){
                return;
            }
            inFile = fc.getSelectedFile();
            LOGGER.info("File for import has been chosen, new factory and parser are initiated");
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
                                //only proceed if the Petri net has a place with such an ID and the same name
                                if (p == null){
                                    break;
                                }
                                //set the properties of the place.
                                tokenSim.setPlaceConstant(id, placeConstant);
                                if (!placeConstant){
                                    tokenSim.setTokens(id, placeTokens);
                                }
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
            LOGGER.error("File for import was not found or was invalid", ex);
        } catch (Exception ex) {
            LOGGER.error("General exception while trying to import .XML setup in the asynchronous token simulator", ex);
        }
    }
    
    @Override
    public double getSimulatedTime() {
        return this.tokenSim.getSimulatedSteps();
    }
}
