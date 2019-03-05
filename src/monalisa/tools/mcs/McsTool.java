/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.mcs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import monalisa.Project;
import monalisa.data.Pair;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Transition;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.McsConfiguration;
import monalisa.results.Mcs;
import monalisa.results.TInvariants;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.Tool;
import monalisa.tools.tinv.TInvariantTool;

public final class McsTool extends AbstractTool implements ActionListener {
    private static final String ACTION_CALCULATE = "CALCULATE";

    private JPanel panel;
    private JLabel transitionLabel;
    private JLabel cutSetSizeLabel;
    private SpinnerNumberModel cutSetSizeModel;
    private JComboBox<Transition> transitionCb;
    private JCheckBox calculateButton;
    private Project project;

    @Override
    protected void run(PetriNetFacade pnf, ErrorLog log) throws InterruptedException {
        final TInvariants tinv = project.getResult(TInvariantTool.class, new TInvariantsConfiguration());
        final Transition objective = transitionCb.getItemAt(transitionCb.getSelectedIndex());
        final int maxCutSetSize = cutSetSizeModel.getNumber().intValue();

        McsAlgorithm algorithm = new McsAlgorithm(pnf, tinv, objective);
        List<Set<Transition>> mcs = algorithm.findMcs(maxCutSetSize);
        
        addResult(new McsConfiguration(objective, maxCutSetSize), new Mcs(mcs));
    }

    @Override
    public boolean finishedState(Project project) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Pair<Class<? extends Tool>, Configuration>> getRequirements() {
        if (isActive())
            return Arrays.asList(
                new Pair<Class<? extends Tool>, Configuration>(TInvariantTool.class, new TInvariantsConfiguration()));
        else
            return Collections.emptyList();
    }

    @Override
    public JPanel getUI(final Project project, StringResources strings) {
        this.project = project;
        if (panel == null) {            
            transitionCb = new JComboBox<>();
            
            List<Transition> transitionList = new ArrayList<>(project.getPetriNet().transitions());
            for(Transition t : transitionList) {
                transitionCb.addItem(t);
            }
            transitionLabel = new JLabel(strings.get("ObjectiveTransition"));                        
            
            cutSetSizeLabel = new JLabel(strings.get("MaxCutSetSize"));
            cutSetSizeModel = new SpinnerNumberModel(5, 2, 20, 1);
            JSpinner spinnerSetSize = new JSpinner(cutSetSizeModel);
            spinnerSetSize.setEditor(new JSpinner.NumberEditor(spinnerSetSize, "#"));

            calculateButton = new JCheckBox(strings.get("Calculate"));
            calculateButton.setActionCommand(ACTION_CALCULATE);
            calculateButton.addActionListener(this);

            panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);

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

}