/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.knockout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import monalisa.Project;
import monalisa.MonaLisa;
import monalisa.data.Pair;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.Knockout;
import monalisa.results.KnockoutConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.ProgressEvent;
import monalisa.tools.ProgressListener;
import monalisa.tools.Tool;
import monalisa.tools.tinv.TInvariantCalculationFailedException;

public class KnockoutTool extends AbstractTool implements ActionListener, ProgressListener {
    
    private static final String ACTION_CALCULATE = "CALCULATE";
    private static final String SELECT = "SELECT";
    private JPanel panel;
    private JCheckBox calculate;
    private JPanel koPanel;
    private JRadioButton singleKoPlace;
    private JRadioButton singleKoTransition;
    private JRadioButton doubleKoPlace;
    private JRadioButton doubleKoTransition;
    private JRadioButton selectKoPlace;
    private JRadioButton selectKoTransition;
    private Project project;
    
    @Override
    protected void run(PetriNetFacade pnf, ErrorLog log)
            throws InterruptedException {
        KnockoutAlgorithm algorithm = null;
        String userDefinedName = null;
        KnockoutDialog dialog = null;

        if (singleKoPlace.isSelected()) {
            algorithm = new SinglePlaceKnockout(pnf);
        } else if (singleKoTransition.isSelected()) {
            algorithm = new SingleTransitionKnockout(pnf);
        } else if (doubleKoPlace.isSelected()) {
            algorithm = new DoublePlaceKnockout(pnf);
        } else if (doubleKoTransition.isSelected()) {
            algorithm = new DoubleTransitionKnockout(pnf);
        } else if (selectKoPlace.isSelected()) {
            dialog = new KnockoutDialog(MonaLisa.appMainWindow(), pnf.places());
            algorithm = new MultiPlaceKnockout(pnf, dialog.<Place>knockouts());
        } else if (selectKoTransition.isSelected()) {
            dialog = new KnockoutDialog(MonaLisa.appMainWindow(), pnf.transitions());
            algorithm = new MultiTransitionKnockout(pnf, dialog.<Transition>knockouts());
        }
        
        if (algorithm == null)
            throw new RuntimeException("Unreachable code, this should never happen.");
        try {
            algorithm.run(this, log);
        } catch (TInvariantCalculationFailedException ex) {
            Logger.getLogger(KnockoutTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        userDefinedName = algorithm.getClass().getSimpleName();
        if(dialog != null) {
            userDefinedName += "Selection";
        }
        KnockoutConfiguration config = new KnockoutConfiguration(algorithm.getClass(),userDefinedName);
        Map<List<String>, List<String>> results = algorithm.getResults();
        addResult(config, new Knockout(results));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String action = e.getActionCommand();
        switch (action) {
            case ACTION_CALCULATE:
                fireActivityChanged(isActive());
                break;
            case SELECT:
                calculate.setSelected(true);
                break;
        }
    }

    @Override
    public boolean finishedState(Project project) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public JPanel getUI(Project project, StringResources strings) {
        this.project = project;
        if (panel == null) {
            singleKoPlace = new JRadioButton(strings.get("SinglePlaceKnockout"));
            
            singleKoTransition = new JRadioButton(strings.get("SingleTransitionKnockout"));
            singleKoTransition.setActionCommand(SELECT);
            singleKoTransition.addActionListener(this);
            doubleKoPlace = new JRadioButton(strings.get("DoublePlaceKnockout"));
            doubleKoPlace.setActionCommand(SELECT);
            doubleKoPlace.addActionListener(this);
            doubleKoTransition = new JRadioButton(strings.get("DoubleTransitionKnockout"));
            doubleKoTransition.setActionCommand(SELECT);
            doubleKoTransition.addActionListener(this);
            selectKoPlace = new JRadioButton(strings.get("SelectPlaceKnockout"));
            selectKoPlace.setActionCommand(SELECT);
            selectKoPlace.addActionListener(this);
            selectKoTransition = new JRadioButton(strings.get("SelectTransitionKnockout"));
            selectKoTransition.setActionCommand(SELECT);
            selectKoTransition.addActionListener(this);

            ButtonGroup group = new ButtonGroup();
            group.add(singleKoPlace);
            group.add(singleKoTransition);
            group.add(doubleKoPlace);
            group.add(doubleKoTransition);
            group.add(selectKoPlace);
            group.add(selectKoTransition);
            
            calculate = new JCheckBox(strings.get("Calculate"));
            calculate.setActionCommand(ACTION_CALCULATE);
            calculate.addActionListener(this);
            
            koPanel = new JPanel();
            koPanel.setBorder(BorderFactory.createTitledBorder(
                null, "",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));
            GroupLayout koLayout = new GroupLayout(koPanel);
            koPanel.setLayout(koLayout);
            
            koLayout.setHorizontalGroup(koLayout.createParallelGroup()
                .addComponent(singleKoPlace)
                .addComponent(singleKoTransition)
                .addComponent(doubleKoPlace)
                .addComponent(doubleKoTransition)
                .addComponent(selectKoPlace)
                .addComponent(selectKoTransition));
            
            koLayout.setVerticalGroup(koLayout.createSequentialGroup()
                .addComponent(singleKoPlace)
                .addComponent(singleKoTransition)
                .addComponent(doubleKoPlace)
                .addComponent(doubleKoTransition)
                .addComponent(selectKoPlace)
                .addComponent(selectKoTransition));
            
            panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            
            layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(koPanel)
                .addComponent(calculate));
            
            layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(koPanel)
                .addComponent(calculate));
        }
        return panel;
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
        // TODO Auto-generated method stub
    }

    @Override
    public List<Pair<Class<? extends Tool>, Configuration>> getRequirements() {
        return Collections.emptyList();
    }

    @Override
    public void progressUpdated(ProgressEvent e) {
        // KnockoutAlgorithm progress notification.
        fireProgressUpdated(e.getPercent());
    }
}
