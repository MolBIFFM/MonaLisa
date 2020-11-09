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

import monalisa.addons.netviewer.wrapper.MctsWrapper;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.ToolBar;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Controls the selection of a MCT Set
 *
 * @author Jens Einloft
 */
public class MctsItemListener implements ItemListener {

    private final JComboBox cb;
    private final NetViewer nv;
    private final ToolBar tb;
    private static final Logger LOGGER = LogManager.getLogger(MctsItemListener.class);

    public MctsItemListener(NetViewer nv, ToolBar tb, JComboBox cb) {
        this.cb = cb;
        this.tb = tb;
        this.nv = nv;
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            LOGGER.debug("New MCTS-Item selected, changing colouring");
            if (cb.getSelectedItem() == null) {
                return;
            }
            if (cb.getSelectedIndex() == 0) {
                nv.resetColor();
                return;
            }
            if (!cb.getSelectedItem().getClass().getSimpleName().equalsIgnoreCase("MctsWrapper")) {
                return;
            }

            if (!tb.stackSelection()) {
                nv.resetColor();
            }

            Color chosenColor;
            if (tb.manuellColorSelection()) {
                chosenColor = JColorChooser.showDialog(null, "Select color", null);
            } else {
                chosenColor = NetViewer.MCTS_COLOR;
            }

            nv.resetMessageLabel();

            nv.colorTransitions(((MctsWrapper) cb.getSelectedItem()).getMcts().transitions(), chosenColor);

            nv.getVisualizationViewer().repaint();
            LOGGER.debug("Successfully changed colouring on new MCS-Item selection");
        }
    }
}
