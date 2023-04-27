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

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import monalisa.data.PropertyList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Edge in the Net Viewer
 *
 * @author Jens Einloft
 */
public class NetViewerEdge implements Serializable {

    private static final long serialVersionUID = 8357628722035312827L;

    private final String name;
    private int weight;
    private NetViewerNode source, aim;
    private final NetViewerEdge masterEdge;
    private final List<NetViewerEdge> bendEdges = new ArrayList<>();
    private Boolean visible;
    private Color color, defaultColor;

    private final PropertyList properties;
    private static final Logger LOGGER = LogManager.getLogger(NetViewerEdge.class);

    /**
     * Generate a new NetViewerEdge.
     *
     * @param name
     * @param weight
     * @param source NetViewerNode
     * @param aim NetViewerNode
     */
    public NetViewerEdge(String name, int weight, NetViewerNode source, NetViewerNode aim) {
        LOGGER.debug("Creating new NetViewerEdge");
        this.name = name;
        this.weight = weight;
        this.source = source;
        this.aim = aim;
        this.visible = true;
        this.masterEdge = this;
        source.addOutEdge(this);
        aim.addInEdge(this);
        this.color = Color.BLACK;
        this.defaultColor = Color.BLACK;

        this.properties = new PropertyList();
        LOGGER.debug("Successfully created new NetViewerEdge");
    }

    /**
     * Generate a new NetViewerEdge with a given color.
     *
     * @param name
     * @param weight
     * @param source NetViewerNode
     * @param aim NetViewerNode
     * @param color
     */
    public NetViewerEdge(String name, int weight, NetViewerNode source, NetViewerNode aim, Color color) {
        LOGGER.debug("Creating new NetViewerEdge with custom color");
        this.name = name;
        this.weight = weight;
        this.source = source;
        this.aim = aim;
        this.visible = true;
        this.masterEdge = this;
        source.addOutEdge(this);
        aim.addInEdge(this);
        this.color = color;
        this.defaultColor = Color.BLACK;

        this.properties = new PropertyList();
        LOGGER.debug("Successfully created new NetViewerEdge with custom color");
    }

    /**
     * Generate a new Bend Edge
     *
     * @param name
     * @param weight
     * @param source
     * @param aim
     * @param masterEdge
     * @param color
     */
    public NetViewerEdge(String name, int weight, NetViewerNode source, NetViewerNode aim, NetViewerEdge masterEdge, Color color) {
        LOGGER.debug("Creating new NetViewerEdge with bend");
        this.name = name;
        this.weight = weight;
        this.source = source;
        this.aim = aim;
        this.visible = true;
        source.addOutEdge(this);
        aim.addInEdge(this);
        this.masterEdge = masterEdge;
        masterEdge.addBendEdge(this);
        this.color = color;
        this.defaultColor = Color.BLACK;

        this.properties = new PropertyList();
        LOGGER.debug("Successfully created new NetViewerEdge with bend");
    }

    /**
     * Returns the name of the edge
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * returns the weight of the edge
     *
     * @return
     */
    public int getWeight() {
        return this.weight;
    }

    /**
     * Sets a weight for all edges if egde is an edge with a bend
     *
     * @param weight
     */
    public void setWeightForAllEdges(int weight) {
        LOGGER.debug("Setting weight for all edges of edge with bend");
        this.weight = weight;
        for (NetViewerEdge e : this.masterEdge.bendEdges) {
            e.setWeight(weight);
        }
        LOGGER.debug("Successfully set weight for all edges of edge with bend");
    }

    private void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Returns the source of the edge
     *
     * @return
     */
    public NetViewerNode getSource() {
        return this.source;
    }

    /**
     * Changes the source of an edge
     *
     * @param source
     */
    public void changeSource(NetViewerNode source) {
        this.source = source;
    }

    /**
     * Returns the aim of the edge
     *
     * @return
     */
    public NetViewerNode getAim() {
        return this.aim;
    }

    /**
     * Changes the aim of an edge
     *
     * @param aim
     */
    public void changeAim(NetViewerNode aim) {
        this.aim = aim;
    }

    /**
     * Hidde or show the edge
     *
     * @param value
     */
    public void setVisible(Boolean value) {
        this.visible = value;
    }

    /**
     * Returns the visibile of the edge
     *
     * @return
     */
    public Boolean getVisible() {
        return this.visible;
    }

    /**
     * Returns the master edge of an edge
     *
     * @return
     */
    public NetViewerEdge getMasterEdge() {
        return this.masterEdge;
    }

    /**
     * Add a bend edge to the list of bend edges
     *
     * @param bendEdge
     */
    public void addBendEdge(NetViewerEdge bendEdge) {
        this.bendEdges.add(bendEdge);
    }

    /**
     * remove a bend egde from the list of bend edges
     *
     * @param bendEdge
     */
    public void removeBendEdge(NetViewerEdge bendEdge) {
        this.bendEdges.remove(bendEdge);
    }

    /**
     * Returns the list of bend edges
     *
     * @return
     */
    public List<NetViewerEdge> getBendEdges() {
        return this.bendEdges;
    }

    /**
     * Clear the list of bend edges
     */
    public void clearBendEdges() {
        this.bendEdges.clear();
    }

    /**
     * Set the color of an edge
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Set the default color of an edge
     *
     * @param color
     */
    public void setDefaultColor(Color color) {
        this.defaultColor = color;
    }

    /**
     * Set the color of all bend edges
     *
     * @param color
     */
    public void setColorForAllEdges(Color color) {
        this.color = color;
        for (NetViewerEdge e : this.masterEdge.bendEdges) {
            e.setColor(color);
        }
    }

    /**
     * returns the color of an edge
     *
     * @return
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * returns the default color of an edge
     *
     * @return
     */
    public Color getDefaultColor() {
        return this.defaultColor;
    }
    
    /**
     * Add a property to the Petri net entity.
     *
     * @param <T> The type of the property.
     * @param key The key of the property.
     * @param value The property.
     */
    public <T> void putProperty(String key, T value) {
        properties.put(key, value);
    }

    /**
     * Retrieve a strongly typed property, based on its key.
     *
     * @param <T> The type of the property to retrieve.
     * @param key The key of the property.
     * @return The property, cast to type <code>T</code>.
     */
    public <T> T getProperty(String key) {
        return properties.<T>get(key);
    }

    /**
     * Removes a property from the properties list
     *
     * @param key
     */
    public void removeProperty(String key) {
        if (properties.has(key)) {
            properties.remove(key);
        }
    }

    /**
     * Test whether the entity has a given property.
     *
     * @param key The key to look for.
     * @return <code>true</code>, if the key is present, otherwise
     * <code>false</code>.
     */
    public boolean hasProperty(String key) {
        return properties.has(key);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj.getClass().equals(this.getClass())) {
            return this.getName().equalsIgnoreCase(((NetViewerEdge) obj).getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
