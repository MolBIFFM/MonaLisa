/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools;

import java.util.EventListener;

/**
 * The listener interface for receiving progess events.
 *
 * @author Anja Thormann
 * @author Konrad Rudolph
 */
public interface ProgressListener extends EventListener {

    /**
     * Invoked to report progress.
     */
    void progressUpdated(ProgressEvent e);
}
