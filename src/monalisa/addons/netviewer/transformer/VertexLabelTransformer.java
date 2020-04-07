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
import monalisa.addons.netviewer.NetViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 * Renderer for the label text of a vertex
 *
 * @author Jens Einloft
 */
public class VertexLabelTransformer implements Transformer<NetViewerNode, String> {

    private Boolean showLabel = true;

    @Override
    public String transform(NetViewerNode i) {
        if (i.getNodeType().equalsIgnoreCase(NetViewer.BEND) || !i.showLabel() || !showLabel) {
            return "";
        }

        return i.getName();
//        return new Integer(i.getId()).toString();
    }

    public void setShowLabel(Boolean flag) {
        showLabel = flag;
    }

    public Boolean showLabel() {
        return showLabel;
    }
}
