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

import java.awt.Shape;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerNode;
import org.apache.commons.collections15.Transformer;

/**
 * control's the shape of vertices
 * @author Jens Einloft
 */

public class VertexShapeTransformer implements Transformer<NetViewerNode, Shape> {

    private NetViewerVertexShapeFactory nodesFactory;

    public VertexShapeTransformer(int size) {
        this.nodesFactory = new NetViewerVertexShapeFactory(size);
    }
    
    public int getSize() {
        return this.nodesFactory.getShapeSize();
    }
    
    public void setSize(int size) {        
        this.nodesFactory = new NetViewerVertexShapeFactory(size);
    }
    
    @Override
    public Shape transform(NetViewerNode n) {
        String nodeType = n.getNodeType();
        Shape shape;

        if(nodeType.equalsIgnoreCase(NetViewer.PLACE) || nodeType.equalsIgnoreCase(NetViewer.TRANSITION)) {
            int corners = n.getCorners();
            if(corners == 0)
                shape = this.nodesFactory.getEllipse(n);
            else if(corners >= 3 && corners <= 4)
                shape = this.nodesFactory.getRegularPolygon(n, corners);
            else if(corners >= 5)
                shape = this.nodesFactory.getRegularStar(n, corners);
            else
                shape = this.nodesFactory.getRegularPolygon(n, 4);
        }
        else if(nodeType.equalsIgnoreCase(NetViewer.BEND)) {
            shape = this.nodesFactory.getRegularPolygon(n, 4);
        }
        else {
            shape = this.nodesFactory.getRegularPolygon(n, 4);
        }

        return shape;
    }

}
