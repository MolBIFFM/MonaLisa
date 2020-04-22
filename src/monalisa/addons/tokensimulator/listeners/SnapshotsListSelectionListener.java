/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.listeners;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.SimulationPanel;

/**
 * Handles the navigation through snapshots. When a snapshot is picked, the
 * snapshots marking and history are assigned to current state
 */
public class SnapshotsListSelectionListener implements ListSelectionListener {

    private final SimulationPanel simulationPan;
    private final JList snapshots;
    private final VisualizationViewer<NetViewerNode, NetViewerEdge> vv;


    public SnapshotsListSelectionListener(SimulationPanel simulationPan, JList snapshots, VisualizationViewer<NetViewerNode, NetViewerEdge> vv) {
        this.snapshots = snapshots;
        this.simulationPan = simulationPan;
        this.vv = vv;
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getValueIsAdjusting() == false) {
            int selectedVar = snapshots.getSelectedIndex();
            if (selectedVar > -1) {
                snapshots.clearSelection();
                simulationPan.loadSnap(selectedVar);
                vv.repaint();
            }
        }
    }
}
