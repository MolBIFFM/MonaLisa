/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.data.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import monalisa.data.pn.PetriNet;

/**
 * An interface representing export handlers for Petri nets in specific file
 * formats.
 * @author Konrad Rudolph
 */
public interface OutputHandler {
    /**
     * Save the specified {@code petriNet} into the {@code out}put stream.
     * @param pn The Petri net.
     * @param fileOutputStream The output stream.
     */
     void save(FileOutputStream fileOutputStream, PetriNet pn);

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

    File checkFileNameForExtension(File file);

    String getExtension();

    String getDescription();
}
