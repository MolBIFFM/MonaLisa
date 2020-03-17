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

import monalisa.addons.netviewer.wrapper.PinvWrapper;
import java.awt.Color;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import javax.swing.JColorChooser;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.ToolBar;
import monalisa.data.pn.PInvariant;
import monalisa.data.pn.Place;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class PinvSelectionListener implements ListSelectionListener {
    private final JList js;
    private final NetViewer nv;
    private final ToolBar tb;
    private static final Logger LOGGER = LogManager.getLogger(PinvSelectionListener.class);

    /**
     * Init a new Listener
     * @param nv
     * @param tb
     * @param js
     */
    public PinvSelectionListener(NetViewer nv, ToolBar tb, JList js) {
        this.js = js;
        this.tb = tb;
        this.nv = nv;
    }

    @Override
    public void valueChanged(ListSelectionEvent se) {
        boolean adjust = se.getValueIsAdjusting();
        if(!adjust) {
            LOGGER.debug("New P-Invariant selected, changing colouring");
            if(js.getSelectedValue() == null)
                return;
            
        
            List<PinvWrapper> selectionValues = new ArrayList<PinvWrapper>();
            selectionValues = js.getSelectedValuesList();
            
            for(PinvWrapper sv : selectionValues){

                nv.resetMessageLabel();

                if(!tb.stackSelection()) {
                    nv.resetColor();
                }

                Color chosenColor;
                if(tb.manuellColorSelection()) {
                    chosenColor = JColorChooser.showDialog(null, "Select color", null);
                } else {
                    chosenColor = NetViewer.PINV_COLOR;
                }

                PInvariant pinv = ((PinvWrapper)sv).getPinv();
                Set<Place> places = pinv.places();

                Iterator<Place> it = places.iterator();
                while(it.hasNext()) {
                   nv.getNodeFromVertex(it.next()).getMasterNode().setColorForAllNodes(chosenColor);
                }
            }
            nv.getVisualizationViewer().repaint();
            LOGGER.debug("Successfully changed colouring on new P-Invariant selection");
        }
    }
}
