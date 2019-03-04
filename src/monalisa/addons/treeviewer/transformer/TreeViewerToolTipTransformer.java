/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.treeviewer.transformer;

import monalisa.addons.treeviewer.TreeViewerNode;
import monalisa.data.pn.TInvariant;
import org.apache.commons.collections15.Transformer;

/**
 * Adds a ToolTip to a vertex
 * @author Jens Einloft
 */
public class TreeViewerToolTipTransformer implements Transformer<TreeViewerNode, String> {

    @Override
    public String transform(TreeViewerNode n) {
        StringBuilder sb = new StringBuilder();        
        
        for(TInvariant tinv : n.getTinvs()) {
            sb.append(" EM ");
            sb.append(tinv.id()+1);                    
        }

        return sb.toString();

    }

}
