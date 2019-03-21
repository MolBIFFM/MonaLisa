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
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Node in the NetViewer
 * @author Jens Einloft
 */

public class NetViewerNode implements Serializable {
    private static final long serialVersionUID = 2021607424070798173L;

    private static final String READ_PLACE = "Place";
    private static final String READ_TRANSITION = "Transition";
    private static final Color transparent = new Color(0,0,0,0);

    private final String nodeType;
    private String labelName;
    private final int id;
    private int corners;
    private final List<NetViewerNode> logicalPlaces;
    private final List<NetViewerEdge> inEgdes = new ArrayList<>();
    private final List<NetViewerEdge> outEgdes = new ArrayList<>();
    private Color color, searchBarColor, strokeColor;

    private Long tokens = 0L;

    private final NetViewerNode masterNode;
    private Boolean showLabel, isLogical;
    private Position labelPosition;

    private final PropertyList properties;
    private final static Logger LOGGER = LogManager.getLogger(NetViewerNode.class);

    /**
     * Generate a new NetViewerNode
     * @param id
     * @param nodeType PLACE, TRANSITION or BEND
     * @param name  name shown in layout
     */
    public NetViewerNode(int id, String nodeType, String name) {
        LOGGER.debug("Creating new NetViewerNode: " + nodeType + " " + name);
        this.id = id;
        this.nodeType = nodeType;
        this.labelName = name;

        if(nodeType.equalsIgnoreCase(NetViewer.TRANSITION)) {
            this.corners = 4;
            this.color = Color.BLACK;
        }
        else if(nodeType.equalsIgnoreCase(NetViewer.PLACE)) {
            this.corners = 0;
            this.color = Color.WHITE;
        }
        else if(nodeType.equalsIgnoreCase(NetViewer.BEND)) {
            this.corners = 4;
            this.color = transparent;
        }

        this.searchBarColor = Color.BLACK;
        this.strokeColor = color.BLACK;

        this.logicalPlaces = new ArrayList<>();
        this.masterNode = this;
        this.logicalPlaces.add(this);
        this.showLabel = true;
        this.isLogical = false;

        this.labelPosition = Position.SE;

        this.properties = new PropertyList();
        LOGGER.debug("Successfully created NetViewerNode: " + nodeType + " " + name);
    }

    /**
     * Generates a logical place
     * @param id
     * @param nodeType PLACE
     * @param name  intern name
     * @param name  name shown in layout
     * @param masterNode
     */
    public NetViewerNode(int id, NetViewerNode masterNode) {
        LOGGER.debug("Creating new logical place for '" + masterNode.getName() + "'");
        this.id = id;
        this.nodeType = NetViewer.PLACE;
        this.labelName = masterNode.labelName;
        this.color = Color.LIGHT_GRAY;
        this.searchBarColor = Color.BLACK;
        this.corners = 0;
        this.logicalPlaces = new ArrayList<>();
        this.masterNode = masterNode;
        this.showLabel = true;
        this.isLogical = true;
        this.properties = new PropertyList();
        this.labelPosition = Position.SE;
        LOGGER.debug("Successfully created new logical place for '" + masterNode.getName() + "'");
    }

    /**
     * generates a new logical place
     * @param id
     * @return
     */
    public NetViewerNode generateLocicalPlace(int id) {
        NetViewerNode newNode = new NetViewerNode(id, this);
        this.logicalPlaces.add(newNode);

        return newNode;
    }

    /**
     * Return the corners of shape
     * @return
     */
    public int getCorners() {
        return this.corners;
    }

    /**
     * Set the corners for shape
     * @param corners
     */
    public void setCorners(int corners) {
        this.corners = corners;
    }

    /**
     * Returns the node type
     * @return
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * Returns the label name
     * @return
     */
    public String getName() {
        return labelName;
    }

    /**
     * Returns the label name
     * @param name
     */
    public void setName(String name) {
        this.labelName = name;
    }

    /**
     * Returns the id
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Set the given number of tokens
     * @param tokens
     */
    public void setTokens(Long tokens) {
        this.tokens = tokens;
    }

