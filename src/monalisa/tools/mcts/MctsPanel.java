/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.tools.mcts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import monalisa.Project;
import monalisa.ToolManager;
import monalisa.data.Pair;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.MctsConfiguration;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractToolPanel;
import monalisa.tools.Tool;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.util.Components;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Panel that holds the GUI necessary to control the MctsTool.
 * @author Marcel Gehrmann
 */
public class MctsPanel extends AbstractToolPanel {

    public static final Class<MctsTool> TOOLTYPE = MctsTool.class;
    private static final String ACTION_CALCULATE = "CALCULATE";
    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();

    private final JCheckBox calculateCheckbox;
    private final JComboBox combobox;
    private final JCheckBox includeTrivialTinvCheckbox;
    private final Project project;
    private final MctsTool tool;
    private static final Logger LOGGER = LogManager.getLogger(MctsPanel.class);

    public MctsPanel(Project project) {
        this.project = project;
        this.tool = (MctsTool) project.getToolManager().getTool(TOOLTYPE);

        combobox = new JComboBox();
        combobox.setModel(new DefaultComboBoxModel(
                new String[]{
                    strings.get("MctsSupportOriented"),
                    strings.get("MctsOccurrenceOriented")}));

        includeTrivialTinvCheckbox = new JCheckBox(strings.get("IncludeTrivialTInvariants"));
        includeTrivialTinvCheckbox.setSelected(false);

        calculateCheckbox = new JCheckBox(strings.get("Calculate"));
        calculateCheckbox.setActionCommand(ACTION_CALCULATE);
        calculateCheckbox.addActionListener(new ActionListener() {

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

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(combobox)
                .addComponent(includeTrivialTinvCheckbox)
                .addComponent(calculateCheckbox));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(combobox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                .addComponent(includeTrivialTinvCheckbox)
                .addComponent(calculateCheckbox));
    }

    /**
     *
     * @return new MctsConfiguration
     */
    @Override
    public MctsConfiguration getConfig() {
        return new MctsConfiguration(
                combobox.getSelectedIndex() != 0,
                includeTrivialTinvCheckbox.isSelected());
    }

    /**
     *
     * @return the associated tool's class
     */
    @Override
    public Class<? extends Tool> getToolType() {
        return TOOLTYPE;
    }

    @Override
    public boolean isActive() {
        return calculateCheckbox.isSelected();
    }

    @Override
    public void setActive(boolean active) {
        if (active != isActive()) {
            calculateCheckbox.setSelected(active);
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
        if (isActive()) {
            return Arrays.asList(new Pair<Class<? extends Tool>, Configuration>(
                    TInvariantTool.class, new TInvariantsConfiguration()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean finishedState(ToolManager toolMan) {
        if (toolMan.hasAllResults(tool, 4)) {
            calculateCheckbox.setSelected(false);
            Components.setEnabled(this, false);
            return true;
        }
        return false;
    }
}
