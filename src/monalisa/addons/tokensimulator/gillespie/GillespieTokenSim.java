/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.gillespie;

import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import monalisa.addons.tokensimulator.utils.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import monalisa.addons.tokensimulator.exceptions.FactorialTooBigException;
import monalisa.addons.tokensimulator.exceptions.PlaceConstantException;
import monalisa.addons.tokensimulator.exceptions.PlaceNonConstantException;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A stochastic simulator of Petri nets based on Gillespie's algorithm for
 * stochastic simulation of coupled chemical reactions.
 *
 * @author Pavel Balazki.
 */
public class GillespieTokenSim extends AbstractTokenSim {

    //BEGIN VARIABLES DECLARATION
    //GUI
    //JPanel with custom controls of the simulator.
    //JPanel with preferences of simulator.
    /**
     * Thread which executes simulation.
     */
    private GillespieSimulationSwingWorker simSwingWorker;
    /**
     * Time passed since the begin of simulation.
     */
    private double time = 0.0;
    /**
     * Stochastic reaction rate constants, which are the firing rates of the
     * stochastic PN.
     */
    private Map<Integer, Double> firingRates = new HashMap<>();
    /**
     * This HashMap links the marking dependent firing rate to the corresponding
     * transitions ID. The keys are the IDs of the transitions, which are
     * integers; the values are the marking dependent firing rates, which are
     * doubles. A marking dependent firing rate is a product of the stochastic
     * reaction rate constant, which is calculated from the deterministic
     * reaction rate constant using protected Double convertKToC(Transition t,
     * Double k, double volume), and the number if distinct molecular reactant
     * combinations available in current state, calculated by
     * calculateH(Transition t).
     */
    private Map<Integer, Double> markingDependentRates = new HashMap<>();
    /**
     * Order of reactions. Key is the transition id, value is an array with the
     * reaction order on the first place and the multiplier (used for
     * calculating stochastic reaction rate constant) on the second.
     */
    protected Map<Integer, long[]> reactionOrder = new HashMap<>();
    /**
     * Number of distinct molecular reactant combinations available in current
     * state for the chemical reaction represented by transition with a key-id.
     */
    private Map<Integer, Long> distinctCombinations = new HashMap<>();
    /**
     * This HashMap links the deterministic reaction rate constant to the
     * corresponding chemical reaction. Unit: M, sec.
     */
    protected Map<Integer, MathematicalExpression> deterministicReactionConstants;
    /**
     * Volume of simulated system.
     */
    protected double volume = 1E-9;
    /**
     * Volume multiplied with the Avogadro constant.
     */
    protected double volMol = volume * 6E23;
    /**
     * Number of total simulation threads in all fast modes together.
     */
    private int nrOfRunningThreads = 0;

    private static final Logger LOGGER = LogManager.getLogger(GillespieTokenSim.class);
    //END VARIABLES DECLARATION

    //BEGIN CONSTRUCTORS
    /**
     * Creates a new instance of the stochastic token simulator.
     *
     * @param tsN
     */
    public GillespieTokenSim(SimulationManager tsN) {
        super(tsN);
        /*
         * Initialize deterministic reaction rate constants with 1.
         */
        LOGGER.info("Gillespie Token Sim initiated");
        this.deterministicReactionConstants = new HashMap<>();
        for (Transition t : this.simulationMan.getPetriNet().transitions()) {
            /*
             * In reactions with order greater than or equal to 2, the reaction rate constant k is multiplied with the
             * factorial of each arc weight
             */
            long multiplier = 1;
            /*
             * Determine which order the reaction has.
             */
            LOGGER.debug("Determining the order of the reaction and order the reactions accordingly");
            long order = 0;
            int weight;
            for (Place p : this.petriNet.getInputPlacesFor(t)) {
                weight = this.petriNet.getArc(p, t).weight();
                try {
                    multiplier *= Utilities.factorial((long) weight);
                } catch (FactorialTooBigException ex) {
                    LOGGER.error("Factorial too big while trying to determine the order of the reaction in the gillespie simulation", ex);
                    try {
                        multiplier *= Utilities.factorial(20L);
                    } catch (FactorialTooBigException ex2) {
                        LOGGER.error("Factorial too big while trying to determine the order of the reaction in the gillespie simulation, even after trying to fix it internally", ex2);
                    }
                }
                order += weight;
            }
            long[] inf = {order, multiplier};
            this.reactionOrder.put(t.id(), inf);
            try {
                this.deterministicReactionConstants.put(t.id(), new MathematicalExpression("0.0"));
            } catch (RuntimeException ex) {
                LOGGER.error("Unknown function or unparsable Expression found while trying to put the current transition into the deterministic reaction constans in the gillespie simulation", ex);
            }
            this.firingRates.put(t.id(), 0.0);
        }
    }
    //END CONSTRUCTORS