    /**
     * Change the color of all logical places
     * @param color
     */
    public void setColorForAllNodes(Color color) {
        LOGGER.info("Changing color for all logical places");
        this.setColor(color);
        for(NetViewerNode n : this.logicalPlaces) {
            n.setColor(color);
        }
        LOGGER.info("Successfully changed color for all logical places");
    }


    /**
     * Returns the number of tokens
     * @return
     */
    public Long getTokens() {
        return this.tokens;
    }

    /**
     * Returns "Transition x" instead of "TRANSITION x" (same with PLACE)
     * @return
     */
    public String getReadableNodeType() {
        if(nodeType.equalsIgnoreCase(NetViewer.TRANSITION))
            return READ_TRANSITION;
        else if(nodeType.equalsIgnoreCase(NetViewer.PLACE))
            return READ_PLACE;

        return "ERROR";
    }

    /**
     * Returns all logical places
     * @return
     */
    public List<NetViewerNode> getLogicalPlaces() {
        return Collections.unmodifiableList(this.logicalPlaces);
    }

    /**
     * Clears the list of logical places.
     */
    public void clearLogicalPlaces() {
        LOGGER.info("Clearing list of logical places for '" + this.getName() + "'");
        this.logicalPlaces.clear();
        this.isLogical = false;
        LOGGER.info("Successfully cleared list of logical places for '" + this.getName() + "'");
    }

    /**
     * Returns the number of logical places
     * @return
     */
    public int getNbrOfLogicalPlaces() {
        return this.logicalPlaces.size();
    }

    /**
     * Returns the color
     * @return
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Set to given color
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     *
     * @return
     */
    public Color getStrokeColor() {
        return strokeColor;
    }

    /**
     *
     * @param strokeColor
     */
    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    /**
     * Is it a logical place?
     * @return
     */
    public Boolean hasMasterNode() {
        return !(this.masterNode.equals(this));
    }

    /**
     * Returns the master node of a logical place
     * @return
     */
    public NetViewerNode getMasterNode() {
        return this.masterNode;
    }

    /**
     * Remove a logical place from the list of logical places
     * @param logicalNode
     */
    public void removeLogicalNode(NetViewerNode logicalNode) {
        LOGGER.info("Removing logical place from list for '" + this.getName() + "'");
        this.logicalPlaces.remove(logicalNode);
        logicalNode.setLogical(false);
        if(this.logicalPlaces.isEmpty())
            isLogical = false;
        LOGGER.info("Successfully removed logical place from list for '" + this.getName() + "'");
    }

    /**
     * Add a new input edge
     * @param inEdge
     */
    public void addInEdge(NetViewerEdge inEdge) {
        this.inEgdes.add(inEdge);
    }

    /**
     * Remove a input edge
     * @param inEdge
     */
    public void removeInEdge(NetViewerEdge inEdge) {
        this.inEgdes.remove(inEdge);
    }

    /**
     * Add a new output edge
     * @param outEdge
     */
    public void addOutEdge(NetViewerEdge outEdge) {
        this.outEgdes.add(outEdge);
    }

    /**
     * Remove a output edge
     * @param outEdge
     */
    public void removeOutEdge(NetViewerEdge outEdge) {
        this.outEgdes.remove(outEdge);
    }

    /**
     * Returns a List of all input edges
     * @return
     */
    public List<NetViewerEdge> getInEdges() {
        return Collections.unmodifiableList(this.inEgdes);
    }

    /**
     * Returns a list of all output edges
     * @return
     */
    public List<NetViewerEdge> getOutEdges() {
        return Collections.unmodifiableList(this.outEgdes);
    }

    /**
     * Set the amount of tokens for all logical places of the node
     * @param tokens
     */
    public void setTokensForAllNodes(Long tokens) {
        LOGGER.info("Setting token amount for all logical places of '" + this.getName() + "'");
        this.setTokens(tokens);
        for(NetViewerNode n : this.logicalPlaces) {
            n.setTokens(tokens);
        }
        LOGGER.info("Successfully set token amount for all logical places of '" + this.getName() + "'");
    }

    /**
     * Change the stroke color of all logical places
     * @param color
     */
    public void setStrokeColorForAllNodes(Color color) {
        LOGGER.info("Setting stroke color for all logical places of '" + this.getName() + "'");
        this.setStrokeColor(color);
        for(NetViewerNode n : this.logicalPlaces) {
            n.setStrokeColor(color);
        }
        LOGGER.info("Successfully set stroke color for all logical places of '" + this.getName() + "'");
    }

