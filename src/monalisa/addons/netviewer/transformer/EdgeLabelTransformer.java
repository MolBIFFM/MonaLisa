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

import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import org.apache.commons.collections15.Transformer;

/**
 * Returns the text, which is shown at the edge label
 *
 * @author Jens Einloft
 */
public class EdgeLabelTransformer implements Transformer<NetViewerEdge, String> {

    @Override
    public String transform(NetViewerEdge e) {
        if (e.getWeight() <= 1) {
            return "";
        }

        if (!e.getAim().getNodeType().equalsIgnoreCase(NetViewer.BEND) || !e.getSource().getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
            return Integer.toString(e.getWeight());
        } else {
            return "";
        }
    }

}
