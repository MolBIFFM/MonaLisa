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
public abstract class NetPropertyAlgorithm<T> implements NetPropertyInterface<T> {

    protected PetriNetFacade petriNet;
    protected T algorithmValue;
    protected String algorithmName;

    public NetPropertyAlgorithm(PetriNetFacade pn) {
        this.petriNet = pn;
    }


    /**
     * The return function returns the Value of the Algorithm. The return Type depends on the input value.
     * It's used by all netProperti - Algorithms.
     */
    @Override
    public T returnAlgorithmValue() {
        return algorithmValue;
    }

    /**
     *
     * @return
     */
    @Override
    public String getAlgorithmName() {
        return algorithmName;
    }
}
