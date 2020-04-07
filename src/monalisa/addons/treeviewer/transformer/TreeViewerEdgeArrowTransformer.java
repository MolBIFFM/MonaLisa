/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.treeviewer.transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import monalisa.addons.treeviewer.TreeViewerEdge;
import monalisa.addons.treeviewer.TreeViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Jens Einloft
 */
public class TreeViewerEdgeArrowTransformer implements Transformer<Context<Graph<TreeViewerNode, TreeViewerEdge>, TreeViewerEdge>, Shape> {

    @Override
    public Shape transform(Context<Graph<TreeViewerNode, TreeViewerEdge>, TreeViewerEdge> i) {
        Double dFactor;

        if (i.element.getEdgeType().equalsIgnoreCase(TreeViewerEdge.CLUSTEREDGE)) {
            //dFactor = 1.0;
            //draw line instead of arrow
            dFactor = 0.0;
        } else {
            dFactor = 0.0;
        }

        Double height = 10.0 * dFactor;
        Double base = 8.0 * dFactor;

        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(0, 0);
        arrow.lineTo(-height, base / 2.0f);
        arrow.lineTo(-height, -base / 2.0f);
        arrow.lineTo(0, 0);

        return arrow;
    }
}
