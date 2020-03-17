/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.data.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import monalisa.data.pn.PetriNet;
import monalisa.results.Result;

/**
 * An interface representing import handlers for a given T Invariant file format.
 * @author Jens Einloft
 */
public interface TInputHandler {
    /**
     * Load a T Invariant model from the specified input stream {@code in}.
     * @param in The input stream to read from.
     * @throws IOException If an error occurs while trying to read the file.
     */
    Result load(InputStream in, PetriNet petriNe) throws IOException;
    
    /**
     * Determine whether the given {@code file} is in a known format and can be
     * read by this handler. A typical test may look at the file extension, or
     * at the XML schema used (if the file happens to be in XML format).
     * @param file The file.
     * @return <code>true</code>, if the file format is known to this handler,
     * else <code>false</code>.
     * @throws IOException If an error occurs while trying to access the file.
     */
    boolean isKnownFile(File file) throws IOException;
}
