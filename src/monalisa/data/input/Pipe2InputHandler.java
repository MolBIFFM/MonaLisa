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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

// TODO Parse remaining information, in particular:
// * transition orientation
// * transition priority
// * arc path

/**
 * An input handler for the Pipe 2 PNML format.
 * @author Konrad Rudolph
 * @see <a href="http://pipe2.sourceforge.net/">Platform Independent Petri net Editor 2</a>
 */
public class Pipe2InputHandler implements InputHandler {

    private static final Logger LOGGER = LogManager.getLogger(Pipe2InputHandler.class);

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in PIPE2 format");
        if (!"xml".equalsIgnoreCase(FileUtils.getExtension(file)))
            return false;
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(file);
        } catch (JDOMException ex) {
            LOGGER.error("Caught JDOMException while checking for PIPE2 format: ", ex);
            return false;
        }
        Element root = doc.getRootElement();

        // Pipe 2 uses PNML but doesn't a proper net type. We assume P/T
        // networks.

        if (!root.getName().equals("pnml"))
            return false;
        Element netNode = root.getChild("net");
        if (netNode == null)
            return false;
        // Pipe3?
        if (!netNode.getChildren("token").isEmpty())
            return false;
        // Pipe4?
        if (!netNode.getChildren("tokenclass").isEmpty())
            return false;

        return "P/T net".equals(netNode.getAttributeValue("type"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public PetriNet load(InputStream in) throws IOException {
        LOGGER.info("Loading Petri net from PIPE2 file");
        PetriNet petriNet = new PetriNet();
        Map<Integer, Place> placeMap = new HashMap<>();
        Map<Integer, Transition> transitionMap = new HashMap<>();

        SAXBuilder builder = new SAXBuilder();
        Document doc;

        try {
            doc = builder.build(in);
        } catch (JDOMException e) {
            LOGGER.error("Failed to parse the XML file");
            throw new IOException("Failed to parse the XML file.", e);
        }
        Element root = doc.getRootElement();
        Element netNode = root.getChild("net");

        petriNet.putProperty("name", netNode.getAttributeValue("id"));

        List<Element> placeNodes = (List<Element>) netNode.getChildren("place");

        for (Element placeNode : placeNodes) {
            int id = getId(placeNode, "P");
            Place place = new Place(id);
            placeMap.put(id, place);
            place.putProperty("name", string(placeNode, "name", "value"));

            Element position = child(placeNode, "graphics", "position");
            float x = Float.parseFloat(position.getAttributeValue("x"));
            float y = Float.parseFloat(position.getAttributeValue("y"));
            place.putProperty("posX", x);
            place.putProperty("posY", y);

            String capacity = tryGetString(placeNode, "capacity", "value");

            if (capacity != null)
                place.putProperty("capacity", Integer.parseInt(capacity));

            petriNet.addPlace(place);
            Long tokens =
                Long.parseLong(string(placeNode, "initialMarking", "value"));
            petriNet.setTokens(place, tokens);
        }

        List<Element> transitionNodes =
            (List<Element>) netNode.getChildren("transition");

        for (Element transitionNode : transitionNodes) {
            int id = getId(transitionNode, "T");
            Transition transition = new Transition(id);
            transitionMap.put(id, transition);
            transition.putProperty("name", string(transitionNode, "name",
                "value"));

            Element position = child(transitionNode, "graphics", "position");
            float x = Float.parseFloat(position.getAttributeValue("x"));
            float y = Float.parseFloat(position.getAttributeValue("y"));
            transition.putProperty("posX", x);
            transition.putProperty("posY", y);
            transition.putProperty("rate", Float.parseFloat(string(
                transitionNode, "rate", "value")));
            transition.putProperty("isTimed", Boolean.parseBoolean(string(
                transitionNode, "timed", "value")));

            String priority = tryGetString(transitionNode, "priority", "value");

            if (priority != null)
                transition.putProperty("priority", Integer.parseInt(priority));

            petriNet.addTransition(transition);
        }

        List<Element> arcNodes = (List<Element>) netNode.getChildren("arc");
        int source, target, weight;
        Object from, to;
        for (Element arcNode : arcNodes) {
            boolean isInArc = arcNode.getAttributeValue("source").startsWith("P");
            if(isInArc) {
                source = getId(arcNode, "source", "P");
                target = getId(arcNode, "target", "T");
                weight = Integer.parseInt(string(arcNode, "inscription", "value"));
                from = placeMap.get(source);
                to = transitionMap.get(target);
            }
            else {
                source = getId(arcNode, "source", "T");
                target = getId(arcNode, "target", "P");
                weight = Integer.parseInt(string(arcNode, "inscription", "value"));
                from = transitionMap.get(source);
                to = placeMap.get(target);
            }
            Arc arc = new Arc(from, to, weight);

            Element type = arcNode.getChild("type");
            if (type != null && "inhibitor".equals(type.getAttribute("value")))
                arc.putProperty("isInhibitor", true);

            if (isInArc)
                petriNet.addArc(petriNet.findPlace(source),
                    petriNet.findTransition(target), arc);
            else
                petriNet.addArc(petriNet.findTransition(source),
                    petriNet.findPlace(target), arc);
        }
        LOGGER.info("Successfully loaded Petri net from PIPE2 file");
        return petriNet;
    }

    private static int getId(Element node, String prefix) {
        return getId(node, "id", prefix);
    }

    private static int getId(Element node, String attribute, String prefix) {
        return Integer.parseInt(node.getAttributeValue(attribute).replaceAll("^" + prefix, ""));
    }

    private static Element child(Element node, String... path) {
        for (int i = 0; i < path.length; i++)
            node = node.getChild(path[i]);
        return node;
    }

    private static String string(Element node, String... path) {
        if (path.length == 0)
            return node.getValue();
        for (int i = 0; i < path.length - 1; i++)
            node = node.getChild(path[i]);

        return node.getChildText(path[path.length - 1]);
    }

    private static String tryGetString(Element node, String... path) {
        if (path.length == 0)
            return node.getValue();
        for (int i = 0; i < path.length - 1; i++) {
            Element child = node.getChild(path[i]);
            if (child == null)
                return null;
            else
                node = child;
        }

        return node.getChildText(path[path.length - 1]);
    }

    @Override
    public String getDescription() {
        return "Pipe2 (PNML)";
    }
}
