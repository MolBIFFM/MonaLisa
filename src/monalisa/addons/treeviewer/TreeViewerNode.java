/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.treeviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import monalisa.data.pn.TInvariant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Stefan Marchi
 */
public class TreeViewerNode {

    private static final long serialVersionUID = -5990160642604154557L;
    public static final String CLUSTERNODE = "cn";
    public static final String BENDNODE = "bn";

    private final String name;
    private final String nodeType;
    private final List<TInvariant> tinvs;

    private double height;
    private static final Logger LOGGER = LogManager.getLogger(TreeViewerNode.class);

    public TreeViewerNode(String name, String nodeType) {
        this.name = name;
        this.nodeType = nodeType;
        this.tinvs = new ArrayList<>();
        LOGGER.debug("Created new vertex with name '" + name + "' without T-Invariants");
    }

    public TreeViewerNode(String name, String nodeType, List<TInvariant> tinvs) {
        this.name = name;
        this.nodeType = nodeType;
        this.tinvs = tinvs;
        LOGGER.debug("Created new vertex with name '" + name + "' with given T-Invariants");
    }

    public List<TInvariant> getTinvs() {
        return Collections.unmodifiableList(this.tinvs);
    }

    public String getNodeType() {
        return this.nodeType;
    }

    /**
     * Returns the original hight of this node in the Clustertree
     *
     * @return
     */
    public double getHeight() {
        return height;
    }

    /**
     * Set the original hight of this node in the Clustertree
     *
     * @param height
     */
    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TreeViewerNode other = (TreeViewerNode) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.name);
        return hash;
    }

}
