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

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import monalisa.Project;
import monalisa.addons.treeviewer.transformer.TreeViewerEdgeArrowTransformer;
import monalisa.addons.treeviewer.transformer.TreeViewerToolTipTransformer;
import monalisa.addons.treeviewer.transformer.TreeViewerVertexDrawPaintTransformer;
import monalisa.addons.treeviewer.transformer.TreeViewerVertexLabelTransformer;
import monalisa.addons.treeviewer.transformer.TreeViewerVertexPaintTransformer;
import monalisa.addons.treeviewer.transformer.TreeViewerVertexShapeTransformer;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.Transition;
import monalisa.gui.MonaLisaFrame;
import monalisa.results.ClusterConfiguration;
import monalisa.results.Clustering;
import monalisa.results.Configuration;
import monalisa.results.Result;
import monalisa.tools.cluster.ClusterTool;
import monalisa.util.MonaLisaFileChooser;
import monalisa.util.MonaLisaFileFilter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jens
 */
public final class TreeViewer extends MonaLisaFrame {

    private final Project project;

    private final DefaultComboBoxModel<ClusteringWrapper> clusterModel;
    private final ClusterComboBoxItemListener ccbl;

    private ClusterTreeImpl forest;
    private TreeLayout layout;

    private static final Logger LOGGER = LogManager.getLogger(TreeViewer.class);

