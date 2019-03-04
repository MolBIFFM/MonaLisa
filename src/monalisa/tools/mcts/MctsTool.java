/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.mcts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import monalisa.data.Pair;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.Transition;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.Mcts;
import monalisa.results.MctsConfiguration;
import monalisa.results.TInvariants;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.Tool;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.util.Components;
import monalisa.Project;
import monalisa.data.pn.PetriNetFacade;

public final class MctsTool extends AbstractTool implements ActionListener {
    private static final String ACTION_CALCULATE = "CALCULATE";
    private JPanel panel;
    private JCheckBox calculateCheckbox;
    private JComboBox combobox;
    private JCheckBox includeTrivialTinvCheckbox;
    private Project project;
    
    @Override
    public void run(PetriNetFacade pnf, ErrorLog log) {
        boolean includeTrivialTInvariants = includeTrivialTinvCheckbox.isSelected();
        boolean supportOriented = combobox.getSelectedIndex() == 0;
        
        computeMCTSets(project, includeTrivialTInvariants, supportOriented);
    }
    
    @Override
    public boolean finishedState(Project project) {
        if (project.hasAllResults(this, 4)) {
            calculateCheckbox.setSelected(false);
            Components.setEnabled(panel, false);
            return true;
        }
        return false;
    }
    
    @Override
    public JPanel getUI(Project project, StringResources strings) {
        this.project = project;
        if (panel == null) {
            combobox = new JComboBox();
            combobox.setModel(new DefaultComboBoxModel(
                new String[] {
                    strings.get("MctsSupportOriented"),
                    strings.get("MctsOccurrenceOriented") }));
            
            includeTrivialTinvCheckbox = new JCheckBox(strings.get("IncludeTrivialTInvariants"));
            includeTrivialTinvCheckbox.setSelected(false);
            
            calculateCheckbox = new JCheckBox(strings.get("Calculate"));
            calculateCheckbox.setActionCommand(ACTION_CALCULATE);
            calculateCheckbox.addActionListener(this);
            
            
            panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            
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
        return calculateCheckbox.isSelected();
    }
    
    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub
        
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
        if (isActive())
            return Arrays.asList(new Pair<Class<? extends Tool>, Configuration>(
                TInvariantTool.class, new TInvariantsConfiguration()));
        else
            return Collections.emptyList();
    }

    private static List<Transition> sortedTransitions(PetriNetFacade petriNet) {
        ArrayList<Transition> result = new ArrayList<>(petriNet.transitions());
        Collections.sort(result);
        return result;
    }
    
    private void computeMCTSets(Project project, boolean includeTrivialInvariants, boolean supportOriented) {
        TInvariants allTinvariants = project.getResult(
            TInvariantTool.class, new TInvariantsConfiguration());
        List<TInvariant> tinvariants = new ArrayList<>(
            includeTrivialInvariants ? allTinvariants :
                allTinvariants.nonTrivialTInvariants());
        List<Transition> transitions = sortedTransitions(project.getPNFacade());
        
        int[][] matrix = InvariantStatistics.calculateMatrixTInvariant2TransitionOccurrence(
            tinvariants, transitions);
        List<TInvariant> mcts = supportOriented ?
            InvariantStatistics.getSupportMCTset(matrix, transitions, tinvariants, project.getPNFacade()) :
            InvariantStatistics.getStrongMCTset(matrix, transitions, tinvariants, project.getPNFacade());
        if(mcts != null) {
            addResult(
                new MctsConfiguration(!supportOriented, includeTrivialInvariants),
                new Mcts(mcts));
        }
    }
    
    /**
     * For Manatees
     * @param includeTrivialInvariants
     * @param supportOriented
     * @return 
     */
    public List<TInvariant> computeMCTSets(TInvariants allTinvariants, PetriNetFacade pnf, boolean includeTrivialInvariants) {
        List<TInvariant> tinvariants = new ArrayList<>(
            includeTrivialInvariants ? allTinvariants :
                allTinvariants.nonTrivialTInvariants());
        List<Transition> transitions = sortedTransitions(pnf);
        
        int[][] matrix = InvariantStatistics.calculateMatrixTInvariant2TransitionOccurrence(tinvariants, transitions);
        List<TInvariant> mcts = InvariantStatistics.getSupportMCTset(matrix, transitions, tinvariants, pnf);
        if(mcts != null) {
            return mcts;
        }
        
        return new ArrayList<>();
    }
}
