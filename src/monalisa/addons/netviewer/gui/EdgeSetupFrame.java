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

import monalisa.addons.netviewer.listener.MyColorOptionsMouseListener;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.util.MonaLisaWindowListener;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class EdgeSetupFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = -8913627945453633118L;
    private static final Logger LOGGER = LogManager.getLogger(EdgeSetupFrame.class);

    /**
     * Creates new form EdgeSetupFrame
     */
    public EdgeSetupFrame(NetViewer netViewer, NetViewerEdge nvEdge) {
        LOGGER.info("Initializing new EdgeSetupFrame");
        this.netViewer = netViewer;
        this.nvEdge = nvEdge;

        setAlwaysOnTop(true);

        initComponents();

        loadProperties();

        showColorLabelEdge.addMouseListener(new MyColorOptionsMouseListener(showColorLabelEdge));

        addWindowListener(new MonaLisaWindowListener(this.netViewer));
        LOGGER.info("Successfully initialized new EdgeSetupFrame");
    }

    private void loadProperties() {
        LOGGER.debug("Loading properties of edge");
        weightSpinner.setValue(nvEdge.getWeight());
        showColorLabelEdge.setForeground(nvEdge.getColor());
        showColorLabelEdge.setBackground(nvEdge.getColor());
        if (nvEdge.hasProperty("toolTip")) {
            edgeNoteTextArea.setText((String) nvEdge.getProperty("toolTip"));
        } else {
            edgeNoteTextArea.setText("");
        }
        LOGGER.debug("Successfully loaded properties of edge");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        maiNPanel = new javax.swing.JPanel();
        weightLabel = new javax.swing.JLabel();
        colorLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        edgeNoteTextArea = new javax.swing.JTextArea();
        saveButton = new javax.swing.JButton();
        noteLabel = new javax.swing.JLabel();
        sepertator1 = new javax.swing.JSeparator();
        weightSpinner = new javax.swing.JSpinner();
        showColorLabelEdge = new javax.swing.JLabel();
        hintLabel = new javax.swing.JLabel();

        setTitle("Edge Setup");
        setIconImage(resources.getImage("icon-16.png"));

        maiNPanel.setPreferredSize(new java.awt.Dimension(290, 230));

        weightLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        weightLabel.setText("Weight:");

        colorLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        colorLabel.setText("Color:");

        edgeNoteTextArea.setColumns(20);
        edgeNoteTextArea.setRows(5);
        jScrollPane2.setViewportView(edgeNoteTextArea);

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        noteLabel.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        noteLabel.setText("Note:");

        weightSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        showColorLabelEdge.setText(strings.get("NVColorLabelDummy"));
        showColorLabelEdge.setOpaque(true);

        hintLabel.setFont(new java.awt.Font("Cantarell", 0, 10)); // NOI18N
        hintLabel.setText("(click to change)");

        javax.swing.GroupLayout maiNPanelLayout = new javax.swing.GroupLayout(maiNPanel);
        maiNPanel.setLayout(maiNPanelLayout);
        maiNPanelLayout.setHorizontalGroup(
            maiNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maiNPanelLayout.createSequentialGroup()
                .addGroup(maiNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(maiNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(sepertator1, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(maiNPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(maiNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(maiNPanelLayout.createSequentialGroup()
                                    .addGroup(maiNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(weightLabel)
                                        .addComponent(colorLabel))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(maiNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(maiNPanelLayout.createSequentialGroup()
                                            .addComponent(showColorLabelEdge)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(hintLabel))
                                        .addComponent(weightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(maiNPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(noteLabel))
                    .addGroup(maiNPanelLayout.createSequentialGroup()
                        .addGap(121, 121, 121)
                        .addComponent(saveButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        maiNPanelLayout.setVerticalGroup(
            maiNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maiNPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(maiNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(weightLabel)
                    .addComponent(weightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(maiNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel)
                    .addComponent(showColorLabelEdge)
                    .addComponent(hintLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sepertator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveButton)
                .addGap(6, 6, 6))
        );

        jScrollPane1.setViewportView(maiNPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        LOGGER.info("Saving edge properties");
        if ((Integer) weightSpinner.getValue() != nvEdge.getWeight()) {
            netViewer.modificationActionHappend();
        }

        this.netViewer.writeEdgeSetup(nvEdge, (Integer) weightSpinner.getValue(), showColorLabelEdge.getForeground(), edgeNoteTextArea.getText());
        this.dispose();
        LOGGER.info("Successfully saved edge properties");
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel colorLabel;
    private javax.swing.JTextArea edgeNoteTextArea;
    private javax.swing.JLabel hintLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel maiNPanel;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JButton saveButton;
    private javax.swing.JSeparator sepertator1;
    private javax.swing.JLabel showColorLabelEdge;
    private javax.swing.JLabel weightLabel;
    private javax.swing.JSpinner weightSpinner;
    // End of variables declaration//GEN-END:variables

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private final NetViewer netViewer;
    private final NetViewerEdge nvEdge;
}
