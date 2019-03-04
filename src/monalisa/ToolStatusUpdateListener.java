/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa;

import java.util.EventListener;

import monalisa.tools.Tool;

/**
 * A listener for changes in the execution status of a {@link Tool}.
 * @author Konrad Rudolph
 */
public interface ToolStatusUpdateListener extends EventListener {
    /**
     * Invoked to report a status update.
     */
    void updated(ToolStatusUpdateEvent e);
}
