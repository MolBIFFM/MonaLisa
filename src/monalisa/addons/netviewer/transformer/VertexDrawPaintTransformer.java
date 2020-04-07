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
import monalisa.data.pn.Compartment;
import org.apache.commons.collections15.Transformer;

/**
 * Color of the stroke of vertices
 *
 * @author Jens Einloft
 */
public class VertexDrawPaintTransformer implements Transformer<NetViewerNode, Paint> {

    private static final Color transparent = new Color(0, 0, 0, 0);
    private boolean showCompartments = false;

    @Override
    public Paint transform(NetViewerNode n) {
        if (!n.getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
            if (showCompartments) {
                if (n.hasProperty("compartment")) {
                    if (n.getProperty("compartment") != null) {
                        return (Paint) ((Color) ((Compartment) n.getProperty("compartment")).getProperty("color"));
                    } else {
                        return (Paint) Color.BLACK;
                    }
                } else {
                    return (Paint) Color.BLACK;
                }
            } else {
                if (n.getStrokeColor() == null) {
                    return (Paint) Color.BLACK;
                } else {
                    return (Paint) n.getStrokeColor();
                }
            }
        } else {
            return (Paint) transparent;
        }
    }

    /**
     * Should the compartment of a node coded by its stroke?
     *
     * @param b
     */
    public void setShowCompartments(boolean b) {
        this.showCompartments = b;
    }

}
