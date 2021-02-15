/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.data.input;

import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;

import javax.xml.stream.XMLStreamException;
import monalisa.addons.annotations.AnnotationUtils;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.DOMBuilder;
import org.sbml.jsbml.AbstractTreeNode;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.History;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.util.filters.Filter;
import org.sbml.jsbml.ext.layout.*;
import org.sbml.jsbml.ext.SBasePlugin;

/**
 * Input handler for the SBML format. This class parses a SBML-file. Supports
 * SBML all Versions and Levels specified in
 * <a href="http://sbml.org/Documents/Specifications">http://sbml.org/Documents/Specifications</a>
 *
 */
public final class SbmlInputHandler implements InputHandler {

    private final Map<Integer, Place> places = new HashMap<>();
    private final Map<Integer, Transition> transitions = new HashMap<>();
    private final Map<String, Integer> species = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(SbmlInputHandler.class);

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in SBML format");
        if ("sbml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            return true;
        }

        if ("xml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            SAXBuilder builder = new SAXBuilder();
            Document doc;
            try {
                doc = builder.build(file);
            } catch (JDOMException e) {
                LOGGER.error("Caught JDOMException while checking for SBML format: ", e);
                return false;
            } catch (IOException e) {
                LOGGER.error("Caught IOException while checking for SBML format: ", e);
                return false;
            }

            Element root = doc.getRootElement();
            if (!root.getName().equals("sbml")) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PetriNet load(InputStream in, File file) throws IOException {
        LOGGER.info("Loading Petri net from SBML file");
        places.clear();
        transitions.clear();
        species.clear();

        PetriNet petriNet = new PetriNet();

        SBMLReader reader = new SBMLReader();
        SBMLDocument doc = null;

        List<CVTerm> tmp;

        try {
            doc = reader.readSBMLFromStream(in);
        } catch (XMLStreamException ex) {
            LOGGER.error("Caught XMLStreamException while parsing XML file: ", ex);
        }

        doc.removeAllTreeNodeChangeListeners(true);
        Model model = doc.getModel();
        SBasePlugin mplugin = null;
        Layout layout = null;
        Boolean hasLayout = false;

        if (model.isSetPlugin(LayoutConstants.getNamespaceURI(3, 1))) {
            mplugin = model.getExtension(LayoutConstants.getNamespaceURI(3, 1));
            LayoutModelPlugin layplugin = (LayoutModelPlugin) mplugin;
            layout = layplugin.getLayout(0);

            //check if the layout is not empty  
            if (layout.getReactionGlyphCount() != 0 && layout.getSpeciesGlyphCount() != 0) {
                hasLayout = true;
            }
        }

        model.removeAllTreeNodeChangeListeners(true);

        model.getListOfTreeNodeChangeListeners().clear();

        History history = model.getHistory();

        /*History history = model.getHistory();
        /*List<Creator> creators = history.getListOfCreators();
        for(int i = 0; i < history.getCreatorCount(); i++) {
            history.removeCreator(i);
        }
        for(Creator c : creators) {;
            history.addCreator(c);
        }*/
        petriNet.putProperty(AnnotationUtils.HISTORY, history);
        petriNet.putProperty(AnnotationUtils.MODEL_NAME, model.getName());

        if (model.getCVTermCount() > 0) {
            tmp = model.getCVTerms();
            petriNet.putProperty(AnnotationUtils.MIRIAM_MODEL_QUALIFIERS, tmp);
        }

        Map<org.sbml.jsbml.Compartment, monalisa.data.pn.Compartment> compartmentMap = new HashMap<>();
        if (!model.getListOfCompartments().isEmpty()) {
            for (org.sbml.jsbml.Compartment c : model.getListOfCompartments()) {
                monalisa.data.pn.Compartment pnComp = new monalisa.data.pn.Compartment(c.getName());
                pnComp.putProperty("size", c.getSize());
                pnComp.putProperty("constant", c.getConstant());
                pnComp.putProperty("spatialDimensions", c.getSpatialDimensions());
                pnComp.putProperty(AnnotationUtils.SBO_TERM, c.getSBOTermID());
                tmp = c.getCVTerms();
                pnComp.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, tmp);
                petriNet.addCompartment(pnComp);
                compartmentMap.put(c, pnComp);
            }
        }

        Double weight;
        int countPlaces = 0, countTransitions = 0;
        Long tokens;
        String name, id, reactantName, productName, compartmentId;
        double posX = 0, posY = 0;
        Boolean reversible;
        Place place;
        Transition transition, transition_rev = null;

        for (Species s : model.getListOfSpecies()) {
            name = s.getName();
            id = s.getId();

            if (hasLayout) {
                posX = layout.getSpeciesGlyph("SG" + id).getBoundingBox().getPosition().getX();
                posY = layout.getSpeciesGlyph("SG" + id).getBoundingBox().getPosition().getY();
            }

            if (name.equals("")) {
                name = id;
            }
            tokens = new Double(s.getInitialAmount()).longValue();
            place = findPlace(countPlaces, petriNet);
            place.putProperty("name", name);
            if (hasLayout) {
                place.putProperty("posX", posX);
                place.putProperty("posY", posY);
            }

            if (!s.getSBOTermID().isEmpty()) {
                place.putProperty(AnnotationUtils.SBO_TERM, s.getSBOTermID());
            } else {
                place.putProperty(AnnotationUtils.SBO_TERM, "SBO:0000000");
            }
            tmp = s.getCVTerms();
            place.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, tmp);

            if (s.getCompartmentInstance() != null) {
                petriNet.setCompartment(place, compartmentMap.get(s.getCompartmentInstance()));
            }

            petriNet.setTokens(place, tokens);
            species.put(id, countPlaces);
            countPlaces++;
        }

