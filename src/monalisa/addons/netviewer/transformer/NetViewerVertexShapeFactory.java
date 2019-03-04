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

import edu.uci.ics.jung.visualization.util.VertexShapeFactory;
import monalisa.addons.netviewer.NetViewerNode;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

/**
 * controls the size of a node
 * @author Jens Einloft
 */

public class NetViewerVertexShapeFactory extends VertexShapeFactory{
    
    private int size;
    
    public NetViewerVertexShapeFactory(int size) {                
        this(new ConstantTransformer(size), new ConstantTransformer(1.0F));
        this.size = size;
    }

    public NetViewerVertexShapeFactory(Transformer<NetViewerNode, Integer> vsf, Transformer<NetViewerNode, Float> varf) {
        this.vsf = vsf;
        this.varf = varf;
    }    

    public int getShapeSize() {
        return this.size;
    }

}
