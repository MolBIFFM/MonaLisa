/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.netviewer.listener;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JColorChooser;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.ToolBar;
import monalisa.addons.netviewer.wrapper.MinvWrapper;
import monalisa.data.pn.MInvariant;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author daniel
 */
public class MinvSelectionListener implements ListSelectionListener {

    private final JList js;
    private final Boolean colorPlaces;
    private final NetViewer nv;
    private Boolean blocked;
    private final ToolBar tb;
    private final Logger LOGGER = LogManager.getLogger(MinvSelectionListener.class);

    public MinvSelectionListener(NetViewer nv, ToolBar tb, JList js, Boolean colorPlaces) {
        this.js = js;
        this.tb = tb;
        this.colorPlaces = colorPlaces;;
        this.nv = nv;

        this.blocked = true;
    }

    public void enableSelectionListener() {
        this.blocked = false;
    }

    @Override
    public void valueChanged(ListSelectionEvent se) {
        boolean adjust = se.getValueIsAdjusting();
        if (!adjust) {
            LOGGER.debug("New M-Invariant selected, changing colouring");
            // Nothing is selected
            if (js.getSelectedValue() == null) {
                return;
            }

            List<MinvWrapper> selectionValues;
            selectionValues = js.getSelectedValuesList();
            
            for (Object sv : selectionValues) {
                nv.resetMessageLabel();
                if (!tb.stackSelection()) {
                    nv.switchColors();
                    nv.resetColor();
                }

                Color chosenColor;
                if (tb.manuellColorSelection()) {
                    chosenColor = JColorChooser.showDialog(null, "Select color", null);
                    if (chosenColor == null) {
                        chosenColor = NetViewer.MINV_COLOR;
                    }
                } else {
                    chosenColor = NetViewer.MINV_COLOR;
                }

                Set<Transition> transitions;
                Iterator<Transition> it;
                Transition t;
                float min = 0, max, maxmin = 0;
                float[] hsbvals = null;
                float norm;
                List<Integer> allFactors = new ArrayList<>();

                MInvariant tinv = ((MinvWrapper) sv).getMinv();
                transitions = new HashSet<>(tinv.transitions());

                // Find minimum and maximum value
                if (nv.getHeatMap()) {
                    hsbvals = Color.RGBtoHSB(chosenColor.getRed(), chosenColor.getGreen(), chosenColor.getBlue(), null);
                    allFactors.clear();
                    it = transitions.iterator();
                    while (it.hasNext()) {
                        allFactors.add(tinv.factor(it.next()));
                    }
                    min = (float) Collections.min(allFactors);
                    max = (float) Collections.max(allFactors);
                    maxmin = max - min;
                }

                // Coloring of the transitions, edges and places
                it = transitions.iterator();
                while (it.hasNext()) {
                    t = it.next();
                    if (nv.getHeatMap()) {
                        norm = (tinv.factor(t) - min) / maxmin;
                        if (norm < 0.05) {
                            norm = (float) 0.05;
                        }
                        chosenColor = new Color(Color.HSBtoRGB(hsbvals[0], norm, hsbvals[2]));
                    }

                    nv.getNodeFromVertex(t).setColor(chosenColor);
                    if (colorPlaces) {
                        // Color all in/out edges and their source/aim places
                        for (NetViewerEdge nvEdge : nv.getNodeFromVertex(t).getOutEdges()) {
                            nvEdge.getMasterEdge().setColorForAllEdges(chosenColor);
                            if (nvEdge.getMasterEdge().getAim().getNodeType().equals(NetViewer.PLACE)) {
                                nvEdge.getMasterEdge().getAim().getMasterNode().setColorForAllNodes(chosenColor);
                            }
                        }

                        for (NetViewerEdge nvEdge : nv.getNodeFromVertex(t).getInEdges()) {
                            nvEdge.getMasterEdge().setColorForAllEdges(chosenColor);
                            if (nvEdge.getMasterEdge().getSource().getNodeType().equals(NetViewer.PLACE)) {
                                nvEdge.getMasterEdge().getSource().getMasterNode().setColorForAllNodes(chosenColor);
                            }
                        }
                    }
                }
            }
            nv.getVisualizationViewer().repaint();
            LOGGER.debug("Successfully changed colouring on new M-Invariant selection");
        }
    }
}