        boolean doubleName = false;
        for (Place p1 : petriNet.places()) {
            for (Place p2 : petriNet.places()) {
                if (p1.equals(p2)) {
                    continue;
                }

                if (p1.getProperty("name").equals(p2.getProperty("name"))) {
                    if (p2.getCompartment() != null) {
                        doubleName = true;
                        p2.putProperty("name", p2.getProperty("name") + "_" + p2.getCompartment().getName());
                    }
                }
            }
            if (doubleName) {
                if (p1.getCompartment() != null) {
                    p1.putProperty("name", p1.getProperty("name") + "_" + p1.getCompartment().getName());
                    doubleName = false;
                }
            }
        }

        // Transition data section.
        // List of output and input arcs.
        Boolean floatWeighs;
        int multiplikator, subLen, indexOfPoint;
        String weighString;

        for (Reaction r : model.getListOfReactions()) {
            name = r.getName();
            id = r.getId();

            if (hasLayout) {
                posX = layout.getReactionGlyph("RG" + id).getBoundingBox().getPosition().getX();
                posY = layout.getReactionGlyph("RG" + id).getBoundingBox().getPosition().getY();
            }

            if (name.equals("")) {
                name = id;
            }
            reversible = r.getReversible();
            floatWeighs = false;
            multiplikator = 1;

            transition = findTransition(countTransitions, petriNet);
            transition.putProperty("name", name);

            if (hasLayout) {
                transition.putProperty("posX", posX);
                transition.putProperty("posY", posY);
            }

            if (r.getCompartmentInstance() != null) {
                petriNet.setCompartment(transition, compartmentMap.get(r.getCompartmentInstance()));
            }

            if (!r.getSBOTermID().isEmpty()) {
                transition.putProperty(AnnotationUtils.SBO_TERM, r.getSBOTermID());
            } else {
                transition.putProperty(AnnotationUtils.SBO_TERM, "SBO:0000000");
            }
            tmp = r.getCVTerms();
            transition.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, tmp);

