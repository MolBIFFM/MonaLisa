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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import monalisa.gui.MonaLisaFrame;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author jens
 */
public class FastSimulationModes extends MonaLisaFrame {

    private GillespieTokenSim gillTS;
    private final ArrayList<StochasticSimulator> fastSimModes;
    private static final Logger LOGGER = LogManager.getLogger(FastSimulationModes.class);
    /**
     * Set of running fast modes of stochastic simulation.
     */
    private Set<StochasticSimulator> fastModes;
    private GillespieTokenSimPanel owner;

    /**
     * Creates new form FastSimulationModes2
     */
    public FastSimulationModes() {
        super();
        this.fastSimModes = new ArrayList<>();
        this.setTitle("Fast Simulation Mode");
        initComponents();
    }

    public FastSimulationModes(GillespieTokenSim tsN, GillespieTokenSimPanel owner) {
        super();
        this.setTitle("Fast Simulation Mode");
        this.fastSimModes = new ArrayList<>();
        this.owner = owner;
        initComponents();
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.gillTS = tsN;

        this.simulationPane.addTab("", null);
        FlowLayout f = new FlowLayout(FlowLayout.CENTER, 5, 0);

        // Make a small JPanel with the layout and make it non-opaque
        JPanel addTabbPane = new JPanel(f);
        addTabbPane.setOpaque(false);
        // Create a JButton for adding the tabs
        JButton addTabButton = new JButton("+");
        addTabButton.setOpaque(false); //
        addTabButton.setBorder(null);
        addTabButton.setContentAreaFilled(false);
        addTabButton.setFocusPainted(false);

        addTabButton.setFocusable(false);

        addTabbPane.add(addTabButton);

        this.simulationPane.setTabComponentAt(this.simulationPane.getTabCount() - 1, addTabbPane);

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner.fastSimFrame.addFastSim(new StochasticSimulator(gillTS, gillTS.deterministicReactionConstants, gillTS.getSimulationMan().getMarking(), gillTS.volume, gillTS.getRandom(), owner.fastSimFrame));
            }
        };
        addTabButton.setFocusable(false);
        addTabButton.addActionListener(listener);
        this.simulationPane.setVisible(true);

        /*
         * Add listener for close operation - all running threads should be ended before exiting
         */
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        for (Iterator<StochasticSimulator> iterator = fastSimModes.iterator(); iterator.hasNext();) {
                            final StochasticSimulator sim = iterator.next();
                            int result = JOptionPane.OK_OPTION;
                            if (sim.running) {
                                result = JOptionPane.showConfirmDialog(sim, "Stop all running simulations and close?", "Terminate simulations",
                                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                            }
                            if (result == JOptionPane.OK_OPTION) {
                                SwingUtilities.invokeAndWait(new Runnable() {
                                    @Override
                                    public void run() {
                                        sim.stopSimulation();
                                    }
                                });
                                fastModes.remove(sim);
                                simulationPane.remove(sim.getContentPane());
                                iterator.remove();
                                sim.dispose();
                            }

                            //If no simulation left, close the frame
                            if (fastSimModes.isEmpty()) {
                                FastSimulationModes.this.setVisible(false);
                            }
                        }
                        return null;
                    }
                }.execute();
            }
        });
    }

    /**
     * Adds a new instance of StochasticSimulator and displays in in the
     * TabbedPane.
     */
    public void addFastSim(StochasticSimulator sim) {
        //If no simulations are running, set the frame visible.
        if (this.fastSimModes.isEmpty()) {
            this.setVisible(true);
        }
        this.fastSimModes.add(sim);
        this.fastModes.add(sim);
        this.simulationPane.insertTab("Simulation " + this.fastSimModes.size(), null, sim.getContentPane(), null, simulationPane.getTabCount() - 1);
        this.simulationPane.setTabComponentAt(simulationPane.getTabCount() - 2, new FastSimulationTabComponent(simulationPane, sim));
        this.simulationPane.setSelectedIndex(simulationPane.getTabCount() - 2);
        this.toFront();
        LOGGER.info("New instance of StochasticSimulator has been initiated");
    }

    /**
     * Stops an instance of StochasticSimulator and removes it from the
     * TabbedPane.
     *
     * @param sim
     */
    public void removeFastSim(StochasticSimulator sim) {
        sim.stopSimulation();
        this.fastSimModes.remove(sim);
        this.fastModes.remove(sim);
        this.simulationPane.remove(sim.getContentPane());
        sim.dispose();
        //If no simulation left, close the frame
        if (this.fastSimModes.isEmpty()) {
            this.setVisible(false);
        }
        LOGGER.info("Instance of StochasticSimulator has finished and stopped");
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

        jScrollPane1 = new javax.swing.JScrollPane();
        simulationPane = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(300, 350));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setMinimumSize(new java.awt.Dimension(400, 300));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 300));
        jScrollPane1.setViewportView(simulationPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane simulationPane;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the fastModes
     */
    public Set<StochasticSimulator> getFastModes() {
        return fastModes;
    }

    /**
     * @param fastModes the fastModes to set
     */
    public void setFastModes(Set<StochasticSimulator> fastModes) {
        this.fastModes = fastModes;
    }
}
