/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netproperties;

import monalisa.data.pn.PetriNetFacade;

/**
 *
 * @author daniel
 */
public abstract class NetPropertieAlgorithm<T> implements NetPropertieInterface<T> {

    protected PetriNetFacade petriNet;
    protected T algorithmValue;
    protected String algorithmName;

    public NetPropertieAlgorithm(PetriNetFacade pn) {
        this.petriNet = pn;
        //this.algorithmValue = false;
        
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
