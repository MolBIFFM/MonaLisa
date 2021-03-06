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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.TransformingGraphics;
import monalisa.addons.netviewer.NetViewerNode;

/**
 * VertexLabelRenderer with a specific label position for every vertex
 *
 * @author from JUNG modifyed by Jens Einloft
 */
public class AdvancedVertexLabelRenderer<V, E> implements Renderer.VertexLabel<V, E> {

    protected Position position = Position.SE;
    private Positioner positioner = new OutsidePositioner();

    public AdvancedVertexLabelRenderer() {
        super();
    }

    /**
     * @return the position
     */
    @Override
    public Position getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    @Override
    public void setPosition(Position position) {
        this.position = position;
    }

    public Component prepareRenderer(RenderContext<V, E> rc, VertexLabelRenderer graphLabelRenderer, Object value,
            boolean isSelected, V vertex) {
        return rc.getVertexLabelRenderer().<V>getVertexLabelRendererComponent(rc.getScreenDevice(), value,
                rc.getVertexFontTransformer().transform(vertex), isSelected, vertex);
    }

    /**
     * Labels the specified vertex with the specified label. Uses the font
     * specified by this instance's <code>VertexFontFunction</code>. (If the
     * font is unspecified, the existing font for the graphics context is used.)
     * If vertex label centering is active, the label is centered on the
     * position of the vertex; otherwise the label is offset slightly.
     */
    @Override
    public void labelVertex(RenderContext<V, E> rc, Layout<V, E> layout, V v, String label) {
        Graph<V, E> graph = layout.getGraph();
        if (rc.getVertexIncludePredicate().evaluate(Context.<Graph<V, E>, V>getInstance(graph, v)) == false) {
            return;
        }
        Point2D pt = layout.transform(v);
        pt = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, pt);

        float x = (float) pt.getX();
        float y = (float) pt.getY();

        Component component = prepareRenderer(rc, rc.getVertexLabelRenderer(), label, rc.getPickedVertexState().isPicked(v), v);
        GraphicsDecorator g = rc.getGraphicsContext();
        Dimension d = component.getPreferredSize();
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);

        Shape shape = rc.getVertexShapeTransformer().transform(v);
        shape = xform.createTransformedShape(shape);
        if (rc.getGraphicsContext() instanceof TransformingGraphics) {
            BidirectionalTransformer transformer = ((TransformingGraphics) rc.getGraphicsContext()).getTransformer();
            if (transformer instanceof ShapeTransformer) {
                ShapeTransformer shapeTransformer = (ShapeTransformer) transformer;
                shape = shapeTransformer.transform(shape);
            }
        }
        Rectangle2D bounds = shape.getBounds2D();

        position = ((NetViewerNode) v).getLabelPosition();
        Point p = null;
        if (position == Position.AUTO) {
            Dimension vvd = rc.getScreenDevice().getSize();
            if (vvd.width == 0 || vvd.height == 0) {
                vvd = rc.getScreenDevice().getPreferredSize();
            }
            p = getAnchorPoint(bounds, d, positioner.getPosition(x, y, vvd));
        } else {
            p = getAnchorPoint(bounds, d, position);
        }
        g.draw(component, rc.getRendererPane(), p.x, p.y, d.width, d.height, true);
    }

    protected Point getAnchorPoint(Rectangle2D vertexBounds, Dimension labelSize, Position position) {
        double x;
        double y;
        int offset = 3;
        switch (position) {

            case N:
                x = vertexBounds.getCenterX() - labelSize.width / 2;
                y = vertexBounds.getMinY() - offset - labelSize.height;
                return new Point((int) x, (int) y);

            case NE:
                x = vertexBounds.getMaxX() + offset;
                y = vertexBounds.getMinY() - offset - labelSize.height;
                return new Point((int) x, (int) y);

            case E:
                x = vertexBounds.getMaxX() + offset;
                y = vertexBounds.getCenterY() - labelSize.height / 2;
                return new Point((int) x, (int) y);

            case SE:
                x = vertexBounds.getMaxX() + offset;
                y = vertexBounds.getMaxY() + offset;
                return new Point((int) x, (int) y);

            case S:
                x = vertexBounds.getCenterX() - labelSize.width / 2;
                y = vertexBounds.getMaxY() + offset;
                return new Point((int) x, (int) y);

            case SW:
                x = vertexBounds.getMinX() - offset - labelSize.width;
                y = vertexBounds.getMaxY() + offset;
                return new Point((int) x, (int) y);

            case W:
                x = vertexBounds.getMinX() - offset - labelSize.width;
                y = vertexBounds.getCenterY() - labelSize.height / 2;
                return new Point((int) x, (int) y);

            case NW:
                x = vertexBounds.getMinX() - offset - labelSize.width;
                y = vertexBounds.getMinY() - offset - labelSize.height;
                return new Point((int) x, (int) y);

            case CNTR:
                x = vertexBounds.getCenterX() - labelSize.width / 2;
                y = vertexBounds.getCenterY() - labelSize.height / 2;
                return new Point((int) x, (int) y);

            default:
                return new Point();
        }

    }

    public static class InsidePositioner implements Positioner {

        @Override
        public Position getPosition(float x, float y, Dimension d) {
            int cx = d.width / 2;
            int cy = d.height / 2;
            if (x > cx && y > cy) {
                return Position.NW;
            }
            if (x > cx && y < cy) {
                return Position.SW;
            }
            if (x < cx && y > cy) {
                return Position.NE;
            }
            return Position.SE;
        }
    }

    public static class OutsidePositioner implements Positioner {

        @Override
        public Position getPosition(float x, float y, Dimension d) {
            int cx = d.width / 2;
            int cy = d.height / 2;
            if (x > cx && y > cy) {
                return Position.SE;
            }
            if (x > cx && y < cy) {
                return Position.NE;
            }
            if (x < cx && y > cy) {
                return Position.SW;
            }
            return Position.NW;
        }
    }

    /**
     * @return the positioner
     */
    @Override
    public Positioner getPositioner() {
        return positioner;
    }

    /**
     * @param positioner the positioner to set
     */
    @Override
    public void setPositioner(Positioner positioner) {
        this.positioner = positioner;
    }
}
