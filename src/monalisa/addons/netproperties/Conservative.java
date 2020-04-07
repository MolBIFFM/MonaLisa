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
 *
 * @author daniel
 */
public class Conservative extends NetPropertyAlgorithm<Boolean> {

    private boolean subConservative;
    private static final Logger LOGGER = LogManager.getLogger(Conservative.class);

    public Conservative(PetriNetFacade pn) {
        super(pn);
    }

    /**
     * Checks, if all transitions add exactly as many tokens to their
     * post-places as they subtract from their pre-places.
     */
    @Override
    public void runAlgorithm() {
        LOGGER.info("Checking whether net is conservative and / or subconservative");
        algorithmName = "conservative";
        algorithmValue = true;
        subConservative = true;
        int tokenSumInput;
        int tokenSumOutput;
        for (Transition t : petriNet.transitions()) { //checks all Transitions.
            tokenSumInput = 0;
            tokenSumOutput = 0;
            for (Place p : t.inputs()) { //checks all input Places of the Transition t.
                tokenSumInput += petriNet.getArc(p, t).weight();
            }
            for (Place p : t.outputs()) {
                tokenSumOutput += petriNet.getArc(t, p).weight();
            }
            if (tokenSumInput != tokenSumOutput) {
                algorithmValue = false;
            }
            if (tokenSumOutput > tokenSumInput) {
                subConservative = false;
                break;
            }
        }
        LOGGER.info("Successfully checked whether net is conservative and / or subconservative");
    }

    /**
     * A net is sub-conservative, if all transitions add at most as many tokens
     * to their post-place as they subtract from their pre-places.
     *
     * @return true, if the net is sub-conservative.
     */
    public boolean getSubConservativeValue() {
        return subConservative;
    }

}
