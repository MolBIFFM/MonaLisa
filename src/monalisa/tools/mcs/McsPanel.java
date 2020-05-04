/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.tools.mcs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import monalisa.Project;
import monalisa.ToolManager;
import monalisa.data.Pair;
import monalisa.data.pn.Transition;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.McsConfiguration;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractToolPanel;
import monalisa.tools.Tool;
import monalisa.tools.tinv.TInvariantTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Panel that holds the GUI necessary to control the McsTool.
 * @author Marcel Gehrmann
 */
public class McsPanel extends AbstractToolPanel {

    public static final Class<McsTool> TOOLTYPE = McsTool.class;
    private static final String ACTION_CALCULATE = "CALCULATE";
    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();

    private final JLabel transitionLabel;
    private final JLabel cutSetSizeLabel;
    private final SpinnerNumberModel cutSetSizeModel;
    private final JComboBox<Transition> transitionCb;
    private final JCheckBox calculateButton;
    private final Project project;
    private final McsTool tool;
    private static final Logger LOGGER = LogManager.getLogger(McsPanel.class);

    public McsPanel(Project project) {
        this.project = project;
        this.tool = (McsTool) project.getToolManager().getTool(TOOLTYPE);
        transitionCb = new JComboBox<>();
        List<Transition> transitionList = new ArrayList<>(project.getPetriNet().transitions());
        for (Transition t : transitionList) {
            transitionCb.addItem(t);
        }
        transitionLabel = new JLabel(strings.get("ObjectiveTransition"));

        cutSetSizeLabel = new JLabel(strings.get("MaxCutSetSize"));
        cutSetSizeModel = new SpinnerNumberModel(5, 2, 20, 1);
        JSpinner spinnerSetSize = new JSpinner(cutSetSizeModel);
        spinnerSetSize.setEditor(new JSpinner.NumberEditor(spinnerSetSize, "#"));

        calculateButton = new JCheckBox(strings.get("Calculate"));
        calculateButton.setActionCommand(ACTION_CALCULATE);
        calculateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String action = e.getActionCommand();
                switch (action) {
                    case ACTION_CALCULATE:
                        fireActivityChanged(isActive());
                        break;
                }
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(transitionLabel)
                        .addComponent(transitionCb))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(cutSetSizeLabel)
                        .addComponent(spinnerSetSize))
                .addComponent(calculateButton));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createBaselineGroup(false, false)
                        .addComponent(transitionLabel)
                        .addComponent(transitionCb))
                .addGroup(layout.createParallelGroup()
                        .addComponent(cutSetSizeLabel)
                        .addComponent(spinnerSetSize))
                .addComponent(calculateButton));

        layout.linkSize(SwingConstants.VERTICAL, cutSetSizeLabel, spinnerSetSize);
    }

    /**
     *
     * @return new McsConfiguration
     */
    @Override
    public McsConfiguration getConfig() {
        return new McsConfiguration(
                transitionCb.getItemAt(transitionCb.getSelectedIndex()),
                cutSetSizeModel.getNumber().intValue());
    }

    /**
     * @return the associated tool's class
     */
    @Override
    public Class<? extends Tool> getToolType() {
        return TOOLTYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Pair<Class<? extends Tool>, Configuration>> getRequirements() {
        if (isActive()) {
            return Arrays.asList(
                    new Pair<Class<? extends Tool>, Configuration>(TInvariantTool.class, new TInvariantsConfiguration()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isActive() {
        return calculateButton.isSelected();
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
    public boolean finishedState(ToolManager toolMan) {
        return false;
    }
}
