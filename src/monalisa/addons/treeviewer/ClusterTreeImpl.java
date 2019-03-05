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
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import de.molbi.mjcl.clustering.ds.Properties;
import edu.uci.ics.jung.graph.DelegateForest;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;
import monalisa.tools.cluster.ClusterTreeNodeProperties;

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
    private final float treshold;

    private boolean redrawed = false;
    private int edgeCount = 0;

    public ClusterTreeImpl() {
        super();

        this.nodesMap = new HashMap<>();
        this.leafs = new ArrayList<>();
        this.clusterNodes = new ArrayList<>();
        this.allNodes = new ArrayList<>();
        this.tree = new ClusterTree<>();
        this.treshold = 100;
    }

    /**
     * Function call the root as start point for the constructing of the tree
     * and call the function goDeep();
     *
     * @param ct Clustertree get the given properties for the tree form function
     * HierarchicalClustering
     * @param treshold get threshold from user interface
     *
     */
    public ClusterTreeImpl(ClusterTree ct, float treshold) {
        super();

        this.nodesMap = new HashMap<>();
        this.leafs = new ArrayList<>();
        this.clusterNodes = new ArrayList<>();
        this.allNodes = new ArrayList<>();
        this.tree = ct;
        this.treshold = treshold;

        TreeViewerNode tvn = new TreeViewerNode(String.valueOf(ct.getRoot().getID()), TreeViewer.CLUSTERNODE);
        nodesMap.put(ct.getRoot().getID(), tvn);

        this.addVertex(tvn);
        
        //call function goDeep with root of clustertree
        goDeep(ct.getRoot());
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
        double cutDist = (distanceToFurthestLeaf * this.treshold) / 100;

        if (c.getDistanceToRoot() <= cutDist) {
            //create tree top-down
            for (int i = 0; i < c.getMemberCount(); i++) {
                mem = c.getMember(i);

                if (tree.getNodeProperty(mem) != null) {
                    tvnMem = new TreeViewerNode(String.valueOf(mem.getID()), TreeViewer.CLUSTERNODE, tree.getNodeProperty(mem).getTinvs());
                } else {
                    tvnMem = new TreeViewerNode(String.valueOf(mem.getID()), TreeViewer.CLUSTERNODE);
                }
                nodesMap.put(mem.getID(), tvnMem);
                this.addVertex(tvnMem);
                this.addEdge(new TreeViewerEdge(edgeCount++, TreeViewer.CLUSTEREDGE), tvnC, tvnMem);
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

    /**
     * redraw change the look from tree, without changing the structur or the
     * order of the tree
     *
     * @param layout
     */
    public void redraw(TreeLayout<TreeViewerNode, TreeViewerEdge> layout) {
        if (redrawed == false) {
            redrawed = true;

            //Set Nodes on Distance to Root
            for (TreeViewerNode tvn : allNodes) {
                Point2D tvP = layout.transform(tvn);
                Point.Double newcluster = new Point.Double();
                //Distance is really small and +30 because root is 0.0
                double parentY = tvn.getHeight() + 30;
                newcluster.y = parentY;
                newcluster.x = tvP.getX();
                layout.setLocation(tvn, newcluster);
            }
            // get max Y value to set leafs on the same line (important for threshold after calculating
            // to set leaves at same level)
            double maxY = 0.0;
            for (TreeViewerNode tvn : leafs) {
                Point2D tvL = layout.transform(tvn);
                double pointer = tvL.getY();
                if (maxY <= pointer) {
                    maxY = pointer;
                }
            }
            //set all Leaves on max-Y (put them in one line after threshold)
            for (TreeViewerNode tvn : leafs) {
                Point2D tvL = layout.transform(tvn);
                Point.Double newPointLeaf = new Point.Double();
                if (tvL.getY() < maxY) {
                    newPointLeaf.y = maxY;
                    newPointLeaf.x = tvL.getX();
                    layout.setLocation(tvn, newPointLeaf);
                }
            }

            Double beforeNodeA = 0.0;
            Double beforeNodeB = 0.0;
            TreeViewerNode beforeTvnA = null;
            TreeViewerNode beforeTvnB = null;
            int counter = 0;
            //create new list for edges which get delete
            List<TreeViewerEdge> toDelete = new ArrayList<>();

            Point.Double newPointNode1, newPointNode2, newPointOldNode;
            Point2D tvnCTransform;
            TreeViewerNode newNode1, newNode2;
            TreeViewerEdge findEdge;

            for (TreeViewerNode tvn : clusterNodes) {
                Point2D tvnTransform = layout.transform(tvn);
                Collection<TreeViewerNode> children = this.getChildren(tvn);
                //get Y- Cordinates from Children to set nodes in same lines
                for (TreeViewerNode tvnC : children) {
                    //list to remove old Edges from the Tree between Nodes
                    findEdge = this.findEdge(tvn, tvnC);
                    toDelete.add(findEdge);
                    //saves the X Cordinates from first Children of the Node
                    tvnCTransform = layout.transform(tvnC);
                    if (counter == 0) {
                        beforeNodeA = tvnCTransform.getX();
                        beforeTvnA = tvnC;
                        counter += 1;
                        //saves from the second children
                    } else if (counter == 1) {
                        beforeNodeB = tvnCTransform.getX();
                        beforeTvnB = tvnC;
                        counter += 1;
                    }
                }
                //set the new Points right and left of the Node
                newPointNode1 = new Point.Double(beforeNodeA, tvnTransform.getY());
                newPointNode2 = new Point.Double(beforeNodeB, tvnTransform.getY());

                newNode1 = new TreeViewerNode(newPointNode1.toString(), TreeViewer.BENDNODE);
                newNode2 = new TreeViewerNode(newPointNode2.toString(), TreeViewer.BENDNODE);

                //add both new points (right and left)
                this.addVertex(newNode1);
                this.addVertex(newNode2);

                //set old CusterNode in the middle of the two children
                Double Zahl = (beforeNodeA + beforeNodeB) / 2;
                newPointOldNode = new Point.Double(Zahl, tvnTransform.getY());
                layout.setLocation(tvn, newPointOldNode);

                //add edges between new and old node
                this.addEdge(new TreeViewerEdge(edgeCount++, TreeViewer.BENDEDGE), tvn, newNode1);
                this.addEdge(new TreeViewerEdge(edgeCount++, TreeViewer.BENDEDGE), tvn, newNode2);
                this.addEdge(new TreeViewerEdge(edgeCount++, TreeViewer.CLUSTEREDGE), newNode1, beforeTvnA);
                this.addEdge(new TreeViewerEdge(edgeCount++, TreeViewer.CLUSTEREDGE), newNode2, beforeTvnB);

                //set new points to the layout
                layout.setLocation(newNode1, newPointNode1);
                layout.setLocation(newNode2, newPointNode2);
                counter = 0;
            }
            //remove the old Edges
            for (TreeViewerEdge tne : toDelete) {
                this.removeEdge(tne, false);
            }
        }
    }

    // Collections.unmodifiableCollection() ensures that lists outsides the class do not
    // get changed.
    public Collection<TreeViewerNode> getAllNode() {
        return Collections.unmodifiableCollection(this.allNodes);
    }

    public Collection<TreeViewerNode> getAllClusterNode() {
        return Collections.unmodifiableCollection(this.clusterNodes);
    }

    public Collection<TreeViewerNode> getAllTreeLeaves() {
        return Collections.unmodifiableCollection(this.leafs);
    }

    public TreeViewerNode getTreeViewerNode(int id) {
        return this.nodesMap.get(id);
    }
}
