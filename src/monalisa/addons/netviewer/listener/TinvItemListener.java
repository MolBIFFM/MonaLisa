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

import monalisa.addons.netviewer.wrapper.TinvWrapper;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.ToolBar;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.Transition;

/**
 *
 * @author Jens Einloft
 */
public class TinvItemListener implements ItemListener {
    private final JComboBox cb;
    private final Boolean colorPlaces;
    private final NetViewer nv;
    private Boolean blocked;
    private final ToolBar tb;

   public TinvItemListener(NetViewer nv, ToolBar tb, JComboBox cb, Boolean colorPlaces) {
        this.cb = cb;
        this.tb  = tb;
        this.colorPlaces = colorPlaces;;
        this.nv = nv;
        
        this.blocked = true;
    }
   
    public void enableItemListener() {
        this.blocked = false;
    }
   
    @Override
    public void itemStateChanged(ItemEvent ie) {
        if(ie.getStateChange() == ItemEvent.SELECTED && !this.blocked) {
            // Nothing is selected
            if(cb.getSelectedItem() == null)
                return;
            if(cb.getSelectedIndex() == 0) {
                nv.resetColor();
                return;
            }
            // If, for what ever reason, the item is not a Wrapper
            if(!cb.getSelectedItem().getClass().getSimpleName().equalsIgnoreCase("TinvWrapper"))
                return;

            nv.resetMessageLabel();

            if(!tb.stackSelection()) {
                nv.resetColor();                    
            } 

            Color choosenColor;
            if(tb.manuellColorSelection()) {
                choosenColor = JColorChooser.showDialog(null, "Select color", null);
            } else {
                choosenColor = NetViewer.TINV_COLOR;
            }             

            Set<Transition> transitions;  
            Iterator<Transition> it;
            Transition t;                
            float min = 0, max, maxmin = 0;
            float[] hsbvals = null;
            float norm;
            List<Integer> allFactors = new ArrayList<>();                

            TInvariant tinv = ((TinvWrapper)cb.getSelectedItem()).getTinv();
            transitions = new HashSet<>(tinv.transitions());

            // Find minimum and maximum value
            if(nv.getHeatMap()) {
                hsbvals = Color.RGBtoHSB(choosenColor.getRed(), choosenColor.getGreen(), choosenColor.getBlue(), null);
                allFactors.clear();                                                                                        
                it = transitions.iterator();
                while(it.hasNext()) {
                    allFactors.add(tinv.factor(it.next()));
                }
                min = (float)Collections.min(allFactors);
                max = (float)Collections.max(allFactors);
                maxmin = max-min;
            }

            // Coloring of the transitions, edges and places                   
            it = transitions.iterator();                                       
            while(it.hasNext()) {
                t = it.next();
                if(nv.getHeatMap()) {
                    norm = (tinv.factor(t) - min) / maxmin;
                    if(norm < 0.05)
                        norm = (float)0.05;
                    choosenColor = new Color(Color.HSBtoRGB(hsbvals[0], norm, hsbvals[2]));
                }

                nv.getNodeFromVertex(t).setColor(choosenColor);
                if(colorPlaces) {
                    // Color all in/out edges and their source/aim places
                    for(NetViewerEdge nvEdge : nv.getNodeFromVertex(t).getOutEdges()) {
                        nvEdge.getMasterEdge().setColorForAllEdges(choosenColor);
                        if(nvEdge.getMasterEdge().getAim().getNodeType().equals(NetViewer.PLACE))
                            nvEdge.getMasterEdge().getAim().getMasterNode().setColorForAllNodes(choosenColor);
                    }

                    for(NetViewerEdge nvEdge : nv.getNodeFromVertex(t).getInEdges()) {
                        nvEdge.getMasterEdge().setColorForAllEdges(choosenColor);
                        if(nvEdge.getMasterEdge().getSource().getNodeType().equals(NetViewer.PLACE))
                            nvEdge.getMasterEdge().getSource().getMasterNode().setColorForAllNodes(choosenColor);
                    }
                }
            }

            nv.getVisualizationViewer().repaint();
        }
    }
        
}
