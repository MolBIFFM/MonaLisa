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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Algorithm to check if the multiplicity of every arc equals one
 *
 * @author daniel
 */
public class Ordinary extends NetPropertyAlgorithm<Boolean> {

    private static final Logger LOGGER = LogManager.getLogger(Ordinary.class);

    public Ordinary(PetriNetFacade pn) {
        super(pn);
    }

    /**
     * A net is ordinary, if the multiplicity of every arc equals one.
     *
     * @return true if the petri net is ordinary, otherwise false.
     */
    @Override
    public void runAlgorithm() {
        LOGGER.info("Checks whether net is ordinary");
        algorithmName = "ordinary";
        algorithmValue = true;
        for (Place p : petriNet.places()) { //checks all Places.
            for (Transition t : p.inputs()) { //checks all input Transitions of the Place p.
                if (petriNet.getArc(t, p).weight() != 1) {
                    algorithmValue = false;
                    break;
                }
            }
            for (Transition t : p.outputs()) { //checks all output Transitions of the Place p.
                if (petriNet.getArc(p, t).weight() != 1) {
                    algorithmValue = false;
                    break;
                }
            }
        }
        LOGGER.info("Successfully checked whether net is ordinary");
    }
}