    /**
     * Convert the deterministic rate constant k into the stochastic reaction
     * rate constant c for the given transition t. If the chemical reaction
     * which is represented by transition t has no identical reactant molecules,
     * the stochastic reaction rate constant is identical to the deterministic
     * reaction rate constant. For every x identical reactant molecules, the
     * stochastic reaction rate constant is x! times larger that the
     * deterministic reaction rate constant.
     *
     * @param t Transition with a deterministic reaction rate constant.
     * @param k Deterministic reaction rate constant.
     * @return Stochastic reaction rate constant for the transition t
     */
    protected double convertKToC(Transition t, double k) {
        LOGGER.debug("Converting the deterministic rate constant k into the stochastic reaction rate constanc c for transtition " + Integer.toString(t.id()));
        long order = this.reactionOrder.get(t.id())[0];
        long multiplier = this.reactionOrder.get(t.id())[1];
        /*
         * zero order reaction
         */
        if (order == 0) {
            return volMol * k;
        }
        /*
         * first order reaction
         */
        if (order == 1) {
            return k;
        }
        /*
         * second order reaction
         */
        if (order == 2) {
            return (k * multiplier) / volMol;
        }

        /*
         * third or greater order reaction
         */
        double volMolPow = 1;
        for (int i = 0; i < order - 1; i++) {
            volMolPow *= volMol;
        }
        return (k * multiplier) / volMolPow;
    }

    /**
     * Convert the deterministic the stochastic reaction rate constant c into
     * the rate constant k for the given transition t.
     *
     * @param t Transition with a deterministic reaction rate constant.
     * @param Stochastic reaction rate constant
     * @return Deterministic rate constant for the transition t
     */
    protected double convertCToK(Transition t, double c) {
        LOGGER.debug("Converting the stochastic rate constant c into the deterministic reaction rate constanc k for transtition " + Integer.toString(t.id()));
        long order = this.reactionOrder.get(t.id())[0];
        long multiplier = this.reactionOrder.get(t.id())[1];

        /*
         * zero order reaction
         */
        if (order == 0) {
            return c / volMol;
        }
        /*
         * first order reaction
         */
        if (order == 1) {
            return c;
        }
        /*
         * second order reaction
         */
        if (order == 2) {
            return (c * volMol) / multiplier;
        }

        /*
         * third or greater order reaction
         */
        double volMolPow = 1;
        for (int i = 0; i < order - 1; i++) {
            volMolPow *= volMol;
        }
        return (c * volMolPow) / multiplier;
    }

    /**
     * Calculates the number of distinct molecular reactant combinations
     * available in current state for the chemical reaction represented by
     * transition t.
     *
     * @param t Transition representing the chemical reaction
     * @return Number of distinct t molecular reactant combinations available in
     * current state.
     */
    private long calculateH(Transition t) {
        //h is the number of combinations
        LOGGER.debug("Calculating the number of distinct molecular reactant combinations in the current state of the chemical reaction represented by the transition " + Integer.toString(t.id()));
        long h = 1;
        /*
         * For every species involved in the reaction, calculate the number of combinations for molecules of identical species.
         * For this, if multiple identical molecules (represented by an arc weight > 1) are involved,
         * the number of combinations is a binomial coefficient "n choose k" with n = number of
         * the molecules in current state (number of tokens on the place in current marking) and
         * k = weight of the arc from the pre-place to the transition.
         * Multiply the number of combinations for identical species with other species' combination count.
         */
        long tokens;
        int weight;
        for (Place p : this.getPetriNet().getInputPlacesFor(t)) {
            tokens = this.getTokens(p.id());
            weight = this.getPetriNet().getArc(p, t).weight();
            h *= Utilities.binomialCoefficient(tokens, (long) weight);
        }
        return h;
    }

