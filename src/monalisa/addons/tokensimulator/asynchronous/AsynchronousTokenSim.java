/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.asynchronous;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
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
import monalisa.addons.tokensimulator.AbstractTokenSim;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.exceptions.PlaceConstantException;
import monalisa.addons.tokensimulator.exceptions.PlaceNonConstantException;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This simulation mode allows to fire selected amount of steps with selected
 * delay between firings. In each step only one active transition is fired, i.e.
 * the firing of transitions is asynchronous.
 *
 * @author Pavel Balazki
 */
public class AsynchronousTokenSim extends AbstractTokenSim {

    //BEGIN VARIABLES DECLARATION
    //GUI
    /**
     * Thread which executes simulation.
     */
    private AsynchronousSimulationSwingWorker simSwingWorker;
    private static final Logger LOGGER = LogManager.getLogger(AsynchronousTokenSim.class);
    //END VARIABLES DECLARATION

    //BEGIN CONSTRUCTORS    
    public AsynchronousTokenSim(SimulationManager tsN) {
        super(tsN);
    }
    //END CONSTRUCTORS

    /**
     * Create GUI and etc, so classes that extend this class can use it too.
     */
    @Override
    protected void init() {
        LOGGER.debug("GUI for Asynchronous Token Simulator initialized");
        this.getSimulationMan().getPreferences().put("Time delay", 0);
        this.getSimulationMan().getPreferences().put("Update interval", 1);
    }

    /**
     * The transition to fire will be chosen randomly from all active
     * transitions.
     *
     * @return randomly chosen active transition.
     */
    @Override
    public Transition getTransitionToFire() {
        Transition[] activeTransitions = this.getSimulationMan().getActiveTransitions().toArray(new Transition[0]);
        int transitionIndex = getRandom().nextInt(activeTransitions.length);
        LOGGER.debug("Random transition to fire has been chosen in asynchronous token simulator");
        return activeTransitions[transitionIndex];
    }

    @Override
    protected void exportSetup(File outFile) throws ParserConfigurationException, TransformerException {
        /*
         * Ask user where he wants to have the setup file and how to name it.
         */
        LOGGER.info("Exporting the setup of the asynchronous token simulator");
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
        for (Place place : this.getPetriNet().places()) {
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
            if (!place.isConstant()) {
                LOGGER.debug("Place is constant and gets the according properties");
                placeEl.setAttribute("nrOfTokens", String.valueOf(this.getSimulationMan().getMarking().get(place.id())));
            } else {
                LOGGER.debug("Creating a new mathematical expression element for this place since it is not constant");
                //Create a mathematical expression element for the place
                Element mathExpressionEl = doc.createElement("mathematicalExpression");
                placeEl.appendChild(mathExpressionEl);
                MathematicalExpression placeMathExp = this.getSimulationMan().getConstantPlaces().get(place.id());
                //Text of the expression
                String expText = placeMathExp.toString();
                Element expTextEl = doc.createElement("expressionText");
                expTextEl.appendChild(doc.createTextNode(expText));
                mathExpressionEl.appendChild(expTextEl);
                /*
                 * Iterate through variables and create an element for each one.
                 */
                Map<String, Integer> variables = placeMathExp.getVariables();
                for (String variable : variables.keySet()) {
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
    }

    @Override
    protected void importSetup(File inFile) throws FileNotFoundException, XMLStreamException {
        LOGGER.info("File for import has been chosen, new factory and parser are initiated");
        //Create a SAX-parser.
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = factory.createXMLStreamReader(new FileInputStream(inFile));

        int id = 0;
        String name = "";
        boolean placeConstant = false;
        Long placeTokens = 0L;
        MathematicalExpression mathExp = null;
        Map<String, Integer> variables = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        while (parser.hasNext()) {
            switch (parser.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    parser.close();
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    switch (parser.getLocalName()) {
                        case "place":
                            //iterate through attributes
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                switch (parser.getAttributeLocalName(i)) {
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
                            Place p = getPetriNet().findPlace(id);
                            //only proceed if the Petri net has a place with such an ID and the same name
                            if (p == null) {
                                break;
                            }
                            //set the properties of the place.
                            getSimulationMan().setPlaceConstant(id, placeConstant);
                            if (!placeConstant) {
                                try {
                                    getSimulationMan().setTokens(id, placeTokens);
                                } catch (PlaceConstantException ex) {
                                    LOGGER.error("Error while setting tokens for places: ", ex);
                                }
                            }
                            break;
                        case "mathematicalExpression":
                            sb = new StringBuilder();
                            variables.clear();
                            break;
                        case "variable":
                            String varName = "";
                            String placeId = "";
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                switch (parser.getAttributeLocalName(i)) {
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
                    if (!parser.isWhiteSpace()) {
                        sb.append(parser.getText());
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    switch (parser.getLocalName()) {
                        case "mathematicalExpression":
                            mathExp = new MathematicalExpression(sb.toString(), variables);
                            sb = new StringBuilder();
                            break;
                        case "place":
                            if (getPetriNet().findPlace(id) == null) {
                                break;
                            }
                            if (placeConstant) {
                                try {
                                    getSimulationMan().setMathExpression(id, mathExp);
                                } catch (PlaceNonConstantException ex) {
                                    LOGGER.error("Error while setting mathematical expression for places: ", ex);
                                }
                            }
                            break;
                    }
                    break;
                default:
                    break;
            }
            parser.next();
        }
    }

    @Override
    public double getSimulatedTime() {
        return this.getSimulationMan().getSimulatedSteps();
    }

    /**
     * @return the simSwingWorker
     */
    public AsynchronousSimulationSwingWorker getSimSwingWorker() {
        return simSwingWorker;
    }

    /**
     * @param simSwingWorker the simSwingWorker to set
     */
    public void setSimSwingWorker(AsynchronousSimulationSwingWorker simSwingWorker) {
        this.simSwingWorker = simSwingWorker;
    }
}
