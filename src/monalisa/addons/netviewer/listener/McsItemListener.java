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

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.ToolBar;
import monalisa.addons.netviewer.wrapper.McsWrapper;

/**
 * Controls the selection of a MCT Set
 * @author Jens Einloft
 */
public class McsItemListener implements ItemListener {
    private final JComboBox cb;
    private final NetViewer nv;
    private final ToolBar tb;

    public McsItemListener(NetViewer nv, ToolBar tb, JComboBox cb) {
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
            if(!cb.getSelectedItem().getClass().getSimpleName().equalsIgnoreCase("McsWrapper")) {
                return;
            }

            if(!tb.stackSelection()) {
                nv.resetColor();                    
            } 

            Color choosenColor;
            if(tb.manuellColorSelection()) {
                choosenColor = JColorChooser.showDialog(null, "Select color", null);
            } else {
                choosenColor = NetViewer.MCS_COLOR;
            }                 

            nv.resetMessageLabel();

            McsWrapper mcsWrapper = (McsWrapper)cb.getSelectedItem();
            
            nv.colorTransitions(NetViewer.MCSOBJECTIV_COLOR, mcsWrapper.getObjective());      
            nv.colorTransitions(mcsWrapper.getMcs(), choosenColor);             
        }   
    }     
}
