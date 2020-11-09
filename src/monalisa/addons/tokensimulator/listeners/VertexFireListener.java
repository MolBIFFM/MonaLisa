/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.listeners;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.data.pn.Transition;

/**
 * If a transitions is picked and is active, it will be fired and visual output
 * will be updated.
 */
public class VertexFireListener implements ItemListener {

    private final SimulationManager simulationMan;
    private final VisualizationViewer<NetViewerNode, NetViewerEdge> vv;

    public VertexFireListener(SimulationManager simulationManager, VisualizationViewer vv) {
        this.simulationMan = simulationManager;
        this.vv = vv;
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            Set<NetViewerNode> pickedVertices = vv.getPickedVertexState().getPicked();
            if (pickedVertices.size() == 1) {
                final NetViewerNode node = pickedVertices.iterator().next();
                if (node.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
                    Transition t = simulationMan.getPetriNet().findTransition(node.getMasterNode().getId());
                    if (simulationMan.getActiveTransitions().contains(t)) {
                        simulationMan.fireTransitions(t);
                        simulationMan.updateVisualOutput();
                    }
                    vv.getPickedVertexState().clear();
                }
            }
        }
    }
}
