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
import monalisa.data.pn.TInvariant;

/**
 * Wrapper Class for Mcts Combobox Items
 * @author Jens Einloft
 */
public class MctsWrapper {
    private final TInvariant tinv;

    public MctsWrapper(TInvariant input) {
        this.tinv = input;
    }

    public TInvariant getMcts() {
        return tinv;
    }    
    
    @Override
    public String toString() {
        return "MCT-Set "+(this.tinv.id()+1)+" ("+this.tinv.size()+")";
    }
}
