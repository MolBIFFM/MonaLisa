/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.synchronous;

import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import monalisa.addons.tokensimulator.AbstractTokenSimPanel;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.SimulationPanel;
import monalisa.addons.tokensimulator.listeners.SimulationEvent;
import monalisa.addons.tokensimulator.listeners.SimulationListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Pavel Balazki.
 */
public class SynchronousTokenSimPanel extends AbstractTokenSimPanel implements SimulationListener {

    //BEGIN VARIABLES DECLARATION
    private SynchronousTokenSim syncTS;
    private static final Logger LOGGER = LogManager.getLogger(SynchronousTokenSimPanel.class);
    //END VARIABLES DECLARATION
    private SimulationPanel owner;

    //BEGIN CONSTRUCTORS
    /**
     * Creates new form AsynchronousTokenSimPanel
     */
    private SynchronousTokenSimPanel() {
        initComponents();
    }

    public SynchronousTokenSimPanel(SynchronousTokenSim tsN, SimulationPanel owner) {
        this.syncTS = tsN;
        this.owner = owner;
        initComponents();
    }
    //END CONSTRUCTORS

    /*
     * @return the progressBar
     */
    public javax.swing.JProgressBar getProgressBar() {
        return progressBar;
    }

    public boolean isContinuous() {
        return continuousModeCheckBox.isSelected();
    }

    @Override
    public void unlock() {
        fireTransitionsButton.setText(SimulationManager.strings.get("ATSFireTransitionsB"));
        fireTransitionsButton.setToolTipText(SimulationManager.strings.get("ATSFireTransitionsBT"));

        if (!continuousModeCheckBox.isSelected()) {
            stepField.setEnabled(true);
        }
        continuousModeCheckBox.setEnabled(true);
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

        stepLabel.setText(SimulationManager.strings.get("ATSStepLabel"));
        stepLabel.setToolTipText("Number of steps to perform, if \"Start simulation sequence\" is used.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(stepLabel, gridBagConstraints);

        stepField.setText("1000");
        stepField.setToolTipText(SimulationManager.strings.get("ATSFiringPerStepT"));
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

        fireTransitionsButton.setText(SimulationManager.strings.get("ATSFireTransitionsB"));
        fireTransitionsButton.setToolTipText(SimulationManager.strings.get("ATSFireTransitionsBT"));
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

        continuousModeCheckBox.setText(SimulationManager.strings.get("ATSContinuousModeCheckBox"));
        continuousModeCheckBox.setToolTipText(SimulationManager.strings.get("ATSContinuousModeCheckBoxT"));
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
        if (this.fireTransitionsButton.getText().equals(SimulationManager.strings.get("ATSFireTransitionsB"))) {
            //at this point, user can no more enable or disable the continuous mode
            this.continuousModeCheckBox.setEnabled(false);
            //switch button mode from "fire transitions" to "stop firing"
            this.fireTransitionsButton.setText(SimulationManager.strings.get("ATSStopFiringB"));
            this.fireTransitionsButton.setToolTipText(SimulationManager.strings.get("ATSStopFiringBT"));
            startFiring();
        } //if a firing sequence is being executed, stop it
        else if (this.fireTransitionsButton.getText().equals(SimulationManager.strings.get("ATSStopFiringB"))) {
            stopFiring();
        }
    }//GEN-LAST:event_fireTransitionsButtonActionPerformed

    private void continuousModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_continuousModeCheckBoxActionPerformed
        if (this.continuousModeCheckBox.isSelected()) {
            this.stepField.setEnabled(false);
        } else {
            this.stepField.setEnabled(true);
        }
        if (syncTS.getSimSwingWorker() != null) {
            syncTS.getSimSwingWorker().setContinuous(isContinuous());
        }
    }//GEN-LAST:event_continuousModeCheckBoxActionPerformed

    private void enterPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_enterPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            //at this point, user can not enable or disable the continuous mode no more
            this.continuousModeCheckBox.setEnabled(false);
            //switch button mode from "fire transitions" to "stop firing"
            this.fireTransitionsButton.setText(SimulationManager.strings.get("ATSStopFiringB"));
            this.fireTransitionsButton.setToolTipText(SimulationManager.strings.get("ATSStopFiringBT"));
            startFiring();
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

    @Override
    protected void startFiring() {
        LOGGER.info("Firing of Synchronous Token Simulator started");
        stepField.setEnabled(false);
        continuousModeCheckBox.setEnabled(false);
        syncTS.getSimulationMan().lockGUI(true);
        int steps = 0;
        try {
            //number of steps that will be performed
            steps = Integer.parseInt(stepField.getText());
        } catch (NumberFormatException nfe) {
            stopFiring();
            LOGGER.error("NumberFormatException while checking the number of firing steps in the synchronous token simulator", nfe);
            JOptionPane.showMessageDialog(null, SimulationManager.strings.get("TSNumberFormatExceptionM"));
        } finally {
            if (steps < 1) {
                steps = 1;
                stepField.setText("1");
            }
            //Create new thread that will perform all firing steps.
            syncTS.setSimSwingWorker(new SynchronousSimulationSwingWorker(syncTS.getSimulationMan(), syncTS, isContinuous(), steps));
            syncTS.getSimSwingWorker().addSimulationListener(this);
            syncTS.getSimSwingWorker().execute();
        }
    }

    @Override
    protected void stopFiring() {
        LOGGER.info("Firing of Synchronous Token Simulator stopped");
        if (syncTS.getSimSwingWorker() != null) {
            syncTS.getSimSwingWorker().stopSequence();
        }
    }

    @Override
    public void simulationUpdated(SimulationEvent e) {
        String type = e.getType();
        switch (type) {
            case SimulationEvent.INIT:
                getProgressBar().setMaximum((int) e.getValue());
                break;
            case SimulationEvent.UPDATE_PROGRESS:
                getProgressBar().setValue(getProgressBar().getMaximum() - ((int) e.getValue()));
                break;
            case SimulationEvent.UPDATE_VISUAL:
                syncTS.getSimulationMan().updateVisualOutput();
                break;
            case SimulationEvent.DONE:
                getProgressBar().setMaximum(0);
                getProgressBar().setValue(0);
                //unlock GUI
                unlock();
                break;
            case SimulationEvent.STOPPED:
                syncTS.getSimulationMan().updateVisualOutput();
                break;
            default:
                break;
        }
    }

    @Override
    public void setSimName(String name) {
        simName.setText(name);
    }

    @Override
    public void startSim() {
        LOGGER.info("Starting synchronous Simulation");
        stepField.setEnabled(true);
        fireTransitionsButton.setEnabled(true);
        syncTS.computeActiveTransitions();
    }

    @Override
    public void endSim() {
        LOGGER.info("Ending synchronous Simulation");
        try {
            if (syncTS.getSimSwingWorker() != null) { // Should only be null if the sim was stopped without starting it.
                syncTS.getSimSwingWorker().cancel(true);
            }
        } catch (NullPointerException ex) {
            LOGGER.error("NullPointerException while trying to cancel the simSwingWorker in the synchronous simulation: ", ex);
        }
        stepField.setEnabled(false);
        fireTransitionsButton.setEnabled(false);
        continuousModeCheckBox.setEnabled(false);
        owner.disableSetup();
        syncTS.getSimulationMan().lockGUI(true);
    }
}
