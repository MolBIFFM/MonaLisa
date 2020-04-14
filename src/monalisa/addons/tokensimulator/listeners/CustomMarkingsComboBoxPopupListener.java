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
import monalisa.addons.tokensimulator.utils.Statistic;
import monalisa.addons.tokensimulator.TokenSimulator;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.jfree.data.xy.XYSeries;

/**
 * Handles selection of a marking from the customMarkingsComboBox.
 */
public class CustomMarkingsComboBoxPopupListener implements PopupMenuListener {

    private boolean canceled = false;
    private final TokenSimulator ts;
    private final JComboBox combobox;

    public CustomMarkingsComboBoxPopupListener(TokenSimulator ts, JComboBox combobox) {
        this.ts = ts;
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
            ts.marking.putAll(ts.customMarkingsMap.get(markingName));
            //re-compute active transitions
            ts.getTokenSim().addTransitionsToCheck(ts.getPetriNet().transitions().toArray(new Transition[0]));
            ts.getTokenSim().computeActiveTransitions();
            //clear history
            ts.historyListModel.clear();
            ts.historyArrayList.clear();
            ts.lastHistoryStep = -1;
            //clear snapshots
            ts.snapshots.clear();
            ts.snapshotsListModel.clear();
            //clear statistic
            ts.totalStepNr = 0;
            ts.currStatistic = new Statistic(ts);
            for (Map.Entry<Place, XYSeries> entr : ts.seriesMap.entrySet()) {
                entr.getValue().clear();
                entr.getValue().add(ts.getTokenSim().getSimulatedTime(), ts.getTokenSim().getTokens(entr.getKey().id()), false);
            }

            ts.updateVisualOutput();
        }
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent pme) {
        canceled = true;
    }
}
