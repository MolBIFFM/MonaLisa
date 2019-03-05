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
import monalisa.data.pn.TInvariant;

/**
 * Wrapper Class for Tinv Combobox Items
 * @author Jens Einloft
 */
public class TinvWrapper {
    private final TInvariant tinv;

    public TinvWrapper(TInvariant input) {
        this.tinv = input;
    }

    public TInvariant getTinv() {
        return tinv;
    }

    @Override
    public String toString() {
        if(this.tinv.id() >= 0)
            return "Elementary mode "+(this.tinv.id()+1)+" ("+this.tinv.size()+")";
        else
            return "Combined elementary modes "+" ("+this.tinv.size()+")";
    }
}
