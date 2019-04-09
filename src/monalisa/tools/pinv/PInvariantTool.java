/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.pinv;

import monalisa.data.pn.PInvariant;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import monalisa.data.Pair;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.Tool;
import monalisa.Project;
import monalisa.data.pn.PetriNetFacade;
import monalisa.resources.ResourceManager;
import monalisa.results.PInvariants;
import monalisa.results.PInvariantsConfiguration;
import monalisa.util.Components;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class PInvariantTool extends AbstractTool implements ActionListener {
    private static final String ACTION_CALCULATE = "CALCULATE";
    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();
    private JPanel panel;
    private JCheckBox calculate;
    private JLabel cpi;
    private Project project;
    private static final Logger LOGGER = LogManager.getLogger(PInvariantTool.class);

    @Override
    public void run(PetriNetFacade pnf, ErrorLog log) throws InterruptedException {
        PInvariantCalculator calculator = null;
        try {
            LOGGER.info("Running PInvariantTool");
            calculator = new PInvariantCalculator(project.getPNFacade(), log);
            addResult(new PInvariantsConfiguration(), calculator.pinvariants(log));
            LOGGER.info("Successfully ran PInvariantTool");
        } catch (PInvariantCalculationFailedException e) {
            // Error already handled in calculator.
        }
    }

    @Override
    public boolean finishedState(Project project) {
        if (project.hasAllResults(this, 2)) {
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

            cpi = new JLabel();
            setCPILabelText();

            panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(calculate)
                .addComponent(cpi));

            layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(calculate)
                .addComponent(cpi));
        }

        return panel;
    }

    private void setCPILabelText() {
        int status = isCPI();
        if(status == 1) {
            LOGGER.info("Petri net is CPI");
            cpi.setText(strings.get("CPI"));
            cpi.setForeground(new java.awt.Color(35, 132, 71));
        }
        else if(status == 0) {
            LOGGER.info("Petri net is not CPI");
            cpi.setText(strings.get("NotCPI"));
            cpi.setForeground(new java.awt.Color(215, 69, 19));
        }
        else if(status == -1) {
            cpi.setText("");
        }
    }

    private void setCPILabelText(PInvariants pinv) {
        int status = isCPI(pinv);
        if(status == 1) {
            LOGGER.info("Petri net is CPI");
            cpi.setText(strings.get("CPI"));
            cpi.setForeground(new java.awt.Color(35, 132, 71));
        }
        else if(status == 0) {
            LOGGER.info("Petri net is not CPI");
            cpi.setText(strings.get("NotCPI"));
            cpi.setForeground(new java.awt.Color(215, 69, 19));
        }
        else if(status == -1) {
            cpi.setText("");
        }
    }

    private int isCPI() {
        LOGGER.info("Checking whether Petri net is CPI");
        PInvariants pinv = project.getResult(PInvariantTool.class, new PInvariantsConfiguration());
        if(pinv == null) {
            LOGGER.warn("P-Invariants could not be found");
            return -1;
        }
        else {
            int nbrOfPlaces = project.getPetriNet().places().size();
            int i = 0;
            List<Integer> counterList = new ArrayList<>();

            try {
                for(PInvariant t: pinv) {
                    i = 0;

                    for(int j : t.asVector()) {
                        if(j > 0 && !counterList.contains(i))
                            counterList.add(i);
                        i++;
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error("Caught exception while checking whether Petri net is CPI", e);
                return -1;
            }

            if(counterList.size() == nbrOfPlaces)
                return 1;
            else
               return 0;
        }
    }

    private int isCPI(PInvariants pinv) {
        LOGGER.info("Checking whether Petri net is CPI");
        if(pinv == null){
            LOGGER.warn("P-Invariants could not be found");
            return -1;
        }
        else {
            int nbrOfTransitions = project.getPetriNet().transitions().size();
            int i = 0;
            List<Integer> counterList = new ArrayList<>();

            for(PInvariant t: pinv) {
                i = 0;
                for(int j : t.asVector()) {
                    if(j > 0 && !counterList.contains(i))
                        counterList.add(i);
                    i++;
                }
            }

            if(counterList.size() == nbrOfTransitions)
                return 1;
            else
               return 0;
        }
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
