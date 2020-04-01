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

import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;

import javax.xml.stream.XMLStreamException;
import monalisa.addons.annotations.AnnotationUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
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
 * Input handler for the SBML format.
 * This class parses a SBML-file.
 * Supports SBML all Versions and Levels specified in <a href="http://sbml.org/Documents/Specifications">http://sbml.org/Documents/Specifications</a>
 **/
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

        if("xml".equalsIgnoreCase(FileUtils.getExtension(file))) {
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
            }
            else {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PetriNet load(InputStream in) throws IOException {
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

        if(model.isSetPlugin(LayoutConstants.getNamespaceURI(3, 1))){
            mplugin = model.getExtension(LayoutConstants.getNamespaceURI(3, 1));          
            LayoutModelPlugin layplugin = (LayoutModelPlugin)mplugin;
            layout = layplugin.getLayout(0);
            
            //check if the layout is not empty  
            if(layout.getReactionGlyphCount()!= 0 && layout.getSpeciesGlyphCount() != 0){
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

        if(model.getCVTermCount() > 0) {
            tmp =  model.getCVTerms();
            petriNet.putProperty(AnnotationUtils.MIRIAM_MODEL_QUALIFIERS, tmp);
        }

        Map<org.sbml.jsbml.Compartment, monalisa.data.pn.Compartment> compartmentMap = new HashMap<>();
        if(!model.getListOfCompartments().isEmpty()) {
            for(org.sbml.jsbml.Compartment c : model.getListOfCompartments()) {
                monalisa.data.pn.Compartment pnComp = new monalisa.data.pn.Compartment(c.getName());
                pnComp.putProperty("size", c.getSize());
                pnComp.putProperty("constant", c.getConstant());
                pnComp.putProperty("spatialDimensions", c.getSpatialDimensions());
                pnComp.putProperty(AnnotationUtils.SBO_TERM, c.getSBOTermID());
                tmp = c.getCVTerms();
                pnComp.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, tmp);
                petriNet.addCompartment(pnComp);
                compartmentMap.put(c,pnComp);
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

        for(Species s : model.getListOfSpecies()) {
            name = s.getName();
            id = s.getId();
            
            if(hasLayout){
                posX = layout.getSpeciesGlyph("SG"+id).getBoundingBox().getPosition().getX();
                posY = layout.getSpeciesGlyph("SG"+id).getBoundingBox().getPosition().getY();
            }
            
            if(name.equals("")) {
                name = id;
            }
            tokens = new Double(s.getInitialAmount()).longValue();
            place = findPlace(countPlaces, petriNet);
            place.putProperty("name", name);
            if(hasLayout){
                place.putProperty("posX", posX);
                place.putProperty("posY", posY);
            }

            if(!s.getSBOTermID().isEmpty()) {
                place.putProperty(AnnotationUtils.SBO_TERM, s.getSBOTermID());
            } else {
                place.putProperty(AnnotationUtils.SBO_TERM, "SBO:0000000");
            }
            tmp = s.getCVTerms();
            place.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, tmp);

            if(s.getCompartmentInstance() != null) {
                petriNet.setCompartment(place, compartmentMap.get(s.getCompartmentInstance()));
            }

            petriNet.setTokens(place, tokens);
            species.put(id, countPlaces);
            countPlaces++;
        }

        boolean doubleName = false;
        for(Place p1 : petriNet.places()) {
            for(Place p2 : petriNet.places()) {
                if(p1.equals(p2))
                    continue;

                if(p1.getProperty("name").equals(p2.getProperty("name"))) {
                    if(p2.getCompartment() != null) {
                        doubleName = true;
                        p2.putProperty("name", p2.getProperty("name")+"_"+p2.getCompartment().getName());
                    }
                }
            }
            if(doubleName) {
               if(p1.getCompartment() != null) {
                   p1.putProperty("name", p1.getProperty("name")+"_"+p1.getCompartment().getName());
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
            
            if(hasLayout){
                posX = layout.getReactionGlyph("RG"+id).getBoundingBox().getPosition().getX();
                posY = layout.getReactionGlyph("RG"+id).getBoundingBox().getPosition().getY();
            }
            
            if(name.equals("")) {
                name = id;
            }
            reversible = r.getReversible();
            floatWeighs = false;
            multiplikator = 1;

            transition = findTransition(countTransitions, petriNet);
            transition.putProperty("name", name);
            
            if(hasLayout){
                transition.putProperty("posX", posX);
                transition.putProperty("posY", posY);
            }

            if(r.getCompartmentInstance() != null) {
                petriNet.setCompartment(transition, compartmentMap.get(r.getCompartmentInstance()));
            }

            if(!r.getSBOTermID().isEmpty()) {
                transition.putProperty(AnnotationUtils.SBO_TERM, r.getSBOTermID());
            } else {
                transition.putProperty(AnnotationUtils.SBO_TERM, "SBO:0000000");
            }
            tmp = r.getCVTerms();
            transition.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, tmp);

            if(reversible) {
                countTransitions++;
                transition_rev = findTransition(countTransitions, petriNet);
                transition_rev.putProperty("name", name+"_rev");

                if(r.getCompartmentInstance() != null) {
                    petriNet.setCompartment(transition_rev, compartmentMap.get(r.getCompartmentInstance()));
                }

                if(!r.getSBOTermID().isEmpty()) {
                    transition_rev.putProperty(AnnotationUtils.SBO_TERM, r.getSBOTermID());
                } else {
                    transition_rev.putProperty(AnnotationUtils.SBO_TERM, "SBO:0000000");
                }
                tmp = r.getCVTerms();
                transition_rev.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, tmp);
            }

            for(SpeciesReference spr : r.getListOfReactants()) {
                weighString = Double.toString(spr.getStoichiometry());
                indexOfPoint = weighString.indexOf(".");
                subLen = weighString.substring(indexOfPoint+1, weighString.length()).length();
                if(subLen > 1 || !weighString.substring(indexOfPoint+1, weighString.length()).equals("0")) {
                    floatWeighs = true;
                    if(subLen > multiplikator) {
                        multiplikator = subLen;
                    }
                }
            }

            for(SpeciesReference spr : r.getListOfProducts()) {
                weighString = Double.toString(spr.getStoichiometry());
                indexOfPoint = weighString.indexOf(".");
                subLen = weighString.substring(indexOfPoint+1, weighString.length()).length();
                if(subLen > 1 || !weighString.substring(indexOfPoint+1, weighString.length()).equals("0")) {
                    floatWeighs = true;
                    if(subLen > multiplikator) {
                        multiplikator = subLen;
                    }
                }
            }

            multiplikator = (int) Math.pow(10.0, (double) multiplikator);
            Object source, aim;
            // input arcs: (place = reactant) --> transition
            for(SpeciesReference spr : r.getListOfReactants()) {

                reactantName = spr.getSpecies();
                weight = spr.getStoichiometry();

                if(floatWeighs) {
                    weight *= multiplikator;
                }

                source = findPlace(species.get(reactantName), petriNet);
                aim = transition;

                petriNet.addArc((Place)source, (Transition)aim, new Arc(source, aim, weight.intValue()));

                if(reversible) {
                    source = transition_rev;
                    aim = findPlace(species.get(reactantName), petriNet);
                    petriNet.addArc((Transition)source, (Place)aim, new Arc(aim, source, weight.intValue()));
                }
            }
            // output arcs: transition --> (place = product)
            for(SpeciesReference spr : r.getListOfProducts()) {
                productName = spr.getSpecies();
                weight = spr.getStoichiometry();

                if(floatWeighs) {
                    weight *= multiplikator;
                }

                source = transition;
                aim = findPlace(species.get(productName), petriNet);

                petriNet.addArc((Transition)source, (Place)aim, new Arc(source, aim, weight.intValue()));

                if(reversible) {
                    source = findPlace(species.get(productName), petriNet);
                    aim = transition_rev;
                    petriNet.addArc((Place)source, (Transition)aim, new Arc(aim, source, weight.intValue()));
                }
            }
            // Modifier
            for(ModifierSpeciesReference msr : r.getListOfModifiers()) {
                weight = 1.0;
                productName = msr.getSpecies();

                source = transition;
                aim = findPlace(species.get(productName), petriNet);
                petriNet.addArc((Transition)source, (Place)aim, new Arc(source, aim, weight.intValue()));
                petriNet.addArc((Place)aim, (Transition)source, new Arc(source, aim, weight.intValue()));
            }

            countTransitions++;
        }

        doubleName = false;
        for(Transition t1 : petriNet.transitions()) {
            for(Transition t2 : petriNet.transitions()) {
                if(t1.equals(t2))
                    continue;

                if(t1.getProperty("name").equals(t2.getProperty("name"))) {
                    if(t2.getCompartment() != null) {
                        doubleName = true;
                        t2.putProperty("name", t2.getProperty("name")+"_"+t2.getCompartment().getName());
                    }
                }
            }
            if(doubleName) {
               if(t1.getCompartment() != null) {
                   t1.putProperty("name", t1.getProperty("name")+"_"+t1.getCompartment().getName());
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
