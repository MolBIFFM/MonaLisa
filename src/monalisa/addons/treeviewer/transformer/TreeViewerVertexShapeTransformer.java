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

import java.awt.Shape;
import monalisa.addons.netviewer.transformer.NetViewerVertexShapeFactory;
import monalisa.addons.treeviewer.TreeViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Jens Einloft
 */
public class TreeViewerVertexShapeTransformer implements Transformer<TreeViewerNode, Shape> {

    private NetViewerVertexShapeFactory nodesFactory;
        
    public TreeViewerVertexShapeTransformer() {
        this.nodesFactory = new NetViewerVertexShapeFactory(12);
    }
    
    @Override
    public Shape transform(TreeViewerNode i) {
        Shape shape;                               

        shape = this.nodesFactory.getEllipse(i);        

        return shape;
    }


}
