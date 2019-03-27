/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer.wrapper;
import monalisa.data.pn.PInvariant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Wrapper Class for Tinv Combobox Items
 * @author Jens Einloft
 */
public class PinvWrapper {
    private final PInvariant pinv;
    private static final Logger LOGGER = LogManager.getLogger(PinvWrapper.class);

    public PinvWrapper(PInvariant input) {
        this.pinv = input;
        LOGGER.debug("Created new PinvWrapper " + (this.pinv.id())+" ("+this.pinv.size()+")");
    }

    public PInvariant getPinv() {
        return pinv;
    }

    @Override
    public String toString() {
        if(this.pinv.id() >= 0)
            return "P-Inv "+(this.pinv.id()+1)+" ("+this.pinv.size()+")";
        else
            return "Combined Place Invariants "+" ("+this.pinv.size()+")";
    }
}
