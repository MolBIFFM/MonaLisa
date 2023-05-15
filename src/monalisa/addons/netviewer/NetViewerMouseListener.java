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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import monalisa.synchronisation.Synchronizer;


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
    boolean released = true;

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
                if (nv.tb.getEnableGrid()) {
                    // start trying to change coordinates to spezific numbers (grid)
                    point.x = nv.formatCoordinates(e.getX());
                    point.y = nv.formatCoordinates(e.getY());
                } else {
                    point.x = e.getX();
                    point.y = e.getY(); 
                }
                Point2D pointInVV = nv.vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point);
                nv.addNode(NetViewer.PLACE, "P" + (++nv.placeCount), pointInVV.getX(), pointInVV.getY());
                nv.modificationActionHappend();
            } else if (this.mouseMode.equalsIgnoreCase(TRANSITION)) {
                Point.Double point = new Point.Double();
                if (nv.tb.getEnableGrid()) {
                    // start trying to change coordinates to spezific numbers (grid)
                    point.x = nv.formatCoordinates(e.getX());
                    point.y = nv.formatCoordinates(e.getY());
                } else {
                    point.x = e.getX();
                    point.y = e.getY();
                }
                Point2D pointInVV = nv.vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point);
                nv.addNode(NetViewer.TRANSITION, "T" + (++nv.transitionCount), pointInVV.getX(), pointInVV.getY());
                nv.modificationActionHappend();
            }
        } else if (e.getClickCount() == 2 && nv.getMouseMode() && !nv.getGM().getSimulatorMode()) {
            psN = nv.vv.getRenderContext().getPickedVertexState();
            if (psN.getPicked().size() >= 1) {
                List<NetViewerNode> selectedNodes = new ArrayList<>();
                for (Object o : psN.getPicked()) {
                    selectedNodes.add((NetViewerNode) o);
                }
                nv.showVertexSetup(selectedNodes);
            }

            psE = nv.vv.getPickedEdgeState();
            if (psE.getPicked().size() >= 1) {
                nv.showEdgeSetup((NetViewerEdge) psE.getPicked().toArray()[0]);
            }
        }

    }

    
    @Override
    public void mousePressed(MouseEvent e) {
    
    }

    @Override
    public void mouseReleased(MouseEvent e) { 
        released = true;
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

    public void setReleased(boolean b) {
        released = b;
    }
    
    public boolean getReleased() {
        return released;
    }
}
