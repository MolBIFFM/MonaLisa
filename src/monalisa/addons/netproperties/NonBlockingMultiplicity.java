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
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author daniel
 */
public class NonBlockingMultiplicity extends NetPropertyAlgorithm<Boolean> {

    private static final Logger LOGGER = LogManager.getLogger(NonBlockingMultiplicity.class);

    public NonBlockingMultiplicity(PetriNetFacade pn) {
        super(pn);
    }

    /**
     * Checks if for each place p, the minimum of multiplicities of the arcs
     * ending at p is not less than the maximum of multiplicities of the arcs
     * starting at p.
     */
    @Override
    public void runAlgorithm() {
        LOGGER.info("Checking whether non-blocking multiplicity applies");
        algorithmName = "non-blocking multiplicity";
        algorithmValue = true;
        for (Place p : petriNet.places()) { //checks all Places.
            int minMultiplicity = Integer.MAX_VALUE;
            for (Transition t : p.inputs()) { //checks all input Transitions of the Place p.
                if (petriNet.getArc(t, p).weight() < minMultiplicity) {
                    minMultiplicity = petriNet.getArc(t, p).weight();
                }
            }
            for (Transition t : p.outputs()) { //checks all output Transitions of the Place p.
                if (minMultiplicity < petriNet.getArc(p, t).weight()) {
                    algorithmValue = false;
                    break;
                }
            }
        }
        LOGGER.info("Successfully checked whether non-blocking multiplicity applies");
    }
}
