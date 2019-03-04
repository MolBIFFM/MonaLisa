/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import javax.swing.JOptionPane;
import monalisa.util.HighQualityRandom;

/**
 *
 * @author Pavel Balazki
 */
public class GillespieTokenSimPrefPanel extends javax.swing.JPanel {
    //BEGIN VARIABLES DECLARATION
    private GillespieTokenSim ts;
    //END VARIABLES DECLARATION
    //BEGIN CONSTRUCTORS
    /**
     * Creates new form GillespieTokenSimPrefPanel
     */
    private GillespieTokenSimPrefPanel() {
        initComponents();
    }
    
    public GillespieTokenSimPrefPanel(GillespieTokenSim tsN) {
        this.ts = tsN;
        initComponents();
    }
    //END CONSTRUCTORS

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        timeDelayJFormattedTextField = new javax.swing.JFormattedTextField();
        timeDelayJLabel = new javax.swing.JLabel();
        updateIntervalFormattedTextField = new javax.swing.JFormattedTextField();
        updateIntervalLabel = new javax.swing.JLabel();
        setSeedButton = new javax.swing.JButton();
        limitThreadsCB = new javax.swing.JCheckBox();
        nrOfMaxThreadsField = new javax.swing.JTextField();

        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(this.getPreferredSize());
        setLayout(new java.awt.GridBagLayout());

        timeDelayJFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        timeDelayJFormattedTextField.setText("0");
        timeDelayJFormattedTextField.setToolTipText(TokenSimulator.strings.get("ATSTimeDelayT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        add(timeDelayJFormattedTextField, gridBagConstraints);

        timeDelayJLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        timeDelayJLabel.setText(TokenSimulator.strings.get("ATSTimeDelayLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(timeDelayJLabel, gridBagConstraints);

        updateIntervalFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));
        updateIntervalFormattedTextField.setText("1");
        updateIntervalFormattedTextField.setToolTipText(TokenSimulator.strings.get("ATSUpdateIntervalT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(updateIntervalFormattedTextField, gridBagConstraints);

        updateIntervalLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        updateIntervalLabel.setText(TokenSimulator.strings.get("ATSUpdateIntervalLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(updateIntervalLabel, gridBagConstraints);

        setSeedButton.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        setSeedButton.setText(TokenSimulator.strings.get("STSSetSeedB"));
        setSeedButton.setToolTipText(TokenSimulator.strings.get("STSSetSeedB"));
        setSeedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setSeedButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 36;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(setSeedButton, gridBagConstraints);

        limitThreadsCB.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        limitThreadsCB.setText(TokenSimulator.strings.get("GilTSLimitThreadsCB"));
        limitThreadsCB.setToolTipText(TokenSimulator.strings.get("GilTSLimitThreadsTT")
        );
        limitThreadsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limitThreadsCBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(limitThreadsCB, gridBagConstraints);

        nrOfMaxThreadsField.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        nrOfMaxThreadsField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(nrOfMaxThreadsField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void setSeedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setSeedButtonActionPerformed
        try {
            long seed = new Long(JOptionPane.showInputDialog(TokenSimulator.strings.get("STSSetSeedBT"), this.ts.random.getSeed()));
            this.ts.random = new HighQualityRandom(seed);
        } catch (NumberFormatException E) {
            JOptionPane.showMessageDialog(null, "The seed must be a 48-bit long (Integer)");
        }
    }//GEN-LAST:event_setSeedButtonActionPerformed

    private void limitThreadsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limitThreadsCBActionPerformed
        this.nrOfMaxThreadsField.setEnabled(this.limitThreadsCB.isSelected());
    }//GEN-LAST:event_limitThreadsCBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JCheckBox limitThreadsCB;
    protected javax.swing.JTextField nrOfMaxThreadsField;
    protected javax.swing.JButton setSeedButton;
    protected javax.swing.JFormattedTextField timeDelayJFormattedTextField;
    private javax.swing.JLabel timeDelayJLabel;
    protected javax.swing.JFormattedTextField updateIntervalFormattedTextField;
    private javax.swing.JLabel updateIntervalLabel;
    // End of variables declaration//GEN-END:variables
}