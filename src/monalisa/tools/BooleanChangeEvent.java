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

import java.util.EventObject;

/**
 * An event object holding the new value of a boolean property.
 *
 * @author Konrad Rudolph
 */
public class BooleanChangeEvent extends EventObject {

    private static final long serialVersionUID = 1467548627246806043L;

    private final boolean newValue;

    public BooleanChangeEvent(Object source, boolean newValue) {
        super(source);
        this.newValue = newValue;
    }

    /**
     * Returns the new value.
     */
    public boolean getNewValue() {
        return newValue;
    }
}
