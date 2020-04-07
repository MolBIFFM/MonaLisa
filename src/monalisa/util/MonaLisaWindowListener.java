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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import monalisa.addons.netviewer.NetViewer;

/**
 * Window listener for option frames in addons
 *
 * @author Jens Einloft
 */
public class MonaLisaWindowListener implements WindowListener {

    private final NetViewer parent;

    public MonaLisaWindowListener(NetViewer parent) {
        this.parent = parent;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.parent.setEnabled(true);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        this.parent.setEnabled(true);
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

}
