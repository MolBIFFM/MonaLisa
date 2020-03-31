/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.minv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import monalisa.data.Pair;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.MInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.Tool;
import monalisa.Project;
import monalisa.ToolManager;
import monalisa.data.pn.PetriNetFacade;
import monalisa.resources.ResourceManager;
import monalisa.util.Components;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 *
 * @author daniel
 */
public final class MInvariantTool extends AbstractTool implements ActionListener {
    private static final String ACTION_CALCULATE = "CALCULATE";
    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();
    private JPanel panel;
    private JCheckBox calculate;
    private Project project;
    private static final Logger LOGGER = LogManager.getLogger(MInvariantTool.class);
    
    @Override
    public void run(PetriNetFacade pnf, ErrorLog log) throws InterruptedException {
        MInvariantCalculator calculator = null;
        try {
            LOGGER.info("Running MInvariantTool");
            calculator = new MInvariantCalculator(project.getPNFacade(), log);
            addResult(new MInvariantsConfiguration(), calculator.minvariants(log));
            LOGGER.info("Successfully ran MInvariantTool");
        } catch (MInvariantCalculationFailedException e) {
            // Error already handled in calculator.
        }
    }
    
    @Override
    public boolean finishedState(ToolManager toolMan) {
        if (toolMan.hasAllResults(this, 2)) {
            calculate.setSelected(false);
            Components.setEnabled(panel, false);
        }
        return false;
    }
    @Override
    public JPanel getUI(Project project, StringResources strings) {
        this.project = project;
        if(panel == null) {
            calculate = new JCheckBox(strings.get("Calculate"));
            calculate.setActionCommand(ACTION_CALCULATE);
            calculate.addActionListener(this);


            panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(calculate));

            layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(calculate));
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
        return calculate.isSelected();
    }

    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub

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
        if (!isActive())
            setActive(true);
    }

    @Override
    public List<Pair<Class<? extends Tool>, Configuration>> getRequirements() {
        return Collections.emptyList();
    }
}