    /**
     * The init()-function is called in the super-constructor of the
     * AbstractTokenSim, which is called in the constructor of
     * StochasticTokenSim. A JPanel with controls will be created, this panel in
     * coded in StochasticTokenSimPanel. This panel will be given to the
     * NetViewer and will be built in the control-toolbar.
     */
    @Override
    protected void init() {
        //START CREATING GUI ELEMENTS
        /*
         * create new GUI-Panel
         */
        LOGGER.info("GUI-Panel for Gillespie Simulation initiated");
        /*
         * Set the name of the Stochastic simulator as a text for the JLabel simName
         */
        //END CREATING GUI ELEMENTS
        this.getSimulationMan().getPreferences().put("Time delay", 0);
        this.getSimulationMan().getPreferences().put("Update interval", 1);
        this.getSimulationMan().getPreferences().put("LimitMaxThreads", false);
        this.getSimulationMan().getPreferences().put("MaxThreadsNr", Runtime.getRuntime().availableProcessors());

    }

    @Override
    protected void updatePreferences() {
    }

    @Override
    protected void loadPreferences() {
    }

    @Override
    protected void startSim() {
    }

    @Override
    protected void endSim() {
    }

    /**
     * This method calculates what transition will fire next and when it will
     * fire. A random time of firing will be drawn for each enabled transition,
     * according to its firing rate. Transition with shortest time will be
     * fired. If multiple transitions have the same (shortest) firing time,
     * choose randomly.
     *
     * @return Transition which will be fired in the next step.
     */
    @Override
    public Transition getTransitionToFire() {
        LOGGER.debug("Calculate the Transition that should fire next in the gillespie Simulation");
        /*
         * Calculate marking dependent rate for each transition in updateMarkingRates
         * For this, first transform the numbers of molecules of non-constant places to concentrations.
         */
        Map<Integer, Double> concentrations = new HashMap<>();
        for (Entry<Integer, Long> entr : this.getSimulationMan().getMarking().entrySet()) {
            concentrations.put(entr.getKey(), entr.getValue() / volMol);
        }
        /*
         * Evaluate concentrations for all constant places using non-constant concentrations as variables.
         */
        for (Entry<Integer, MathematicalExpression> entr : this.getSimulationMan().getConstantPlaces().entrySet()) {
            concentrations.put(entr.getKey(), entr.getValue().evaluateML(concentrations, this.time));
        }
        LOGGER.debug("Calculated the marking dependent rate for each transition and the concentrations for all constant places");
        /*
         * Sum of marking rates. It is used for calculating the probability the time point at which
         * next reaction will occur.
         */
        double markingRatesSum = 0.0;
        int tID;
        double detReactionRateConst;
        double stochReactionRateConst;
        LOGGER.debug("Search through all active Transitions and find the sum of marking dependent firing rate");
        for (Transition t : this.getSimulationMan().getActiveTransitions()) {
            LOGGER.info("Active transitions found in SimulationMan");
            //ID of the transition.
            tID = t.id();
            /*
             * Evaluate deterministic reaction rate constant
             */
            LOGGER.info(concentrations);
            detReactionRateConst = this.deterministicReactionConstants.get(tID).evaluateML(concentrations, this.time);
            LOGGER.info(deterministicReactionConstants.get(tID));
            LOGGER.info("DetReactionRateConst: " + detReactionRateConst);
            /*
             * Convert deterministic reaction rate constant to stochastic reaction rate constant.
             */
            stochReactionRateConst = this.convertKToC(t, detReactionRateConst);
            this.firingRates.put(tID, stochReactionRateConst);
            /*
             * Multiply the stochastic reaction rate constant with the number of distinct
             * molecular reactant combinations available in current state to get the actual rate of reaction.
             */
            LOGGER.info("StochReactionRateConst: " + stochReactionRateConst);
            LOGGER.info("distComb: " + this.distinctCombinations.get(tID));
            double reactionRate = stochReactionRateConst * this.distinctCombinations.get(tID);
            LOGGER.info("Reaction Rate to be added: " + reactionRate);
            this.markingDependentRates.put(tID, reactionRate);
            markingRatesSum += reactionRate;
        }
        //if the sum of marking dependent firing rate is zero, no reaction can occur and the simulation should stop.
        if (markingRatesSum == 0) {
            LOGGER.info("MARKING IS ZERO");
            return null;
        }
        /*
         * Generate two random doubles in the interval (0, 1)
         */
        double r1 = this.getRandom().nextDouble();
        double r2 = this.getRandom().nextDouble();
        /*
         * calculate the time at which next firing will occur.
         */
        LOGGER.debug("Calculating the time at which the next firing will occur");
        double nextFiringTime = -(Math.log(1 - r1) / markingRatesSum);
        /*
         * calculate which transition will fire next
         * transitionsIDs is a array of IDs which marking dependent rates are stored in markingRates
         * currentMarkingRatesSum is the sum of the marking dependent rates of the first i transitions. Initialize
         * currentMarkingRatesSum with the rate of the first transition.
         * i is the position of the current transition in the array of transitions
         */
        Integer[] transitionIDs = this.markingDependentRates.keySet().toArray(new Integer[this.markingDependentRates.size()]);
        int i = 0;
        double currentMarkingRatesSum = this.markingDependentRates.get(transitionIDs[i]);
        while (currentMarkingRatesSum < markingRatesSum * r2) {
            i++;
            currentMarkingRatesSum += this.markingDependentRates.get(transitionIDs[i]);
        }
        Transition transitionToFire = this.getPetriNet().findTransition(transitionIDs[i]);
        this.markingDependentRates.clear();
        /*
         * increase time by nextFiringTime
         */
        this.time += nextFiringTime;
        this.stepsSimulated++;
        LOGGER.debug("Finished calculating the next Transition to fire, it will be " + Integer.toString(transitionToFire.id()));
        return (transitionToFire);
    }

