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
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 *
 * @author jens
 */
public final class TreeViewer extends MonaLisaFrame {

    private final Project project;

    public final static String CLUSTERNODE = "cn";
    public final static String BENDNODE = "bn";

    public final static String CLUSTEREDGE = "ce";
    public final static String BENDEDGE = "be";

    private final DefaultComboBoxModel<ClusteringWrapper> clusterModel;
    private final ClusterComboBoxItemListener ccbl;

    private ClusterTreeImpl forest;
    private TreeLayout layout;

    /**
     * Creates new form TreeViewer
     * @param project
     */
    public TreeViewer(Project project) {
        super();
        this.project = project;
        this.clusterModel = new DefaultComboBoxModel<>();

        this.forest = new ClusterTreeImpl();
        this.layout = new TreeLayout(forest);

        ClusterConfiguration config;
        for (Map.Entry<Configuration, Result> entry : this.project.getResults(ClusterTool.class).entrySet()) {
            config = (ClusterConfiguration) entry.getKey();
            try {
                clusterModel.addElement(new ClusteringWrapper(((Clustering) project.getResult(new ClusterTool(), config)),  config));
            } catch (ClassCastException e) {

            }
        }

        setTitle("TreeViewer");

        initComponents();

        //Transformer used for TreeViewer
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

    }

    public void updateClusterResults() {
        if (this.project.hasResults(ClusterTool.class)) {
            ccbl.setDisabled(true);
            clusterModel.removeAllElements();
            ClusterConfiguration config;
            for (Map.Entry<Configuration, Result> entry : this.project.getResults(ClusterTool.class).entrySet()) {
                config = (ClusterConfiguration) entry.getKey();
                clusterModel.addElement(new ClusteringWrapper(((Clustering) project.getResult(new ClusterTool(), config)), config));
            }
            ccbl.setDisabled(false);
            showClustering(clusterModel.getElementAt(0));
        }
    }

    protected void showClustering(ClusteringWrapper cw) {
        ccbl.setDisabled(true);
        this.forest = new ClusterTreeImpl(cw.getClustering().getClusterTree(), cw.getTreshold());
        this.layout = new TreeLayout(this.forest);
        this.layout.initialize();
        ((VisualizationViewer<TreeViewerNode, TreeViewerEdge>) this.vv).setGraphLayout(this.layout);
        this.forest.redraw(this.layout);
        this.vv.repaint();
        ccbl.setDisabled(false);
    }

    public void reset() {
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
        vv = new VisualizationViewer(layout);
        sidePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        transitionLabel = new javax.swing.JLabel();
        clusterCb = new javax.swing.JComboBox();
        makePicButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(800, 600));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        mainPanel.setMinimumSize(new java.awt.Dimension(800, 600));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout vvLayout = new javax.swing.GroupLayout(vv);
        vv.setLayout(vvLayout);
        vvLayout.setHorizontalGroup(
            vvLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );
        vvLayout.setVerticalGroup(
            vvLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(vv, gridBagConstraints);

        sidePanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(transitionLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 15, 5);
        sidePanel.add(jScrollPane1, gridBagConstraints);

        clusterCb.setModel(clusterModel);
        clusterCb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clusterCbActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        sidePanel.add(clusterCb, gridBagConstraints);

        makePicButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/save_picture.png"))); // NOI18N
        makePicButton.setText("Save as image");
        makePicButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makePicButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        sidePanel.add(makePicButton, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Noto Sans", 1, 12)); // NOI18N
        jLabel1.setText("Available T-Clusters");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        sidePanel.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        mainPanel.add(sidePanel, gridBagConstraints);

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
                    ImageIO.write(img, "png", imgFile);
                } catch (IOException ex) {
                    Logger.getLogger(TreeViewer.class.getName()).log(Level.SEVERE, null, ex);
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
                    svgGenerator.stream(imgFile.getAbsolutePath());
                } catch (SVGGraphics2DIOException ex) {
                    Logger.getLogger(TreeViewer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_makePicButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox clusterCb;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton makePicButton;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JLabel transitionLabel;
    private javax.swing.JPanel vv;
    // End of variables declaration//GEN-END:variables

}
