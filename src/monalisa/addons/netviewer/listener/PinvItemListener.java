/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer.listener;

import monalisa.addons.netviewer.wrapper.PinvWrapper;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.ToolBar;
import monalisa.data.pn.PInvariant;
import monalisa.data.pn.Place;

/**
 *
 * @author Jens Einloft
 */
public class PinvItemListener implements ItemListener {
    private final JComboBox cb;
    private final NetViewer nv;
    private final ToolBar tb;

    /**
     * Init a new Listener
     * @param nv
     * @param cb
     * @param color         *
     */
    public PinvItemListener(NetViewer nv, ToolBar tb, JComboBox cb) {
        this.cb = cb;
        this.tb = tb;
        this.nv = nv;
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        if(ie.getStateChange() == ItemEvent.SELECTED) {
            if(cb.getSelectedItem() == null)
                return;
            if(cb.getSelectedIndex() == 0) {
                nv.resetColor();
                return;
            }
            if(!cb.getSelectedItem().getClass().getSimpleName().equalsIgnoreCase("PinvWrapper"))
                return;

            nv.resetMessageLabel();

            if(!tb.stackSelection()) {
                nv.resetColor();                    
            } 

            Color choosenColor;
            if(tb.manuellColorSelection()) {
                choosenColor = JColorChooser.showDialog(null, "Select color", null);
            } else {
                choosenColor = NetViewer.PINV_COLOR;
            }                 

            PInvariant pinv = ((PinvWrapper)cb.getSelectedItem()).getPinv();
            Set<Place> places = pinv.places();

            Iterator<Place> it = places.iterator();
            while(it.hasNext()) {
               nv.getNodeFromVertex(it.next()).getMasterNode().setColorForAllNodes(choosenColor);
            }

            nv.getVisualizationViewer().repaint();
        }   
    }
}
