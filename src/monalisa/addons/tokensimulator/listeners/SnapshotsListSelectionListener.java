/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.listeners;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import monalisa.addons.tokensimulator.TokenSimulator;

/**
 * Handles the navigation through snapshots. When a snapshot is picked, the
 * snapshots marking and history are assigned to current state
 */
public class SnapshotsListSelectionListener implements ListSelectionListener {

    private final TokenSimulator ts;
    private final JList snapshots;

    public SnapshotsListSelectionListener(TokenSimulator ts, JList snapshots) {
        this.snapshots = snapshots;
        this.ts = ts;
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getValueIsAdjusting() == false) {
            int selectedVar = snapshots.getSelectedIndex();
            if (selectedVar > -1) {
                snapshots.clearSelection();
                ts.loadSnapshot(ts.snapshots.get(selectedVar));
            }
        }
    }
}
