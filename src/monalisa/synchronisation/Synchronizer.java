/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.synchronisation;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.layout.PersistentLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import monalisa.addons.netviewer.MonaLisaLayout;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.data.pn.UniquePetriNetEntity;

/**
 * A class to synchronize NetViewer and PetriNet class. This class synchronize
 * the structure and properties of the PetriNet and the Graph used by the
 * NetViewer. Changes will always take part in the graph and needs applied to
 * the PetriNet
 *
 * @author Jens Einloft
 */
public class Synchronizer implements Serializable {

    private static final long serialVersionUID = -7561258127410331113L;

    private final PetriNet pn;
    private final Graph<NetViewerNode, NetViewerEdge> g;
    private Map<NetViewerNode, PersistentLayout.Point> map;
    private transient MonaLisaLayout<NetViewerNode, NetViewerEdge> layout;

    private int latestVertexID = 0;
    private int latestEdgeID = 0;

    private final Map<Integer, NetViewerNode> transitionMap;
    private final Map<Integer, NetViewerNode> placeMap;

    /**
     * Create a new Synchronizer for a given PetriNet
     *
     * @param pn The PetriNet to synchronize
     */
    public Synchronizer(PetriNet pn) {
        this.pn = pn;
        this.g = new SparseGraph<>();        
        this.layout = new MonaLisaLayout<>(new FRLayout<>(g));

        transitionMap = new HashMap<>();
        placeMap = new HashMap<>();

        translatePNtoGraph();
    }

    /**
     * Return the PetriNet which is synchronized by the instance of the
     * Synchronizer
     *
     * @return The PetriNet which is synchronized
     */
    public PetriNet getPetriNet() {
        return this.pn;
    }

    /**
     * Return the Graph which is synchronized by the instance of the
     * Synchronizer
     *
     * @return The graph which is synchronized
     */
    public Graph<NetViewerNode, NetViewerEdge> getGraph() {
        return this.g;
    }

    /**
     * Returns the layout, corresponding to this Synchronizer instance.
     *
     * @return
     */
    public MonaLisaLayout<NetViewerNode, NetViewerEdge> getLayout() {
        return this.layout;
    }

    /**
     * Returns the corresponding NetViewerNode to a place or transition. If the
     * given UPNE is neither a Place or Transition, the function returns NULL.
     *
     * @param upne Either a Place or a Transition
     * @return If the given UPNE is neither a Place or Transition, the function
     * returns NULL otherwise the reference of a NetViewerNode.
     */
    public NetViewerNode getNodeFromVertex(UniquePetriNetEntity upne) {
        NetViewerNode ret = null;

        if (upne == null) {
            return null;
        }

        if (upne.getClass().equals(monalisa.data.pn.Transition.class)) {
            ret = this.transitionMap.get(upne.id());
        }
        if (upne.getClass().equals(monalisa.data.pn.Place.class)) {
            ret = this.placeMap.get(upne.id());
        }

        return ret;
    }

    /**
     * Returns the corresponding NetViewerNode to ID of a transition.
     *
     * @param upne Either a Place or a Transition
     * @return The corresponding transition, if for the given ID exist a
     * Transition, NULL otherwise.
     */
    public NetViewerNode getNodeFromTransitionId(Integer id) {
        NetViewerNode ret = null;

        if (this.transitionMap.containsKey(id)) {
            ret = this.transitionMap.get(id);
        }

        return ret;
    }

    /**
     * Returns the corresponding NetViewerNode to ID of a place.
     *
     * @param upne Either a Place or a Transition
     * @return The corresponding place, if for the given ID exist a place, NULL
     * otherwise.
     */
    public NetViewerNode getNodeFromPlaceId(Integer id) {
        NetViewerNode ret = null;

        if (this.placeMap.containsKey(id)) {
            ret = this.placeMap.get(id);
        }

        return ret;
    }

    /**
     * Returns the corresponding Place of a NetViewerNode.
     *
     * @param nvNode
     * @return The corresponding Place or NULL, if nvNode is not a Place
     */
    public Place getPlaceFromNode(NetViewerNode nvNode) {
        return this.pn.findPlace(nvNode.getMasterNode().getId());
    }

