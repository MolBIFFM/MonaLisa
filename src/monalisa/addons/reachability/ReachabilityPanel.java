/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import monalisa.addons.AddonPanel;
import monalisa.addons.netviewer.NetViewer;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.results.PInvariants;
import monalisa.results.PInvariantsConfiguration;
import monalisa.tools.pinv.PInvariantTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marcel
 */
public class ReachabilityPanel extends AddonPanel implements ActionListener, ReachabilityListener {

    /**
     * Creates new form ReachabilityPanel
     */
    private static final long serialVersionUID = -8541347764965669414L;

    private final HashMap<Place, Long> start;
    private final HashMap<Place, Long> target;
    private static final Logger LOGGER = LogManager.getLogger(ReachabilityPanel.class);
    private Pathfinder pf;
    private PInvariants pinvs;

    /**
     * Creates new form ReachabilityPanel
     */
        public ReachabilityPanel (NetViewer nv, PetriNetFacade pnf) {
        super(nv, pnf, "Reachability");
        LOGGER.info("Initializing ReachabilityPanel.");
        // Required for the pre-check: Multiply all tokens on a place by the
        // corresponding entry in the vector. Can be obtained using
        // pin.factor(PLACE). If the resulting sum for the start and target
        // marking are different, target marking is confirmed unreachable.
        this.pnf = pnf;
        this.start = new HashMap<>();
        this.start.putAll(pnf.marking());
        this.target = new HashMap<>();
        this.target.putAll(pnf.marking());
        initComponents();
        LOGGER.info("Filling table with current marking");
        DefaultTableModel model = (DefaultTableModel) markingTable.getModel();
        for (Place p : pnf.places()) {
            model.addRow(new Object[]{
                p,
                start.get(p),
                target.get(p)
            });
        }
        aStarRButton.addActionListener(this);
        aStarRButton.setActionCommand("A*");
        breadthRButton.addActionListener(this);
        breadthRButton.setActionCommand("Breadth First Search");
        bestRButton.addActionListener(this);
        bestRButton.setActionCommand("Best First Search");
        LOGGER.info("Successfully initialized ReachabilityPanel.");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        algoRadioGroup = new javax.swing.ButtonGroup();
        startLabel = new javax.swing.JLabel();
        tableScrollPane = new javax.swing.JScrollPane();
        markingTable = new javax.swing.JTable();
        algoLabel = new javax.swing.JLabel();
        breadthRButton = new javax.swing.JRadioButton();
        bestRButton = new javax.swing.JRadioButton();
        aStarRButton = new javax.swing.JRadioButton();
        comboHeuristic = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();
        reachButton = new javax.swing.JButton();
        coverButton = new javax.swing.JButton();
        computeButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        spacerLeft = new javax.swing.JPanel();
        spacerRight = new javax.swing.JPanel();

        startLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        startLabel.setText("Marking");

        markingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
            },
            new String [] {
                "Place", "Starting Token Amount", "Target Token Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Long.class, java.lang.Long.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }

            public Object getValueAt(int row, int col) {
                if (col == 0) {
                    return start.keySet().toArray(new Place[0])[row];
                }
                else {
                    return super.getValueAt(row, col);
                }
            }
        });
        markingTable.getTableHeader().setReorderingAllowed(false);
        tableScrollPane.setViewportView(markingTable);

        algoLabel.setText("Algorithm");

        algoRadioGroup.add(breadthRButton);
        breadthRButton.setSelected(true);
        breadthRButton.setText("Breadth First Search");
        breadthRButton.setToolTipText("");

        algoRadioGroup.add(bestRButton);
        bestRButton.setText("Best First Search");

        algoRadioGroup.add(aStarRButton);
        aStarRButton.setText("A*");

        comboHeuristic.setEnabled(false);
        comboHeuristic.setPreferredSize(new java.awt.Dimension(100, 29));

        jLabel1.setText("Weighting/Heuristic");

        progressLabel.setText("Number of nodes expanded so far: 0");

        reachButton.setText("Full Reachability");
        reachButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reachButtonActionPerformed(evt);
            }
        });

        coverButton.setText("Full Coverability");
        coverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                coverButtonActionPerformed(evt);
            }
        });

        computeButton.setText("Compute selected");
        computeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                computeButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop computation");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(spacerLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(breadthRButton)
                        .addGap(18, 18, 18)
                        .addComponent(bestRButton)
                        .addGap(18, 18, 18)
                        .addComponent(aStarRButton))
                    .addComponent(algoLabel)
                    .addComponent(comboHeuristic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(progressLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(computeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(reachButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(coverButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(tableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(spacerRight, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(startLabel)
                .addGap(18, 18, 18)
                .addComponent(tableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(algoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(breadthRButton)
                    .addComponent(bestRButton)
                    .addComponent(aStarRButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboHeuristic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(progressLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reachButton)
                    .addComponent(coverButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(computeButton)
                    .addComponent(stopButton))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spacerLeft, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 649, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spacerRight, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 649, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void reachButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reachButtonActionPerformed
        updateMarkings();
        LOGGER.info("Requested computation of full reachability graph.");
        //pf = new Pathfinder(pnf, start, target, "FullReach");
        pf.addListenerToAlgorithm(this);
        pf.run();
    }//GEN-LAST:event_reachButtonActionPerformed

    private void coverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_coverButtonActionPerformed
        updateMarkings();
        LOGGER.info("Requested computation of full coverability graph.");
       // pf = new Pathfinder(pnf, start, target, "FullCover");
        pf.addListenerToAlgorithm(this);
        pf.run();
    }//GEN-LAST:event_coverButtonActionPerformed

    private void computeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_computeButtonActionPerformed
        updateMarkings();
        LOGGER.info("Requested computation of a path from start to target marking.");
        String algo = algoRadioGroup.getSelection().getActionCommand();
        if (algo.equals("Breadth First Search")) {
         //   pf = new Pathfinder(pnf, start, target, algo);
        } else {
        //    pf = new Pathfinder(pnf, start, target, algo, (String) comboHeuristic.getSelectedItem());
        }
        if (netViewer.getProject().getToolManager().hasResult(PInvariantTool.class, new PInvariantsConfiguration())) {
            pinvs = netViewer.getProject().getToolManager().getResult(PInvariantTool.class, new PInvariantsConfiguration());      
        } else {
            LOGGER.warn("Results for place invariants not found. Reachability analysis aborted.");
            JOptionPane.showMessageDialog(this, "No results for place invariants have been found. Please compute place invariants before starting the reachability analysis.");
            return;
        }
        if (!pf.checkPIs(pinvs, start, target)) {
            LOGGER.warn("Aborting reachability analysis.");
            JOptionPane.showMessageDialog(this, "Start marking and target marking are incompatible. Sums for place invariants do not match.");
            return;
        }
        pf.addListenerToAlgorithm(this);
        pf.run();
    }//GEN-LAST:event_computeButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        pf.stopAlgorithm();
    }//GEN-LAST:event_stopButtonActionPerformed

    @Override
    public void actionPerformed(ActionEvent e) {
        LOGGER.debug("Action performed: " + e.getActionCommand());
        String command = e.getActionCommand();
        switch (command) {
            case "A*":
                comboHeuristic.setEnabled(true);
                comboHeuristic.removeAllItems();
                comboHeuristic.addItem("Default");
                break;
            case "Best First Search":
                comboHeuristic.setEnabled(true);
                comboHeuristic.removeAllItems();
                comboHeuristic.addItem("Default");
                comboHeuristic.addItem("Weighted Default");
                break;
            case "Breadth First Search":
                comboHeuristic.setEnabled(false);
                comboHeuristic.removeAllItems();
                break;
            default:
                break;
        }
    }

    private void updateMarkings() {
        LOGGER.info("Updating markings from table.");
        for (int i = 0; i < markingTable.getRowCount(); ++i) {
            start.put((Place) markingTable.getValueAt(i, 0), (Long) markingTable.getValueAt(i, 1));
            target.put((Place) markingTable.getValueAt(i, 0), (Long) markingTable.getValueAt(i, 2));
            LOGGER.debug("Updated values for Place " + ((Place) markingTable.getValueAt(i, 0)).getProperty("name"));
        }
        LOGGER.info("Successfully updated markings from table.");
    }

    /**
     * Locks or unlocks the GUI. If b is true, the GUI will be locked. If b is
     * false, the GUI will be unlocked.
     *
     * @param b whether GUI should be locked or unlocked
     */
    private void lock(boolean b) {
        stopButton.setEnabled(b);
        computeButton.setEnabled(!b);
        reachButton.setEnabled(!b);
        coverButton.setEnabled(!b);
        markingTable.setEnabled(!b);
        comboHeuristic.setEnabled(!b);
        aStarRButton.setEnabled(!b);
        bestRButton.setEnabled(!b);
        breadthRButton.setEnabled(!b);
    }

    @Override
    public void update(ReachabilityEvent e) {
        switch (e.getStatus()) {
            case ABORTED: // Aborted should be fired after stopButton was pressed and the thread was successfully canceled.
                lock(false);
                // Do a popup that says things have been terminated at X steps?
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before execution was aborted.");
                progressLabel.setText("Number of nodes expanded before execution was aborted: " + Integer.toString(e.getSteps()));
                break;
            case STARTED: // Should be fired after Compute or either of the full-Buttons was pressed and the algorithm is started.
                lock(true); // Ensures that only one algorithm runs at a time.
                break;
            case SUCCESS: // Fired when an algorithm successfully finds the target marking.
                lock(false);
                ArrayList<Transition> path = e.getBacktrack();
                // Should probably handle displaying output
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before successfully finding target marking.");
                progressLabel.setText("Number of nodes expanded before target marking was successfully found: " + Integer.toString(e.getSteps()));
                break;
            case FAILURE: // Fired when an algorithm fails to find the target marking.
                lock(false);
                // Should output failure.
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before failure was determined.");
                progressLabel.setText("Number of nodes expanded before failure was determined: " + Integer.toString(e.getSteps()));
                break;
            case PROGRESS: // Fired every 100 expanded nodes.
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes so far.");
                progressLabel.setText("Number of nodes expanded so far: " + Integer.toString(e.getSteps()));
                break;
            case FINISHED: // Fired by FullReachability and FullCoverability on completion
                lock(false);
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes to complete the graph.");
                progressLabel.setText("Number of nodes expanded until completion: " + Integer.toString(e.getSteps()));
                // Somehow display the graph? Otherwise this doesn't do much.
                break;
            default:
                break;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton aStarRButton;
    private javax.swing.JLabel algoLabel;
    private javax.swing.ButtonGroup algoRadioGroup;
    private javax.swing.JRadioButton bestRButton;
    private javax.swing.JRadioButton breadthRButton;
    private javax.swing.JComboBox<String> comboHeuristic;
    private javax.swing.JButton computeButton;
    private javax.swing.JButton coverButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTable markingTable;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JButton reachButton;
    private javax.swing.JPanel spacerLeft;
    private javax.swing.JPanel spacerRight;
    private javax.swing.JLabel startLabel;
    private javax.swing.JButton stopButton;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables
}
