/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools;

import java.util.EventObject;

/**
 * An event indicating progess in a {@link Tool}'s work.
 * @author Konrad Rudolph
 */
public final class ProgressEvent extends EventObject {
    private static final long serialVersionUID = -3665438681517655877L;
    
    private final int percent;

    public ProgressEvent(Object source, int percent) {
        super(source);
        this.percent = percent;
    }

    /**
     * Returns the approximate amount of progress made, relative to the
     * overall work, in percent.
     * @return The progress percentage of the work.
     */
    public int getPercent() {
        return percent;
    }
}
