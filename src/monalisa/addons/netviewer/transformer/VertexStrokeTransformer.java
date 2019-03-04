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

import java.awt.BasicStroke;
import java.awt.Stroke;
import monalisa.addons.netviewer.NetViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Jens Einloft
 */
public class VertexStrokeTransformer implements Transformer<NetViewerNode, Stroke>{

    @Override
    public Stroke transform(NetViewerNode i) {
        return new BasicStroke(1.25f);
    }

}