    /**
     * Returns the corresponding Transition of a NetViewerNode.
     *
     * @param nvNode
     * @return The corresponding Transition or NULL, if nvNode is not a
     * Transition
     */
    public Transition getTransitionFromNode(NetViewerNode nvNode) {
        return this.pn.findTransition(nvNode.getId());
    }

    /**
     * Generates the graph class for the NetViewer out of the PetriNet.
     */
    private void translatePNtoGraph() {
        if (this.g != null) {
            int edgeCount = 0;
            NetViewerNode nvNode;
            // generate Places
            String labelName, propertyName;
            for (Place place : this.pn.places()) {
                labelName = place.<String>getProperty("name");
                nvNode = new NetViewerNode(place.id(), NetViewer.PLACE, labelName);
                placeMap.put(place.id(), nvNode); // this map is needed to synchrone the PN with ne Netviewer graph and for coloring of the nodes
                g.addVertex(nvNode);

                // has the place a compartment?
                if (place.getCompartment() != null) {
                    nvNode.putProperty("compartment", place.getCompartment());
                }
                // save all properties from place to nvNode
                Iterator it = place.getPropertyList().iterator();
                while (it.hasNext()) {
                    propertyName = (String) it.next();
                    nvNode.putProperty(propertyName, place.getProperty(propertyName));
                }
                // serach for highest id, to get no conflict by creating new nodes
                if (place.id() > latestVertexID) {
                    latestVertexID = place.id();
                }
            }

            // generate Transitions
            for (Transition transition : this.pn.transitions()) {
                labelName = (String) transition.getProperty("name");
                nvNode = new NetViewerNode(transition.id(), NetViewer.TRANSITION, labelName);
                transitionMap.put(transition.id(), nvNode);
                g.addVertex(nvNode);
                // has the transition a compartment?
                if (transition.getCompartment() != null) {
                    nvNode.putProperty("compartment", transition.getCompartment());
                }
                // save all properties from place to nvNode
                Iterator it = transition.getPropertyList().iterator();
                while (it.hasNext()) {
                    propertyName = (String) it.next();
                    nvNode.putProperty(propertyName, transition.getProperty(propertyName));
                }
                // serach for highest id, to get no conflict by creating new nodes
                if (transition.id() > latestVertexID) {
                    latestVertexID = transition.id();
                }
            }

            // generate Edges
            int weight;
            NetViewerEdge edge;
            for (Place place : this.pn.places()) {
                // inputs
                for (Transition transition : place.inputs()) {
                    weight = this.pn.getArc(this.pn.findTransition(transition.id()), this.pn.findPlace(place.id())).weight();
                    edge = new NetViewerEdge("e" + edgeCount, weight, transitionMap.get(transition.id()), placeMap.get(place.id()));
                    g.addEdge(edge, edge.getSource(), edge.getAim(), EdgeType.DIRECTED);
                    edgeCount++;
                }
                // outputs
                for (Transition transition : place.outputs()) {
                    weight = this.pn.getArc(this.pn.findPlace(place.id()), this.pn.findTransition(transition.id())).weight();
                    edge = new NetViewerEdge("e" + edgeCount, weight, placeMap.get(place.id()), transitionMap.get(transition.id()));
                    g.addEdge(edge, edge.getSource(), edge.getAim(), EdgeType.DIRECTED);
                    edgeCount++;
                }
            }
        }
    }

    /**
     * START: Section for manipulating the Net *
     */
    /**
     * Creates a new node and add him to the Graph and the PetriNet.
     *
     * @param type NetViewer.PLACE or NetViewer.TRANSITION
     * @param labelName String which is displayed in the NetViewer
     * @param x x coordinate
     * @param y y coordinate
     * @return the created NetViewerNode
     */
    public NetViewerNode addNode(String type, String labelName, double x, double y) {
        NetViewerNode ret = new NetViewerNode(getNewNodeId(), type, labelName);
        g.addVertex(ret);       
        
        Point.Double point = new Point.Double(x,y);  
        layout.setLocation(ret, point);        

        if (ret.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
            Place place = new Place(ret.getId());
            place.putProperty("name", ret.getName());
            place.putProperty("posX", x);
            place.putProperty("posY", y);
            this.pn.addPlace(place);
            this.placeMap.put(place.id(), ret);
        } else if (ret.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
            Transition transition = new Transition(ret.getId());
            transition.putProperty("name", ret.getName());
            transition.putProperty("posX", x);
            transition.putProperty("posY", y);
            this.pn.addTransition(transition);
            this.transitionMap.put(transition.id(), ret);
        } 
        
        return ret;
    }
    
