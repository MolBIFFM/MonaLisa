/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.cluster;

import de.molbi.mjcl.clustering.distancemeasure.DistanceFunction;
import de.molbi.mjcl.clustering.ds.Cluster;
import de.molbi.mjcl.clustering.ds.ClusterTree;
import de.molbi.mjcl.clustering.ds.Leaf;
import de.molbi.mjcl.clustering.ds.Properties;
import de.molbi.mjcl.clustering.hcl.AverageLinkageSettings;
import de.molbi.mjcl.clustering.hcl.SingleLinkageSettings;
import de.molbi.mjcl.clustering.hcl.CompleteLinkageSettings;
import de.molbi.mjcl.clustering.hcl.HierarchicSettings;
import de.molbi.mjcl.clustering.hcl.HierarchicalClustering;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import monalisa.data.Pair;
import monalisa.data.pn.TInvariant;
import monalisa.gui.components.ComboBoxItem;
import monalisa.resources.StringResources;
import monalisa.results.ClusterConfiguration;
import monalisa.results.Clustering;
import monalisa.results.Configuration;
import monalisa.results.TInvariants;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.Tool;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.Project;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Transition;
import monalisa.tools.cluster.distances.Distances;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class ClusterTool extends AbstractTool implements ActionListener {
    private static final String ACTION_CALCULATE = "CALCULATE";

    private JPanel panel;

    private JLabel distanceFctLabel;
    private JComboBox distanceFctCombobox;
    private JLabel clusterFctLabel;
    private JComboBox clusterFctCombobox;
    private JLabel thresholdLabel;
    private JCheckBox includeTrivialTinvCheckbox;
    private JCheckBox calculateButton;
    private SpinnerModel model;

    private Project project;
    private static final Logger LOGGER = LogManager.getLogger(ClusterTool.class);

    @Override
    public void run(PetriNetFacade pnf, ErrorLog log) {
        LOGGER.info("Running ClusterTool");
        float percent = ((SpinnerNumberModel) model).getNumber().floatValue() * 100;

        Class<? extends DistanceFunction> distanceFunction = getActiveDistanceFunction();
        String clusterAlgorithm = getActiveClusterFunction();
        boolean includeTrivialTInvariants = includeTrivialTinvCheckbox.isSelected();

        // Building the input for the cluster library
        TInvariants tinvs = project.getResult(TInvariantTool.class, new TInvariantsConfiguration());
        int nbrOfTransitions = pnf.transitions().size();
        int nbrOfTInvs = 0;
        // Determine the number of TInvariants
        for(TInvariant tinv : tinvs) {
            if((tinv.isTrivial() && includeTrivialTInvariants) || !tinv.isTrivial()) {
                nbrOfTInvs++;
            }
        }
        double[][] data = new double[nbrOfTInvs][nbrOfTransitions];
        int[] iDs = new int[nbrOfTInvs];
        double[] tmp;
        int j,i = 0;
        // Fill the array
        for(TInvariant tinv : tinvs) {
            iDs[i] = tinv.id();
            if((tinv.isTrivial() && includeTrivialTInvariants) || !tinv.isTrivial()) {
                tmp = new double[nbrOfTransitions];
                j = 0;
                for(Transition t : pnf.transitions()) {
                    tmp[j] = tinv.factor(t);
                    j++;
                }
                data[i] = tmp;
                i++;
            }
        }

        // Create a Setting for the Cluster Library
        DistanceFunction distance = null;
        try {
            distance = distanceFunction.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            LOGGER.error("Caught exception while trying to initialize new distance function: ", ex);
        }

        // Create a Setting for the Cluster Library
        HierarchicSettings hs = null;
        // Which Algorithm should be used?
        if(clusterAlgorithm.equalsIgnoreCase(ClusterFunctions.UPGMA)) {
            hs = new AverageLinkageSettings(distance, false, true, null, null);
        } else if(clusterAlgorithm.equalsIgnoreCase(ClusterFunctions.WPGMA)) {
            hs = new AverageLinkageSettings(distance, true, true, null, null);
        } else if(clusterAlgorithm.equalsIgnoreCase(ClusterFunctions.SingleLinkage)) {
            hs = new SingleLinkageSettings(distance, true, null, null);
        } else if(clusterAlgorithm.equalsIgnoreCase(ClusterFunctions.CompleteLinkage)) {
            hs = new CompleteLinkageSettings(distance, true, null, null);
        }

        // Start the clustering
        HierarchicalClustering hc = new HierarchicalClustering(data, iDs, hs);
        ClusterTree<ClusterTreeNodeProperties, Properties> tree = hc.execute();

        for(Cluster c : tree.getCluster()) {
            if(c instanceof Leaf) {
                tree.addNodeProperty(c, new ClusterTreeNodeProperties(tinvs.getTInvariant(c.getID())));
            } else {
                TInvariant[] t = new TInvariant[c.getLeavesCount()];
                i = 0;
                for(Leaf l : c.getLeaves()) {
                    t[i] = tinvs.getTInvariant(l.getID());
                    i++;
                }
                tree.addNodeProperty(c, new ClusterTreeNodeProperties(t));
            }
        }
        LOGGER.info("Successfully ran ClusterTool, adding result");
        // Save the result
        addResult(new ClusterConfiguration(distanceFunction.getSimpleName(),
                                          clusterAlgorithm,
                                          percent,
                                          includeTrivialTInvariants),
                                          new Clustering(tree));
        LOGGER.info("Successfully added result");
    }


    @Override
    public boolean finishedState(Project project) {
        return false;
    }

    @Override
    public JPanel getUI(Project project, StringResources strings) {
        this.project = project;
        if (panel == null) {
            distanceFctLabel = new JLabel(strings.get("DistanceFunction"));
            distanceFctCombobox = new JComboBox();

            ComboBoxItem[] distItems = new ComboBoxItem[3];
            int itemIndex = 0;
            for(String df : Distances.distances.keySet()) {
                distItems[itemIndex++] = new ComboBoxItem(Distances.distances.get(df), df);
            }

            distanceFctCombobox.setModel(new DefaultComboBoxModel(distItems));

            clusterFctLabel = new JLabel(strings.get("ClusteringFunction"));
            clusterFctCombobox = new JComboBox();

            ComboBoxItem[] clusterItems = new ComboBoxItem[4];
            itemIndex = 0;
            for (String cf : new String[] {
                   ClusterFunctions.UPGMA,
                   ClusterFunctions.WPGMA,
                   ClusterFunctions.SingleLinkage,
                   ClusterFunctions.CompleteLinkage})
                clusterItems[itemIndex++] = new ComboBoxItem(cf, ClusterFunctions.getName(cf));

            clusterFctCombobox.setModel(new DefaultComboBoxModel(clusterItems));

            // (similarity of t-invariants grouped in a cluster [%])
            thresholdLabel = new JLabel(strings.get("Threshold"));

            model = new SpinnerNumberModel(1,     //initial value
                                           0,     //min
                                           1,     //max
                                           0.05); // step
            JSpinner spinner = new JSpinner(model);
            spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.##%"));

            includeTrivialTinvCheckbox = new JCheckBox(strings.get("IncludeTrivialTInvariants"));
            includeTrivialTinvCheckbox.setSelected(false);

            calculateButton = new JCheckBox(strings.get("Calculate"));
            calculateButton.setActionCommand(ACTION_CALCULATE);
            calculateButton.addActionListener(this);

            panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);

            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(distanceFctLabel)
                .addComponent(distanceFctCombobox)
                .addComponent(clusterFctLabel)
                .addComponent(clusterFctCombobox)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(thresholdLabel)
                    .addComponent(spinner))
                .addComponent(includeTrivialTinvCheckbox)
                .addComponent(calculateButton));

            layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(distanceFctLabel)
                .addComponent(distanceFctCombobox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE)
                .addComponent(clusterFctLabel)
                .addComponent(clusterFctCombobox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(thresholdLabel)
                    .addComponent(spinner,  GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE))
                .addComponent(includeTrivialTinvCheckbox)
                .addComponent(calculateButton));
            layout.linkSize(SwingConstants.VERTICAL, thresholdLabel, spinner);
        }
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String action = e.getActionCommand();
        switch (action) {
            case ACTION_CALCULATE:
                fireActivityChanged(isActive());
                break;
        }
    }

    @Override
    public boolean isActive() {
        return calculateButton.isSelected();
    }

    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setActive(boolean active) {
        if (active != isActive()) {
            calculateButton.setSelected(active);
            fireActivityChanged(active);
        }
    }

    @Override
    public void setActive(Configuration... configs) {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Pair<Class<? extends Tool>, Configuration>> getRequirements() {
        if (isActive())
            return Arrays.asList(
                new Pair<Class<? extends Tool>, Configuration>(
                    TInvariantTool.class, new TInvariantsConfiguration())
                );
        else
            return Collections.emptyList();
    }

    private Class<? extends DistanceFunction> getActiveDistanceFunction() {
        ComboBoxItem selected = (ComboBoxItem) distanceFctCombobox.getSelectedItem();
        return (Class<? extends DistanceFunction>) selected.getItem();
    }

    private String getActiveClusterFunction() {
        ComboBoxItem selected = (ComboBoxItem) clusterFctCombobox.getSelectedItem();
        return (String) selected.getItem();
    }

}
