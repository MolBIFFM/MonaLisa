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

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

/**
 * Zoom in: on mouse, zoom out: at center
 * @author JUNG Library, modified by Jens Einloft
 */
public class NetViewerScalingGraphMousePlugin extends ScalingGraphMousePlugin {
    
    private NetViewer owner;
    
    double newViewScale, newLayoutScale;
    
    public NetViewerScalingGraphMousePlugin(ScalingControl scaler, int modifiers) {
        super(scaler, modifiers, 1.1F, 0.9090909F);
    }

    public NetViewerScalingGraphMousePlugin(ScalingControl scaler, int modifiers, float in, float out, NetViewer owner) {
        super(scaler, modifiers, in, out);
        this.owner = owner;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        
        boolean accepted = checkModifiers(e);
        if (accepted == true) {        
            VisualizationViewer vv = (VisualizationViewer) e.getSource();
            Point2D mouse = e.getPoint();
            Point2D center = vv.getCenter();
            int amount = e.getWheelRotation();           
            
            if(amount > 0)
                this.scaler.scale(vv, this.in, center);
            else if(amount < 0)
                this.scaler.scale(vv, this.out, mouse);

            newViewScale = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getScale();
            newLayoutScale = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale();                   
            
            owner.setZoomScale((newViewScale*newLayoutScale)*100);                

            e.consume();
            vv.repaint();
        }
    }
    
    @Override
    public ScalingControl getScaler() {
        return this.scaler;
    }

}