    /**
     * Creates new form TreeViewer
     *
     * @param project
     */
    public TreeViewer(Project project) {
        super();
        LOGGER.info("Initializing TreeViewer");
        this.project = project;
        this.clusterModel = new DefaultComboBoxModel<>();

        this.forest = new ClusterTreeImpl();
        this.layout = new TreeLayout(forest);

        ClusterConfiguration config;
        for (Map.Entry<Configuration, Result> entry : this.project.getToolManager().getResults(ClusterTool.class).entrySet()) {
            config = (ClusterConfiguration) entry.getKey();
            try {
                clusterModel.addElement(new ClusteringWrapper(((Clustering) project.getToolManager().getResult(new ClusterTool(), config)), config));
            } catch (ClassCastException e) {
                LOGGER.error("Issue while initializing TreeViewer: ", e);
            }
        }

        setTitle("TreeViewer");

        initComponents();

        //Transformer used for TreeViewer
        LOGGER.debug("Setting transformers for TreeViewer");
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<TreeViewerNode, TreeViewerEdge>());
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getRenderContext().setEdgeArrowTransformer(new TreeViewerEdgeArrowTransformer());
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getRenderContext().setVertexDrawPaintTransformer(new TreeViewerVertexDrawPaintTransformer());
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getRenderContext().setVertexLabelTransformer(new TreeViewerVertexLabelTransformer());
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getRenderContext().setVertexFillPaintTransformer(new TreeViewerVertexPaintTransformer());
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getRenderContext().setVertexShapeTransformer(new TreeViewerVertexShapeTransformer());
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).setVertexToolTipTransformer(new TreeViewerToolTipTransformer());
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).setBackground(Color.white);
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getRenderer().getVertexLabelRenderer().setPosition(Position.S);

        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getRenderContext().getPickedVertexState().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    LOGGER.debug("Vertex selected in TreeViewer, gathering EMs");
                    List<TInvariant> tInvs = ((TreeViewerNode) e.getItem()).getTinvs();
                    StringBuilder strBuilder = new StringBuilder();
                    strBuilder.append("<html>");

                    for (TInvariant tInv : tInvs) {
                        strBuilder.append("EM ");
                        strBuilder.append(tInv.id() + 1);
                        strBuilder.append(":<br>");

                        for (Transition t : tInv.transitions()) {
                            strBuilder.append((String) t.getProperty("name"));
                            strBuilder.append("<br>");
                        }
                        strBuilder.append("<br>");
                    }
                    strBuilder.append("</html>");
                    transitionLabel.setText(strBuilder.toString());
                    LOGGER.debug("Successfully handled vertex selection in TreeViewer, displaying EMs");
                }
            }
        });

        TreeViewerModalGraphMouse gm;
        gm = new TreeViewerModalGraphMouse();
        gm.setMode(TreeViewerModalGraphMouse.Mode.PICKING);

        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).setGraphMouse(gm);

        ccbl = new ClusterComboBoxItemListener(this, clusterCb);
        clusterCb.addItemListener(ccbl);

        if (clusterModel.getSize() != 0) {
            showClustering(clusterModel.getElementAt(0));
        }
        LOGGER.info("Successfully initialized TreeViewer");
    }

    public void updateClusterResults() {
        LOGGER.info("Updating clustering results");
        if (this.project.getToolManager().hasResults(ClusterTool.class)) {
            ccbl.setDisabled(true);
            clusterModel.removeAllElements();
            ClusterConfiguration config;
            for (Map.Entry<Configuration, Result> entry : this.project.getToolManager().getResults(ClusterTool.class).entrySet()) {
                config = (ClusterConfiguration) entry.getKey();
                clusterModel.addElement(new ClusteringWrapper(((Clustering) project.getToolManager().getResult(new ClusterTool(), config)), config));
            }
            ccbl.setDisabled(false);
            showClustering(clusterModel.getElementAt(0));
        }
        LOGGER.info("Successfully updated clustering results");
    }

    protected void showClustering(ClusteringWrapper cw) {
        LOGGER.debug("Preparing to show new clustering");
        ccbl.setDisabled(true);
        this.forest = new ClusterTreeImpl(cw.getClustering().getClusterTree(), cw.getTreshold());
        this.layout = new TreeLayout(this.forest);
        this.layout.initialize();
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) this.vv).setGraphLayout(this.layout);
        redraw(this.layout, this.forest);
        this.vv.repaint();
        ccbl.setDisabled(false);
        LOGGER.debug("Successfully displaying clustering");
    }

    public void reset() {
        LOGGER.debug("Resetting TreeViewer");
        this.ccbl.setDisabled(true);
        this.clusterCb.removeAllItems();
        this.ccbl.setDisabled(false);
        this.dispose();
    }

    /**
     * Shows the TreeViewer
     */
    public void showMe() {
        this.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        sidePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        transitionLabel = new javax.swing.JLabel();
        clusterCb = new javax.swing.JComboBox();
        makePicButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        vv = new VisualizationViewer(layout);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(800, 600));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        mainPanel.setMinimumSize(new java.awt.Dimension(800, 600));

        jScrollPane1.setViewportView(transitionLabel);

        clusterCb.setModel(clusterModel);
        clusterCb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clusterCbActionPerformed(evt);
            }
        });

        makePicButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/save_picture.png"))); // NOI18N
        makePicButton.setText("Save as image");
        makePicButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makePicButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Noto Sans", 1, 12)); // NOI18N
        jLabel1.setText("Available T-Clusters");

        javax.swing.GroupLayout sidePanelLayout = new javax.swing.GroupLayout(sidePanel);
        sidePanel.setLayout(sidePanelLayout);
        sidePanelLayout.setHorizontalGroup(
            sidePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidePanelLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel1))
            .addGroup(sidePanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(clusterCb, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(makePicButton)
            .addGroup(sidePanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        sidePanelLayout.setVerticalGroup(
            sidePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidePanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel1)
                .addGap(10, 10, 10)
                .addComponent(clusterCb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(makePicButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout vvLayout = new javax.swing.GroupLayout(vv);
        vv.setLayout(vvLayout);
        vvLayout.setHorizontalGroup(
            vvLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 661, Short.MAX_VALUE)
        );
        vvLayout.setVerticalGroup(
            vvLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(vv);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 646, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(sidePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(sidePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(mainPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clusterCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clusterCbActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_clusterCbActionPerformed

    private void makePicButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makePicButtonActionPerformed
        //save Picture
        LOGGER.info("Exporting clustering as image");
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getPickedVertexState().clear();
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) vv).getPickedEdgeState().clear();

        MonaLisaFileFilter pngFilter = new MonaLisaFileFilter("png", "Portable Network Graphics");
        MonaLisaFileFilter svgFilter = new MonaLisaFileFilter("svg", "Support Vector Graphics");

        MonaLisaFileChooser imgPathChooser = new MonaLisaFileChooser();
        imgPathChooser.setDialogTitle(strings.get("NVImageLocation"));
        imgPathChooser.setAcceptAllFileFilterUsed(false);
        imgPathChooser.addChoosableFileFilter(pngFilter);
        imgPathChooser.addChoosableFileFilter(svgFilter);
        imgPathChooser.showSaveDialog(this);

        File imgFile = imgPathChooser.getSelectedFile();

        if (imgFile != null) {
            MonaLisaFileFilter selectedFileFilter = ((MonaLisaFileFilter) imgPathChooser.getFileFilter());

            // Are an ".png" or ".svg" at the end of the filename?
            imgFile = selectedFileFilter.checkFileNameForExtension(imgFile);

            if (selectedFileFilter.getExtension().equalsIgnoreCase("png")) {
                BufferedImage img = new BufferedImage(vv.getSize().width, vv.getSize().height, BufferedImage.TYPE_INT_RGB);
                vv.paintAll(img.getGraphics());
                try {
                    LOGGER.info("Exporting clustering as .png file");
                    ImageIO.write(img, "png", imgFile);
                    LOGGER.info("Successfully exported as .png file");
                } catch (IOException ex) {
                    LOGGER.error("Issue while exporting clustering as .png file: ", ex);
                }
            } else if (selectedFileFilter.getExtension().equalsIgnoreCase("svg")) {
                // Get a DOMImplementation
                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                // Create an instance of org.w3c.dom.Document
                Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
                // Create an instance of the SVG Generator
                SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
                vv.paint(svgGenerator);
                try {
                    LOGGER.info("Exporting clustering as .svg file");
                    svgGenerator.stream(imgFile.getAbsolutePath());
                    LOGGER.info("Successfully exported clustering as .svg file");
                } catch (SVGGraphics2DIOException ex) {
                    LOGGER.error("Issue while exporting clustering as .svg file: ", ex);
                }
            }
        }
    }//GEN-LAST:event_makePicButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox clusterCb;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton makePicButton;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JLabel transitionLabel;
    private javax.swing.JPanel vv;
    // End of variables declaration//GEN-END:variables

    /**
     * redraw changes the look of the tree, without changing the structure or
     * the order of the tree
     *
     * @param layout
     * @param clusterTreeImpl
     */
    public void redraw(TreeLayout<TreeViewerNode, TreeViewerEdge> layout, ClusterTreeImpl clusterTreeImpl) {
        if (clusterTreeImpl.isRedrawn() == false) {
            clusterTreeImpl.setRedrawn(true);
            LOGGER.info("Redrawing tree");
            //Set Nodes on Distance to Root
            for (TreeViewerNode tvn : clusterTreeImpl.getAllNodes()) {
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
            for (TreeViewerNode tvn : clusterTreeImpl.getAllTreeLeaves()) {
                Point2D tvL = layout.transform(tvn);
                double pointer = tvL.getY();
                if (maxY <= pointer) {
                    maxY = pointer;
                }
            }
            //set all Leaves on max-Y (put them in one line after threshold)
            for (TreeViewerNode tvn : clusterTreeImpl.getAllTreeLeaves()) {
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
            Point.Double newPointNode1;
            Point.Double newPointNode2;
            Point.Double newPointOldNode;
            Point2D tvnCTransform;
            TreeViewerNode newNode1;
            TreeViewerNode newNode2;
            TreeViewerEdge findEdge;
            for (TreeViewerNode tvn : clusterTreeImpl.getAllClusterNodes()) {
                Point2D tvnTransform = layout.transform(tvn);
                Collection<TreeViewerNode> children = clusterTreeImpl.getChildren(tvn);
                //get Y- Cordinates from Children to set nodes in same lines
                for (TreeViewerNode tvnC : children) {
                    //list to remove old Edges from the Tree between Nodes
                    findEdge = clusterTreeImpl.findEdge(tvn, tvnC);
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
                newNode1 = new TreeViewerNode(newPointNode1.toString(), TreeViewerNode.BENDNODE);
                newNode2 = new TreeViewerNode(newPointNode2.toString(), TreeViewerNode.BENDNODE);
                //add both new points (right and left)
                clusterTreeImpl.addVertex(newNode1);
                clusterTreeImpl.addVertex(newNode2);
                //set old CusterNode in the middle of the two children
                Double Zahl = (beforeNodeA + beforeNodeB) / 2;
                newPointOldNode = new Point.Double(Zahl, tvnTransform.getY());
                layout.setLocation(tvn, newPointOldNode);
                //add edges between new and old node
                /*These errors should be fixable by using clusterTreeImpl.getEdgeCount from its super-class. No idea why this was used in the first place.
                However, can't test this until I have a working implementation of MonaLisa.*/
                clusterTreeImpl.addEdge(new TreeViewerEdge(clusterTreeImpl.getEdgeCount(), TreeViewerEdge.BENDEDGE), tvn, newNode1);
                clusterTreeImpl.addEdge(new TreeViewerEdge(clusterTreeImpl.getEdgeCount(), TreeViewerEdge.BENDEDGE), tvn, newNode2);
                clusterTreeImpl.addEdge(new TreeViewerEdge(clusterTreeImpl.getEdgeCount(), TreeViewerEdge.CLUSTEREDGE), newNode1, beforeTvnA);
                clusterTreeImpl.addEdge(new TreeViewerEdge(clusterTreeImpl.getEdgeCount(), TreeViewerEdge.CLUSTEREDGE), newNode2, beforeTvnB);
                //set new points to the layout
                layout.setLocation(newNode1, newPointNode1);
                layout.setLocation(newNode2, newPointNode2);
                counter = 0;
            }
            //remove the old Edges
            for (TreeViewerEdge tne : toDelete) {
                clusterTreeImpl.removeEdge(tne, false);
            }
            LOGGER.info("Successfully redrawn tree");
        }
    }

}
