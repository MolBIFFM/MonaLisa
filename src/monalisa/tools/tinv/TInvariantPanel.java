/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.tools.tinv;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import monalisa.Project;
import monalisa.ToolManager;
import monalisa.data.Pair;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.TInvariants;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractToolPanel;
import monalisa.tools.Tool;
import monalisa.util.Components;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Panel that holds the GUI necessary to control the TInvariantTool.
 * @author Marcel Gehrmann
 */
public class TInvariantPanel extends AbstractToolPanel {

    public static final Class<TInvariantTool> TOOLTYPE = TInvariantTool.class;
    private static final String ACTION_CALCULATE = "CALCULATE";

    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();
    private final JCheckBox calculate;
    private final TInvariantTool tool;
    public JLabel cti;
    private final Project project;
    private static final Logger LOGGER = LogManager.getLogger(TInvariantPanel.class);

    public TInvariantPanel(Project project) {
        this.project = project;
        this.tool = (TInvariantTool) project.getToolManager().getTool(TOOLTYPE);
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
        cti = new JLabel();
        setCTILabelText();

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(calculate)
                .addComponent(cti));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(calculate)
                .addComponent(cti));
    }

    private void setCTILabelText() {
        int status = tool.isCTI(project);
        switch (status) {
            case 1:
                LOGGER.info("Petri net is CTI");
                cti.setText(strings.get("CTI"));
                cti.setForeground(new Color(35, 132, 71));
                break;
            case 0:
                LOGGER.info("Petri net is not CTI");
                cti.setText(strings.get("NotCTI"));
                cti.setForeground(new Color(215, 69, 19));
                break;
            case -1:
                cti.setText("");
                break;
            default:
                break;
        }
    }

    void setCTILabelText(TInvariants tinv) {
        int status = tool.isCTI(tinv, project);
        switch (status) {
            case 1:
                LOGGER.info("Petri net is CTI");
                cti.setText(strings.get("CTI"));
                cti.setForeground(new Color(35, 132, 71));
                break;
            case 0:
                LOGGER.info("Petri net is not CTI");
                cti.setText(strings.get("NotCTI"));
                cti.setForeground(new Color(215, 69, 19));
                break;
            case -1:
                cti.setText("");
                break;
            default:
                break;
        }
    }

    /**
     *
     * @return new TInvariantsConfiguration
     */
    @Override
    public TInvariantsConfiguration getConfig() {
        return new TInvariantsConfiguration();
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
    public boolean finishedState(ToolManager toolMan) {
        if (toolMan.hasAllResults(tool, 2)) {
            calculate.setSelected(false);
            Components.setEnabled(this, false);
        }
        return false;
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
}
