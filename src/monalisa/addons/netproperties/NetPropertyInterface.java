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

/**
 * An interface representing Petri net algorithms.
 * @author daniel noll
 */
public interface NetPropertyInterface<T> {   
    public void runAlgorithm();
    public T returnAlgorithmValue();
    public String getAlgorithmName();
}
