/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netproperties;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Class where the logic and the gui can be separated;
 *
 * @author daniel
 */
public class NetPropertiesPanelLogic {

    private static final Logger LOGGER = LogManager.getLogger(NetPropertiesPanelLogic.class);
    HashMap<JCheckBox, NetPropertyAlgorithm> algorithmMap;
    HashMap<JCheckBox, JLabel> labelMap;

    public NetPropertiesPanelLogic(HashMap<JCheckBox, NetPropertyAlgorithm> algorithmMap, HashMap<JCheckBox, JLabel> labelMap) {
        this.algorithmMap = algorithmMap;
        this.labelMap = labelMap;
    }

    protected void checkAlgorithms() {
        LOGGER.info("Running algorithms and setting results for net properties");
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
        LOGGER.info("Successfully ran algorithms and set results for net properties");
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