    @Override
    protected void exportSetup(File outFile) throws ParserConfigurationException, TransformerException {
        LOGGER.info("Exporting the setup of the gillespie token simulator");
        /*
         * Create an XML-document
         */
        LOGGER.debug("After getting the chosen directory, trying to create a .XML-File for the setup of the gillespie simulation");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        //Create root element and append it to the document.
        Element root = doc.createElement("SimulationSetup");
        doc.appendChild(root);

        //Create an element for the volume
        Element vol = doc.createElement("volume");
        root.appendChild(vol);
        vol.appendChild(doc.createTextNode(String.valueOf(this.volume)));

        //Create element for places and append it to the root.
        Element placesEl = doc.createElement("places");
        root.appendChild(placesEl);
        LOGGER.debug("Default tree-structure and builders created, filling the data for places in next");
        //Iterate through all places and create an element for each one.
        for (Place place : this.getPetriNet().places()) {
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
                placeEl.setAttribute("nrOfTokens", String.valueOf(this.getSimulationMan().getMarking().get(place.id())));
            } else {
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
                Element varEl;
                for (String variable : variables.keySet()) {
                    varEl = doc.createElement("variable");
                    mathExpressionEl.appendChild(varEl);
                    varEl.setAttribute("name", variable);
                    varEl.setAttribute("placeId", String.valueOf(variables.get(variable)));
                }
            }
        }
        LOGGER.debug("Places-Data has been successfully added in the tree-structure");

        //Create an element for transitions and append it to the root.
        Element transitionsEl = doc.createElement("transitions");
        root.appendChild(transitionsEl);

        //Iterate through all transitions and create an element for each one.
        Element transitionEl, detReactionConst, detExpTextEl, varEl;
        MathematicalExpression detConstMathExp;
        String detExpText;
        Map<String, Integer> variables = null;
        LOGGER.debug("Created the root transition element and filling in the data for the other transitions next");
        for (Transition transition : this.getPetriNet().transitions()) {
            //Create a transition element.
            transitionEl = doc.createElement("transition");
            transitionsEl.appendChild(transitionEl);
            //Attributes - id, name, firing rate.
            transitionEl.setAttribute("id", String.valueOf(transition.id()));
            transitionEl.setAttribute("name", (String) transition.getProperty("name"));
            transitionEl.setAttribute("firingRate", String.valueOf(firingRates.get(transition.id())));
            //Create a mathematical expression element for deterministic reaction rate constant
            detReactionConst = doc.createElement("detReactionRateConstant");
            transitionEl.appendChild(detReactionConst);
            detConstMathExp = this.deterministicReactionConstants.get(transition.id());
            //Text of the expression
            detExpText = detConstMathExp.toString();
            detExpTextEl = doc.createElement("expressionText");
            detExpTextEl.appendChild(doc.createTextNode(detExpText));
            detReactionConst.appendChild(detExpTextEl);
            /*
             * Iterate through variables and create an element for each one.
             */
            variables = detConstMathExp.getVariables();
            for (String variable : variables.keySet()) {
                varEl = doc.createElement("variable");
                detReactionConst.appendChild(varEl);
                varEl.setAttribute("name", variable);
                varEl.setAttribute("placeId", String.valueOf(variables.get(variable)));
            }
        }

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
        LOGGER.info("Importing the setup of the gillespie simulation");

