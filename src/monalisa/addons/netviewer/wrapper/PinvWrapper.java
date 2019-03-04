/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer.wrapper;
import monalisa.data.pn.PInvariant;


/**
 * Wrapper Class for Tinv Combobox Items
 * @author Jens Einloft
 */
public class PinvWrapper {
    private final PInvariant pinv;

    public PinvWrapper(PInvariant input) {
        this.pinv = input;
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
