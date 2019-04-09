/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.tinv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import monalisa.data.Pair;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.Tool;
import monalisa.Project;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.TInvariant;
import monalisa.resources.ResourceManager;
import monalisa.results.TInvariants;
import monalisa.util.Components;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TInvariantTool extends AbstractTool implements ActionListener {
    private static final String ACTION_CALCULATE = "CALCULATE";
    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();
    private JPanel panel;
    private JCheckBox calculate;
    private JLabel cti;
    private Project project;
    private static final Logger LOGGER = LogManager.getLogger(TInvariantTool.class);

    @Override
    public void run(PetriNetFacade pnf, ErrorLog log) throws InterruptedException {
        TInvariantCalculator calculator = null;
        try {
            LOGGER.info("Running TInvariantTool");
            calculator = new TInvariantCalculator(pnf, log);
            addResult(new TInvariantsConfiguration(), calculator.tinvariants(log));
            LOGGER.info("Successfully ran TInvariantTool");
            setCTILabelText(calculator.tinvariants(log));
//            if (calculator.postScriptSource(log) != null)
//                addResult(new MauritiusMapConfiguration(),
//                calculator.postScriptSource(log));
        } catch (TInvariantCalculationFailedException e) {
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

            cti = new JLabel();
            setCTILabelText();

            panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(calculate)
                .addComponent(cti));

            layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(calculate)
                .addComponent(cti));
        }

        return panel;
    }

    private void setCTILabelText() {
        int status = isCTI();
        if(status == 1) {
            LOGGER.info("Petri net is CTI");
            cti.setText(strings.get("CTI"));
            cti.setForeground(new java.awt.Color(35, 132, 71));
        }
        else if(status == 0) {
            LOGGER.info("Petri net is not CTI");
            cti.setText(strings.get("NotCTI"));
            cti.setForeground(new java.awt.Color(215, 69, 19));
        }
        else if(status == -1) {
            cti.setText("");
        }
    }

    private void setCTILabelText(TInvariants tinv) {
        int status = isCTI(tinv);
        if(status == 1) {
            LOGGER.info("Petri net is CTI");
            cti.setText(strings.get("CTI"));
            cti.setForeground(new java.awt.Color(35, 132, 71));
        }
        else if(status == 0) {
            LOGGER.info("Petri net is not CTI");
            cti.setText(strings.get("NotCTI"));
            cti.setForeground(new java.awt.Color(215, 69, 19));
        }
        else if(status == -1) {
            cti.setText("");
        }
    }

    private int isCTI() {
        LOGGER.info("Checking whether Petri net is CTI");
        TInvariants tinv = project.getResult(TInvariantTool.class, new TInvariantsConfiguration());
        if(tinv == null) {
            LOGGER.warn("T-Invariants could not be found");
            return -1;
        }
        else {
            int nbrOfTransitions = project.getPetriNet().transitions().size();
            int i = 0;
            List<Integer> counterList = new ArrayList<>();

            try {
                for(TInvariant t: tinv) {
                    i = 0;
                    for(int j : t.asVector()) {
                        if(j > 0 && !counterList.contains(i))
                            counterList.add(i);
                        i++;
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error("Caught exception while checking whether Petri net is CTI", e);
                return -1;
            }

            if(counterList.size() == nbrOfTransitions)
                return 1;
            else
               return 0;
        }
    }

    private int isCTI(TInvariants tinv) {
        LOGGER.info("Checking whether Petri net is CTI");
        if(tinv == null) {
            LOGGER.warn("T-Invariants could not be found");
            return -1;
        }
        else {
            int nbrOfTransitions = project.getPetriNet().transitions().size();
            int i = 0;
            List<Integer> counterList = new ArrayList<>();

            for(TInvariant t: tinv) {
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
