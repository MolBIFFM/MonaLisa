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

import java.awt.Color;
import java.awt.Paint;
import monalisa.addons.treeviewer.TreeViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author stefan marchi
 */
public class TreeViewerVertexPaintTransformer implements Transformer<TreeViewerNode, Paint> {

    private static final Color transparent = new Color(0, 0, 0, 0);

    @Override
    public Paint transform(TreeViewerNode n) {
        if (n.getNodeType().equalsIgnoreCase(TreeViewerNode.BENDNODE)) {
            return transparent;
        } else {
            return Color.RED;
        }
    }
}