    /**
     * Add a new node in dependence to another node
     * @param nvNode
     * @param direction 0 = input ; 1 = output
     */    
    public NetViewerNode addNode(NetViewerNode nvNode, Boolean direction) {
        NetViewerNode ret;
        
        Point2D coordinates = layout.transform(nvNode);
        if((nvNode.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)))
            ret = addNode(NetViewer.PLACE, "Created_from_"+nvNode.getName(), (int)coordinates.getX()+50, (int)coordinates.getY()+50);
        else
            ret = addNode(NetViewer.TRANSITION, "Created_from_"+nvNode.getName(), (int)coordinates.getX()+50, (int)coordinates.getY()+50);

        if(direction)
            addEdge(1, ret, nvNode);
        else
            addEdge(1, nvNode , ret);       
        
        return ret;
    }
    
    /**
     * Create a new logical place
     * @param sourceNode
     * @param selectedNodes
     * @param nodePosition
     */
    public void addLogicalPlace(NetViewerNode sourceNode, List<NetViewerNode> selectedNodes, Point2D nodePosition) {
        if(!selectedNodes.isEmpty()) {
            NetViewerNode newNode = sourceNode.getMasterNode().generateLocicalPlace(getNewNodeId());
            sourceNode.getMasterNode().setColor(Color.GRAY);
            g.addVertex(newNode);

            if(!(sourceNode.getMasterNode().getColor().equals(Color.WHITE) || sourceNode.getMasterNode().getColor().equals(Color.GRAY)))
                newNode.setColor(sourceNode.getColor());
            
            if(!sourceNode.getMasterNode().getStrokeColor().equals(Color.BLACK))
                newNode.setStrokeColor(sourceNode.getMasterNode().getStrokeColor());
            else
                newNode.setStrokeColor(Color.BLACK);
            
            if(sourceNode.getCorners() != 0)
                newNode.setCorners(sourceNode.getCorners());

            NetViewerEdge oldEdge, newEdge;
            for(Object n : selectedNodes) {
                // in which direction are the edge?
                oldEdge = g.findEdge(sourceNode, (NetViewerNode)n);
                if(oldEdge != null) {
                    newEdge = new NetViewerEdge("L"+getNewEdgeId(), oldEdge.getWeight(), newNode, (NetViewerNode)n);
                    removeAllBends(oldEdge);
                    g.removeEdge(oldEdge);
                    g.addEdge(newEdge, newEdge.getSource(), newEdge.getAim(), EdgeType.DIRECTED);
                    sourceNode.removeOutEdge(oldEdge);
                }
                else {
                    oldEdge = g.findEdge((NetViewerNode)n, sourceNode);
                    newEdge = new NetViewerEdge("L"+getNewEdgeId(), oldEdge.getWeight(), (NetViewerNode)n, newNode);
                    removeAllBends(oldEdge);
                    g.removeEdge(oldEdge);
                    g.addEdge(newEdge, newEdge.getSource(), newEdge.getAim(), EdgeType.DIRECTED);
                    sourceNode.removeInEdge(oldEdge);
                }

                oldEdge.getAim().removeInEdge(oldEdge);
                oldEdge.getSource().removeOutEdge(oldEdge);                
            }
            if(nodePosition == null) {
                Point2D coordinates = layout.transform(sourceNode);
                layout.setLocation(newNode, new Point.Double(coordinates.getX()+50.0, coordinates.getY()+50.0));
            }
            else {
                layout.setLocation(newNode, nodePosition);
            }            
        }
    }    
    
    /**
     * Add a bend to a given edge at a given point (x,y)
     * @param nvEdge
     * @param x
     * @param y 
     */
    public NetViewerEdge addBend(NetViewerEdge nvEdge, double x, double y) {
        NetViewerEdge newEdge;
        NetViewerEdge masterEdge = nvEdge.getMasterEdge();        
        NetViewerNode newNode = addNode(NetViewer.BEND, "B", x+30.0, y+30.0);        
        
        addBendEdge(nvEdge.getSource(), newNode, masterEdge);

        newEdge = new NetViewerEdge("n" + this.getNewEdgeId(), nvEdge.getWeight(), newNode, nvEdge.getAim(), masterEdge);
        if(newEdge.getAim().getNodeType().equalsIgnoreCase(NetViewer.BEND) )
            g.addEdge(newEdge, newEdge.getSource(), newEdge.getAim(), EdgeType.UNDIRECTED);
        else
            g.addEdge(newEdge, newEdge.getSource(), newEdge.getAim(), EdgeType.DIRECTED);

        if(!nvEdge.getSource().getNodeType().equalsIgnoreCase(NetViewer.BEND) && !nvEdge.getAim().getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
            nvEdge.setVisible(false);
        }
        else {
            g.removeEdge(nvEdge);
            masterEdge.removeBendEdge(nvEdge);
            nvEdge.getAim().removeInEdge(nvEdge);
            nvEdge.getSource().removeOutEdge(nvEdge);
        }
                   
        return newEdge;        
    }
   
    /**
     * Add a edge to the Graph and the PetriNet.
     *
     * @param weight Weight of the edge
     * @param source Source of the edge
     * @param aim Aim of the edge
     * @return The new created NetViewerEdge
     */
    public NetViewerEdge addEdge(int weight, NetViewerNode source, NetViewerNode aim) {
        NetViewerEdge ret = new NetViewerEdge("n" + this.getNewEdgeId(), weight, source, aim);
        g.addEdge(ret, source, aim, EdgeType.DIRECTED);

        Object from, to;
        Arc arc;
        switch (ret.getSource().getNodeType()) {
            case NetViewer.PLACE:
                from = this.pn.findPlace(ret.getSource().getMasterNode().getId());
                to = this.pn.findTransition(ret.getAim().getId());
                arc = new Arc(from, to, ret.getWeight());
                this.pn.addArc((Place) from, (Transition) to, arc);
                break;
            case NetViewer.TRANSITION:
                from = this.pn.findTransition(ret.getSource().getId());
                to = this.pn.findPlace(ret.getAim().getMasterNode().getId());
                arc = new Arc(from, to, ret.getWeight());
                this.pn.addArc((Transition) from, (Place) to, arc);
                break;
        }

        return ret;
    }
        
    /**
     * Add a new bend edge to the Graph
     * @param weight
     * @param source
     * @param aim
     * @param masterEdge
     * @return 
     */
    private NetViewerEdge addBendEdge(NetViewerNode source, NetViewerNode aim, NetViewerEdge masterEdge) {
        NetViewerEdge ret = new NetViewerEdge("n" + this.getNewEdgeId(), masterEdge.getWeight(), source, aim, masterEdge);
        g.addEdge(ret, ret.getSource(), ret.getAim(), EdgeType.UNDIRECTED);
        
        return ret;
    }
    
    /**
     * Removes the given edge from the Graph and the PetriNet.
     *
     * @param edge The edge to remove
     */
    public void removeEdge(NetViewerEdge edge) {
        removeEdgeFromPetriNet(edge);        
        removeAllBends(edge);        
        removeEdgeFromGraph(edge);
    }
    
    /**
     * Removes the edge from the graph
     * @param edge 
     */
    private void removeEdgeFromGraph(NetViewerEdge edge) {
        if(edge.getMasterEdge().equals(edge)) {
            edge.getAim().removeInEdge(edge);
            edge.getSource().removeOutEdge(edge);
        }

        edge.getMasterEdge().getAim().removeInEdge(edge.getMasterEdge());
        edge.getMasterEdge().getSource().removeOutEdge(edge.getMasterEdge());
        g.removeEdge(edge.getMasterEdge());        
    }
    
    /**
     * Removes the edge from the Petri net.
     * @param edge 
     */
    private void removeEdgeFromPetriNet(NetViewerEdge edge) {
        if(edge == null) {
            return;
        }
        if(edge.getSource().getNodeType().equals(NetViewer.PLACE)) {
            Place from = this.pn.findPlace(edge.getSource().getMasterNode().getId());
            Transition to = this.pn.findTransition(edge.getAim().getId());
            Arc arc = this.pn.getArc(from, to);
            if (arc != null) {
                this.pn.removeArc(from, to);
            }
        } else {
            Transition from = this.pn.findTransition(edge.getSource().getId());
            Place to = this.pn.findPlace(edge.getAim().getMasterNode().getId());
            Arc arc = this.pn.getArc(from, to);
            if (arc != null) {
                this.pn.removeArc(from, to);
            }
        }        
    }
    
    /**
     * Removes a logical places. All edges a go back to master node or to a
     * other given logical places
     *
     * @param nvNode Place to remove
     * @param aim Aim for edges.
     */
    private void removeLogicalPlace(NetViewerNode nvNode, NetViewerNode aim) {
        if(!nvNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE) || !aim.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
            return;
        }
        if (nvNode.equals(nvNode.getMasterNode())) {
            return;
        }

        List<NetViewerEdge> edgesToDelete = new ArrayList<>();

        for (NetViewerEdge e : nvNode.getInEdges()) {
            this.addEdge(e.getWeight(), e.getSource(), aim);
            edgesToDelete.add(g.findEdge(e.getSource(), nvNode));
        }
        for (NetViewerEdge e : nvNode.getOutEdges()) {
            this.addEdge(e.getWeight(), aim, e.getAim());
            edgesToDelete.add(g.findEdge(nvNode, e.getAim()));
        }

        for (NetViewerEdge nvEdge : edgesToDelete) {
            nvEdge.getSource().removeOutEdge(nvEdge);
            nvEdge.getAim().removeInEdge(nvEdge);
            removeEdgeFromGraph(nvEdge);
        }

        nvNode.getMasterNode().removeLocicalNode(nvNode);

        if(!nvNode.getMasterNode().getColor().equals(Color.WHITE)) {
            nvNode.getMasterNode().setColor(nvNode.getMasterNode().getColor());
        } else {
            nvNode.getMasterNode().setColor(Color.WHITE);
        }

        if(nvNode.getMasterNode().getLogicalPlaces().size() == 1) {
            nvNode.getMasterNode().setColor(Color.WHITE);
        }
    }
    
    /**
     * Melt multiple logical places to the master node. The master node is detected automatically
     * @param nvNodes The list of logical places to be melted
     */
    public void mergeLogicalPlaces(List<NetViewerNode> nvNodes) {               
        NetViewerNode masterNode = nvNodes.get(0).getMasterNode();
        
        for(NetViewerNode nvNode : nvNodes) {
            if(!nvNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
                return;
            }
        }
        
        if(nvNodes.contains(masterNode)) {
            nvNodes.remove(masterNode);
        }
        
        for(NetViewerNode nvNode : nvNodes) {
            removeNode(nvNode);
        }
    }
    
    /**
     * Removes the given node from the Graph and the Petri net
     * @param nvNode 
     */
    public void removeNode(NetViewerNode nvNode) {
        boolean notLogicalAnymore = false;
        
        // Remove all bends
        List<NetViewerEdge> bends2remove = new ArrayList<>();
        for (NetViewerEdge edge : nvNode.getInEdges()) {            
            if (!edge.getMasterEdge().equals(edge)) {
                continue;
            }
            if (!edge.getMasterEdge().getBendEdges().isEmpty()) {
                bends2remove.add(edge.getMasterEdge());
            }
        }
        for (NetViewerEdge edge : nvNode.getOutEdges()) {
            if (!edge.getMasterEdge().equals(edge)) {
                continue;
            }
            if(!edge.getMasterEdge().getBendEdges().isEmpty()) {
                bends2remove.add(edge.getMasterEdge());
            }
        }
        for (NetViewerEdge edge : bends2remove) {
            removeAllBends(edge);
        }        
        
        if(nvNode.isMasterNode()) {
            // Needed to avoid ConcurrentModification Errors
            List<NetViewerNode> nvList = new ArrayList<>();
            for(NetViewerNode n : nvNode.getLogicalPlaces()) {
                if(n.equals(nvNode)) {
                    continue;
                }
                nvList.add(n);
            }            
            for(NetViewerNode n : nvList) {
                removeNode(n);
            }
            nvNode.clearLogicalPlaces();
        } else if(nvNode.isLogical()) {
            removeLogicalPlace(nvNode, nvNode.getMasterNode());
            // If a logical place is delted, bends and egdes have to be removed from Graph but not from Petri net!!!
            notLogicalAnymore = true;
        }      

        // Now remove all edges
        for(NetViewerEdge edge : nvNode.getOutEdges()) {
            removeEdgeFromPetriNet(edge);
            edge.getAim().removeInEdge(edge.getMasterEdge());
        }

        for(NetViewerEdge edge : nvNode.getInEdges()) {
            removeEdgeFromPetriNet(edge);
            edge.getSource().removeOutEdge(edge.getMasterEdge());
        }

        
        // and than the node
        g.removeVertex(nvNode);
        // and from the PetriNet
        if(!nvNode.isLogical() && !notLogicalAnymore) {
            if(nvNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
                this.pn.removePlace(this.pn.findPlace(nvNode.getId()));
                this.placeMap.remove(nvNode.getId());
            } else if(nvNode.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
                this.pn.removeTransition(this.pn.findTransition(nvNode.getId()));
                this.transitionMap.remove(nvNode.getId());
            }
        } 
    }
    
    /**
     * Remove all bends of a gives edge
     *
     * @param bendEdges
     */
    public void removeAllBends(NetViewerEdge edge) {
        List<NetViewerEdge> edgeList = edge.getMasterEdge().getBendEdges();

        for (NetViewerEdge e : edgeList) {
            e.getSource().removeOutEdge(e);
            e.getAim().removeInEdge(e);

            g.removeEdge(e);

            if (e.getAim().getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
                g.removeVertex(e.getAim());
            }
        }
        edge.getMasterEdge().clearBendEdges();
        edge.getMasterEdge().setVisible(true);
    }
    
    /**
     * Removes a bend from the given edge
     * @param edge 
     */
    public void removeBend(NetViewerEdge edge) {
        NetViewerEdge invisibleEdge = null, otherEdge;

        // NetViewer.BEND ------ NODE
        if(edge.getSource().getNodeType().equalsIgnoreCase(NetViewer.BEND) && !edge.getAim().getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
            edge.getAim().removeInEdge(edge);
            edge.getSource().removeOutEdge(edge);                        
            otherEdge = (NetViewerEdge)edge.getSource().getInEdges().toArray()[0];
            
            if(!otherEdge.getSource().getNodeType().equals(NetViewer.BEND)) {
                invisibleEdge = g.findEdge(otherEdge.getSource(), edge.getAim());
            } else {            
                addBendEdge(otherEdge.getSource(), edge.getAim(), edge.getMasterEdge());                                 
            }
            g.removeVertex(edge.getSource());
        }
        // NODE ------ NetViewer.BEND
        else if(edge.getAim().getNodeType().equalsIgnoreCase(NetViewer.BEND) && !edge.getSource().getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
            edge.getSource().removeOutEdge(edge);
            edge.getAim().removeInEdge(edge);
            otherEdge = (NetViewerEdge)edge.getAim().getOutEdges().toArray()[0];
            
            if(!otherEdge.getAim().getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
                invisibleEdge = g.findEdge(edge.getSource(), otherEdge.getAim());
            } else {            
                addBendEdge(edge.getSource(), otherEdge.getAim(), otherEdge.getMasterEdge());
            }
            g.removeVertex(edge.getAim());
        }        
        // NetViewer.BEND ------ NetViewer.BEND
        else {
            edge.getAim().removeInEdge(edge);
            edge.getSource().removeOutEdge(edge);
            otherEdge = ((NetViewerEdge) edge.getSource().getInEdges().toArray()[0]);            
            addBendEdge(otherEdge.getSource(), edge.getAim(), edge.getMasterEdge());                       
            g.removeVertex(otherEdge.getAim());           
        }

        otherEdge.getMasterEdge().removeBendEdge(otherEdge);
        g.removeEdge(otherEdge);
        edge.getMasterEdge().removeBendEdge(edge);
        g.removeEdge(edge);

        if(invisibleEdge != null) {
            invisibleEdge.setVisible(true);
        }        
    }
    
    /**
     * Merges a set of vertices to one vertex
     * @param meltingPot List of NetViewerNode to be merged
     */
    public void mergeVertices(List<NetViewerNode> meltingPot) {
        // Define the vertex which "survive"
        NetViewerNode meltAim = meltingPot.get(0);

        // Go through all other vertices to melt these
        NetViewerEdge newEdge;
        List<NetViewerEdge> toDelte = new ArrayList<>();
        for(NetViewerNode nvNode : meltingPot) {;
            if(nvNode.equals(meltAim)) {
                continue;
            }              
            
            // Delte old edges and create the new ones
            for(NetViewerEdge e : nvNode.getOutEdges()) {
                toDelte.add(e);
            }
            for(NetViewerEdge e : toDelte) {
                addEdge(e.getWeight(), meltAim, e.getAim());
                removeEdge(e);
            }            
            toDelte.clear();
            for(NetViewerEdge e : nvNode.getInEdges()) {
                toDelte.add(e);
            }
            for(NetViewerEdge e : toDelte) {
                addEdge(e.getWeight(), e.getSource(), meltAim);
                removeEdge(e);
            }
            
            // Remove the old vertex
            removeNode(nvNode);
        }        
    }
    
    /**
     * Reverse a transition. Do nothing if a place is given
     * @param nvNode 
     */
    public void reverseTransition(NetViewerNode nvNode, int x, int y) {
        if(nvNode.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {            
            NetViewerNode newNode = addNode(NetViewer.TRANSITION, nvNode.getName()+"_rev" , x, y);
                
            Arc a;
            Transition t = this.getTransitionFromNode(nvNode);            
            for(Place p : t.inputs()) {
                a = this.pn.getArc(p, t);
                addEdge(a.weight(), newNode, getNodeFromPlaceId(p.id()));
            }
            
            for(Place p : t.outputs()) {
                a = this.pn.getArc(t, p);
                addEdge(a.weight(), getNodeFromPlaceId(p.id()), newNode);
            }                      
        }
    }
    
    /**
     * Return a new node id. This ID is higher than any other, older, ID.
     *
     * @return
     */
    public int getNewNodeId() {
        return ++this.latestVertexID;
    }

    /**
     * Return a new edge id. This ID is higher than any other, older, ID.
     *
     * @return
     */
    public int getNewEdgeId() {
        return ++this.latestEdgeID;
    }

    /**
     * END: Section for manipulating the Net *
     */
    /**
     * Return a map to get the corresponding NetViewerNode of a transition.
     *
     * @return
     */
    public Map<Integer, NetViewerNode> getTransitionMap() {
        return Collections.unmodifiableMap(transitionMap);
    }

    /**
     * Return a map to get the corresponding NetViewerNode of a place.
     *
     * @return
     */
    public Map<Integer, NetViewerNode> getPlaceMap() {
        return Collections.unmodifiableMap(placeMap);
    }

    private void writeObject(ObjectOutputStream objectOutput) throws IOException {
        map = layout.persist();
        objectOutput.defaultWriteObject();
    }

    private void readObject(ObjectInputStream objectInput) throws IOException, ClassNotFoundException {
        objectInput.defaultReadObject();

        this.layout = new MonaLisaLayout<>(new FRLayout<>(g));
        this.layout.setSize(new Dimension(1024 * 2, 768 * 2));
        // Happens, if the user try to load an older project format
        if(map != null) {
            this.layout.restore(map);
        }
    }    
}
