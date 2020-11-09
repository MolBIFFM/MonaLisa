/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.utils;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import layout.TableLayout;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.SimulationPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a new frame with a table with statistics
 *
 * @author Pavel Balazki.
 */
public class StatisticFrame {

    //BEGIN VARIABLES DECLARATION
    //visible frame with statistic data in it
    private JFrame statisticFrame;
    //textual ouput of the statistics
    private JTextArea textOutput;
    //shows the number of the step that is currently shown
    private JLabel stepNr;
    private JScrollPane statisticScrollPane, snapshotListScrollPane;
    //List of snapshots
    private JList snapshotList;
    //If a snapshot is picked, show its statistic in statisticScrollPane
    private final ListSelectionListener snapshotListSelectionListener;
    private SimulationPanel owner;
    private static final Logger LOGGER = LogManager.getLogger(StatisticFrame.class);

    //END VARIABLES DECLARATION
    //BEGIN CONSTRUCTORS
    /**
     * Constructor without parameters is not allowed.
     */
    private StatisticFrame() {
        snapshotListSelectionListener = null;
    }

    public StatisticFrame(SimulationPanel owner) {
        this.owner = owner;
        /*
         * when a snapshot in the list is picked, show statistic for it
         */
        this.snapshotListSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (lse.getValueIsAdjusting() == false) {
                    int selectedVar = snapshotList.getSelectedIndex();
                    if (selectedVar > -1) {
                        snapshotList.clearSelection();
                        Snapshot snap = owner.getSnapshots().get(selectedVar);
                        Statistic stat = snap.getStatistic();
                        stepNr.setText("Step " + snap.getStepNr());
                        statisticScrollPane.setViewportView(getStatisticTable(stat));

                        textOutput.setText("");
                        textOutput.append(SimulationManager.strings.get("STATTotalStepsFired").concat(" ").concat(Integer.toString(
                                stat.stepsFired)));
                        textOutput.append(System.getProperty("line.separator").concat(SimulationManager.strings.get("STATTotalTransitionsFired")).concat(" ").
                                concat(Integer.toString(stat.transitionsFired)));

                        statisticFrame.repaint();
                    }
                }
            }
        };
        initGUI();
    }
    //END CONSTRUCTORS

    /**
     * On initialization of the frame, currStatistic of the current state of
     * TokenSimulator is shown.
     */
    private void initGUI() {
        LOGGER.info("Showing current statistics to the simulation");
        this.statisticFrame = new JFrame(SimulationManager.strings.get("STATName"));
        this.statisticFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.statisticFrame.setLocationRelativeTo(null);
        this.statisticFrame.setIconImage(SimulationManager.resources.getImage("icon-16.png"));
        /*
         * Make frame disappear when ESC pressed.
         */
        this.statisticFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
        this.statisticFrame.getRootPane().getActionMap().put("Cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statisticFrame.dispose();
            }
        });

        double statisticFrameSize[][]
                = {
                    //X-Axes    1
                    {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL},
                    //Y-Axes    1 | 3 | 5 | 7 | 9 | 11 | 13 | 15 | 17 | 19 | 21
                    {15, TableLayout.PREFERRED, 1, TableLayout.PREFERRED, 1, TableLayout.PREFERRED, 1, TableLayout.PREFERRED,
                        1, TableLayout.PREFERRED, 1, TableLayout.PREFERRED, 1, TableLayout.PREFERRED, 1, TableLayout.PREFERRED, 1, TableLayout.PREFERRED,
                        1, TableLayout.PREFERRED, 1, TableLayout.PREFERRED, 15}};

        this.statisticFrame.getContentPane().setLayout(new TableLayout(statisticFrameSize));

        /*
         * state which currStatistic is currently shown. at the begin, currStatistic of the last snapshot is shown; it represents the state at which
         * "Show Statistic"-Button was clicked
         * 
         */
        Snapshot snap = owner.getSnapshots().get(owner.getSnapshots().size() - 1);
        Statistic stat = snap.getStatistic();

        this.stepNr = new JLabel("Step " + snap.getStepNr(), JLabel.CENTER);

        this.textOutput = new JTextArea();
        this.textOutput.setEditable(false);

        this.statisticScrollPane = new JScrollPane(getStatisticTable(stat));
        this.textOutput.append(SimulationManager.strings.get("STATTotalStepsFired").concat(" ").concat(Integer.toString(stat.stepsFired)));
        this.textOutput.append(System.getProperty("line.separator").concat(SimulationManager.strings.get("STATTotalTransitionsFired"))
                .concat(" ").concat(Integer.toString(stat.transitionsFired)));

        this.snapshotList = new JList(this.owner.snapshotsListModel);
        this.snapshotList.addListSelectionListener(this.snapshotListSelectionListener);
        this.snapshotListScrollPane = new JScrollPane(this.snapshotList);

        this.statisticFrame.getContentPane().add(this.stepNr, "1,1");
        this.statisticFrame.getContentPane().add(this.statisticScrollPane, "1,3");
        this.statisticFrame.getContentPane().add(this.snapshotListScrollPane, "1,5");
        this.statisticFrame.getContentPane().add(this.textOutput, "1,7");

        this.statisticFrame.pack();
        this.statisticFrame.setVisible(true);
    }

    /**
     * return a table with statistics
     *
     * @return
     */
    public JTable getStatisticTable(Statistic statistic) {
        JTable statisticTable = new JTable();
        statisticTable.setModel(statistic.getTableModel());
        statisticTable.setFillsViewportHeight(true);
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(statistic.getTableModel());
        statisticTable.setRowSorter(rowSorter);
        List sortKeys = new ArrayList();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        rowSorter.setComparator(1, new Comparator<Integer>() {
            @Override
            public int compare(Integer t, Integer t1) {
                return t - t1;
            }
        });
        rowSorter.setSortKeys(sortKeys);
        return statisticTable;
    }
}
