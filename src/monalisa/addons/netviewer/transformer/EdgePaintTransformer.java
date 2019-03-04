/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer.transformer;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.Color;
import java.awt.Paint;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 * Transformer for the color of an edge
 * @author Jens Einloft
 */

public class EdgePaintTransformer implements Transformer<NetViewerEdge, Paint>{

    private final VisualizationViewer<NetViewerNode, NetViewerEdge> vv;
    private Boolean hideColor = false;

    public EdgePaintTransformer(VisualizationViewer<NetViewerNode, NetViewerEdge> vv) {
        this.vv = vv;
    }

    @Override
    public Paint transform(NetViewerEdge e) {
        if(vv.getRenderContext().getPickedEdgeState().getPicked().contains(e))
            return Color.CYAN;
        if(hideColor)
            return Color.BLACK;
        else
            return e.getColor();
    }

    public Boolean getHideColor() {
        return this.hideColor;
    }

    public void setHideColor(Boolean b) {
        this.hideColor = b;
    }
}
