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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import static monalisa.addons.centrality.AdjacencyMatrix.LOGGER;

/**
 * KeyListener to react on pressed keys in the NetViewer
 *
 * @author Jens Einloft
 */
public class NetViewerKeyListener implements KeyListener {

    private final NetViewer nv;
    private Integer keyCode;
    private boolean activated;

    public NetViewerKeyListener(NetViewer nv) {
        this.nv = nv;
        activated = true;
    }

    public void setActivated(boolean b) {
        this.activated = b;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (activated) {
            keyCode = e.getKeyCode();
            // ESC
            if (keyCode.equals(KeyEvent.VK_ESCAPE)) {
                nv.cancelMouseAction();
            } // 1
            else if (keyCode.equals(KeyEvent.VK_1)) {
                nv.placeMouseAction();
            } // 2
            else if (keyCode.equals(KeyEvent.VK_2)) {
                nv.transitionMouseAction();
            } // 3
            else if (keyCode.equals(KeyEvent.VK_3)) {
                nv.edgeMouseAction();
            } // 4
            else if (keyCode.equals(KeyEvent.VK_4)) {
                nv.deleteMouseAction();
            } // Q
            else if (keyCode.equals(KeyEvent.VK_Q)) {
                nv.outVertexMouseAction();
            } // W
            else if (keyCode.equals(KeyEvent.VK_W)) {
                nv.inVertexMouseAction();
            } // E
            else if (keyCode.equals(KeyEvent.VK_E)) {
                nv.addBendMouseAction();
            } // R
            else if (keyCode.equals(KeyEvent.VK_R)) {
                nv.deleteBendMouseAction();
            } // S
            else if (keyCode.equals(KeyEvent.VK_S)) {
                nv.alignXMouseAction();
            } // A
            else if (keyCode.equals(KeyEvent.VK_A)) {
                nv.alignYMouseAction();
            } // D
            else if (keyCode.equals(KeyEvent.VK_D)) {
                nv.changeMouseModeToPicking();
            } // F
            else if (keyCode.equals(KeyEvent.VK_F)) {
                nv.changeMouseModeToTransforming();
            } // Y
            else if (keyCode.equals(KeyEvent.VK_Y)) {
                nv.saveProject();
            } // X
            else if (keyCode.equals(KeyEvent.VK_X)) {
                try {
                    nv.makePic();
                } catch (IOException ex) {
                    LOGGER.error("Issue while making picture: ", ex);
                }
            } // C
            else if (keyCode.equals(KeyEvent.VK_C)) {
                nv.showLabels(); // TODO change image
            } // V
            else if (keyCode.equals(KeyEvent.VK_V)) {
                nv.hideColor();
            } // Delete
            else if (keyCode.equals(KeyEvent.VK_DELETE)) {
                nv.removeSelectedVertices();
            }
        }
    }
}
