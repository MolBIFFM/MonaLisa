/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer.gui;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.listener.MyColorOptionsMouseListener;
import monalisa.util.MonaLisaWindowListener;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Jens Einloft A Menu to control the color settings of the NetViewer
 */
public class ColorOptionsFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = 8653637787349905596L;
    private static final Logger LOGGER = LogManager.getLogger(ColorOptionsFrame.class);

    /**
     * Creates new form ColorOptionsFrame
     */
    public ColorOptionsFrame(NetViewer netViewer) {
        LOGGER.info("Initializing ColorOptionsFrame");
        this.netViewer = netViewer;

        initComponents();

        setColors();

        tinvColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(tinvColorSelectionLabel));
        pinvColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(pinvColorSelectionLabel));
        mctsColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(mctsColorSelectionLabel));
        heatMapColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(heatMapColorSelectionLabel));
        knockedOutColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(knockedOutColorSelectionLabel));
        alsoKnockedOutColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(alsoKnockedOutColorSelectionLabel));
        notKnockedOutColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(notKnockedOutColorSelectionLabel));
        backgroundColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(backgroundColorSelectionLabel));
        mcsObjectivColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(mcsObjectivColorSelectionLabel));
        mcsColorSelectionLabel.addMouseListener(new MyColorOptionsMouseListener(mcsColorSelectionLabel));

        addWindowListener(new MonaLisaWindowListener(this.netViewer));
        LOGGER.info("Successfully initialized ColorOptionsFrame");
    }

    private void setColors() {
        LOGGER.debug("Setting colors");
        changeLabelColor(tinvColorSelectionLabel, NetViewer.TINV_COLOR);
        changeLabelColor(pinvColorSelectionLabel, NetViewer.PINV_COLOR);
        changeLabelColor(mctsColorSelectionLabel, NetViewer.MCTS_COLOR);
        changeLabelColor(heatMapColorSelectionLabel, NetViewer.HEATMAP_COLOR);
        changeLabelColor(knockedOutColorSelectionLabel, NetViewer.KNOCKEDOUT_COLOR);
        changeLabelColor(alsoKnockedOutColorSelectionLabel, NetViewer.ALSOKNOCKEDOUT_COLOR);
        changeLabelColor(notKnockedOutColorSelectionLabel, NetViewer.NOTKNOCKEDOUTCOLOR);
        changeLabelColor(backgroundColorSelectionLabel, NetViewer.BACKGROUND_COLOR);
        changeLabelColor(mcsObjectivColorSelectionLabel, NetViewer.MCS_COLOR);
        changeLabelColor(mcsColorSelectionLabel, NetViewer.MCSOBJECTIV_COLOR);
        LOGGER.debug("Successfully set colors");
    }

    private Map<String, Color> getColors() {
        LOGGER.debug("Getting colors");
        Map<String, Color> colorMap = new HashMap<>();

        colorMap.put("tinvColor", tinvColorSelectionLabel.getBackground());
        colorMap.put("pinvColor", pinvColorSelectionLabel.getBackground());
        colorMap.put("mctsColor", mctsColorSelectionLabel.getBackground());
        colorMap.put("heatMapColor", heatMapColorSelectionLabel.getBackground());
        colorMap.put("knockedOutColor", knockedOutColorSelectionLabel.getBackground());
        colorMap.put("alsoKnockedOutColor", alsoKnockedOutColorSelectionLabel.getBackground());
        colorMap.put("notKnockedOutColor", notKnockedOutColorSelectionLabel.getBackground());
        colorMap.put("backgroundColor", backgroundColorSelectionLabel.getBackground());
        colorMap.put("mcsObjectivColor", mcsObjectivColorSelectionLabel.getBackground());
        colorMap.put("mcsColor", mcsColorSelectionLabel.getBackground());
        LOGGER.debug("Successfully got colors");
        return colorMap;
    }

    /**
     * Changes the color of given label
     *
     * @param label
     * @param color
     */
    private void changeLabelColor(JLabel label, Color color) {
        LOGGER.debug("Changing label color");
        label.setForeground(color);
        label.setBackground(color);
        LOGGER.debug("Successfully changed label color");
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        heatMapColorSelectionLabel = new javax.swing.JLabel();
        mctsColorSelectionLabel = new javax.swing.JLabel();
        pinvColorSelectionLabel = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        alsoKnockedOutColorSelectionLabel = new javax.swing.JLabel();
        knockedOutColorSelectionLabel = new javax.swing.JLabel();
        notKnockedOutColorSelectionLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        backgroundColorSelectionLabel = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        mcsObjectivColorSelectionLabel = new javax.swing.JLabel();
        mcsColorSelectionLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        tinvColorSelectionLabel = new javax.swing.JLabel();

        setTitle("Color options");
        setIconImage(resources.getImage("icon-16.png"));
        setMinimumSize(new java.awt.Dimension(320, 420));
        setPreferredSize(new java.awt.Dimension(380, 480));
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        mainPanel.setMinimumSize(new java.awt.Dimension(300, 400));
        mainPanel.setPreferredSize(new java.awt.Dimension(300, 400));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        jLabel1.setText("Analysis");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        mainPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel2.setText("P-Invariants");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        mainPanel.add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel3.setText("MCTS");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        mainPanel.add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel4.setText("Heat maps");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        mainPanel.add(jLabel4, gridBagConstraints);

        heatMapColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        heatMapColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        heatMapColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        mainPanel.add(heatMapColorSelectionLabel, gridBagConstraints);

        mctsColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        mctsColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        mctsColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        mainPanel.add(mctsColorSelectionLabel, gridBagConstraints);

        pinvColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        pinvColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        pinvColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        mainPanel.add(pinvColorSelectionLabel, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        jLabel8.setText("Knockout Analysis");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        mainPanel.add(jLabel8, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel9.setText("knocked out Reaction");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        mainPanel.add(jLabel9, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel10.setText("Objectiv Reaction");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        mainPanel.add(jLabel10, gridBagConstraints);

        jLabel11.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel11.setText("MCS Reactions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        mainPanel.add(jLabel11, gridBagConstraints);

        alsoKnockedOutColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        alsoKnockedOutColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        alsoKnockedOutColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        mainPanel.add(alsoKnockedOutColorSelectionLabel, gridBagConstraints);

        knockedOutColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        knockedOutColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        knockedOutColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        mainPanel.add(knockedOutColorSelectionLabel, gridBagConstraints);

        notKnockedOutColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        notKnockedOutColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        notKnockedOutColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        mainPanel.add(notKnockedOutColorSelectionLabel, gridBagConstraints);

        jLabel15.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        jLabel15.setText("Minimal cut Sets");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        mainPanel.add(jLabel15, gridBagConstraints);

        jLabel16.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel16.setText("Background color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        mainPanel.add(jLabel16, gridBagConstraints);

        backgroundColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        backgroundColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        backgroundColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        mainPanel.add(backgroundColorSelectionLabel, gridBagConstraints);

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        mainPanel.add(saveButton, gridBagConstraints);

        jLabel17.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        jLabel17.setText("Other");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        mainPanel.add(jLabel17, gridBagConstraints);

        jLabel12.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel12.setText("not affected Reactions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        mainPanel.add(jLabel12, gridBagConstraints);

        jLabel13.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel13.setText("affected Reactions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        mainPanel.add(jLabel13, gridBagConstraints);

        mcsObjectivColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14));
        mcsObjectivColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        mcsObjectivColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        mainPanel.add(mcsObjectivColorSelectionLabel, gridBagConstraints);

        mcsColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14));
        mcsColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        mcsColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        mainPanel.add(mcsColorSelectionLabel, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel5.setText("T-Invariants");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        mainPanel.add(jLabel5, gridBagConstraints);

        tinvColorSelectionLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        tinvColorSelectionLabel.setText(strings.get("NVColorLabelDummy"));
        tinvColorSelectionLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        mainPanel.add(tinvColorSelectionLabel, gridBagConstraints);

        getContentPane().add(mainPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        try {
            LOGGER.info("Saving color selection");
            this.netViewer.saveColorOptions(getColors());
            this.netViewer.setEnabled(false);
            this.dispose();
        } catch (FileNotFoundException ex) {
            LOGGER.error("Issue while saving color selection: ", ex);
        } catch (IOException ex) {
            LOGGER.error("Issue while saving color selection: ", ex);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JLabel alsoKnockedOutColorSelectionLabel;
    protected javax.swing.JLabel backgroundColorSelectionLabel;
    protected javax.swing.JLabel heatMapColorSelectionLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    protected javax.swing.JLabel knockedOutColorSelectionLabel;
    protected javax.swing.JPanel mainPanel;
    private javax.swing.JLabel mcsColorSelectionLabel;
    private javax.swing.JLabel mcsObjectivColorSelectionLabel;
    protected javax.swing.JLabel mctsColorSelectionLabel;
    protected javax.swing.JLabel notKnockedOutColorSelectionLabel;
    protected javax.swing.JLabel pinvColorSelectionLabel;
    private javax.swing.JButton saveButton;
    protected javax.swing.JLabel tinvColorSelectionLabel;
    // End of variables declaration//GEN-END:variables

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private final NetViewer netViewer;

}
