/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netproperties;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

/**
 * A Class where the logic and the gui can be separated;
 *
 * @author daniel
 */
public class NetPropertiesPanelLogic {

    HashMap<JCheckBox, NetPropertieAlgorithm> algorithmMap;
    HashMap<JCheckBox, JLabel> labelMap;

    public NetPropertiesPanelLogic(HashMap<JCheckBox, NetPropertieAlgorithm> algorithmMap, HashMap<JCheckBox, JLabel> labelMap) {
        this.algorithmMap = algorithmMap;
        this.labelMap = labelMap;
    }

    protected void checkAlgorithms() {
        for (JCheckBox b : algorithmMap.keySet()) {
            if (b.isSelected()) {
                algorithmMap.get(b).runAlgorithm();
                if ((Boolean) algorithmMap.get(b).returnAlgorithmValue()) {
                    labelMap.get(b).setText(" True ");
                    labelMap.get(b).setForeground(Color.green.darker());
                } else {
                    labelMap.get(b).setText(" False");
                    labelMap.get(b).setForeground(Color.red);
                }
            }
        }
    }

    protected void checkAlgorithms(int a) {
        for (JCheckBox b : algorithmMap.keySet()) {
            if (b.isSelected()) {
                labelMap.get(b).setText("no Net");
                labelMap.get(b).setForeground(Color.red);
            }
        }
    }

    protected void checkAlgorithmsNot() {
        for (JLabel l : labelMap.values()) {
            l.setText("");
        }
    }

    protected void selectAllAlgorithmsIsSelected() {
        for (JCheckBox B : algorithmMap.keySet()) {
            B.setSelected(true);
        }
    }

    protected void selectAllAlgorithmsIsNotSelected() {
        for (JCheckBox B : algorithmMap.keySet()) {
            B.setSelected(false);
        }
    }

}
