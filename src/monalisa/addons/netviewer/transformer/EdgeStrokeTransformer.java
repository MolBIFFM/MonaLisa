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

import java.awt.BasicStroke;
import java.awt.Stroke;
import monalisa.addons.netviewer.NetViewerEdge;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Jens Einloft
 */
public class EdgeStrokeTransformer implements Transformer<NetViewerEdge, Stroke> {

    private int strokeFactor;

    public EdgeStrokeTransformer(int strokeFactor) {
        this.strokeFactor = strokeFactor;
    }

    public int getStrokeFactor() {
        return this.strokeFactor;
    }

    public void setStrokeFactor(int strokeFactor) {
        this.strokeFactor = strokeFactor;
    }

    @Override
    public Stroke transform(NetViewerEdge i) {
        return new BasicStroke(strokeFactor);
    }
}
