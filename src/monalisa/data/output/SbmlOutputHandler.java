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
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import javax.xml.stream.XMLStreamException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import monalisa.addons.annotations.AnnotationUtils;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.sbml.jsbml.CVTerm;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.History;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.*;

/**
 *
 * @author Jens Einloft
 */
public class SbmlOutputHandler implements OutputHandler {

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private final int version;
    private final int level;
    private static final Logger LOGGER = LogManager.getLogger(SbmlOutputHandler.class);

    public SbmlOutputHandler(int level, int version) {
        this.level = level;
        this.version = version;
    }

    @Override
    public void save(FileOutputStream fileOutputStream, PetriNet pn, File file, NetViewer netViewer) {
        LOGGER.info("Exporting Petri net to SBML format - Level: " + this.level + " - Version: " + this.version);
        SBMLDocument doc = new SBMLDocument(this.level, this.version);
        Model model = doc.createModel("MonaLisaExport");

        if (pn.hasProperty(AnnotationUtils.MODEL_NAME)) {
            model.setName((String) pn.getProperty(AnnotationUtils.MODEL_NAME));
        }

        if (pn.hasProperty(AnnotationUtils.MIRIAM_MODEL_QUALIFIERS)) {
            List<CVTerm> cvts = (List<CVTerm>) pn.getProperty(AnnotationUtils.MIRIAM_MODEL_QUALIFIERS);
            for (CVTerm cvt : cvts) {
                model.addCVTerm(cvt);
            }
        }

        if (pn.hasProperty(AnnotationUtils.HISTORY)) {
            model.setHistory((History) pn.getProperty(AnnotationUtils.HISTORY));
        }
        LayoutModelPlugin mplugin = new LayoutModelPlugin(model);
        model.addExtension(LayoutConstants.getNamespaceURI(level, version), mplugin);

        Layout layout = mplugin.createLayout();

        Compartment defaultCompartment = null;
        Map<monalisa.data.pn.Compartment, org.sbml.jsbml.Compartment> compartmentMap = new HashMap<>();
        if (this.level > 2) {
            boolean thereAreCompartments = false;

            if (pn.getCompartments() != null) {
                if (!pn.getCompartments().isEmpty()) {
                    thereAreCompartments = true;
                    Integer i = 1;
                    for (monalisa.data.pn.Compartment c : pn.getCompartments()) {
                        Compartment compartment = model.createCompartment("C" + i.toString());

                        CompartmentGlyph cglyph = layout.createCompartmentGlyph("CG" + c.toString());
                        cglyph.setCompartment(compartment.getId());
                        cglyph.createBoundingBox();

                        if (c == null) {
                            continue;
                        }

                        compartment.setName(c.getName());

                        if (c.hasProperty("spatialDimensions")) {
                            compartment.setSpatialDimensions((double) c.getProperty("spatialDimensions"));
                        } else {
                            compartment.setSpatialDimensions(1.0);
                        }

                        if (c.hasProperty("size")) {
                            compartment.setSize((double) c.getProperty("size"));
                        } else {
                            compartment.setSize(1.0);
                        }

                        if (c.hasProperty("constant")) {
                            compartment.setConstant((boolean) c.getProperty("constant"));
                        } else {
                            compartment.setConstant(true);
                        }

                        if (c.hasProperty(AnnotationUtils.SBO_TERM)) {
                            compartment.setSBOTerm((String) c.getProperty(AnnotationUtils.SBO_TERM));
                        }

                        if (c.hasProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS)) {
                            List<CVTerm> cvts = (List<CVTerm>) c.getProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS);
                            for (CVTerm cvt : cvts) {
                                compartment.addCVTerm(cvt);
                            }
                        }
                        compartmentMap.put(c, compartment);
                        i++;
                    }
                }
            }
            if (!thereAreCompartments) {
                defaultCompartment = model.createCompartment("default_compartment");
                defaultCompartment.setSize(1.0);
                defaultCompartment.setConstant(true);
                defaultCompartment.setSpatialDimensions(3.0);
            }
        }

        Species species = null;
        SpeciesGlyph sglyph = null;
        for (Place p : pn.places()) {
            species = model.createSpecies("P" + p.id());

            if (this.level > 2) {
                if (p.getCompartment() != null) {
                    species.setCompartment(compartmentMap.get(p.getCompartment()));

                } else {
                    species.setCompartment(defaultCompartment);
                }

                sglyph = layout.createSpeciesGlyph("SG" + species.getId());
                sglyph.setSpecies(species.getId());
                BoundingBox bb = sglyph.createBoundingBox();
                bb.createPosition(p.getProperty("posX"), p.getProperty("posY"), 0);
            }

            species.setName((String) p.getProperty("name"));
            species.setHasOnlySubstanceUnits(true);
            species.setBoundaryCondition(false);
            species.setConstant(false);

            if (p.hasProperty("token")) {
                species.setInitialAmount((Double) p.getProperty("token"));
            } else {
                species.setInitialAmount(0.0);
            }

            if (p.hasProperty("toolTip")) {
                try {
                    species.setNotes((String) p.getProperty("toolTip"));
                } catch (XMLStreamException ex) {
                    LOGGER.error("XMLStreamException while saving tooltips for place '"
                            + species.getName() + "': ", ex);
                }
            }

            // MIRIAM Annotaions
            if (p.hasProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS)) {
                List<CVTerm> cvts = (List<CVTerm>) p.getProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS);
                for (CVTerm cvt : cvts) {
                    species.addCVTerm(cvt);
                }
            }

            // SBO Term
            if (p.hasProperty(AnnotationUtils.SBO_TERM)) {
                species.setSBOTerm((String) p.getProperty(AnnotationUtils.SBO_TERM));
            }
        }

        Reaction reaction = null;
        ReactionGlyph rglyph = null;
        for (Transition t : pn.transitions()) {
            reaction = model.createReaction();

            reaction.setId("T" + t.id());
            reaction.setName((String) t.getProperty("name"));
            reaction.setReversible(false);
            reaction.setFast(false);

            if (this.level > 2) {
                if (t.getCompartment() != null) {
                    reaction.setCompartment(compartmentMap.get(t.getCompartment()));
                } else {
                    reaction.setCompartment(defaultCompartment);
                }

                rglyph = layout.createReactionGlyph("RG" + reaction.getId());
                rglyph.setReaction(reaction.getId());
                BoundingBox bb = rglyph.createBoundingBox();
                bb.createPosition(t.getProperty("posX"), t.getProperty("posY"), 0);
            }

            if (t.hasProperty("toolTip")) {
                try {
                    reaction.setNotes((String) t.getProperty("toolTip"));
                } catch (XMLStreamException ex) {
                    LOGGER.error("XMLStreamException while saving tooltips for transition '"
                            + reaction.getName() + "': ", ex);
                }
            }

            // MIRIAM Annotaion
            if (t.hasProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS)) {
                List<CVTerm> cvts = (List<CVTerm>) t.getProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS);
                for (CVTerm cvt : cvts) {
                    reaction.addCVTerm(cvt);
                }
            }

            // SBO Term
            if (t.hasProperty(AnnotationUtils.SBO_TERM)) {
                reaction.setSBOTerm((String) t.getProperty(AnnotationUtils.SBO_TERM));
            }

            SpeciesReference sr = null;
            ArrayList<String> ppcheck = new ArrayList<String>();
            for (Place p : t.inputs()) {
                if(!ppcheck.contains("P" + p.id())){
                    sr = reaction.createReactant(model.getSpecies("P" + p.id()));
                    sr.setStoichiometry(pn.getArc(p, t).weight());
                    if (this.level > 2) {
                        sr.setConstant(true);
                    }
                    if (pn.getArc(p, t).hasProperty("toolTip")) {
                        try {
                            sr.setNotes((String) pn.getArc(p, t).getProperty("toolTip"));
                        } catch (XMLStreamException ex) {
                            LOGGER.error("XMLStreamException while saving tooltips for arc between place '"
                                    + p.getProperty("name") + "' and transition '" + t.getProperty("name") + "': ", ex);
                        }
                    }
                    ppcheck.add("P" + p.id());
                }
            }
            ArrayList<String> postplacecheck = new ArrayList<String>();

            for (Place p : t.outputs()) {
                if (!postplacecheck.contains("P" + p.id())){
                    sr = reaction.createProduct(model.getSpecies("P" + p.id()));
                    sr.setStoichiometry(pn.getArc(t, p).weight());
                    if (this.level > 2) {
                        sr.setConstant(true);
                    }
                    if (pn.getArc(t, p).hasProperty("toolTip")) {
                        try {
                            sr.setNotes((String) pn.getArc(t, p).getProperty("toolTip"));
                        } catch (XMLStreamException ex) {
                            LOGGER.error("XMLStreamException while saving tooltips for arc between transition '"
                                    + t.getProperty("name") + "' and place '" + p.getProperty("name") + "': ", ex);
                        }
                    }
                    postplacecheck.add("P" + p.id());
                }
            }
        }

        SBMLWriter writer = new SBMLWriter();
        try {
            writer.write(doc, fileOutputStream, "MonaLisa", strings.get("CurrentVersion"));
            LOGGER.info("Successfully exported Petri net to SBML format - Level: "
                    + this.level + " - Version: " + this.version);
        } catch (XMLStreamException ex) {
            LOGGER.error("Caught XMLStreamException while exporting to SBML format - Level: "
                    + this.level + " - Version: " + this.version + ": ", ex);
        } catch (SBMLException ex) {
            LOGGER.error("Caught SBMLException while exporting to SBML format - Level: "
                    + this.level + " - Version: " + this.version + ": ", ex);
        }
        LOGGER.info("Initiating Layout-File export");
        String filePath = file.getAbsolutePath();
        String layoutFilePath = filePath.substring(0, filePath.length() - 4) + "layout" + filePath.substring(filePath.length() - 4, filePath.length());
        
        try {
            Document document = new Document();
            document.setRootElement(new Element("Layoutdata"));
            Collection<NetViewerNode> allVertices = netViewer.getAllVertices();
            Integer bendNodeCounter = 0;
            for (NetViewerNode node : allVertices) {
                if (node.getNodeType().equals("BEND")) {
                    node.setName("BendNodeID" + String.valueOf(bendNodeCounter));
                    bendNodeCounter++;
                }
            }
            //logical places
            Element logicalPlaces = new Element("LogicalPlaces");
            document.getRootElement().addContent(logicalPlaces);
            Integer logicalNodeCounter = 0;
            Map<Integer, NetViewerNode> placeMap = netViewer.getPlaceMap();
            Collection<NetViewerNode> places = placeMap.values();
            for (NetViewerNode node : places) {
                if (node.getLogicalPlaces().size() > 1) {
                    for (NetViewerNode n : node.getLogicalPlaces().subList(1, node.getLogicalPlaces().size())) {
                        Element logicalPlace = new Element("LogicalPlace");
                        logicalPlace.setAttribute("Name", "LogicalPlaceID" + String.valueOf(logicalNodeCounter));
                        logicalPlace.addContent(new Element("MasterNode").setText(n.getMasterNode().getName()));
                        Element inEdges = new Element("IncomingEdges");
                        Collection<NetViewerEdge> inEdgesCollection = n.getInEdges();
                        for (NetViewerEdge e : inEdgesCollection) {
                            inEdges.addContent(new Element("IncomingEdgeFrom").setText(e.getSource().getName()));
                        }
                        logicalPlace.addContent(inEdges);
                        Element outEdges = new Element("OutgoingEdges");
                        Collection<NetViewerEdge> outEdgesCollection = n.getOutEdges();
                        for (NetViewerEdge e : outEdgesCollection) {
                            outEdges.addContent(new Element("OutgoingEdgeTo").setText(e.getAim().getName()));
                        }
                        logicalPlace.addContent(outEdges);
                        logicalPlace.addContent(new Element("X").setText(String.valueOf(netViewer.getMLLayout().transform(n).getX())));
                        logicalPlace.addContent(new Element("Y").setText(String.valueOf(netViewer.getMLLayout().transform(n).getY())));
                        logicalPlaces.addContent(logicalPlace);
                        //set internal Name for further reference
                        n.setName("LogicalPlaceID" + String.valueOf(logicalNodeCounter));
                        logicalNodeCounter++;
                    }
                }
            }
            //other layout information
            Element bendEdges = new Element("BendEdges");
            document.getRootElement().addContent(bendEdges);
            Element addInfo = new Element("AdditionalInformationAboutNodes");
            document.getRootElement().addContent(addInfo);
            Element edgeInfo = new Element("AdditionalInformationAboutEdges");
            document.getRootElement().addContent(edgeInfo);
            Integer edgeCount = 0;
            for (NetViewerNode n : allVertices) {
                //bend
                if (n.getNodeType().equals("BEND")) {
                    Element bendEdge = new Element("BendEdge");
                    bendEdge.setAttribute("Name", n.getName());
                    bendEdge.addContent(new Element("X").setText(String.valueOf(netViewer.getMLLayout().transform(n).getX())));
                    bendEdge.addContent(new Element("Y").setText(String.valueOf(netViewer.getMLLayout().transform(n).getY())));
                    Collection<NetViewerEdge> inEdgesCollection = n.getInEdges();
                    for (NetViewerEdge e : inEdgesCollection) {    
                        bendEdge.addContent(new Element("IncomingEdgeFrom").setText(e.getSource().getName()));
                    }
                    Collection<NetViewerEdge> outEdgesCollection = n.getOutEdges();
                    for (NetViewerEdge e : outEdgesCollection) {
                        bendEdge.addContent(new Element("OutgoingEdgeTo").setText(e.getAim().getName()));
                    }
                    bendEdges.addContent(bendEdge);
                } //label position
                //color
                //corners
                else if (n.getNodeType().equals("PLACE") || n.getNodeType().equals("TRANSITION")) {
                    Element node = new Element("Node");
                    node.setAttribute("Name", n.getName());
                    node.addContent(new Element("LabelPosition").setText(String.valueOf(n.getLabelPosition())));
                    node.addContent(new Element("Color").setText(String.valueOf(n.getColor())));
                    node.addContent(new Element("Corners").setText(String.valueOf(n.getCorners())));
                    node.addContent(new Element("StrokeColor").setText(String.valueOf(n.getStrokeColor())));
                    addInfo.addContent(node);
                }
                //edge color 
                ArrayList<String> targetnode = new ArrayList<String>();
                for (NetViewerEdge e : n.getOutEdges()) {
                    if(!targetnode.contains(e.getAim().getName())){
                        Element edge = new Element("Edge");
                        edge.setAttribute("Name", "E" + String.valueOf(edgeCount));
                        edge.addContent(new Element("Source").setText(n.getName()));
                        edge.addContent(new Element("Target").setText(e.getAim().getName()));
                        edge.addContent(new Element("Color").setText(String.valueOf(e.getColor())));
                        edgeInfo.addContent(edge);
                    edgeCount++;
                    targetnode.add(e.getAim().getName());
                    }
                }
            }
            //fixing the internal names again
            for (NetViewerNode node : places) {
                if (node.getLogicalPlaces().size() > 1) {
                    for (NetViewerNode n : node.getLogicalPlaces().subList(1, node.getLogicalPlaces().size())) {
                        n.setName(n.getMasterNode().getName());
                    }
                }
            }

            //output
            FileWriter writerLayout = new FileWriter(layoutFilePath);
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writerLayout);
            LOGGER.info("Finished Layout-File export");
        } catch (Exception e) {
            e.printStackTrace();
            }

        /*try {
            File layoutFile = new File(layoutFilePath);
            if (layoutFile.createNewFile()) {
                System.out.println("File created: " + layoutFile.getName());
            } else {
                layoutFile.delete();
                layoutFile.createNewFile();
                System.out.println("File did already exist, got deleted and recreated.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        */

    }

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in SBML format");
        if ("sbml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            return true;
        }

        if ("xml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            SAXBuilder builder = new SAXBuilder();
            Document doc = null;
            try {
                doc = builder.build(file);
            } catch (JDOMException ex) {
                LOGGER.error("Caught JDOMException while checking for SBML format: ", ex);
                return false;
            } catch (IOException ex) {
                LOGGER.error("Caught IOException while checking for SBML format: ", ex);
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

    @Override
    public File checkFileNameForExtension(File file) {
        if (!"xml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            file = new File(file.getAbsolutePath() + ".xml");
        }
        return file;
    }

    @Override
    public String getExtension() {
        return "xml";
    }

    @Override
    public String getDescription() {
        return "SBML Level " + this.level + " Version " + this.version;
    }
}
