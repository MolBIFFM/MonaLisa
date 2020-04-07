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

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import org.apache.commons.collections15.Predicate;

/**
 * This class decides if an edge is rendered or not
 *
 * @author Jens Einloft
 */
public class EdgeIncludePredicate implements Predicate<Context<Graph<NetViewerNode, NetViewerEdge>, NetViewerEdge>> {

    @Override
    public boolean evaluate(Context<Graph<NetViewerNode, NetViewerEdge>, NetViewerEdge> t) {
        return t.element.getVisible();
    }

}
