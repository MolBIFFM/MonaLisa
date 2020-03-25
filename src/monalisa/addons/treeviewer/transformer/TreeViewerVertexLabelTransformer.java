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

import monalisa.addons.treeviewer.TreeViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Jens Einloft
 */
public class TreeViewerVertexLabelTransformer implements Transformer<TreeViewerNode, String> {

    @Override
    public String transform(TreeViewerNode n) {
        if(n.getNodeType().equalsIgnoreCase(TreeViewerNode.CLUSTERNODE)) {
            if(n.getTinvs().size() == 1) {
                StringBuilder sb = new StringBuilder();       
                sb.append("EM:");
                sb.append(n.getTinvs().get(0).id()+1);                    
                return sb.toString();
            } else {
                return "";
            }            
        } else {
            return "";
        }
    }

}
