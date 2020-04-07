/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import monalisa.Settings;

/**
 *
 * @author Jens Einloft Removes Tabs of AddOns from the ToolBar
 */
public class AddonMenuItemListener implements ItemListener {

    private final String name;
    private final Component tab;
    private final ToolBar tbf;
    private Boolean showMe;

    public AddonMenuItemListener(String name, Component tab, ToolBar tb) {
        this.name = name;
        this.tab = tab;
        this.tbf = tb;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            tbf.menuPane.remove(tbf.menuPane.indexOfTab(name));
            showMe = false;
        } else if (e.getStateChange() == ItemEvent.SELECTED) {
            tbf.addTabToMenuBar(name, tab);
            showMe = true;
        }
        Settings.set(name, showMe.toString());
        Settings.writeToFile(System.getProperty("user.home") + "/.monalisaSettings");
    }

}
