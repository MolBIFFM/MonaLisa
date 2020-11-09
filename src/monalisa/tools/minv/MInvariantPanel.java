/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.tools.minv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import monalisa.Project;
import monalisa.ToolManager;
import monalisa.data.Pair;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.MInvariantsConfiguration;
import monalisa.tools.AbstractToolPanel;
import monalisa.tools.Tool;
import monalisa.util.Components;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Panel that holds the GUI necessary to control the MInvariantTool.
 * @author Marcel Gehrmann
 */
public class MInvariantPanel extends AbstractToolPanel {

    public static final Class<MInvariantTool> TOOLTYPE = MInvariantTool.class;
    private static final String ACTION_CALCULATE = "CALCULATE";
    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();
    private final JCheckBox calculate;
    private final Project project;
    private final MInvariantTool tool;
    private static final Logger LOGGER = LogManager.getLogger(MInvariantPanel.class);

    public MInvariantPanel(Project project) {
        this.project = project;
        this.tool = (MInvariantTool) project.getToolManager().getTool(TOOLTYPE);
        calculate = new JCheckBox(strings.get("Calculate"));
        calculate.setActionCommand(ACTION_CALCULATE);
        calculate.addActionListener(new ActionListener() {

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
                .addComponent(calculate));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(calculate));
    }

    /**
     *
     * @return new MInvariantsConfiguration
     */
    @Override
    public MInvariantsConfiguration getConfig() {
        return new MInvariantsConfiguration();
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
        return calculate.isSelected();
    }

    @Override
    public void setActive(boolean active) {
        if (active != isActive()) {
            calculate.setSelected(active);
            fireActivityChanged(active);
        }
    }

    @Override
    public void setActive(Configuration... configs) {
        if (!isActive()) {
            setActive(true);
        }
    }

    @Override
    public List<Pair<Class<? extends Tool>, Configuration>> getRequirements() {
        return Collections.emptyList();
    }

    @Override
    public boolean finishedState(ToolManager toolMan) {
        if (toolMan.hasAllResults(tool, 2)) {
            calculate.setSelected(false);
            Components.setEnabled(this, false);
        }
        return false;
    }
}