    /**
     * Change the corners of all logical places
     * @param corners
     */
    public void setCornersForAllNodes(int corners) {
        LOGGER.info("Setting corner for all logical places of '" + this.getName() + "'");
        this.setCorners(corners);
        for(NetViewerNode n : this.logicalPlaces) {
            n.setCorners(corners);
        }
        LOGGER.info("Successfully set corner for all logical places of '" + this.getName() + "'");
    }

    /**
     * Change the name of all logical places
     * @param name
     */
    public void setNameForAllNodes(String name) {
        LOGGER.info("Setting name for all logical places of '" + this.getName() + "' to '" + name + "'");
        for(NetViewerNode n : this.logicalPlaces) {
            n.setName(name);
        }
        LOGGER.info("Successfully set name for all logical places of '" + this.getName() + "' to '" + name + "'");
    }

    /**
     *
     * @return
     */
    public Position getLabelPosition() {
        if(labelPosition == null)
            return Position.SE;

        return labelPosition;
    }

    public void setLabelPosition(Position labelPosition) {
        this.labelPosition = labelPosition;
    }

    /**
     * Is master node?
     * @return
     */
    public Boolean isMasterNode() {
        if(this.logicalPlaces.isEmpty())
            return false;
        else if(this.logicalPlaces.size() == 1 && !this.isLogical)
            return false;
        else if(this.logicalPlaces.size() > 1)
            return true;
        else
            return false;
    }

    /**
     * Is this node a logical place?
     * @return
     */
    public Boolean isLogical() {
        return this.isLogical;
    }

    private void setLogical(boolean b) {
        this.isLogical = b;
    }

    /*
     * Set the showLabel flag
     */
    public void setShowLabel(Boolean value) {
            showLabel = value;
    }

    /*
     * Show the label or not?
     */
    public Boolean showLabel() {
        return showLabel;
    }

    /**
     * Set the color of the nodes label in the SearchBar
     * @param searchBarColor
     */
    public void setSearchBarColor(Color searchBarColor) {
        this.searchBarColor = searchBarColor;
    }

    /**
     *
     * @return
     */
    public Color getSearchBarColor() {
        return this.searchBarColor;
    }

    /**
     * Add a property to the Petri net entity.
     * @param <T> The type of the property.
     * @param key The key of the property.
     * @param value The property.
     */
    public <T> void putProperty(String key, T value) {
        properties.put(key, value);
    }

    /**
     * Retrieve a strongly typed property, based on its key.
     * @param <T> The type of the property to retrieve.
     * @param key The key of the property.
     * @return The property, cast to type <code>T</code>.
     */
    public <T> T getProperty(String key) {
        return properties.<T>get(key);
    }

    /**
     * Removes a property from the properties list
     * @param key
     */
    public void removeProperty(String key) {
        if(properties.has(key)) {
            properties.remove(key);
        }
    }

     /**
     * Test whether the entity has a given property.
     * @param key The key to look for.
     * @return <code>true</code>, if the key is present, otherwise <code>false</code>.
     */
    public boolean hasProperty(String key) {
        return properties.has(key);
    }

    /**
     * Retrieve the whole PropertyList
     * @return The PropertyList of the PetriNetEntity
     */
    public PropertyList getPropertyList() {
        return this.properties;
    }

    @Override
    public String toString() {
        return this.labelName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        try {
            if(obj.getClass().equals(this.getClass())) {
                NetViewerNode nvNode = (NetViewerNode) obj;
                if(!this.getNodeType().equalsIgnoreCase(nvNode.getNodeType()))
                    return false;
                return this.id == nvNode.getId();
            }
            else
                return false;
        }
        catch(Exception ex) {
                return false;
        }
    }

    @Override
    public int hashCode() {
        return getId();
    }

    /**
     * Function is called, if a project is loaded.
     * @param objectInput
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream objectInput) throws IOException, ClassNotFoundException {
        objectInput.defaultReadObject();

        if(tokens == null) {
            tokens = 0L;
        }
    }

}