            if (reversible) {
                countTransitions++;
                transition_rev = findTransition(countTransitions, petriNet);
                transition_rev.putProperty("name", name + "_rev");

                if (r.getCompartmentInstance() != null) {
                    petriNet.setCompartment(transition_rev, compartmentMap.get(r.getCompartmentInstance()));
                }

                if (!r.getSBOTermID().isEmpty()) {
                    transition_rev.putProperty(AnnotationUtils.SBO_TERM, r.getSBOTermID());
                } else {
                    transition_rev.putProperty(AnnotationUtils.SBO_TERM, "SBO:0000000");
                }
                tmp = r.getCVTerms();
                transition_rev.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, tmp);
            }

            for (SpeciesReference spr : r.getListOfReactants()) {
                weighString = Double.toString(spr.getStoichiometry());
                indexOfPoint = weighString.indexOf(".");
                subLen = weighString.substring(indexOfPoint + 1, weighString.length()).length();
                if (subLen > 1 || !weighString.substring(indexOfPoint + 1, weighString.length()).equals("0")) {
                    floatWeighs = true;
                    if (subLen > multiplikator) {
                        multiplikator = subLen;
                    }
                }
            }

            for (SpeciesReference spr : r.getListOfProducts()) {
                weighString = Double.toString(spr.getStoichiometry());
                indexOfPoint = weighString.indexOf(".");
                subLen = weighString.substring(indexOfPoint + 1, weighString.length()).length();
                if (subLen > 1 || !weighString.substring(indexOfPoint + 1, weighString.length()).equals("0")) {
                    floatWeighs = true;
                    if (subLen > multiplikator) {
                        multiplikator = subLen;
                    }
                }
            }

            multiplikator = (int) Math.pow(10.0, (double) multiplikator);
            Object source, aim;
            // input arcs: (place = reactant) --> transition
            for (SpeciesReference spr : r.getListOfReactants()) {

                reactantName = spr.getSpecies();
                weight = spr.getStoichiometry();

                if (floatWeighs) {
                    weight *= multiplikator;
                }

                source = findPlace(species.get(reactantName), petriNet);
                aim = transition;

                petriNet.addArc((Place) source, (Transition) aim, new Arc(source, aim, weight.intValue()));

                if (reversible) {
                    source = transition_rev;
                    aim = findPlace(species.get(reactantName), petriNet);
                    petriNet.addArc((Transition) source, (Place) aim, new Arc(aim, source, weight.intValue()));
                }
            }
            // output arcs: transition --> (place = product)
            for (SpeciesReference spr : r.getListOfProducts()) {
                productName = spr.getSpecies();
                weight = spr.getStoichiometry();

                if (floatWeighs) {
                    weight *= multiplikator;
                }

                source = transition;
                aim = findPlace(species.get(productName), petriNet);

                petriNet.addArc((Transition) source, (Place) aim, new Arc(source, aim, weight.intValue()));

                if (reversible) {
                    source = findPlace(species.get(productName), petriNet);
                    aim = transition_rev;
                    petriNet.addArc((Place) source, (Transition) aim, new Arc(aim, source, weight.intValue()));
                }
            }
            // Modifier
            for (ModifierSpeciesReference msr : r.getListOfModifiers()) {
                weight = 1.0;
                productName = msr.getSpecies();

                source = transition;
                aim = findPlace(species.get(productName), petriNet);
                petriNet.addArc((Transition) source, (Place) aim, new Arc(source, aim, weight.intValue()));
                petriNet.addArc((Place) aim, (Transition) source, new Arc(source, aim, weight.intValue()));
            }

            countTransitions++;
        }

        doubleName = false;
        for (Transition t1 : petriNet.transitions()) {
            for (Transition t2 : petriNet.transitions()) {
                if (t1.equals(t2)) {
                    continue;
                }

                if (t1.getProperty("name").equals(t2.getProperty("name"))) {
                    if (t2.getCompartment() != null) {
                        doubleName = true;
                        t2.putProperty("name", t2.getProperty("name") + "_" + t2.getCompartment().getName());
                    }
                }
            }
            if (doubleName) {
                if (t1.getCompartment() != null) {
                    t1.putProperty("name", t1.getProperty("name") + "_" + t1.getCompartment().getName());
                    doubleName = false;
                }
            }
        }

        doc.filter(new Filter() {
            @Override
            public boolean accepts(Object o) {
                if (o instanceof AbstractTreeNode && ((AbstractTreeNode) o).getTreeNodeChangeListenerCount() > 0) {
                    System.out.println(o.getClass().getSimpleName() + " has still some TreeNodeChangeListeners (" + ((AbstractTreeNode) o).getTreeNodeChangeListenerCount() + ")");
                }

                return false;
            }
        });
        
        
        LOGGER.info("Successfully loaded Petri net from SBML file");
        return petriNet;
    }

    public static void layoutImport(File layoutFile, NetViewer netViewer, PetriNet petriNet) {
        try {
            //building the document
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document layoutdoc = dBuilder.parse(layoutFile);
            Document document = new DOMBuilder().build(layoutdoc);
            Element rootNode = document.getRootElement();
            //going through the information nodes
            List<Element> topicNodes = rootNode.getChildren();
            //mapping all vertices  to their name for convenience and speed
            Collection<NetViewerNode> allVertices = netViewer.getAllVertices();
            HashMap<String, NetViewerNode> nameMap = new HashMap<>();
            for (NetViewerNode node : allVertices) {
                nameMap.put(node.getName(), node);
            }
            //logical places
            List<Element> logPlaceNodes = topicNodes.get(0).getChildren();
            for (Element logPlace : logPlaceNodes) {
                List<Element> logPlaceInfo = logPlace.getChildren();
                String masterNodeString = logPlaceInfo.get(0).getValue();
                List<String> connectedNodes = new ArrayList<>();
                for (Element connectedNode : logPlaceInfo.get(1).getChildren()) {
                    if (!connectedNode.getValue().contains("BendNodeID")) {
                        connectedNodes.add(connectedNode.getValue());
                    }
                }
                for (Element connectedNode : logPlaceInfo.get(2).getChildren()) {
                    if (!connectedNode.getValue().contains("BendNodeID")) {
                        connectedNodes.add(connectedNode.getValue());
                    }
                }
                List<NetViewerNode> connectedNetViewerNodes = new ArrayList<>();
                for (String nodeName : connectedNodes) {
                    connectedNetViewerNodes.add(nameMap.get(nodeName));
                }
                Double posX = Double.parseDouble(logPlaceInfo.get(3).getValue());
                Double posY = Double.parseDouble(logPlaceInfo.get(4).getValue());
                netViewer.addLogicalPlace(nameMap.get(masterNodeString), connectedNetViewerNodes, new Point2D.Double(posX, posY));
                nameMap.put(logPlace.getAttributeValue("Name"), nameMap.get(masterNodeString).getLogicalPlaces().get(nameMap.get(masterNodeString).getLogicalPlaces().size() - 1));
            }
            //Bend edges
            //beginning to build tasks for multiple bends
            List<Element> edgeBends = topicNodes.get(1).getChildren();
            ArrayList<String> doneBends = new ArrayList<>();
            ArrayList<ArrayList<String>> taskList = new ArrayList<ArrayList<String>>();
            HashMap<String, Element> bendElementMap = new HashMap<>();
            for (Element edgeBend : edgeBends){
                bendElementMap.put(edgeBend.getAttributeValue("Name"), edgeBend);
            }
            while (!edgeBends.isEmpty()) {
                Iterator<Element> iterator = edgeBends.listIterator();
                while (iterator.hasNext()) {
                    Element currEdgeBend = iterator.next();
                    String incomingName = currEdgeBend.getChildren().get(2).getValue();
                    String outgoingName = currEdgeBend.getChildren().get(3).getValue();
                    if (nameMap.containsKey(incomingName)) {
                        ArrayList<String> tempList = new ArrayList<String>();
                        tempList.add(incomingName);
                        tempList.add(currEdgeBend.getAttributeValue("Name"));
                        if (!outgoingName.contains("BendNodeID")) {
                            tempList.add(outgoingName);
                        }
                        taskList.add(tempList);
                        iterator.remove();
                        continue;
                    }
                    for (ArrayList<String> insideList : taskList) {
                        if (incomingName.equals(insideList.get(insideList.size() - 1))) {
                            insideList.add(currEdgeBend.getAttributeValue("Name"));
                            if (!outgoingName.contains("BendNodeID")) {
                                insideList.add(outgoingName);
                            }
                            iterator.remove();
                            break;
                        }
                    }
                }
            }            
            for (ArrayList<String> task : taskList){
                while (task.size() > 2){
                    NetViewerEdge oldEdge = netViewer.g.findEdge(nameMap.get(task.get(0)), nameMap.get(task.get(task.size() - 1)));
                    Element currBend = bendElementMap.get(task.get(1));
                    NetViewerEdge newEdge = netViewer.addBend(oldEdge, Double.valueOf(currBend.getChildren().get(0).getValue()) - 30, Double.valueOf(currBend.getChildren().get(1).getValue()) - 30);
                    nameMap.put(task.get(1), newEdge.getSource());
                    task.remove(0);
                }
            }
            //NodeInformation
            List<Element> nodeInformation = topicNodes.get(2).getChildren();
            for (Element currNodeElement : nodeInformation){
                NetViewerNode currNode = nameMap.get(currNodeElement.getAttributeValue("Name"));
                //LabelPosition
                if (currNodeElement.getChildren().get(0).getValue().equals("SE")){
                    currNode.setLabelPosition(Position.SE);
                } else if (currNodeElement.getChildren().get(0).getValue().equals("S")){
                    currNode.setLabelPosition(Position.S);
                } else if (currNodeElement.getChildren().get(0).getValue().equals("SW")){
                    currNode.setLabelPosition(Position.SW);
                } else if (currNodeElement.getChildren().get(0).getValue().equals("W")){
                    currNode.setLabelPosition(Position.W);
                } else if (currNodeElement.getChildren().get(0).getValue().equals("NW")){
                    currNode.setLabelPosition(Position.NW);
                } else if (currNodeElement.getChildren().get(0).getValue().equals("N")){
                    currNode.setLabelPosition(Position.N);
                } else if (currNodeElement.getChildren().get(0).getValue().equals("NE")){
                    currNode.setLabelPosition(Position.NE);
                } else if (currNodeElement.getChildren().get(0).getValue().equals("E")){
                    currNode.setLabelPosition(Position.E);
                } else if (currNodeElement.getChildren().get(0).getValue().equals("CNTR")){
                    currNode.setLabelPosition(Position.CNTR);
                }
                //color
                Integer indexRed = currNodeElement.getChildren().get(1).getValue().indexOf("r=") + 2;
                Integer indexGreen = currNodeElement.getChildren().get(1).getValue().indexOf("g=") + 2;
                Integer indexBlue = currNodeElement.getChildren().get(1).getValue().indexOf("b=") + 2;
                Integer redEnd = currNodeElement.getChildren().get(1).getValue().indexOf(",", currNodeElement.getChildren().get(1).getValue().indexOf("r=") + 1);
                Integer greenEnd = currNodeElement.getChildren().get(1).getValue().indexOf(",", currNodeElement.getChildren().get(1).getValue().indexOf("g=") + 1);
                Integer blueEnd = currNodeElement.getChildren().get(1).getValue().length() - 1;
                Integer red = Integer.parseInt(currNodeElement.getChildren().get(1).getValue().substring(indexRed, redEnd));
                Integer green = Integer.parseInt(currNodeElement.getChildren().get(1).getValue().substring(indexGreen, greenEnd));
                Integer blue = Integer.parseInt(currNodeElement.getChildren().get(1).getValue().substring(indexBlue, blueEnd));
                Color color = new Color(red, green, blue);
                currNode.setColor(color);
                //strokecolor
                Integer indexRed2 = currNodeElement.getChildren().get(3).getValue().indexOf("r=") + 2;
                Integer indexGreen2 = currNodeElement.getChildren().get(3).getValue().indexOf("g=") + 2;
                Integer indexBlue2 = currNodeElement.getChildren().get(3).getValue().indexOf("b=") + 2;
                Integer redEnd2 = currNodeElement.getChildren().get(3).getValue().indexOf(",", currNodeElement.getChildren().get(3).getValue().indexOf("r=") + 1);
                Integer greenEnd2 = currNodeElement.getChildren().get(3).getValue().indexOf(",", currNodeElement.getChildren().get(3).getValue().indexOf("g=") + 1);
                Integer blueEnd2 = currNodeElement.getChildren().get(3).getValue().length() - 1;
                Integer red2 = Integer.parseInt(currNodeElement.getChildren().get(3).getValue().substring(indexRed2, redEnd2));
                Integer green2 = Integer.parseInt(currNodeElement.getChildren().get(3).getValue().substring(indexGreen2, greenEnd2));
                Integer blue2 = Integer.parseInt(currNodeElement.getChildren().get(3).getValue().substring(indexBlue2, blueEnd2));
                Color color2 = new Color(red2, green2, blue2);
                currNode.setStrokeColor(color2);
                //corners
                currNode.setCorners(Integer.parseInt(currNodeElement.getChildren().get(2).getValue()));
            }
            //EdgeInformation
            List<Element> edgeInformation = topicNodes.get(3).getChildren();
            for (Element currEdgeElement : edgeInformation){
                NetViewerEdge currEdge = netViewer.g.findEdge(nameMap.get(currEdgeElement.getChildren().get(0).getValue()), nameMap.get(currEdgeElement.getChildren().get(1).getValue()));
                Integer indexRed3 = currEdgeElement.getChildren().get(2).getValue().indexOf("r=") + 2;
                Integer indexGreen3 = currEdgeElement.getChildren().get(2).getValue().indexOf("g=") + 2;
                Integer indexBlue3 = currEdgeElement.getChildren().get(2).getValue().indexOf("b=") + 2;
                Integer redEnd3 = currEdgeElement.getChildren().get(2).getValue().indexOf(",", currEdgeElement.getChildren().get(2).getValue().indexOf("r=") + 1);
                Integer greenEnd3 = currEdgeElement.getChildren().get(2).getValue().indexOf(",", currEdgeElement.getChildren().get(2).getValue().indexOf("g=") + 1);
                Integer blueEnd3 = currEdgeElement.getChildren().get(2).getValue().length() - 1;
                Integer red3 = Integer.parseInt(currEdgeElement.getChildren().get(2).getValue().substring(indexRed3, redEnd3));
                Integer green3 = Integer.parseInt(currEdgeElement.getChildren().get(2).getValue().substring(indexGreen3, greenEnd3));
                Integer blue3 = Integer.parseInt(currEdgeElement.getChildren().get(2).getValue().substring(indexBlue3, blueEnd3));
                Color color3 = new Color(red3, green3, blue3);
                currEdge.setColor(color3);
            }
            LOGGER.info("Successfully finished loading in additional data");
        } catch (Exception e) {
            LOGGER.error("Error while trying to load the layoutfile during SBML-Import", e);
        }
    }

    private Place findPlace(int placeId, PetriNet petriNet) {
        Place place = places.get(placeId);

        if (place == null) {
            LOGGER.debug("Creating new place with placeID '" + Integer.toString(placeId) + "'");
            place = new Place(placeId);
            places.put(placeId, place);
            petriNet.addPlace(place);
            LOGGER.debug("Successfully created new place with placeID '" + Integer.toString(placeId) + "'");
        }
        return place;
    }

    private Transition findTransition(int transitionId, PetriNet petriNet) {
        Transition transition = transitions.get(transitionId);

        if (transition == null) {
            LOGGER.debug("Creating new transition with transitionID '" + Integer.toString(transitionId) + "'");
            transition = new Transition(transitionId);
            transitions.put(transitionId, transition);
            petriNet.addTransition(transition);
            LOGGER.debug("Successfully created new transition with transitionID '" + Integer.toString(transitionId) + "'");
        }
        return transition;
    }

    @Override
    public String getDescription() {
        return "SBML (all Versions)";
    }

}
