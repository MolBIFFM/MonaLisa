/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.listeners;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import monalisa.addons.tokensimulator.TokenSimulator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles navigation through the history. When a history entry in historyList
 * is picked, all steps between the current state and picked state will be
 * performed (if picked state is later than current state) or reverse-fired (if
 * picked state is earlier than current state); i.e. the picked state will be
 * the last performed state.
 */
public class HistorySelectionListener implements ListSelectionListener {

    private static final Logger LOGGER = LogManager.getLogger(HistorySelectionListener.class);
    private final JButton back;
    private final JButton forward;
    private final TokenSimulator ts;
    private final JList history;

    public HistorySelectionListener(JButton back, JButton forward, JList history, TokenSimulator ts) {
        this.back = back;
        this.forward = forward;
        this.history = history;
        this.ts = ts;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        LOGGER.info("New Entry chosen from history list");
        if (e.getValueIsAdjusting() == false) {
            int selectedVar = history.getSelectedIndex();
            if (selectedVar > -1) {
                history.clearSelection();
                //if the last step was performed later than the picked state, reverse-fire all steps between the last performer step and the picked
                history.setEnabled(false);
                while (selectedVar < ts.lastHistoryStep) {
                    ts.reverseFireTransitions(ts.historyArrayList.get(ts.lastHistoryStep--));
                }
                //if the selected step was performed after the curren step, perform steps between selected and lastHistoryStep and update the visual output after the last firing
                while (selectedVar > ts.lastHistoryStep) {
                    ts.fireTransitions(false, ts.historyArrayList.get(++ts.lastHistoryStep));
                }
                back.setEnabled(ts.lastHistoryStep > -1);
                forward.setEnabled(ts.lastHistoryStep < ts.historyArrayList.size() - 1);
                ts.updateVisualOutput();

                history.repaint();
                history.ensureIndexIsVisible(ts.lastHistoryStep);
                history.setEnabled(true);
            }
        }
    }
}
