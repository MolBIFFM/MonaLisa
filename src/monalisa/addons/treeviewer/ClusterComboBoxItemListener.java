/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.treeviewer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class ClusterComboBoxItemListener implements ItemListener {

    private final JComboBox cb;
    private final TreeViewer tv;
    private static final Logger LOGGER = LogManager.getLogger(ClusterComboBoxItemListener.class);

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
            LOGGER.debug("Handling clustering selection");
            tv.showClustering((ClusteringWrapper) cb.getModel().getSelectedItem());
            LOGGER.debug("Successfully handled clustering selection");
        }
    }
}
