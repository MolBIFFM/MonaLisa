/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.data.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import monalisa.addons.netviewer.NetViewer;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Jens Einloft
 */
public class Pipe4OutputHandler implements OutputHandler {

    private static final Logger LOGGER = LogManager.getLogger(Pipe4OutputHandler.class);

    public void save(FileOutputStream fileOutputStream, PetriNet pn, File file, NetViewer netViewer) {
        LOGGER.info("Exporting Petri net to PIPE4 format");
        Double minX = 0.0, minY = 0.0, x, y;
        for (Place p : pn.places()) {
            x = (Double) p.getProperty("posX");
            y = (Double) p.getProperty("posY");

            if (x < minX) {
                minX = x;
            }
            if (y < minY) {
                minY = y;
            }
        }

        for (Transition t : pn.transitions()) {
            x = (Double) t.getProperty("posX");
            y = (Double) t.getProperty("posY");

            if (x < minX) {
                minX = x;
            }
            if (y < minY) {
                minY = y;
            }
        }

        Double corectX = Math.abs(minX) + 10.0;
        Double corectY = Math.abs(minY) + 10.0;

        Element root = new Element("pnml");

        Element net = new Element("net");
        net.setAttribute("id", "Net-One");
        net.setAttribute("type", "P/T net");

        Element tokenclass = new Element("token");
        tokenclass.setAttribute("id", "Default");
        tokenclass.setAttribute("enabled", "true");
        tokenclass.setAttribute("red", "0");
        tokenclass.setAttribute("green", "0");
        tokenclass.setAttribute("blue", "0");
        net.addContent(tokenclass);

        Element place = null, graphicsMain = null, position = null, graphicsSub = null,
                name = null, initialMarking = null, capacity = null, value = null, offset = null;
        String placeName;
        List<Element> arcList = new ArrayList<>();
        for (Place p : pn.places()) {
            place = new Element("place");

            place.setAttribute("id", "P" + p.id());

            graphicsMain = new Element("graphics");
            position = new Element("position");
            x = ((Double) p.getProperty("posX")) + corectX;
            y = ((Double) p.getProperty("posY")) + corectY;
            position.setAttribute("x", "" + x);
            position.setAttribute("y", "" + y);
            graphicsMain.addContent(position);

            place.addContent(graphicsMain);

            name = new Element("name");
            value = new Element("value");
            placeName = (String) p.getProperty("name");
            value.addContent(placeName);
            name.addContent(value);
            graphicsSub = new Element("graphics");
            offset = new Element("offset");
            offset.setAttribute("x", "0");
            offset.setAttribute("y", "0");
            graphicsSub.addContent(offset);
            name.addContent(graphicsSub);

            place.addContent(name);

            initialMarking = new Element("initialMarking");
            value = new Element("value");
            value.addContent("Default," + 0);
            initialMarking.addContent(value);
            graphicsSub = new Element("graphics");
            offset = new Element("offset");
            offset.setAttribute("x", "0");
            offset.setAttribute("y", "0");
            graphicsSub.addContent(offset);
            initialMarking.addContent(graphicsSub);

            place.addContent(initialMarking);

            capacity = new Element("capacity");
            value = new Element("value");
            value.addContent("0");
            capacity.addContent(value);

            place.addContent(capacity);

            net.addContent(place);
        }

        Element transition = null, orientaton = null, rate = null, timed = null,
                infiniteServer = null, priority = null;
        Element arc = null, inscription = null, tagged = null, type = null;
        String transitionName, arcName;
        for (Transition t : pn.transitions()) {
            transition = new Element("transition");
            transition.setAttribute("id", "T" + t.id());

            graphicsMain = new Element("graphics");
            position = new Element("position");
            x = ((Double) t.getProperty("posX")) + corectX;
            y = ((Double) t.getProperty("posY")) + corectY;
            position.setAttribute("x", "" + x);
            position.setAttribute("y", "" + y);
            graphicsMain.addContent(position);

            transition.addContent(graphicsMain);

            name = new Element("name");
            value = new Element("value");
            transitionName = (String) t.getProperty("name");
            value.addContent(transitionName);
            name.addContent(value);
            graphicsSub = new Element("graphics");
            offset = new Element("offset");
            offset.setAttribute("x", "0");
            offset.setAttribute("y", "0");
            graphicsSub.addContent(offset);
            name.addContent(graphicsSub);

            transition.addContent(name);

            orientaton = new Element("orientaton");
            value = new Element("value");
            value.addContent("0");
            orientaton.addContent(value);

            transition.addContent(orientaton);

            rate = new Element("rate");
            value = new Element("value");
            value.addContent("1.0");
            rate.addContent(value);

            transition.addContent(rate);

            timed = new Element("timed");
            value = new Element("value");
            value.addContent("false");
            timed.addContent(value);

            transition.addContent(timed);

            infiniteServer = new Element("infiniteServer");
            value = new Element("value");
            value.addContent("false");
            infiniteServer.addContent(value);

            transition.addContent(infiniteServer);

            priority = new Element("priority");
            value = new Element("value");
            value.addContent("1");
            priority.addContent(value);

            transition.addContent(priority);

            for (Place p : t.inputs()) {
                arc = new Element("arc");
                arcName = "P" + p.id() + " to T" + t.id();
                arc.setAttribute("id", arcName);
                arc.setAttribute("source", "P" + p.id());
                arc.setAttribute("target", "T" + t.id());

                inscription = new Element("inscription");
                value = new Element("value");
                value.addContent("Default," + new Integer(pn.getArc(p, t).weight()).toString());
                inscription.addContent(value);
                graphicsSub = new Element("graphics");
                inscription.addContent(graphicsSub);

                arc.addContent(inscription);

                tagged = new Element("tagged");
                value = new Element("value");
                value.addContent("false");
                tagged.addContent(value);

                arc.addContent(tagged);

                type = new Element("type");
                type.setAttribute("value", "normal");

                arc.addContent(type);

                arcList.add(arc);
            }

            for (Place p : t.outputs()) {
                arc = new Element("arc");
                arcName = "T" + t.id() + " to P" + p.id();
                arc.setAttribute("id", arcName);
                arc.setAttribute("source", "T" + t.id());
                arc.setAttribute("target", "P" + p.id());

                inscription = new Element("inscription");
                value = new Element("value");
                value.addContent("Default," + new Integer(pn.getArc(t, p).weight()).toString());
                inscription.addContent(value);
                graphicsSub = new Element("graphics");
                inscription.addContent(graphicsSub);

                arc.addContent(inscription);

                tagged = new Element("tagged");
                value = new Element("value");
                value.addContent("false");
                tagged.addContent(value);

                arc.addContent(tagged);

                type = new Element("type");
                type.setAttribute("value", "normal");

                arc.addContent(type);

                arcList.add(arc);
            }

            net.addContent(transition);
        }

        for (Element e : arcList) {
            net.addContent(e);
        }

        root.addContent(net);
        Document doc = new Document(root);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(doc, fileOutputStream);
            LOGGER.info("Successfully exported Petri net to PIPE4 format");
        } catch (IOException ex) {
            LOGGER.error("Caught IOException while exporting Petri net to PIPE4 format: ", ex);
        }
    }

    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in PIPE4 format");
        if (!"xml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            return false;
        }

        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(file);
        } catch (JDOMException e) {
            LOGGER.error("Caught JDOMException while checking for PIPE4 format", e);
            return false;
        }
        Element root = doc.getRootElement();

        // Pipe 4 uses PNML but doesn't a proper net type. We assume P/T
        // networks.
        if (!root.getName().equals("pnml")) {
            return false;
        }
        Element netNode = root.getChild("net");
        if (netNode == null) {
            return false;
        }
        // Pipe4?
        if (!netNode.getChildren("token").isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public File checkFileNameForExtension(File file) {
        if (!"xml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            file = new File(file.getAbsolutePath() + ".xml");
        }
        return file;
    }

    public String getExtension() {
        return "xml";
    }

    public String getDescription() {
        return "Pipe4 (PNML)";
    }

}
