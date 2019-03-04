/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Molekulare Bioinformatik, Goethe University Frankfurt, Frankfurt am Main, Germany
 *
 */

package monalisa.addons.treeviewer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;

/**
 *
 * @author Jens Einloft
 */
public class ClusterComboBoxItemListener implements ItemListener {

    private final JComboBox cb;
    private final TreeViewer tv;
    
    private boolean disabled = true;
    
    public ClusterComboBoxItemListener(TreeViewer tv, JComboBox cb) {
        this.cb = cb;
        this.tv = tv;
    }
    
    public void setDisabled(Boolean val) {
        this.disabled = val;
    }
    
    @Override
    public void itemStateChanged(ItemEvent ie) {
        if(!disabled) {
            tv.showClustering((ClusteringWrapper) cb.getModel().getSelectedItem());
        }
    }

}
