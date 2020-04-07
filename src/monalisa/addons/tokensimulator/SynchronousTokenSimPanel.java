/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import java.awt.event.KeyEvent;

/**
 *
 * @author Pavel Balazki.
 */
public class SynchronousTokenSimPanel extends javax.swing.JPanel {

    //BEGIN VARIABLES DECLARATION
    private AsynchronousTokenSim ts;
    //END VARIABLES DECLARATION

    //BEGIN CONSTRUCTORS
    /**
     * Creates new form AsynchronousTokenSimPanel
     */
    private SynchronousTokenSimPanel() {
        initComponents();
    }

    public SynchronousTokenSimPanel(AsynchronousTokenSim tsN) {
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

        simName = new javax.swing.JLabel();
        stepLabel = new javax.swing.JLabel();
        stepField = new javax.swing.JTextField();
        progressBar = new javax.swing.JProgressBar();
        fireTransitionsButton = new javax.swing.JButton();
        continuousModeCheckBox = new javax.swing.JCheckBox();

        setPreferredSize(this.getPreferredSize());
        setLayout(new java.awt.GridBagLayout());

        simName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        simName.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(simName, gridBagConstraints);

        stepLabel.setText(TokenSimulator.strings.get("ATSStepLabel"));
        stepLabel.setToolTipText("Number of steps to perform, if \"Start simulation sequence\" is used.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(stepLabel, gridBagConstraints);

        stepField.setText("1");
        stepField.setToolTipText(TokenSimulator.strings.get("ATSFiringPerStepT"));
        stepField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                enterPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(stepField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(progressBar, gridBagConstraints);

        fireTransitionsButton.setText(TokenSimulator.strings.get("ATSFireTransitionsB"));
        fireTransitionsButton.setToolTipText(TokenSimulator.strings.get("ATSFireTransitionsBT"));
        fireTransitionsButton.setEnabled(false);
        fireTransitionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireTransitionsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(fireTransitionsButton, gridBagConstraints);

        continuousModeCheckBox.setText(TokenSimulator.strings.get("ATSContinuousModeCheckBox"));
        continuousModeCheckBox.setToolTipText(TokenSimulator.strings.get("ATSContinuousModeCheckBoxT"));
        continuousModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                continuousModeCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(continuousModeCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireTransitionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireTransitionsButtonActionPerformed
        //if no firing takes place, start new sequence of firing
        if (this.fireTransitionsButton.getText().equals(TokenSimulator.strings.get("ATSFireTransitionsB"))) {
            //at this point, user can no more enable or disable the continuous mode
            this.continuousModeCheckBox.setEnabled(false);
            //switch button mode from "fire transitions" to "stop firing"
            this.fireTransitionsButton.setText(TokenSimulator.strings.get("ATSStopFiringB"));
            this.fireTransitionsButton.setToolTipText(TokenSimulator.strings.get("ATSStopFiringBT"));
            this.ts.startFiring();
        } //if a firing sequence is being executed, stop it
        else if (this.fireTransitionsButton.getText().equals(TokenSimulator.strings.get("ATSStopFiringB"))) {
            this.ts.stopFiring();
        }
    }//GEN-LAST:event_fireTransitionsButtonActionPerformed

    private void continuousModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_continuousModeCheckBoxActionPerformed
        if (this.continuousModeCheckBox.isSelected()) {
            this.stepField.setEnabled(false);
        } else {
            this.stepField.setEnabled(true);
        }
    }//GEN-LAST:event_continuousModeCheckBoxActionPerformed

    private void enterPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_enterPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            //at this point, user can not enable or disable the continuous mode no more
            this.continuousModeCheckBox.setEnabled(false);
            //switch button mode from "fire transitions" to "stop firing"
            this.fireTransitionsButton.setText(TokenSimulator.strings.get("ATSStopFiringB"));
            this.fireTransitionsButton.setToolTipText(TokenSimulator.strings.get("ATSStopFiringBT"));
            this.ts.startFiring();
        }
    }//GEN-LAST:event_enterPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JCheckBox continuousModeCheckBox;
    protected javax.swing.JButton fireTransitionsButton;
    protected javax.swing.JProgressBar progressBar;
    protected javax.swing.JLabel simName;
    protected javax.swing.JTextField stepField;
    private javax.swing.JLabel stepLabel;
    // End of variables declaration//GEN-END:variables

}
