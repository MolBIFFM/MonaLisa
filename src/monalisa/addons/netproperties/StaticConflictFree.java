/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netproperties;

import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Checks if a net is static conflict free.
 *
 * @author daniel
 */
public class StaticConflictFree extends NetPropertyAlgorithm<Boolean> {

    private static final Logger LOGGER = LogManager.getLogger(StaticConflictFree.class);

    public StaticConflictFree(PetriNetFacade pn) {
        super(pn);
    }

    /**
     * If every place has just one post transition, the net is static conflict
     * free.
     */
    @Override
    public void runAlgorithm() {
        LOGGER.info("Checking whether network is free of static conflicts");
        algorithmName = "static conflict free";
        algorithmValue = true;
        for (Place p : petriNet.places()) { //checks all Places.
            if (p.outputs().size() >= 2) {
                algorithmValue = false;
                break;
            }
        }
        LOGGER.info("Successfully checked whether network is free of static conflicts");
    }
}
