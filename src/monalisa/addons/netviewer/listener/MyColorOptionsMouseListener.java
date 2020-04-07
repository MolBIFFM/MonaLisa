/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer.listener;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JColorChooser;
import javax.swing.JLabel;

/**
 * Listener to open a JColorChooser by clicking on a JLabel
 *
 * @author Jens Einloft
 */
public class MyColorOptionsMouseListener implements MouseListener {

    private final JLabel label;

    public MyColorOptionsMouseListener(JLabel label) {
        this.label = label;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        selectColor();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private void selectColor() {
        Color color = JColorChooser.showDialog(null, "Select color", null);

        if (color != null) {
            this.label.setForeground(color);
            this.label.setBackground(color);
        }
    }
}
