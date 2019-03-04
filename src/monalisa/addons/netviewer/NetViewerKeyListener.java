/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * KeyListener to react on pressed keys in the NetViewer
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
        if(activated) {
            keyCode = e.getKeyCode();

            // ESC
            if(keyCode.equals(KeyEvent.VK_ESCAPE))
                nv.cancelMouseAction();
            // Q
            else if(keyCode.equals(KeyEvent.VK_Q))
                nv.placeMouseAction();
            // W
            else if(keyCode.equals(KeyEvent.VK_W))
                nv.transitionMouseAction();
            // E
            else if(keyCode.equals(KeyEvent.VK_E))
                nv.edgeMouseAction();
            // R
            else if(keyCode.equals(KeyEvent.VK_R))
                nv.deleteMouseAction();        
            // S
            else if(keyCode.equals(KeyEvent.VK_S))
                nv.inVertexMouseAction();
            // A
            else if(keyCode.equals(KeyEvent.VK_A))
                nv.outVertexMouseAction();
            // D
            else if(keyCode.equals(KeyEvent.VK_D))
                nv.addBendMouseAction();
            // F
            else if(keyCode.equals(KeyEvent.VK_F))
                    nv.deleteBendMouseAction();
            // Y
            else if(keyCode.equals(KeyEvent.VK_Y))
                nv.alignYMouseAction();
            // X
            else if(keyCode.equals(KeyEvent.VK_X))
                nv.alignXMouseAction();
            // C
            else if(keyCode.equals(KeyEvent.VK_C))
                nv.changeMouseModeToPicking();
            // V
            else if(keyCode.equals(KeyEvent.VK_V))
                    nv.changeMouseModeToTransforming();
            // Delte
            else if(keyCode.equals(KeyEvent.VK_DELETE))
                nv.removeSelectedVertices();
        }
    }
}
