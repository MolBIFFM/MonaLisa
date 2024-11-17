/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import java.util.HashSet;

/**
 *
 * @author Marcel Gehrmann
 */
public class ReachabilityGraph {

    private final HashSet<ReachabilityNode> vertices;
    private final HashSet<ReachabilityEdge> edges;

    public ReachabilityGraph(HashSet vertices, HashSet edges) {
        this.vertices = vertices;
        this.edges = edges;
    }

    /**
     * @return the vertices
     */
    public HashSet getVertices() {
        return vertices;
    }

    /**
     * @return the edges
     */
    public HashSet getEdges() {
        return edges;
    }

    /**
     * Returns the requested edge, if it exists, null otherwise.
     *
     * @param source of the edge
     * @param target of the edge
     * @return ReachabilityEdge
     */
    public ReachabilityEdge getEdge(ReachabilityNode source, ReachabilityNode target) {
        for (ReachabilityEdge e : edges) {
            if (e.getSource().equals(source) && e.getTarget().equals(target)) {
                return e;
            }
        }
        return null;
    }
}
