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

import java.awt.Color;
import java.awt.Paint;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 * Fill color of vertices
 *
 * @author Jens Einloft
 */
public class VertexPaintTransformer implements Transformer<NetViewerNode, Paint> {

    private static final Color transparent = new Color(0, 0, 0, 0);
    private Boolean hideColor = false;

    @Override
    public Paint transform(NetViewerNode n) { // probably useless?
        if (hideColor) {
            Color color;
            if (n.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
                color = Color.BLACK;
            } else if (n.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
                if (n.isMasterNode()) {
                    color = Color.GRAY;
                } else if (n.isLogical()) {
                    color = Color.LIGHT_GRAY;
                } else {
                    color = Color.WHITE;
                }
            } else if (n.getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
                color = transparent;
            } else {
                color = Color.BLACK;
            }

            return color;
        } else {
            return n.getColor();
        }
    }

    public Boolean getHideColor() {
        return this.hideColor;
    }

    public void setHideColor(Boolean b) {
        this.hideColor = b;
    }

}
