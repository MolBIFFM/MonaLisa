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

import de.molbi.mjcl.clustering.ds.Cluster;
import de.molbi.mjcl.clustering.ds.ClusterTree;
import de.molbi.mjcl.clustering.ds.Properties;
import edu.uci.ics.jung.graph.DelegateForest;
import java.util.*;
import monalisa.tools.cluster.ClusterTreeNodeProperties;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * The ClusterTreeImpl class creates a Clustertree with given properties, from
 * top-down.
 *
 * @author stefan marchi
 */
public class ClusterTreeImpl extends DelegateForest<TreeViewerNode, TreeViewerEdge> {

    private final ClusterTree<ClusterTreeNodeProperties, Properties> tree;
    private Map<Integer, TreeViewerNode> nodesMap = new HashMap<>();
    private final List<TreeViewerNode> leafs;
    private final List<TreeViewerNode> clusterNodes;
    private final List<TreeViewerNode> allNodes;
    private final float threshold;

    private boolean redrawn = false;
    private int edgeCount = 0;
    private static final Logger LOGGER = LogManager.getLogger(ClusterTreeImpl.class);

    public ClusterTreeImpl() {
        super();
        LOGGER.info("Creating empty ClusterTreeImpl with standard values");
        this.nodesMap = new HashMap<>();
        this.leafs = new ArrayList<>();
        this.clusterNodes = new ArrayList<>();
        this.allNodes = new ArrayList<>();
        this.tree = new ClusterTree<>();
        this.threshold = 100;
        LOGGER.info("Successfully created empty ClusterTreeImpl with standard values");
    }

    /**
     * Function call the root as start point for the constructing of the tree
     * and call the function goDeep();
     *
     * @param ct Clustertree get the given properties for the tree form function
     * HierarchicalClustering
     * @param threshold get threshold from user interface
     *
     */
    public ClusterTreeImpl(ClusterTree ct, float threshold) {
        super();
        LOGGER.info("Creating new ClusterTreeImpl with specified values");
        this.nodesMap = new HashMap<>();
        this.leafs = new ArrayList<>();
        this.clusterNodes = new ArrayList<>();
        this.allNodes = new ArrayList<>();
        this.tree = ct;
        this.threshold = threshold;

        TreeViewerNode tvn = new TreeViewerNode(String.valueOf(ct.getRoot().getID()), TreeViewerNode.CLUSTERNODE);
        nodesMap.put(ct.getRoot().getID(), tvn);

        this.addVertex(tvn);

        //call function goDeep with root of clustertree
        goDeep(ct.getRoot());
        LOGGER.info("Successfully created new ClusterTreeImpl with specified values");
    }

    /**
     * goDeep() function create recursively, top-down the Clustertree
     *
     * @param c Cluster gets the properties from Clustertree
     */
    private void goDeep(Cluster c) {
        TreeViewerNode tvnC = nodesMap.get(c.getID());
        TreeViewerNode tvnMem;
        Cluster mem;

        //get max threshold from node to root
        double distanceToFurthestLeaf = tree.getRoot().getDistanceToFurthestLeaf();
        //calculate the cutDist from the whole tree
        double cutDist = (distanceToFurthestLeaf * this.threshold) / 100;

        if (c.getDistanceToRoot() <= cutDist) {
            //create tree top-down
            for (int i = 0; i < c.getMemberCount(); i++) {
                mem = c.getMember(i);

                if (tree.getNodeProperty(mem) != null) {
                    tvnMem = new TreeViewerNode(String.valueOf(mem.getID()), TreeViewerNode.CLUSTERNODE, tree.getNodeProperty(mem).getTinvs());
                } else {
                    tvnMem = new TreeViewerNode(String.valueOf(mem.getID()), TreeViewerNode.CLUSTERNODE);
                }
                nodesMap.put(mem.getID(), tvnMem);
                this.addVertex(tvnMem);
                this.addEdge(new TreeViewerEdge(edgeCount++, TreeViewerEdge.CLUSTEREDGE), tvnC, tvnMem);
                goDeep(mem);
            }
        }

        //save nodes in list
        if (this.isLeaf(tvnC)) {
            leafs.add(tvnC);
        } else {
            clusterNodes.add(tvnC);
        }

        allNodes.add(tvnC);
        tvnC.setHeight(c.getDistanceToRoot());

    }

    // Collections.unmodifiableCollection() ensures that lists outsides the class do not
    // get changed.
    public Collection<TreeViewerNode> getAllNodes() {
        return Collections.unmodifiableCollection(this.allNodes);
    }

    public Collection<TreeViewerNode> getAllClusterNodes() {
        return Collections.unmodifiableCollection(this.clusterNodes);
    }

    public Collection<TreeViewerNode> getAllTreeLeaves() {
        return Collections.unmodifiableCollection(this.leafs);
    }

    public TreeViewerNode getTreeViewerNode(int id) {
        return this.nodesMap.get(id);
    }

    boolean isRedrawn() {
        return redrawn;
    }

    void setRedrawn(boolean b) {
        redrawn = b;
    }
}
