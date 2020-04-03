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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import monalisa.addons.annotations.AnnotationUtils;

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
    public void save(FileOutputStream fileOutputStream, PetriNet pn) {
        LOGGER.info("Exporting Petri net to SBML format - Level: " + this.level + " - Version: " + this.version);
        SBMLDocument doc = new SBMLDocument(this.level,this.version);
        Model model = doc.createModel("MonaLisaExport");

        if(pn.hasProperty(AnnotationUtils.MODEL_NAME)) {
            model.setName((String) pn.getProperty(AnnotationUtils.MODEL_NAME));
        }

        if(pn.hasProperty(AnnotationUtils.MIRIAM_MODEL_QUALIFIERS)) {
            List<CVTerm> cvts = (List<CVTerm>) pn.getProperty(AnnotationUtils.MIRIAM_MODEL_QUALIFIERS);
            for(CVTerm cvt : cvts) {
                model.addCVTerm(cvt);
            }
        }

        if(pn.hasProperty(AnnotationUtils.HISTORY)) {
            model.setHistory((History) pn.getProperty(AnnotationUtils.HISTORY));
        }
        LayoutModelPlugin mplugin = new LayoutModelPlugin(model);
        model.addExtension(LayoutConstants.getNamespaceURI(level, version),mplugin);
        //(LayoutModelPlugin) model.getPlugin(LayoutConstants.shortLabel);
        //Layout layout = new Layout();
        //mplugin.add(layout);
        Layout layout = mplugin.createLayout();
        
        Compartment defaultCompartment = null;
        Map<monalisa.data.pn.Compartment, org.sbml.jsbml.Compartment> compartmentMap = new HashMap<>();
        if(this.level > 2) {
            boolean thereAreCompartments = false;

            if(pn.getCompartments() != null) {
                if(!pn.getCompartments().isEmpty()) {
                    thereAreCompartments = true;
                    Integer i = 1;
                    for(monalisa.data.pn.Compartment c : pn.getCompartments()) {
                        Compartment compartment = model.createCompartment("C"+i.toString());
                        
                        CompartmentGlyph cglyph = layout.createCompartmentGlyph("CG"+ c.toString());                        
                        cglyph.setCompartment(compartment.getId());
                        cglyph.createBoundingBox(c.getProperty("spatialDimensions"));
                        
                        if(c == null)
                            continue;

                        compartment.setName(c.getName());

                        if(c.hasProperty("spatialDimensions")) {
                            compartment.setSpatialDimensions((double) c.getProperty("spatialDimensions"));
                        } else {
                            compartment.setSpatialDimensions(1.0);
                        }

                        if(c.hasProperty("size")) {
                            compartment.setSize((double) c.getProperty("size"));
                        } else {
                            compartment.setSize(1.0);
                        }

                        if(c.hasProperty("constant")) {
                            compartment.setConstant((boolean) c.getProperty("constant"));
                        } else {
                            compartment.setConstant(true);
                        }

                        if(c.hasProperty(AnnotationUtils.SBO_TERM)) {
                            compartment.setSBOTerm((String) c.getProperty(AnnotationUtils.SBO_TERM));
                        }

                        if(c.hasProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS)) {
                            List<CVTerm> cvts = (List<CVTerm>) c.getProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS);
                            for(CVTerm cvt : cvts) {
                                compartment.addCVTerm(cvt);
                            }
                        }
                        compartmentMap.put(c, compartment);
                        i++;
                    }
                }
            }
            if(!thereAreCompartments) {
                defaultCompartment = model.createCompartment("default_compartment");
                defaultCompartment.setSize(1.0);
                defaultCompartment.setConstant(true);
                defaultCompartment.setSpatialDimensions(3.0);
            }
        }

        Species species = null;
        SpeciesGlyph sglyph = null;
        for(Place p : pn.places()) {
            species = model.createSpecies("P"+p.id());

            if(this.level > 2) {
                if(p.getCompartment() != null) {
                    species.setCompartment(compartmentMap.get(p.getCompartment()));
                    
                } else {
                    species.setCompartment(defaultCompartment);                   
                }
                
                sglyph = layout.createSpeciesGlyph("SG"+species.getId());
                sglyph.setSpecies(species.getId());
                BoundingBox bb = sglyph.createBoundingBox();
                bb.createPosition(p.getProperty("posX"),p.getProperty("posY"),0);
            }

            species.setName((String) p.getProperty("name"));
            species.setHasOnlySubstanceUnits(true);
            species.setBoundaryCondition(false);
            species.setConstant(false);

            if(p.hasProperty("token")) {
                species.setInitialAmount((Double) p.getProperty("token"));
            }
            else {
                species.setInitialAmount(0.0);
            }

            if(p.hasProperty("toolTip")) {
                try {
                    species.setNotes((String) p.getProperty("toolTip"));
                } catch (XMLStreamException ex) {
                    LOGGER.error("XMLStreamException while saving tooltips for place '"
                            + species.getName() + "': ", ex);
                }
            }

            // MIRIAM Annotaions
            if(p.hasProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS)) {
                List<CVTerm> cvts = (List<CVTerm>) p.getProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS);
                for(CVTerm cvt : cvts) {
                    species.addCVTerm(cvt);
                }
            }

            // SBO Term
            if(p.hasProperty(AnnotationUtils.SBO_TERM)) {
                species.setSBOTerm((String) p.getProperty(AnnotationUtils.SBO_TERM));
            }
        }

        Reaction reaction = null;
        ReactionGlyph rglyph = null;
        for(Transition t : pn.transitions()) {
            reaction = model.createReaction();

            reaction.setId("T"+t.id());
            reaction.setName((String) t.getProperty("name"));
            reaction.setReversible(false);
            reaction.setFast(false);

            if(this.level > 2) {
                if(t.getCompartment() != null) {
                    reaction.setCompartment(compartmentMap.get(t.getCompartment()));
                } else {
                    reaction.setCompartment(defaultCompartment);
                }
           
                rglyph = layout.createReactionGlyph("RG"+reaction.getId());
                rglyph.setReaction(reaction.getId());
                BoundingBox bb = rglyph.createBoundingBox();
                bb.createPosition(t.getProperty("posX"),t.getProperty("posY"),0);
            }

            if(t.hasProperty("toolTip")) {
                try {
                    reaction.setNotes((String) t.getProperty("toolTip"));
                } catch (XMLStreamException ex) {
                    LOGGER.error("XMLStreamException while saving tooltips for transition '"
                            + reaction.getName() + "': ", ex);
                }
            }

            // MIRIAM Annotaion
            if(t.hasProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS)) {
                List<CVTerm> cvts = (List<CVTerm>) t.getProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS);
                for(CVTerm cvt : cvts) {
                    reaction.addCVTerm(cvt);
                }
            }

            // SBO Term
            if(t.hasProperty(AnnotationUtils.SBO_TERM)) {
                reaction.setSBOTerm((String) t.getProperty(AnnotationUtils.SBO_TERM));
            }

            SpeciesReference sr = null;
            for(Place p : t.inputs()) {
                sr = reaction.createReactant(model.getSpecies("P"+p.id()));
                sr.setStoichiometry(pn.getArc(p, t).weight());
                if(this.level > 2) {
                    sr.setConstant(true);
                }
                if(pn.getArc(p, t).hasProperty("toolTip")) {
                    try {
                        sr.setNotes((String) pn.getArc(p, t).getProperty("toolTip"));
                    } catch (XMLStreamException ex) {
                        LOGGER.error("XMLStreamException while saving tooltips for arc between place '"
                                + p.getProperty("name") + "' and transition '" + t.getProperty("name") + "': ", ex);
                    }
                }
            }

            for(Place p : t.outputs()) {
                sr = reaction.createProduct(model.getSpecies("P"+p.id()));
                sr.setStoichiometry(pn.getArc(t, p).weight());
                if(this.level > 2) {
                    sr.setConstant(true);
                }
                if(pn.getArc(t, p).hasProperty("toolTip")) {
                    try {
                        sr.setNotes((String) pn.getArc(t, p).getProperty("toolTip"));
                    } catch (XMLStreamException ex) {
                        LOGGER.error("XMLStreamException while saving tooltips for arc between transition '"
                                + t.getProperty("name") + "' and place '" + p.getProperty("name") + "': ", ex);                    }
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
        } catch (SBMLException ex){
            LOGGER.error("Caught SBMLException while exporting to SBML format - Level: "
                    + this.level + " - Version: " + this.version + ": ", ex);
        }
    }

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in SBML format");
        if ("sbml".equalsIgnoreCase(FileUtils.getExtension(file)))
            return true;

        if("xml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            SAXBuilder builder = new SAXBuilder();
            Document doc = null;
            try {
                doc = builder.build(file);
            } catch (JDOMException ex) {
                LOGGER.error("Caught JDOMException while checking for SBML format: ", ex);
                return false;
            } catch (IOException ex){
                LOGGER.error("Caught IOException while checking for SBML format: ", ex);
                return false;
            }

            Element root = doc.getRootElement();
            if (!root.getName().equals("sbml"))
                return false;
            else
                return true;
        }
        return false;
    }

    @Override
    public File checkFileNameForExtension(File file) {
        if(!"xml".equalsIgnoreCase(FileUtils.getExtension(file)))
            file = new File(file.getAbsolutePath()+".xml");
        return file;
    }

    @Override
    public String getExtension() {
        return "xml";
    }

    @Override
    public String getDescription() {
        return "SBML Level "+this.level+" Version "+this.version;
    }
}
