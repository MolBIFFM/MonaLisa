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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import monalisa.addons.netviewer.NetViewer;

/**
 *
 * @author Jens Einloft
 */
public class NetViewerWindowsListener implements WindowListener {

    private final NetViewer netViewer;

    public NetViewerWindowsListener(NetViewer netViewer) {
        this.netViewer = netViewer;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.netViewer.closeAllFrames();
    }

    @Override
    public void windowClosed(WindowEvent e) {
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
