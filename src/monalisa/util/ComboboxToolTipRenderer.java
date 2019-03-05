/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.util;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

/**
 *
 * @author Jens Einloft
 */
public class ComboboxToolTipRenderer extends DefaultListCellRenderer {
    ArrayList tooltips;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (-1 < index && null != value && null != tooltips) {
            list.setToolTipText((String) tooltips.get(index));
        }
        return comp;
    }

    public void setTooltips(ArrayList tooltips) {
        this.tooltips = tooltips;
    }
}