/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.listeners;

import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import monalisa.addons.tokensimulator.SimulationPanel;
import monalisa.addons.tokensimulator.utils.Statistic;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.jfree.data.xy.XYSeries;

/**
 * Handles selection of a marking from the customMarkingsComboBox.
 */
public class CustomMarkingsComboBoxPopupListener implements PopupMenuListener {

    private boolean canceled = false;
    private final SimulationPanel simPanel;
    private final SimulationManager simulationMan;
    private final JComboBox combobox;

    public CustomMarkingsComboBoxPopupListener(SimulationPanel simPanel, SimulationManager simulationManager, JComboBox combobox) {
        this.simPanel = simPanel;
        this.simulationMan = simulationManager;
        this.combobox = combobox;
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
        canceled = false;
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
        //only if the popup becomes invisible in the cause of selecting an item by user.
        if (!canceled) {
            //get the name of selected marking
            String markingName = (String) combobox.getSelectedItem();
            /*
           Put the values from the selected marking into current marking.
             */
            simulationMan.marking.putAll(simulationMan.customMarkingsMap.get(markingName));
            //re-compute active transitions
            simulationMan.getTokenSim().addTransitionsToCheck(simulationMan.getPetriNet().transitions().toArray(new Transition[0]));
            simulationMan.getTokenSim().computeActiveTransitions();
            //clear history
            simulationMan.historyListModel.clear();
            simulationMan.historyArrayList.clear();
            simulationMan.lastHistoryStep = -1;
            //clear snapshots
            simPanel.getSnapshots().clear();
            simPanel.snapshotsListModel.clear();
            //clear statistic
            simulationMan.totalStepNr = 0;
            simulationMan.currStatistic = new Statistic(simulationMan);
            for (Map.Entry<Place, XYSeries> entr : simPanel.seriesMap.entrySet()) {
                entr.getValue().clear();
                entr.getValue().add(simulationMan.getTokenSim().getSimulatedTime(), simulationMan.getTokenSim().getTokens(entr.getKey().id()), false);
            }

            simulationMan.updateVisualOutput();
        }
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent pme) {
        canceled = true;
    }
}
