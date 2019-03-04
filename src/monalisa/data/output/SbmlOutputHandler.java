/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import monalisa.addons.annotations.AnnotationsPanel;

import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.util.FileUtils;
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

/**
 *
 * @author Jens Einloft
 */
public class SbmlOutputHandler implements OutputHandler {

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();
    
    private final int version;
    private final int level;    
    
    public SbmlOutputHandler(int level, int version) {
        this.level = level;
        this.version = version;        
    }
    
    @Override
    public void save(FileOutputStream fileOutputStream, PetriNet pn) {
        SBMLDocument doc = new SBMLDocument(this.level,this.version);
        Model model = doc.createModel("MonaLisaExport");

        if(pn.hasProperty(AnnotationsPanel.MODEL_NAME)) {
            model.setName((String) pn.getProperty(AnnotationsPanel.MODEL_NAME));
        }
        
        if(pn.hasProperty(AnnotationsPanel.MIRIAM_MODEL_QUALIFIERS)) {
            List<CVTerm> cvts = (List<CVTerm>) pn.getProperty(AnnotationsPanel.MIRIAM_MODEL_QUALIFIERS);                                                
            for(CVTerm cvt : cvts) {
                model.addCVTerm(cvt);
            }            
        }
        
        if(pn.hasProperty(AnnotationsPanel.HISTORY)) {        
            model.setHistory((History) pn.getProperty(AnnotationsPanel.HISTORY));
        }

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

                        if(c.hasProperty(AnnotationsPanel.SBO_TERM)) {
                            compartment.setSBOTerm((String) c.getProperty(AnnotationsPanel.SBO_TERM));
                        }

                        if(c.hasProperty(AnnotationsPanel.MIRIAM_BIO_QUALIFIERS)) {
                            List<CVTerm> cvts = (List<CVTerm>) c.getProperty(AnnotationsPanel.MIRIAM_BIO_QUALIFIERS);                                                
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
        for(Place p : pn.places()) {
            species = model.createSpecies("P"+p.id());
            
            if(this.level > 2) {
                if(p.getCompartment() != null) {
                    species.setCompartment(compartmentMap.get(p.getCompartment()));
                } else {
                    species.setCompartment(defaultCompartment);
                }
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
                    Logger.getLogger(SbmlOutputHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            // MIRIAM Annotaions
            if(p.hasProperty(AnnotationsPanel.MIRIAM_BIO_QUALIFIERS)) {
                List<CVTerm> cvts = (List<CVTerm>) p.getProperty(AnnotationsPanel.MIRIAM_BIO_QUALIFIERS);   
                for(CVTerm cvt : cvts) {
                    species.addCVTerm(cvt);
                }
            }
            
            // SBO Term
            if(p.hasProperty(AnnotationsPanel.SBO_TERM)) {
                species.setSBOTerm((String) p.getProperty(AnnotationsPanel.SBO_TERM));
            }             
        }

        Reaction reaction = null;
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
            }
            
            if(t.hasProperty("toolTip")) {
                try {
                    reaction.setNotes((String) t.getProperty("toolTip"));
                } catch (XMLStreamException ex) {
                    Logger.getLogger(SbmlOutputHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }            
            
            // MIRIAM Annotaion
            if(t.hasProperty(AnnotationsPanel.MIRIAM_BIO_QUALIFIERS)) {
                List<CVTerm> cvts = (List<CVTerm>) t.getProperty(AnnotationsPanel.MIRIAM_BIO_QUALIFIERS);                                                
                for(CVTerm cvt : cvts) {
                    reaction.addCVTerm(cvt);
                }
            }            
            
            // SBO Term            
            if(t.hasProperty(AnnotationsPanel.SBO_TERM)) {
                reaction.setSBOTerm((String) t.getProperty(AnnotationsPanel.SBO_TERM));
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
                        Logger.getLogger(SbmlOutputHandler.class.getName()).log(Level.SEVERE, null, ex);
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
                        Logger.getLogger(SbmlOutputHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                
            }                
        }

        SBMLWriter writer = new SBMLWriter();
        try {
            writer.write(doc, fileOutputStream, "MonaLisa", strings.get("CurrentVersion"));
        } catch (XMLStreamException | SBMLException ex) {
            Logger.getLogger(SbmlOutputHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isKnownFile(File file) throws IOException {
        if ("sbml".equalsIgnoreCase(FileUtils.getExtension(file)))
            return true;

        if("xml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            SAXBuilder builder = new SAXBuilder();
            Document doc = null;
            try {
                doc = builder.build(file);
            } catch (JDOMException | IOException e) {
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
