/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.gillespie;

import javax.swing.JOptionPane;
import monalisa.addons.tokensimulator.AbstractTokenSimPrefPanel;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.util.HighQualityRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Pavel Balazki
 */
public class GillespieTokenSimPrefPanel extends AbstractTokenSimPrefPanel {

    //BEGIN VARIABLES DECLARATION
    private GillespieTokenSim gillTS;
    private static final Logger LOGGER = LogManager.getLogger(GillespieTokenSimPrefPanel.class);

    //END VARIABLES DECLARATION
    //BEGIN CONSTRUCTORS
    /**
     * Creates new form GillespieTokenSimPrefPanel
     */
    private GillespieTokenSimPrefPanel() {
        initComponents();
    }

    public GillespieTokenSimPrefPanel(GillespieTokenSim tsN) {
        this.gillTS = tsN;
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
        timeDelayJFormattedTextField.setToolTipText(SimulationManager.strings.get("ATSTimeDelayT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        add(timeDelayJFormattedTextField, gridBagConstraints);

        timeDelayJLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        timeDelayJLabel.setText(SimulationManager.strings.get("ATSTimeDelayLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(timeDelayJLabel, gridBagConstraints);

        updateIntervalFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));
        updateIntervalFormattedTextField.setText("1");
        updateIntervalFormattedTextField.setToolTipText(SimulationManager.strings.get("ATSUpdateIntervalT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(updateIntervalFormattedTextField, gridBagConstraints);

        updateIntervalLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        updateIntervalLabel.setText(SimulationManager.strings.get("ATSUpdateIntervalLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(updateIntervalLabel, gridBagConstraints);

        setSeedButton.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        setSeedButton.setText(SimulationManager.strings.get("STSSetSeedB"));
        setSeedButton.setToolTipText(SimulationManager.strings.get("STSSetSeedB"));
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
        limitThreadsCB.setText(SimulationManager.strings.get("GilTSLimitThreadsCB"));
        limitThreadsCB.setToolTipText(SimulationManager.strings.get("GilTSLimitThreadsTT")
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
            long seed = new Long(JOptionPane.showInputDialog(SimulationManager.strings.get("STSSetSeedBT"), this.gillTS.getRandom().getSeed()));
            this.gillTS.setRandom(new HighQualityRandom(seed));
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

    @Override
    public void updatePreferences() {
        LOGGER.info("Updating the Preferences in the gillespie simulation");
        /*
         * Update time delay.
         */
        LOGGER.debug("Updating the time delay preferences in the gillespie simulation");
        try {
            int timeDelay = Integer.parseInt(timeDelayJFormattedTextField.getText());
            if (timeDelay >= 0) {
                gillTS.getSimulationMan().getPreferences().put("Time delay", timeDelay);
            }
        } catch (NumberFormatException ex) {
            LOGGER.error("NumberFormatException while updating the time delay in the preferences in the gillespie simulation", ex);
        }
        /*
         * Update update interval
         */
        LOGGER.debug("Updating the update interval preferences in the gillespie simulation");
        try {
            int updateInterval = Integer.parseInt(updateIntervalFormattedTextField.getText());
            if (updateInterval >= 0) {
                gillTS.getSimulationMan().getPreferences().put("Update interval", updateInterval);
            }
        } catch (NumberFormatException ex) {
            LOGGER.error("NumberFormatException while updating the update interval in the preferences in the gillespie simulation", ex);
        }
        /*
        Update the limit of parallel simulation threads.
         */
        LOGGER.debug("Updating the preference for the limit of parallel simulation threads in the gillespie simulation");
        gillTS.getSimulationMan().getPreferences().put("LimitMaxThreads", limitThreadsCB.isSelected());
        int nrOfThreads = (int) gillTS.getSimulationMan().getPreferences().get("MaxThreadsNr");
        try {
            nrOfThreads = Integer.parseInt(nrOfMaxThreadsField.getText());
        } catch (NumberFormatException ex) {
            LOGGER.error("NumberFormatException while trying to update the preference for the limit of parallel simulation threads in the gillespie simulation");
            JOptionPane.showMessageDialog(this, SimulationManager.strings.get("TSNumberFormatExceptionM"));
        }
        if (nrOfThreads < 1) {
            LOGGER.error("NumberFormatException while trying to update the preference for the limit of parallel simulation threads in the gillespie simulation, defaulted back to 1");
            JOptionPane.showMessageDialog(this, SimulationManager.strings.get("TSNumberFormatExceptionM"));
            nrOfThreads = 1;
        }
        gillTS.getSimulationMan().getPreferences().put("MaxThreadsNr", nrOfThreads);
    }

    @Override
    public void loadPreferences() {
        timeDelayJFormattedTextField.setText(((Integer) gillTS.getSimulationMan().getPreferences().get("Time delay")).toString());
        updateIntervalFormattedTextField.setText(((Integer) gillTS.getSimulationMan().getPreferences().get("Update interval")).toString());
        limitThreadsCB.setSelected((boolean) gillTS.getSimulationMan().getPreferences().get("LimitMaxThreads"));
        nrOfMaxThreadsField.setEnabled((boolean) gillTS.getSimulationMan().getPreferences().get("LimitMaxThreads"));
        nrOfMaxThreadsField.setText(gillTS.getSimulationMan().getPreferences().get("MaxThreadsNr").toString());
        LOGGER.info("Preferences loaded");
    }
}