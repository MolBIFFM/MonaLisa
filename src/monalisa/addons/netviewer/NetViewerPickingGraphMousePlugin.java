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
import static java.lang.Math.abs;
import static java.lang.Math.round;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author JUNG Library, modified by Jens Einloft
 */
public class NetViewerPickingGraphMousePlugin<V, E> extends PickingGraphMousePlugin {

    private GraphPopupMousePlugin gpmp;
    private NetViewerModalGraphMouse gm;
    private Map<Object, Point2D> distMap = new HashMap<>();

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
        if (gpmp.getNetViewer().tb.getEnableGrid()) {
            this.down.x = (int) gpmp.getNetViewer().formatCoordinates(this.down.x); // important for dragging on spezific coordinates
            this.down.y = (int) gpmp.getNetViewer().formatCoordinates(this.down.y);
        }
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
            VisualizationViewer vv = (VisualizationViewer) e.getSource();
            Layout layout = vv.getGraphLayout();
            int dx, dy;
            
            Point p = e.getPoint();
            if (gpmp.getNetViewer().tb.getEnableGrid()) {
                p.x = (int) gpmp.getNetViewer().formatCoordinates(p.x); // dragging on specific coordinates only
                p.y = (int) gpmp.getNetViewer().formatCoordinates(p.y);
            }
            Point2D graphPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p);
            if (gpmp.getNetViewer().tb.getEnableGrid()) {
                graphPoint.setLocation(gpmp.getNetViewer().formatCoordinates(graphPoint.getX()), gpmp.getNetViewer().formatCoordinates(graphPoint.getY()));
            } else {
                graphPoint.setLocation(round(graphPoint.getX()), round(graphPoint.getY()));
            }
            
            Collection nodeCollection = gpmp.getGraph().getVertices();
            if (!gpmp.getNetViewer().getMouseMode() && !nodeCollection.isEmpty()) { // transforming mode
                for (Object v : nodeCollection) {
                    Point2D vp = (Point2D) layout.transform(v);
                    if (gpmp.getNetViewer().getMouseListener().getReleased()) { // new distance calculation when mouse has been released
                        if (graphPoint.getX() < vp.getX()) {
                            dx = (int) abs(vp.getX() - graphPoint.getX());
                        } else {
                            dx = (int) (-1 * abs(vp.getX() - graphPoint.getX()));
                        }
                        if (graphPoint.getY() < vp.getY()) {
                            dy = (int) abs(vp.getY() - graphPoint.getY());
                        } else {
                            dy = (int) (-1 * abs(vp.getY() - graphPoint.getY()));
                        }
                        if (!distMap.containsKey(v)) {
                            distMap.put(v, new Point(dx, dy));
                        } else {
                            distMap.replace(v, new Point(dx, dy));
                        } 
                    }
                    layout.setLocation(v, new Point((int) (graphPoint.getX() + distMap.get(v).getX()), (int) (graphPoint.getY() + distMap.get(v).getY())));
                }
            } else if (gpmp.getNetViewer().getMouseMode()) {
                if (vertex != null) { // marking vertices and dragging them
                    PickedState<V> ps = vv.getPickedVertexState();
                    Point2D pickedPoint = (Point2D) layout.transform(this.vertex);
                    pickedPoint.setLocation(round(pickedPoint.getX()), round(pickedPoint.getY()));
                    for (V v : ps.getPicked()) {
                        if (!v.equals(this.vertex)) {                        
                            Point2D vp = (Point2D) layout.transform(v);
                            if (pickedPoint.getX() < vp.getX()) {
                                dx = (int) abs(pickedPoint.getX() - vp.getX());
                            } else {
                                dx = (int) (-1 * abs(pickedPoint.getX() - vp.getX()));
                            }
                            if (pickedPoint.getY() < vp.getY()) {
                                dy = (int) abs(pickedPoint.getY() - vp.getY());
                            } else {
                                dy = (int) (-1 * abs(pickedPoint.getY() - vp.getY()));
                            }                        
                            layout.setLocation(v, new Point((int) (graphPoint.getX() + dx), (int) (graphPoint.getY() + dy)));
                        } else {
                            layout.setLocation(v, graphPoint);
                        }
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
            }
            vv.repaint();
        }
        gpmp.getNetViewer().getMouseListener().setReleased(false);
    }

    /**
     * @param locked The locked to set.
     */
    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
