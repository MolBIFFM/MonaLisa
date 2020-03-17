/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer.transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 * Shape transformer for arrow heads (size changeable)
 * @author Jens Einloft
 */
public class EdgeArrowTransformer implements Transformer<Context<Graph<NetViewerNode, NetViewerEdge>, NetViewerEdge>, Shape> {

    private double factor;

    public EdgeArrowTransformer(double factor) {
        this.factor = factor;
    }
    
    /**
     * Returns the current factor of the Transformer
     * @return 
     */
    public double getFactor() {
        return this.factor;
    }
    
    /**
     * Sets the new factor for the Transformer
     * @param factor 
     */
    public void setFactor(double factor) {
        this.factor = factor;
    }
    
    @Override
    public Shape transform(Context<Graph<NetViewerNode, NetViewerEdge>, NetViewerEdge> i) {
        Double dFactor = this.factor;
        Double height = 10.0*dFactor;
        Double base = 8.0*dFactor;

        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(0,0);
        arrow.lineTo(-height, base/2.0f);
        arrow.lineTo(-height, -base/2.0f);
        arrow.lineTo(0,0);
        return arrow;
    }

}
