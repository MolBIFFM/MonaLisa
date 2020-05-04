/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.synchronous;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.exceptions.PlaceConstantException;
import monalisa.addons.tokensimulator.exceptions.PlaceNonConstantException;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Pavel Balazki.
 */
public class SynchronousTokenSim extends AbstractTokenSim {

    //BEGIN VARIABLES DECLARATION
    /**
     * Thread which executes simulation.
     */
    private SynchronousSimulationSwingWorker simSwingWorker;
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SynchronousTokenSim.class);
    //END VARIABLES DECLARATION

    //BEGIN CONSTRUCTORS
    public SynchronousTokenSim(SimulationManager tsN) {
        super(tsN);
    }
    //END CONSTRUCTORS

    /**
     * Initializes preferences for synchronous simulation.
     */
    @Override
    protected void init() {
        LOGGER.info("Initiating the preferences for the synchronous simulation");
        this.getSimulationMan().getPreferences().put("Time delay", 0);
        this.getSimulationMan().getPreferences().put("Update interval", 1);
        this.getSimulationMan().getPreferences().put("Fire at once", 100);
        //by default, all transitions will fire at once
    }

    /**
     * Compute what transitions will fire in next step. This will check every
     * active transitions. Is a transitions is not concurrent with other
     * transitions, i.e. it does not share a pre-place with other transitions,
     * it will fire. If a transitions shares a pre-place with another
     * transition, and the token number on the pre-place is insufficient to fire
     * both concurrent transitions, decide randomly which transitions will fire.
     *
     * @return
     */
    public Set<Transition> getTransitionsToFire() {
        LOGGER.debug("Calculating the Transitions that shall fire out of the active Transitions");
        /*
        this ArrayList saves all active transitions that can be fired synchron,
        i.e. no firing of a transition of this list can disable another transition in the list.
         */
        Set<Transition> outSet = new HashSet<>();
        ArrayList<Transition> activeTransitions = new ArrayList<>(getSimulationMan().getActiveTransitions());
        /*
         * compute how many transitions can be shoot at once, depending on percentage set
         */
        int maxTransitions = (int) Math.round((activeTransitions.size() * 0.01 * (int) this.getSimulationMan().getPreferences().get("Fire at once")));
        /*
         * for every transition in activeTransitions, check whether it shares any pre-place with other transitions
         */
        while (!activeTransitions.isEmpty() && (outSet.size() < maxTransitions)) {
            //randomly choose a transition from activeTransitions list.
            int i = getRandom().nextInt(activeTransitions.size());
            Transition t = activeTransitions.remove(i);
            //Transition t is active and not chosen
            Boolean isActive = true;
            Boolean isChosen = false;

            //Get input places of the current transition t
            List<Place> inPlaces = t.inputs();

            //for each input place of the current transition, all active output transitions are concurrent
            for (Place p : inPlaces) {
                /*
                Get concurrent transitions of transition t.
                 */
                Set<Transition> concurrentTransitions = new HashSet<>(p.outputs());
                /*
                Remove all inactive transitions.
                 */
                concurrentTransitions.retainAll(activeTransitions);

                /*
                Iterator over the set of concurrentTransitions
                 */
                Iterator<Transition> it = concurrentTransitions.iterator();
                while (it.hasNext()) {
                    Transition concurrentT = it.next();
                    /*
                     * If pre-place p has enough tokens to fire both transitions t and concurrentT, remove concurrentT from
                     * concurrentTransitions. Add transition t to list of concurrent transitions of concurrentT for further checks
                     * 
                     */
 /*
                    Number of tokens on place p (for which t and concurrentT are concurrent).
                     */
                    long tokens = getTokens(p.id());
                    /*
                     * For every post-transition of the place p that is chosen to fire, reduce the amount of tokens of this place
                     * by the weight of the arc between the place p and the chosen transition
                     */
                    for (Transition placePosttransition : p.outputs()) {
                        if (outSet.contains(placePosttransition)) {
                            tokens -= this.getPetriNet().getArc(p, placePosttransition).weight();
                        }
                    }
                    /*
                    Check if both transitions can fire.
                     */
                    if ((tokens - this.getPetriNet().getArc(p, t).weight()) >= this.getPetriNet().getArc(p, concurrentT).weight()) {
                        it.remove();
                    }
                }
                /*
                if transition t is not concurrent with any other transition for this place, do nothing and check concurrency for other places. Otherwise
                decide whether transtion t will fire randomly.
                 */
                if (!concurrentTransitions.isEmpty()) {
                    /*
                    If the transition was already chosen to fire during checking concurrency
                    with previous places, remove all concurrent transitions from candidates to fire (activeTransitions).
                     */
                    if (isChosen) {
                        activeTransitions.removeAll(concurrentTransitions);
                    } else {
                        isActive = getRandom().nextBoolean();
                        //if the transition was chosen to be active, add it to transitionsToFire-list and remove all concurrent transitions
                        //from activeTransitions
                        if (isActive) {
                            isChosen = true;
                            outSet.add(t);
                            activeTransitions.removeAll(concurrentTransitions);
                        } //if the transition was chosen not to be active, remove it from activeTransitions and go on with next transition
                        else {
                            activeTransitions.remove(t);
                            break;
                        }
                    }
                }
            }
            if (isActive && !isChosen) {
                outSet.add(t);
            }
        }
        LOGGER.debug("Finished calculating the transitions to fire next in the synchronous simulation");
        return outSet;
    }

    /**
     * @return the simSwingWorker
     */
    public SynchronousSimulationSwingWorker getSimSwingWorker() {
        return simSwingWorker;
    }

    /**
     * @param simSwingWorker the simSwingWorker to set
     */
    public void setSimSwingWorker(SynchronousSimulationSwingWorker simSwingWorker) {
        this.simSwingWorker = simSwingWorker;
    }

    @Override
    public Transition getTransitionToFire() {
        throw new UnsupportedOperationException("Not supported for SynchronousTokenSim. Use getTransitionsToFire instead.");
    }

    @Override
    protected void exportSetup(File outFile) throws ParserConfigurationException, TransformerException {
        /*
         * Ask user where he wants to have the setup file and how to name it.
         */
        LOGGER.info("Exporting the setup of the synchronous token simulator");
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
}
