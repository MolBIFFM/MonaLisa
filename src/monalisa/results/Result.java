/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.results;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import monalisa.Project;
import monalisa.tools.Tool;

/**
 * Any single result produced by a {@link Tool}.
 * Results hold data and can be exported.
 * Every possible result type of a tool should implement this interface.
 * @author Konrad Rudolph
 */
public interface Result extends Serializable {
    static final long serialVersionUID = -3722342948454708055L;

    /**
     * @return The filename extension associated with this result data, without
     *          the prefixed dot (e.g. <code>xml</code>, not <code>.xml</code>).
     */
    String filenameExtension();
    
    /**
     * Export the result data to a file, identified by {@code path}.
     * @param path The output file path.
     * @param config The exact configuration belonging to this result.
     *          To avoid redundancies, configurations should <em>not</em> be
     *          stored inside the result, therefore they are passed to this
     *          method.
     * @param project The associated project, holding additional information.
     * @throws IOException Thrown if an error occurred while exporting.
     */
    void export(File path, Configuration config, Project project) throws IOException;
}
