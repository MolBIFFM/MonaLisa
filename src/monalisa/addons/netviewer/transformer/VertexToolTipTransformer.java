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

import monalisa.addons.netviewer.NetViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 * Adds a ToolTip to a vertex
 * @author Jens Einloft
 */
public class VertexToolTipTransformer implements Transformer<NetViewerNode, String> {

    @Override
    public String transform(NetViewerNode n) {
        if(n.hasProperty("toolTip"))
            return n.getProperty("toolTip");
        return null;
    }

}
