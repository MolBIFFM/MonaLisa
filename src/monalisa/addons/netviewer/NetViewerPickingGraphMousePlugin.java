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

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import static monalisa.addons.centrality.AdjacencyMatrix.LOGGER;


/**
 *
 * @author JUNG Library, modified by Jens Einloft
 */
public class NetViewerPickingGraphMousePlugin<V, E> extends PickingGraphMousePlugin {

    private GraphPopupMousePlugin gpmp;
    private NetViewerModalGraphMouse gm;

    public NetViewerPickingGraphMousePlugin(NetViewerModalGraphMouse gm, GraphPopupMousePlugin gpmp) {
        super(16, 17);
        this.gpmp = gpmp;
        this.gm = gm;
    }

    public NetViewerPickingGraphMousePlugin(int selectionModifiers, int addToSelectionModifiers) {
        super(selectionModifiers, addToSelectionModifiers);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.down = e.getPoint();
        this.down.x = (int) gpmp.getNetViewer().formatCoordinates(this.down.x); // important for dragging on spezific coordinates
        this.down.y = (int) gpmp.getNetViewer().formatCoordinates(this.down.y);
        VisualizationViewer vv = (VisualizationViewer) e.getSource();
        GraphElementAccessor pickSupport = vv.getPickSupport();
        PickedState pickedVertexState = vv.getPickedVertexState();
        PickedState pickedEdgeState = vv.getPickedEdgeState();
        if ((pickSupport != null) && (pickedVertexState != null)) {
            Layout layout = vv.getGraphLayout();
            if (e.getModifiers() == this.modifiers) {
                this.rect.setFrameFromDiagonal(this.down, this.down);

                Point2D ip = e.getPoint();

                this.vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
                if (this.vertex != null) {
                    if (!pickedVertexState.isPicked(this.vertex)) {
                        pickedVertexState.clear();
                        pickedEdgeState.clear();
                        pickedVertexState.pick(this.vertex, true);
                    }

                    Point2D q = (Point2D) layout.transform(this.vertex);
                    Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);
                    this.offsetx = (float) (gp.getX() - q.getX());
                    this.offsety = (float) (gp.getY() - q.getY());
                } else if ((this.edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
                    pickedEdgeState.clear();
                    pickedEdgeState.pick(edge, true);
                    if (((NetViewerEdge) edge).getMasterEdge().getBendEdges().size() > 0 && gpmp.getMouseMode().equalsIgnoreCase(GraphPopupMousePlugin.NORMAL))
                        try {
                        for (NetViewerEdge nvEdge : ((NetViewerEdge) edge).getMasterEdge().getBendEdges()) {
                            pickedEdgeState.pick(nvEdge, true);
                        }
                    } catch (ConcurrentModificationException cme) {
                    } else {
                        pickedVertexState.clear();
                        pickedEdgeState.pick(edge, true);
                    }
                } else {
                    vv.addPostRenderPaintable(this.lensPaintable);
                    pickedEdgeState.clear();
                    pickedVertexState.clear();
                }
            } else if (e.getModifiers() == this.addToSelectionModifiers) {
                vv.addPostRenderPaintable(this.lensPaintable);
                this.rect.setFrameFromDiagonal(this.down, this.down);
                Point2D ip = e.getPoint();
                this.vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
                if (this.vertex != null) {
                    boolean wasThere = pickedVertexState.pick(this.vertex, !pickedVertexState.isPicked(this.vertex));
                    if (wasThere) {
                        this.vertex = null;
                    } else {
                        Point2D q = (Point2D) layout.transform(this.vertex);
                        Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);
                        this.offsetx = (float) (gp.getX() - q.getX());
                        this.offsety = (float) (gp.getY() - q.getY());
                    }
                } else if ((this.edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
                    pickedEdgeState.pick(this.edge, !pickedEdgeState.isPicked(this.edge));
                }
            }
        }
        if (this.vertex != null) {
            e.consume();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mouseDragged(MouseEvent e) {
        if (locked == false) {
            VisualizationViewer<V, E> vv = (VisualizationViewer) e.getSource();
            if (vertex != null) {
                Point p = e.getPoint();
                p.x = (int) gpmp.getNetViewer().formatCoordinates(p.x); // dragging on specific coordinates only
                p.y = (int) gpmp.getNetViewer().formatCoordinates(p.y);
                Point2D graphPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p);
                Point2D graphDown = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
                Layout<V, E> layout = vv.getGraphLayout();
                double dx = graphPoint.getX() - graphDown.getX();
                double dy = graphPoint.getY() - graphDown.getY();
                PickedState<V> ps = vv.getPickedVertexState();

                for (V v : ps.getPicked()) {
                    Point2D vp = layout.transform(v);
                    vp.setLocation(vp.getX() + dx, vp.getY() + dy);
                    layout.setLocation(v, vp);
                }
                down = p;

            } else {
                Point2D out = e.getPoint();
                if (e.getModifiers() == this.addToSelectionModifiers
                        || e.getModifiers() == modifiers) {
                    rect.setFrameFromDiagonal(down, out);
                }
            }
            if (vertex != null) {
                e.consume();
            }
            vv.repaint();
        }
    }

    /**
     * @param locked The locked to set.
     */
    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
