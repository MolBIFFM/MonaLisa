/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import java.awt.geom.Point2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Variation of a JUNG class CrossoverScalingControl.
 *
 * @author JUNG Library, modified by Jens Einloft
 */
public class NetViewerCrossoverScalingControl implements ScalingControl {

    protected double crossover = 1.0D;
    private final static Logger LOGGER = LogManager.getLogger(NetViewerCrossoverScalingControl.class);

    public void setCrossover(double crossover) {
        this.crossover = crossover;
    }

    public double getCrossover() {
        return this.crossover;
    }

    @Override
    public void scale(VisualizationServer<?, ?> vv, float amount, Point2D at) {
        LOGGER.debug("Rescaling for crossover");
        MutableTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
        MutableTransformer viewTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
        double modelScale = layoutTransformer.getScale();
        double viewScale = viewTransformer.getScale();
        double inverseModelScale = Math.sqrt(this.crossover) / modelScale;
        double inverseViewScale = Math.sqrt(this.crossover) / viewScale;
        double scale = modelScale * viewScale;

        Point2D transformedAt = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, at);

        if ((scale * amount - this.crossover) * (scale * amount - this.crossover) < 0.001D) {
            layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt);
            viewTransformer.scale(inverseViewScale, inverseViewScale, at);
        } else if (scale * amount < this.crossover) {
            viewTransformer.scale(amount, amount, at);
            layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt);
        } else {
            layoutTransformer.scale(amount, amount, transformedAt);
            viewTransformer.scale(inverseViewScale, inverseViewScale, at);
        }

        vv.repaint();
        LOGGER.debug("Successfully rescaled for crossover");
    }

}
