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
import monalisa.addons.tokensimulator.SimulationManager;
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
    private final SimulationManager simulationMan;
    private final JList history;

    public HistorySelectionListener(JButton back, JButton forward, JList history, SimulationManager simulationManager) {
        this.back = back;
        this.forward = forward;
        this.history = history;
        this.simulationMan = simulationManager;
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
                while (selectedVar < simulationMan.lastHistoryStep) {
                    simulationMan.reverseFireTransitions(simulationMan.historyArrayList.get(simulationMan.lastHistoryStep--));
                }
                //if the selected step was performed after the curren step, perform steps between selected and lastHistoryStep and update the visual output after the last firing
                while (selectedVar > simulationMan.lastHistoryStep) {
                    simulationMan.fireTransitions(false, simulationMan.historyArrayList.get(++simulationMan.lastHistoryStep));
                }
                back.setEnabled(simulationMan.lastHistoryStep > -1);
                forward.setEnabled(simulationMan.lastHistoryStep < simulationMan.historyArrayList.size() - 1);
                simulationMan.updateVisualOutput();

                history.repaint();
                history.ensureIndexIsVisible(simulationMan.lastHistoryStep);
                history.setEnabled(true);
            }
        }
    }
}
