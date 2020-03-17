/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.minv;

/**
 *
 * @author daniel
 */

@SuppressWarnings("serial")
public final class MInvariantCalculationFailedException extends Exception {
    public MInvariantCalculationFailedException(Exception cause) {
        super(cause);
    }
}
