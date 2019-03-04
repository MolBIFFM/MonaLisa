/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Molekulare Bioinformatik, Goethe University Frankfurt, Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.layout.ObservableCachingLayout;
import edu.uci.ics.jung.visualization.layout.PersistentLayout;
import edu.uci.ics.jung.visualization.util.Caching;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;

/**
 * Variation of the JUNG Layout class. 
 * Allows a simpler way to save and restore the layout without writing a file.
 *  * @author JUNG Library, modified by Jens Einloft
 * @param <V>
 * @param <E> 
 */
public class MonaLisaLayout<V, E> extends ObservableCachingLayout<V, E>
        implements PersistentLayout<V, E>, ChangeEventSupport, Caching, Serializable {

    /**
     * a container for Vertices
     */
    protected Map<V, Point> map;

    /**
     * a collection of Vertices that should not move
     */
    protected Set<V> dontmove;

    /**
     * whether the graph is locked (stops the VisualizationViewer rendering
     * thread)
     */
    protected boolean locked;

    /**
     * create an instance with a passed layout create containers for graph
     * components
     *
     * @param layout
     */
    public MonaLisaLayout(Layout<V, E> layout) {
        super(layout);
        this.map = LazyMap.decorate(new HashMap<V, Point>(), new RandomPointFactory(getSize()));

        this.dontmove = new HashSet<>();
    }

    /**
     * This method calls <tt>initialize_local_vertex</tt> for each vertex, and
     * also adds initial coordinate information for each vertex. (The vertex's
     * initial location is set by calling <tt>initializeLocation</tt>.
     */
    protected void initializeLocations() {
        for (V v : getGraph().getVertices()) {
            Point2D coord = delegate.transform(v);
            if (!dontmove.contains(v)) {
                initializeLocation(v, coord, getSize());
            }
        }
    }

    /**
     * Sets persisted location for a vertex within the dimensions of the space.
     * If the vertex has not been persisted, sets a random location. If you want
     * to initialize in some different way, override this method.
     *
     * @param v
     * @param coord
     * @param d
     */
    protected void initializeLocation(V v, Point2D coord, Dimension d) {
        Point point = map.get(v);
        coord.setLocation(point.x, point.y);
    }

    /**
     * Returns a list which contains the coordinates for every vertex in the graph
     * @return a map of the Point2D for every vertex
     */
    public Map<V, Point> persist()  {
        for (V v : getGraph().getVertices()) {
            Point p = new Point(transform(v));
            map.put(v, p);
        }
        return map;
    }
    
    /**
     * Set the map of points and initialize their location to the given coordinates
     * @param map a map of the Point2D for every vertex
     */
    public void restore(Map<V, Point> map) {
        this.map = map;
        initializeLocations();
        locked = true;
        fireStateChanged();        
    }
    
    /**
     * Restore the graph Vertex locations from a file
     *
     * @param fileName the file to use
     * @throws IOException for file problems
     * @throws ClassNotFoundException for classpath problems
     */
    @SuppressWarnings("unchecked")
    @Override
    public void restore(String fileName) throws IOException, ClassNotFoundException {

    }

    @Override
    public void lock(boolean locked) {
        this.locked = locked;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.uci.ics.jung.visualization.Layout#incrementsAreDone()
     */
    @Override
    public boolean done() {
        return super.done() || locked;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.uci.ics.jung.visualization.Layout#lockVertex(edu.uci.ics.jung.graph.Vertex)
     */
    @Override
    public void lock(V v, boolean state) {
        dontmove.add(v);
        delegate.lock(v, state);
    }

    @Override
    public void persist(String string) throws IOException {
        
    }

    @SuppressWarnings("serial")
    public static class RandomPointFactory implements Factory<Point>, Serializable {

        Dimension d;

        public RandomPointFactory(Dimension d) {
            this.d = d;
        }

        @Override
        public edu.uci.ics.jung.visualization.layout.PersistentLayout.Point create() {
            double x = Math.random() * d.width;
            double y = Math.random() * d.height;
            return new Point(x, y);
        }
    }

}
