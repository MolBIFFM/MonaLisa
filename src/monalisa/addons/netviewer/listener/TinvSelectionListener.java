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
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JColorChooser;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.ToolBar;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class TinvSelectionListener implements ListSelectionListener {

    private final JList js;
    private final Boolean colorPlaces;
    private final NetViewer nv;
    private Boolean blocked;
    private final ToolBar tb;
    private final Logger LOGGER = LogManager.getLogger(TinvSelectionListener.class);

    public TinvSelectionListener(NetViewer nv, ToolBar tb, JList js, Boolean colorPlaces) {
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
            LOGGER.debug("New T-Invariant selected, changing colouring");
            // Nothing is selected
            if (js.getSelectedValue() == null) {
                return;
            }

            List<TinvWrapper> selectionValues;
            selectionValues = js.getSelectedValuesList();

            for (Object sv : selectionValues) {
                // if an object is selected, that is not a Wrapper
                if (!sv.getClass().getSimpleName().equalsIgnoreCase("TinvWrapper")) {
                    nv.resetColor();
                } else {
                    nv.resetMessageLabel();

                    if (!tb.stackSelection()) {
                        nv.resetColor();
                    }

                    Color chosenColor;
                    if (tb.manuellColorSelection()) {
                        chosenColor = JColorChooser.showDialog(null, "Select color", null);
                    } else {
                        chosenColor = NetViewer.TINV_COLOR;
                    }

                    Set<Transition> transitions;
                    Iterator<Transition> it;
                    Transition t;
                    float min = 0, max, maxmin = 0;
                    float[] hsbvals = null;
                    float norm;
                    List<Integer> allFactors = new ArrayList<>();

                    TInvariant tinv = ((TinvWrapper) sv).getTinv();
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
            }
            nv.getVisualizationViewer().repaint();
            LOGGER.debug("Successfully changed colouring on new T-Invariant selection");
        }
    }
}
