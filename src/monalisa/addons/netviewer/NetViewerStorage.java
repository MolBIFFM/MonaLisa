/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.netviewer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.layout.PersistentLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marcel Gehrmann
 */
public class NetViewerStorage implements Serializable {

    private static final long serialVersionUID = 7453006702752115828L;

    private int latestVertexID;
    private int latestEdgeID;
    private Map<Integer, NetViewerNode> placeMap;
    private Map<Integer, NetViewerNode> transitionMap;
    private Map<NetViewerNode, PersistentLayout.Point> map;
    private Graph<NetViewerNode, NetViewerEdge> g;
    transient private MonaLisaLayout layout;

    private static final Logger LOGGER = LogManager.getLogger(NetViewer.class);

    /**
     * Creates new empty NetViewerStorage for a newly created project.
     */
    public NetViewerStorage() {
        LOGGER.info("Initializing new NetViewerStorage");
        this.latestVertexID = 0;
        this.latestEdgeID = 0;
        this.placeMap = new HashMap<>();
        this.transitionMap = new HashMap<>();
        this.g = null;
        this.layout = null;
        this.map = null;
        LOGGER.info("Initialized new NetViewerStorage: " + this.toString());
    }

    public void updateStorage(int vertexID, int edgeID, NetViewer nv) {
        LOGGER.info("Updating NetViewerStorage");
        latestVertexID = vertexID;
        latestEdgeID = edgeID;
        placeMap = nv.getPlaceMap();
        transitionMap = nv.getTransitionMap();
        g = nv.getGraph();
        layout = nv.getMLLayout();
        map = layout.persist();
        LOGGER.info("Updated NetViewerStorage: " + this.toString());
    }

    protected int getLatestVertexID() {
        return latestVertexID;
    }

    protected int getLatestEdgeID() {
        return latestEdgeID;
    }

    protected Map<Integer, NetViewerNode> getPlaceMap() {
        return placeMap;
    }

    protected Map<Integer, NetViewerNode> getTransitionMap() {
        return transitionMap;
    }

    protected Graph<NetViewerNode, NetViewerEdge> getGraph() {
        return g;
    }

    protected MonaLisaLayout getLayout() {
        return layout;
    }

    @Override
    public String toString() {
        return "latestVertexID: " + latestVertexID + " latestEdgeID: "
                + latestEdgeID + " placeMap: " + placeMap.toString()
                + " transitionMap: " + transitionMap.toString();
    }

    private void readObject(ObjectInputStream objectInput) throws IOException, ClassNotFoundException {
        objectInput.defaultReadObject();
        this.layout = new MonaLisaLayout<>(new FRLayout<>(g));
        this.layout.setSize(new Dimension(1024 * 2, 768 * 2));
        // Happens, if the user try to load an older project format

        if (map != null) {
            this.layout.restore(map);
        }
        LOGGER.info("Read in: " + this.toString());
    }

    /**
     * Allows for creation of a NetViewerStorage from the values of an old
     * Synchronizer.
     * @param vertexID
     * @param edgeID
     * @param placeMap
     * @param transitionMap
     * @param g
     * @param map
     */
    public void fromOldSynchronizer(int vertexID, int edgeID,
            Map<Integer, NetViewerNode> placeMap,
            Map<Integer, NetViewerNode> transitionMap,
            Graph<NetViewerNode, NetViewerEdge> g,
            Map<NetViewerNode, PersistentLayout.Point> map) {
        this.latestVertexID = vertexID;
        this.latestEdgeID = edgeID;
        this.placeMap = placeMap;
        this.transitionMap = transitionMap;
        this.g = g;
        this.map = map;
        this.layout.restore(map);
    }
}
