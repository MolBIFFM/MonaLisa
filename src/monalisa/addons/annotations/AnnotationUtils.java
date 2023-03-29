/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.annotations;

import java.util.ArrayList;
import java.util.List;
import monalisa.data.pn.AbstractPetriNetEntity;
import monalisa.data.pn.PetriNetFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sbml.jsbml.CVTerm;

/**
 *
 * @author Marcel
 */
public class AnnotationUtils {

    private final static Logger LOGGER = LogManager.getLogger(AnnotationUtils.class);
    public static final String MODEL_NAME = "MODEL_NAME";
    public static final String MIRIAM_MODEL_QUALIFIERS = "MIRIAM_MODEL_QUALIFIERS";
    public static final String SBO_TERM = "SBO_TERM";
    public static final String HISTORY = "HISTORY";
    public static final String MIRIAM_BIO_QUALIFIERS = "MIRIAM_BIO_QUALIFIERS";

    /**
     * Adds a MIRIAM Bio qualifier to a PetriNetEntity
     * @param ape
     * @param mw
     * @param resource
     */
    public void addMiriam(AbstractPetriNetEntity ape, MiriamWrapper mw, String resource) {
        if (!ape.hasProperty(MIRIAM_BIO_QUALIFIERS)) {
            addProperty(ape, MIRIAM_BIO_QUALIFIERS, new ArrayList<CVTerm>());
        }
        List<CVTerm> cvts = (List<CVTerm>) ape.getProperty(MIRIAM_BIO_QUALIFIERS);
        boolean qualifierWasThere = false;
        for (CVTerm cvt : cvts) {
            if ((mw.getCVTerm().getBiologicalQualifierType()).equals(cvt.getBiologicalQualifierType())) {
                qualifierWasThere = true;
                cvt.addResource(resource);
            }
            addProperty(ape, MIRIAM_BIO_QUALIFIERS, cvts);
        }
        if (!qualifierWasThere) {
            ((List<CVTerm>) ape.getProperty(MIRIAM_BIO_QUALIFIERS)).add(mw.getCVTerm());
        }
    }

    /**
     * Adds a MIRIAM model qualifier to the Petri net.
     * @param petriNet
     * @param mw
     * @param resource
     */
    public void addMiriamModel(PetriNetFacade petriNet, MiriamWrapper mw, String resource) {
        if (!petriNet.hasProperty(MIRIAM_MODEL_QUALIFIERS)) {
            addProperty(petriNet, MIRIAM_MODEL_QUALIFIERS, new ArrayList<CVTerm>());
        }
        List<CVTerm> cvts = (List<CVTerm>) petriNet.getProperty(MIRIAM_MODEL_QUALIFIERS);
        boolean qualifierWasThere = false;
        for (CVTerm cvt : cvts) {
            if ((mw.getCVTerm().getModelQualifierType()).equals(cvt.getModelQualifierType())) {
                qualifierWasThere = true;
                cvt.addResource(resource);
            }
            addProperty(petriNet, MIRIAM_MODEL_QUALIFIERS, cvts);
        }
        if (!qualifierWasThere) {
            ((List<CVTerm>) petriNet.getProperty(MIRIAM_MODEL_QUALIFIERS)).add(mw.getCVTerm());
        }
    }

    /**
     * Edits a MIRIAM Bio qualifier for a PetriNetEntity.
     * @param ape
     * @param identifier
     * @param mrw
     * @param resource
     * @param selectedItem
     */
    public void editMiriam(AbstractPetriNetEntity ape, MiriamWrapper identifier, MiriamRegistryWrapper mrw, String resource, Object selectedItem) {
        List<CVTerm> cvts = (List<CVTerm>) ape.getProperty(MIRIAM_BIO_QUALIFIERS);
        // Same identifier = update the uri
        if (identifier.getCVTerm().getBiologicalQualifierType().equals((CVTerm.Qualifier) selectedItem)) {
            LOGGER.info("Same identifier, updating the uri");
            for (CVTerm cvt : cvts) {
                if ((identifier.getCVTerm().getBiologicalQualifierType()).equals(cvt.getBiologicalQualifierType())) {
                    cvt.getResources().set(cvt.getResources().indexOf(identifier.getURI()), mrw.getURL() + resource);
                    break;
                }
            }
        } else {
            // new identifier = update the identifier and the uri
            LOGGER.info("New identifier, updating identifier and uri");
            // first: delete the old one
            CVTerm toRemove = null;
            for (CVTerm cvt : cvts) {
                if ((identifier.getCVTerm().getBiologicalQualifierType()).equals(cvt.getBiologicalQualifierType())) {
                    cvt.getResources().remove(cvt.getResources().indexOf(identifier.getURI()));
                    if (cvt.getResourceCount() == 0) {
                        toRemove = cvt;
                    }
                    break;
                }
            }
            if (toRemove != null) {
                LOGGER.info("Removing old one");
                cvts.remove(toRemove);
                addProperty(ape, "MIRIAM_BIO_QUALIFIERS", cvts);
            }
            LOGGER.info("Adding new ones");
            // now add the new ones
            cvts = (List<CVTerm>) ape.getProperty(MIRIAM_BIO_QUALIFIERS);
            boolean qualifierWasThere = false;
            for (CVTerm cvt : cvts) {
                if (((CVTerm.Qualifier) selectedItem).equals(cvt.getBiologicalQualifierType())) {
                    qualifierWasThere = true;
                    cvt.addResource(resource);
                }
                addProperty(ape, MIRIAM_BIO_QUALIFIERS, cvts);
            }
            if (!qualifierWasThere) {
                MiriamWrapper mw = new MiriamWrapper((CVTerm.Qualifier) selectedItem, mrw.getURL() + resource);
                updateCVTerms(ape, "add", MIRIAM_BIO_QUALIFIERS, mw.getCVTerm());
            }
        }
    }

