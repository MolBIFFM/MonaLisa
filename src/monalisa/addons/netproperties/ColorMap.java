/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netproperties;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static monalisa.addons.centrality.AdjacencyMatrix.LOGGER;

/**
 * ColorMap managed the Colors of the Nodes. Initializes each node with an empty
 * list of Colors. When a node is assigned a particular color, the color is set
 * to the end of the list. The displayed color of the node is always the last
 * element of the list. In this way it is ensured that when a colored marking of
 * the node is removed, the right color of the node remains.
 *
 * @author daniel
 */
public class ColorMap {

    private HashMap<Integer, List<Color>> colorMap = new HashMap<>();
    private List<Color> colorList;

    /**
     * Adds a color to the node with the corresponding ID.
     *
     * @param id The id of the Node, you want to add a Color.
     * @param color The Color you want to add. Use java.awt.Color.
     */
    public void addColorToList(int id, Color color) {
        if (!colorMap.containsKey(id)) {
            colorList = new ColorList().getColorList();
            colorMap.put(id, colorList);
            colorMap.get(id).add(color);
        } else {
            colorMap.get(id).add(color);
        }
    }

        /**
     * Adds a color to the node with the corresponding ID. Always in the
     * beginning of the list.
     *
     * @param id The id of the Node, you want to add a Color.
     * @param color The Color you want to add. Use java.awt.Color.
     */
    public void addDefaultColorToList(int id, Color color) {
        if (!colorMap.containsKey(id)) {
            colorList = new ColorList().getColorList();
            colorMap.put(id, colorList);
            colorMap.get(id).add(0, color);
        } else {
            colorMap.get(id).add(0, color);
        }
    }
    
    /**
     * Remove a color from the ColorList of the Node. You have to write which
     * color to remove. You can also remove Colors, which are not displayed.
     *
     * @param id The id of the Node, you want to remove a Color.
     * @param color The Color you want to remove. Use java.awt.Color.
     */
    public void removeColorFromList(int id, Color color) {
        while (colorMap.get(id).remove(color));
    }

    /**
     * Returns the Color of the specified node. This is always the last Color in
     * the colorList of the node, if there is more than one color (node can be 
     * allocated to more than one one-sided node). Otherwise returns the default 
     * color (color of node before automatic coloring).
     * If the colorList of the node is empty the return value is null.
     *
     * @param id The id of the Node, you want to get the displayed Color.
     * @return A java.awt.Color.
     */
    public Color getColorFromList(int id) {
        if (!colorMap.get(id).isEmpty()) {
            if (colorMap.get(id).size() > 1) {
                return colorMap.get(id).get(colorMap.get(id).size() - 1);
            } else { // return default color
                return colorMap.get(id).get(0);
            }
        } else {
            return null;
        }
    }

    public List<Color> getColorListTest(int id) {
        return colorMap.get(id);
    }
}

/**
 * List of Colors. Every Node of the PetriNet got such a List to dynamically
 * manage the colors.
 *
 * @author daniel
 */
class ColorList {

    private final List<Color> colorList;

    public ColorList() {
        colorList = new ArrayList<>();
    }

    public List getColorList() {
        return colorList;
    }

}
