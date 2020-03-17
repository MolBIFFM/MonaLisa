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
import java.util.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Input handler for the Pipe3 PNML format.
 * @author Jens Einloft
 * @see <a href="http://pipe2.sourceforge.net/">Platform Independent Petri net Editor 3</a>
 **/

public final class Pipe3InputHandler implements InputHandler {
    private final Map<String, Place> places = new HashMap<>();
    private final Map<String, Transition> transitions = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(Pipe3InputHandler.class);

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in PIPE3 format");
        if (!"xml".equalsIgnoreCase(FileUtils.getExtension(file)))
            return false;

        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(file);
        } catch (JDOMException e) {
            LOGGER.error("Caught JDOMException while checking for PIPE3 format: ", e);
            return false;
        }
        Element root = doc.getRootElement();

        // Pipe 3 uses PNML but doesn't a proper net type. We assume P/T
        // networks.

        if (!root.getName().equals("pnml"))
            return false;
        Element netNode = root.getChild("net");
        if (netNode == null)
            return false;
        // Pipe2 or Pipe3?
        if (netNode.getChildren("tokenclass").isEmpty())
            return false;

        return "P/T net".equals(netNode.getAttributeValue("type"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public PetriNet load(InputStream in) throws IOException {
        LOGGER.info("Loading Petri net from PIPE3 file");
        PetriNet ret = new PetriNet();

        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(in);
        } catch (JDOMException ex) {
            LOGGER.error("Failed to parse XML file", ex);
        }

        // - pnml
        // -- net
        // --- (page) optional
        // ---- place
        // ---- transition
        // ---- arc

        List<Element> pnmlChildren = doc.getRootElement().getChildren();
        List<Element> workspace = null;

        // The <page> tag is optional
        if (pnmlChildren.get(0).getChildren().size() == 1) {
            workspace = pnmlChildren.get(0).getChildren();
        }  else {
            workspace = pnmlChildren;
        }

        // Net structure section
        int countPlaces = 0;
        int countTransitions = 0;
        String id,name;
        Long tokens;
        Double posX,posY;
        Object from, to;
        for(Element e : workspace){
            List<Element> elements = e.getChildren();
            for(Element pe : elements){
                // places
                switch (pe.getName()) {
                    case "place":
                        id = pe.getAttributeValue("id");
                        name = pe.getChild("name").getChild("value").getValue();
                        tokens = Long.parseLong(pe.getChild("initialMarking").getChild("value").getValue().split(",")[1]);
                        posX = new Double(pe.getChild("graphics").getChild("position").getAttributeValue("x"));
                        posY = new Double(pe.getChild("graphics").getChild("position").getAttributeValue("y"));
                        if (name == null)
                            name = id;
                        Place place = findPlace(countPlaces, id, ret);
                        place.putProperty("name", name);
                        place.putProperty("posX", posX);
                        place.putProperty("posY", posY);
                        ret.setTokens(place, tokens);
                        countPlaces++;
                        break;
                    case "transition":
                        id = pe.getAttributeValue("id");
                        name = pe.getChild("name").getChild("value").getValue();
                        posX = new Double(pe.getChild("graphics").getChild("position").getAttributeValue("x"));
                        posY = new Double(pe.getChild("graphics").getChild("position").getAttributeValue("y"));
                        if (name == null)
                            name = id;
                        Transition transition = findTransition(countTransitions, id, ret);
                        transition.putProperty("name", name);
                        transition.putProperty("posX", posX);
                        transition.putProperty("posY", posY);
                        countTransitions++;
                        break;
                    case "arc":
                        int weight = Integer.parseInt(pe.getChild("inscription").getChild("value").getValue().split(",")[1]);
                        String source = pe.getAttributeValue("source");
                        String target = pe.getAttributeValue("target");
                        if (places.containsKey(source)) {
                            from = places.get(source);
                            to = transitions.get(target);
                            ret.addArc((Place)from, (Transition)to, new Arc(from, to, weight));
                        } else if (transitions.containsKey(source))  {
                            from = transitions.get(source);
                            to = places.get(target);
                            ret.addArc((Transition)from, (Place)to, new Arc(from, to, weight));
                        }
                        break;
                }
            }
        }
        LOGGER.debug("Successfully loaded Petri net from PIPE 3 file");
        return ret;
    }

    private Place findPlace(int placeId, String stringId, PetriNet petriNet) {
        Place place = places.get(stringId);

        if (place == null) {
            LOGGER.debug("Creating new place with placeID '" + Integer.toString(placeId) + "'");
            place = new Place(placeId);
            places.put(stringId, place);
            petriNet.addPlace(place);
            LOGGER.debug("Successfully created new place with placeID '" + Integer.toString(placeId) + "'");
        }
        return place;
    }

    private Transition findTransition(int transitionId, String stringId, PetriNet petriNet) {
        Transition transition = transitions.get(stringId);

        if (transition == null) {
            LOGGER.debug("Creating new transition with transitionID '" + Integer.toString(transitionId) + "'");
            transition = new Transition(transitionId);
            transitions.put(stringId, transition);
            petriNet.addTransition(transition);
            LOGGER.debug("Successfully created new transition with transitionID '" + Integer.toString(transitionId) + "'");
        }
        return transition;
    }

    @Override
    public String getDescription() {
        return "Pipe3 (PNML)";
    }

}