    /**
     * Edits a MIRIAM model qualifier for the Petri net.
     * @param petriNet
     * @param identifier
     * @param mrw
     * @param uriTrimmed
     * @param selectedItem
     */
    public void editMiriamModel(PetriNetFacade petriNet, MiriamWrapper identifier, MiriamRegistryWrapper mrw, String uriTrimmed, Object selectedItem) {
        List<CVTerm> cvts = petriNet.getProperty(MIRIAM_MODEL_QUALIFIERS);
        // Same identifier = update the uri
        if (identifier.getCVTerm().getModelQualifierType().equals((CVTerm.Qualifier) selectedItem)) {
            LOGGER.info("Same identifier, updating the uri");
            for (CVTerm cvt : cvts) {
                if ((identifier.getCVTerm().getModelQualifierType()).equals(cvt.getModelQualifierType())) {
                    cvt.getResources().set(cvt.getResources().indexOf(identifier.getURI()), mrw.getURL() + uriTrimmed);
                    break;
                }
            }
        } else { // new identifier = update the identifier and the uri
            LOGGER.info("New identifier, updating identifier and uri");
            // first: delete the old one
            CVTerm toRemove = null;
            for (CVTerm cvt : cvts) {
                if ((identifier.getCVTerm().getModelQualifierType()).equals(cvt.getModelQualifierType())) {
                    cvt.getResources().remove(cvt.getResources().indexOf(identifier.getURI()));
                    if (cvt.getResourceCount() == 0) {
                        toRemove = cvt;
                    }
                    break;
                }
            }
            if (toRemove != null) {
                LOGGER.info("Remove the old one");
                cvts.remove(toRemove);
                addProperty(petriNet, MIRIAM_MODEL_QUALIFIERS, cvts);
            }

            // now add the new ones
            LOGGER.info("Adding new ones");
            boolean qualifierWasThere = false;
            for (CVTerm cvt : cvts) {
                if (((CVTerm.Qualifier) selectedItem).equals(cvt.getModelQualifierType())) {
                    qualifierWasThere = true;
                    cvt.addResource(uriTrimmed);
                }
                addProperty(petriNet, MIRIAM_MODEL_QUALIFIERS, cvts);
            }

            if (!qualifierWasThere) {
                MiriamWrapper mw = new MiriamWrapper((CVTerm.Qualifier) selectedItem, mrw.getURL() + uriTrimmed);
                updateCVTerms(petriNet, "add", MIRIAM_MODEL_QUALIFIERS, mw.getCVTerm());
            }
        }
    }

    /**
     * Adds a property to a PetriNetEntity
     * @param <T>
     * @param ape
     * @param propName
     * @param value
     */
    public <T> void addProperty(AbstractPetriNetEntity ape, String propName, T value) {
        ape.putProperty(propName, value);
    }

    /**
     * Removes a property from a PetrinetEntity
     * @param ape
     * @param propName
     */
    public void removeProperty(AbstractPetriNetEntity ape, String propName) {
        ape.removeProperty(propName);
    }

    /**
     * Updates the CV terms for a Petri net entity.
     * @param ape
     * @param mode
     * @param propName
     * @param term
     */
    public void updateCVTerms(AbstractPetriNetEntity ape, String mode, String propName, CVTerm term) {
        if (mode.equals("add")) {
            ((List<CVTerm>) ape.getProperty(propName)).add(term);
        } else if (mode.equals("remove")) {
            ((List<CVTerm>) ape.getProperty(propName)).remove(term);
        }
    }
}
