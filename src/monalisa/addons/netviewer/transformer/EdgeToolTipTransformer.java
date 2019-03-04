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

import monalisa.addons.netviewer.NetViewerEdge;
import org.apache.commons.collections15.Transformer;

/**
 * Adds a ToolTip to a vertex
 * @author Jens Einloft
 */
public class EdgeToolTipTransformer implements Transformer<NetViewerEdge, String> {

    @Override
    public String transform(NetViewerEdge e) {
        if(e.hasProperty("toolTip"))
            return e.getProperty("toolTip");
        return null;
    }

}
