/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.tools.cluster;

import de.molbi.mjcl.clustering.distancemeasure.DistanceFunction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import monalisa.Project;
import monalisa.ToolManager;
import monalisa.data.Pair;
import monalisa.gui.components.ComboBoxItem;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.ClusterConfiguration;
import monalisa.results.Configuration;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractToolPanel;
import monalisa.tools.Tool;
import monalisa.tools.cluster.distances.Distances;
import monalisa.tools.tinv.TInvariantTool;

/**
 * Panel that holds the GUI necessary to control the ClusterTool.
 * @author Marcel Gehrmann
 */
public class ClusterPanel extends AbstractToolPanel {

    private static final String ACTION_CALCULATE = "CALCULATE";
    public static final Class<ClusterTool> TOOLTYPE = ClusterTool.class;
    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();
    private final JLabel distanceFctLabel;
    private final JComboBox distanceFctCombobox;
    private final JLabel clusterFctLabel;
    private final JComboBox clusterFctCombobox;
    private final JLabel thresholdLabel;
    private final JCheckBox includeTrivialTinvCheckbox;
    private final JCheckBox calculateButton;
    private final SpinnerModel model;
    private final Project project;
    private final ClusterTool tool;

    public ClusterPanel(Project project) {
        this.project = project;
        this.tool = (ClusterTool) project.getToolManager().getTool(TOOLTYPE);

        distanceFctLabel = new JLabel(strings.get("DistanceFunction"));
        distanceFctCombobox = new JComboBox();

        ComboBoxItem[] distItems = new ComboBoxItem[3];
        int itemIndex = 0;
        for (String df : Distances.distances.keySet()) {
            distItems[itemIndex++] = new ComboBoxItem(Distances.distances.get(df), df);
        }

        distanceFctCombobox.setModel(new DefaultComboBoxModel(distItems));

        clusterFctLabel = new JLabel(strings.get("ClusteringFunction"));
        clusterFctCombobox = new JComboBox();

        ComboBoxItem[] clusterItems = new ComboBoxItem[4];
        itemIndex = 0;
        for (String cf : new String[]{
            ClusterFunctions.UPGMA,
            ClusterFunctions.WPGMA,
            ClusterFunctions.SingleLinkage,
            ClusterFunctions.CompleteLinkage}) {
            clusterItems[itemIndex++] = new ComboBoxItem(cf, ClusterFunctions.getName(cf));
        }

        clusterFctCombobox.setModel(new DefaultComboBoxModel(clusterItems));

        // (similarity of t-invariants grouped in a cluster [%])
        thresholdLabel = new JLabel(strings.get("Threshold"));

        model = new SpinnerNumberModel(1, //initial value
                0, //min
                1, //max
                0.05); // step
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.##%"));

        includeTrivialTinvCheckbox = new JCheckBox(strings.get("IncludeTrivialTInvariants"));
        includeTrivialTinvCheckbox.setSelected(false);

        calculateButton = new JCheckBox(strings.get("Calculate"));
        calculateButton.setActionCommand("CALCULATE");
        calculateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String action = e.getActionCommand();
                if (action.equals(ACTION_CALCULATE)) {
                    fireActivityChanged(isActive());
                }
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

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
                        .addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                .addComponent(includeTrivialTinvCheckbox)
                .addComponent(calculateButton));
        layout.linkSize(SwingConstants.VERTICAL, thresholdLabel, spinner);
    }

    private String getActiveClusterFunction() {
        ComboBoxItem selected = (ComboBoxItem) clusterFctCombobox.getSelectedItem();
        return (String) selected.getItem();
    }

    private Class<? extends DistanceFunction> getActiveDistanceFunction() {
        ComboBoxItem selected = (ComboBoxItem) distanceFctCombobox.getSelectedItem();
        return (Class<? extends DistanceFunction>) selected.getItem();
    }

    /**
     *
     * @return new ClusterConfiguration
     */
    @Override
    public ClusterConfiguration getConfig() {
        return new ClusterConfiguration(
                getActiveDistanceFunction(),
                getActiveClusterFunction(),
                ((SpinnerNumberModel) model).getNumber().floatValue() * 100,
                includeTrivialTinvCheckbox.isSelected());
    }

    /**
     * @return the associated tool's class
     */
    @Override
    public Class<? extends Tool> getToolType() {
        return TOOLTYPE;
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

    @Override
    public boolean isActive() {
        return calculateButton.isSelected();
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public List<Pair<Class<? extends Tool>, Configuration>> getRequirements() {
        if (isActive()) {
            return Arrays.asList(new Pair<Class<? extends Tool>, Configuration>(TInvariantTool.class, new TInvariantsConfiguration()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean finishedState(ToolManager toolMan) {
        return false;
    }
}
