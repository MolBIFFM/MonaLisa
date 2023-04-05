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

import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import monalisa.synchronisation.Synchronizer;

import static monalisa.addons.centrality.AdjacencyMatrix.LOGGER; // TODO delete

/**
 * MouseListener to react on mouse clicks for creating new nodes
 *
 * @author Jens Einloft
 */
public class NetViewerMouseListener implements MouseListener {

    private static final String NORMAL = "NORMAL";
    private static final String PLACE = "PLACE";
    private static final String TRANSITION = "TRANSITION";

    private String mouseMode = NORMAL;

    private final NetViewer nv;
    private final Synchronizer synchronizer;

    private PickedState<NetViewerNode> psN;
    private PickedState<NetViewerEdge> psE;

    public NetViewerMouseListener(NetViewer nv, Synchronizer synchronizer) {
        this.nv = nv;
        this.synchronizer = synchronizer;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
            if (this.mouseMode.equalsIgnoreCase(PLACE)) {
                Point.Double point = new Point.Double();
                point.x = e.getX();
                point.y = e.getY(); 
                // start trying to change coordinates to spezific numbers
                LOGGER.info(point.x+"" + " " + point.y+""); // TODO delete
                point.x = nv.formatCoordinates(point.x);
                point.y = nv.formatCoordinates(point.y);
                LOGGER.info(point.x+"" + " " + point.y+""); // TODO delete
                // stop
                Point2D pointInVV = nv.vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point);
                nv.addNode(NetViewer.PLACE, "P" + (++nv.placeCount), pointInVV.getX(), pointInVV.getY());
                nv.modificationActionHappend();
            } else if (this.mouseMode.equalsIgnoreCase(TRANSITION)) {
                Point.Double point = new Point.Double();
                point.x = e.getX();
                point.y = e.getY();
                // start trying to change coordinates to spezific numbers
                LOGGER.info(point.x+"" + " " + point.y+""); // TODO delete
                point.x = nv.formatCoordinates(point.x);
                point.y = nv.formatCoordinates(point.y);
                LOGGER.info(point.x+"" + " " + point.y+""); // TODO delete
                // stop
                Point2D pointInVV = nv.vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point);
                nv.addNode(NetViewer.TRANSITION, "T" + (++nv.transitionCount), pointInVV.getX(), pointInVV.getY());
                nv.modificationActionHappend();
            }
        } else if (e.getClickCount() == 2 && nv.getMouseMode()) {
            psN = nv.vv.getRenderContext().getPickedVertexState();
            if (psN.getPicked().size() == 1) {
                nv.showVertexSetup((NetViewerNode) psN.getPicked().toArray()[0], e.getX(), e.getY());
            }

            psE = nv.vv.getPickedEdgeState();
            if (psE.getPicked().size() == 1) {
                nv.showEdgeSetup((NetViewerEdge) psE.getPicked().toArray()[0], e.getX(), e.getY());
            }
        }

    }

    
    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public void setMouseModeToPlace() {
        this.mouseMode = PLACE;
    }

    public void setMouseModeToTransition() {
        this.mouseMode = TRANSITION;
    }

    public void setMouseModeToNormal() {
        this.mouseMode = NORMAL;
    }

}
