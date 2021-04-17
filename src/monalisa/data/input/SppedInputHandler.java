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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.xpath.XPath;
import org.jdom2.xpath.jaxen.JDOMXPath;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.data.pn.UniquePetriNetEntity;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SppedInputHandler implements InputHandler {

    private final Map<Integer, Place> places = new HashMap<>();
    private final Map<Integer, Transition> transitions = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(SppedInputHandler.class);

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in spped format");
        return "spped".equalsIgnoreCase(FileUtils.getExtension(file));
    }

    @Override
    public PetriNet load(InputStream in, File file) throws IOException {
        LOGGER.info("Loading Petri net from spped file");
        places.clear();
        transitions.clear();

        SAXBuilder builder = new SAXBuilder();
        Document doc;

        try {
            doc = builder.build(in);
        } catch (JDOMException e) {
            LOGGER.error("Failed to parse the XML file", e);
            throw new IOException("Failed to parse the XML file.", e);
        }

        Element root = doc.getRootElement();

        final String expectedVersion = "2";

        if (!"Snoopy".equals(root.getName())
                || !expectedVersion.equals(root.getAttributeValue("version"))) {
            LOGGER.error("spped file has wrong format/version");
            throw new IOException("spped file has wrong format/version.");
        }

        try {
            PetriNet petriNet = parse(doc);
            LOGGER.info("Successfully loaded Petri net from spped file");
            return petriNet;
        } catch (JDOMException e) {
            LOGGER.error("Issue while parsing Petri net from spped file: ", e);
            throw new IOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private PetriNet parse(Document doc) throws JDOMException {
        LOGGER.debug("Parsing Petri net from spped file");
        PetriNet petriNet = new PetriNet();
        petriNet.putProperty("importtype", "speed");

        Map<Integer, UniquePetriNetEntity> entities = new HashMap<>();
        final String nodePath = "/Snoopy/nodeclasses/nodeclass[@name='%s']/node";
        final XPath placesPath = new JDOMXPath(String.format(nodePath, "Place"));
        final XPath coarsePlacesPath = new JDOMXPath(String.format(nodePath, "Coarse Place"));
        final XPath transitionPath = new JDOMXPath(String.format(nodePath, "Transition"));
        final XPath edgePath = new JDOMXPath("/Snoopy/edgeclasses/edgeclass[@name='Edge']/edge");

        petriNet.putProperty("name", doc.getRootElement().getChild("netclass").getAttributeValue("name"));

        // Start: Places
        int countPlaces = 0, id, internalId;
        String name;
        Place place;
        Long marking;

        // Are there coarse places? If, than read them and store them
        boolean coarsePlaceExists = false;
        List<Element> coPlaces = (List<Element>) coarsePlacesPath.selectNodes(doc);
        Map<String, Place> coarsePlacesMap = new HashMap<>();
        if (coPlaces.size() > 0) {
            coarsePlaceExists = true;
            for (Element coarsePlace : coPlaces) {
                internalId = Integer.parseInt(coarsePlace.getAttributeValue("id"));

                name = getAttributeValue(coarsePlace, "Name");

                if (name == null || name.isEmpty()) {
                    name = Integer.toString(internalId);
                }

                place = new Place(countPlaces);
                place.putProperty("name", name);
                place.putProperty("internalId", internalId);
                place.putProperty("net", new Integer(coarsePlace.getAttributeValue("net")));
                place.putProperty("posX", new Double(((Element) coarsePlace.getChild("graphics").getChildren().toArray()[0]).getAttributeValue("x")));
                place.putProperty("posY", new Double(((Element) coarsePlace.getChild("graphics").getChildren().toArray()[0]).getAttributeValue("y")));

                coarsePlacesMap.put(coarsePlace.getAttributeValue("coarse"), place);
                entities.put(internalId, place);
                countPlaces++;
            }
        }

        List<Element> placeNodes = (List<Element>) placesPath.selectNodes(doc);
        List<String> workedCoarsePlaces = new ArrayList<>();
        Boolean logical;
        for (Element placeNode : placeNodes) {
            id = Integer.parseInt(getAttributeValue(placeNode, "ID"));
            internalId = Integer.parseInt(placeNode.getAttributeValue("id"));

            name = getAttributeValue(placeNode, "Name");

            if (placeNode.getAttributeValue("logic") == null) {
                logical = false;
            } else {
                logical = true;
            }

            if (name == null || name.isEmpty()) {
                name = Integer.toString(id);
            }

            String net = placeNode.getAttributeValue("net");
            // If there are coarse places, find the main place and add them
            if (coarsePlaceExists && !net.equalsIgnoreCase("1") && coarsePlacesMap.containsKey(net)) {
                place = coarsePlacesMap.get(net);
                // but only onbe times
                if (!workedCoarsePlaces.contains(net)) {
                    places.put(place.id(), place);
                    petriNet.addPlace(place);
                    workedCoarsePlaces.add(net);
                }
            } else {
                // otherwise, handle it as standard place
                place = findPlace(countPlaces, petriNet);
                place.putProperty("name", name);
                place.putProperty("posX", new Double(((Element) placeNode.getChild("graphics").getChildren().toArray()[0]).getAttributeValue("x")));
                place.putProperty("posY", new Double(((Element) placeNode.getChild("graphics").getChildren().toArray()[0]).getAttributeValue("y")));
                place.putProperty("internalId", internalId);
                place.putProperty("net", new Integer(net));
            }

            marking = Long.parseLong(getAttributeValue(placeNode, "Marking"));

            if (!logical) {
                place.putProperty("logical", false);
            } else {
                place.putProperty("logical", true);
            }

            Object[] children = placeNode.getChild("graphics").getChildren().toArray();
            List<String> graphicalRepresentations = new ArrayList<>();
            Element e;
            for (int i = 1; i < children.length; i++) {
                e = (Element) children[i];
                graphicalRepresentations.add(e.getAttributeValue("x") + "|" + e.getAttributeValue("y") + "|" + e.getAttributeValue("id") + "|" + e.getAttributeValue("net"));
            }
            place.putProperty("graphicalRepresentations", graphicalRepresentations);

            petriNet.setTokens(place, marking);

            entities.put(internalId, place);
            countPlaces++;
        }

        // Start: Transitions
        List<Element> transitionNodes = (List<Element>) transitionPath.selectNodes(doc);
        Transition transition;
        int countTransitions = 0;
        for (Element transitionNode : transitionNodes) {
            id = Integer.parseInt(getAttributeValue(transitionNode, "ID"));
            internalId = Integer.parseInt(transitionNode.getAttributeValue("id"));
            name = getAttributeValue(transitionNode, "Name");
            if (name == null || name.isEmpty()) {
                name = Integer.toString(id);
            }
            transition = findTransition(countTransitions, petriNet);
            transition.putProperty("name", name);
            transition.putProperty("net", new Integer(transitionNode.getAttributeValue("net")));
            transition.putProperty("posX", new Double(((Element) transitionNode.getChild("graphics").getChildren().toArray()[0]).getAttributeValue("x")));
            transition.putProperty("posY", new Double(((Element) transitionNode.getChild("graphics").getChildren().toArray()[0]).getAttributeValue("y")));
            transition.putProperty("internalId", internalId);
            petriNet.addTransition(transition);
            entities.put(internalId, transition);
            countTransitions++;
        }

        // Start: Edges
        List<Element> edgeNodes = (List<Element>) edgePath.selectNodes(doc);
        List<Element> pointsElements;
        List<String> points;
        for (Element edgeNode : edgeNodes) {
            int fromId = Integer.parseInt(edgeNode.getAttributeValue("source"));
            int toId = Integer.parseInt(edgeNode.getAttributeValue("target"));
            int weight = Integer.parseInt(getAttributeValue(edgeNode, "Multiplicity"));
            Arc arc = new Arc(entities.get(fromId), entities.get(toId), weight);

            arc.putProperty("source_internal", new Integer(fromId).toString());
            arc.putProperty("target_internal", new Integer(toId).toString());

            Element arcElement = (Element) ((Element) edgeNode.getChild("graphics")).getChildren().toArray()[0];
            arc.putProperty("source_graphic", arcElement.getAttributeValue("source"));
            arc.putProperty("target_source_graphic", arcElement.getAttributeValue("target"));

            pointsElements = arcElement.getChild("points").getChildren();
            points = new ArrayList<>();
            for (Element e : pointsElements) {
                points.add(e.getAttributeValue("x") + "|" + e.getAttributeValue("y"));
            }
            arc.putProperty("points", points);

            UniquePetriNetEntity from = entities.get(fromId);
            if (from instanceof Place) {
                Arc oldArc = petriNet.getArc((Place) from, (Transition) entities.get(toId));
                if (oldArc != null) {
                    oldArc.setWeight(oldArc.weight() + 1);
                } else {
                    petriNet.addArc((Place) from, (Transition) entities.get(toId), arc);
                }
            } else {
                Arc oldArc = petriNet.getArc((Transition) from, (Place) entities.get(toId));
                if (oldArc != null) {
                    oldArc.setWeight(oldArc.weight() + 1);
                } else {
                    petriNet.addArc((Transition) from, (Place) entities.get(toId), arc);
                }
            }
        }
        LOGGER.debug("Successfully parsed Petri net from spped file");
        return petriNet;
    }

    private String getAttributeValue(Element node, String attribute) throws JDOMException {
        XPath selector = new JDOMXPath("attribute[@name='" + attribute + "']");
        return ((Element) selector.selectSingleNode(node)).getTextTrim();
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
        return "Spped (Snoopy)";
    }

}
