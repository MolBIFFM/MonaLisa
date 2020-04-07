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

/**
 *
 * @author daniel
 */
public class SubConservative extends NetPropertyAlgorithm<Boolean> {

    private final Conservative conservative;

    public SubConservative(PetriNetFacade pn) {
        super(pn);
        conservative = new Conservative(pn);
    }

    /**
     * Checks, if all transitions add exactly as many tokens to their
     * post-places as they subtract from their pre-places.
     */
    @Override
    public void runAlgorithm() {

        conservative.runAlgorithm();
        algorithmValue = conservative.getSubConservativeValue();
    }
}