        //Create a SAX-parser.
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = factory.createXMLStreamReader(new FileInputStream(inFile));
        LOGGER.debug("File for input setup has been found and the proper factory and parser is initiated");
        int id = 0;
        String name = "";
        boolean placeConstant = false;
        Long placeTokens = 0L;
        MathematicalExpression mathExp = null;
        Map<String, Integer> variables = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        LOGGER.debug("Parsing the .XML");
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
                        case "transition":
                            double firingRate = 0;
                            //iterate through attributes
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                switch (parser.getAttributeLocalName(i)) {
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
                            Transition transition = getPetriNet().findTransition(id);
                            //only proceed if the Petri net has a transition with such an ID and the same name
                            if (transition == null) {
                                break;
                            }
                            //set the properties of the transition
                            firingRates.put(id, firingRate);
                            break;
                        case "detReactionRateConstant":
                            variables.clear();
                            break;
                        case "mathematicalExpression":
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
                        case "volume":
                            volume = Double.parseDouble(sb.toString());
                            volMol = volume * 6E23;
                            sb = new StringBuilder();
                            break;
                        case "detReactionRateConstant":
                            if (getPetriNet().findTransition(id) == null) {
                                sb = new StringBuilder();
                                break;
                            }
                            mathExp = new MathematicalExpression(sb.toString(), variables);
                            sb = new StringBuilder();
                            deterministicReactionConstants.put(id, mathExp);
                            break;
                        case "mathematicalExpression":
                            if (getPetriNet().findPlace(id) == null) {
                                sb = new StringBuilder();
                                break;
                            }
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

    /**
     * Calculate number of distinct combinations of molecules first and perform
     * standard active transitions determinations then.
     */
    @Override
    public void computeActiveTransitions() {
        /*
        Update distinct molecular reactant combinations for transitions which pre-places marking has changed.
         */
        LOGGER.info("Calculating the active transitions according to their distinct molecular reactant combinations ");
        for (Transition t : this.transitionsToCheck) {
            this.distinctCombinations.put(t.id(), this.calculateH(t));
        }
        super.computeActiveTransitions();
    }

    /**
     * Return the number of tokens on the place.
     *
     * @param id ID of the place.
     * @return Number of tokens. For constant places mathematical expression is
     * evaluated first, using current marking of non-constant places, converted
     * to concentrations, and current simulated time.
     */
    @Override
    public long getTokens(int id) {
        if (!petriNet.findPlace(id).isConstant()) {
            return this.getSimulationMan().getMarking().get(id);
        } else {
            Map<Integer, Double> concentrations = new HashMap<>();
            for (Entry<Integer, Long> entr : this.getSimulationMan().getMarking().entrySet()) {
                concentrations.put(entr.getKey(), entr.getValue() / volMol);
            }
            MathematicalExpression mathExp = getSimulationMan().getMathematicalExpression(id);
            return Math.round(mathExp.evaluateML(concentrations, time) * volMol);
        }
    }

    /**
     * Simulated time in seconds.
     *
     * @return
     */
    @Override
    public double getSimulatedTime() {
        return this.time;
    }

    /**
     * Set the time which was simulated.
     *
     * @param time
     */
    public void setSimulatedTime(double time) {
        this.time = time;
    }

    /**
     * Check whether a fast simulation mode can start a new thread right now.
     *
     * @return true if no limit to the number of parallel threads is set or the
     * number of running threads is below the limit, false otherwise.
     */
    public boolean isNewThreadAllowed() {
        if ((boolean) this.getSimulationMan().getPreferences().get("LimitMaxThreads")) {
            if (this.nrOfRunningThreads >= (int) this.getSimulationMan().getPreferences().get("MaxThreadsNr")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Signalize that the new simulation thread of the fast mode has started.
     */
    public void registerNewThread() {
        this.nrOfRunningThreads++;
    }

    /**
     * Signalize that a simulation thread of the fast mode has stopped.
     */
    public void checkOutRunningThread() {
        this.nrOfRunningThreads--;
    }

    /**
     * @return the simSwingWorker
     */
    public GillespieSimulationSwingWorker getSimSwingWorker() {
        return simSwingWorker;
    }

    /**
     * @param simSwingWorker the simSwingWorker to set
     */
    public void setSimSwingWorker(GillespieSimulationSwingWorker simSwingWorker) {
        this.simSwingWorker = simSwingWorker;
    }

    protected void computeDistCombinations() {
        for (Transition t : getSimulationMan().getPetriNet().transitions()) {
            distinctCombinations.put(t.id(), calculateH(t));
        }
    }

    /**
     * @return the firingRates
     */
    protected Map<Integer, Double> getFiringRates() {
        return firingRates;
    }
}
