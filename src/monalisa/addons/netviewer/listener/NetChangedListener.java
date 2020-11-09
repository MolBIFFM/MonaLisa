/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer.listener;

/**
 * This event is triggered, if the Petri net structure is changed. That makes
 * all calculated results "false". It also may causes the reset of AddOns. If
 * your AddOn neither, calculate stuff based on the Petri net, or need a rest,
 * do not implement anything inside netChanged()
 *
 * @author jens
 */
public interface NetChangedListener {

    /**
     * Is called from the NetViewer, if the Petri net is changed.
     */
    public void netChanged();

}
