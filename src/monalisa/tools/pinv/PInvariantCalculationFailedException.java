/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.pinv;

@SuppressWarnings("serial")
public final class PInvariantCalculationFailedException extends Exception {

    public PInvariantCalculationFailedException(Exception cause) {
        super(cause);
    }
}
