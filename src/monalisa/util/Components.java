/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.util;

import java.awt.Component;

import javax.swing.JPanel;

public final class Components {
    public static void setEnabled(JPanel panel, boolean enabled) {
        for (Component c : panel.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof JPanel)
                setEnabled((JPanel) c, enabled);
        }
    }
}
