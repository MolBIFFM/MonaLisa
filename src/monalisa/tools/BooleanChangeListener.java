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
 * The listener interface for update on changes in a boolean property.
 *
 * @author Konrad Rudolph
 */
public interface BooleanChangeListener extends EventListener {

    /**
     * Invoked to report a change in a boolean property.
     */
    void changed(BooleanChangeEvent e);
}
