/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.tools.knockout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import monalisa.MonaLisa;
import monalisa.Project;
import monalisa.ToolManager;
import monalisa.data.Pair;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.KnockoutConfiguration;
import monalisa.tools.AbstractToolPanel;
import monalisa.tools.Tool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Panel that holds the GUI necessary to control the KnockoutTool.
 * @author Marcel Gehrmann
 */
public class KnockoutPanel extends AbstractToolPanel {

    public static final Class<KnockoutTool> TOOLTYPE = KnockoutTool.class;
    private static final String ACTION_CALCULATE = "CALCULATE";
    private static final String SELECT = "SELECT";
    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();

    private final JCheckBox calculate;
    private final JPanel koPanel;
    private final JRadioButton singleKoPlace;
    private final JRadioButton singleKoTransition;
    private final JRadioButton doubleKoPlace;
    private final JRadioButton doubleKoTransition;
    private final JRadioButton selectKoPlace;
    private final JRadioButton selectKoTransition;
    private final Project project;
    private final KnockoutTool tool;
    private static final Logger LOGGER = LogManager.getLogger(KnockoutPanel.class);

    public KnockoutPanel(Project project) {
        this.project = project;
        this.tool = (KnockoutTool) project.getToolManager().getTool(KnockoutTool.class);

        ActionListener al = new ActionListener() {
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
        };

        singleKoPlace = new JRadioButton(strings.get("SinglePlaceKnockout"));
        singleKoPlace.setActionCommand(SELECT);
        singleKoTransition = new JRadioButton(strings.get("SingleTransitionKnockout"));
        singleKoTransition.setActionCommand(SELECT);
        singleKoTransition.addActionListener(al);
        doubleKoPlace = new JRadioButton(strings.get("DoublePlaceKnockout"));
        doubleKoPlace.setActionCommand(SELECT);
        doubleKoPlace.addActionListener(al);
        doubleKoTransition = new JRadioButton(strings.get("DoubleTransitionKnockout"));
        doubleKoTransition.setActionCommand(SELECT);
        doubleKoTransition.addActionListener(al);
        selectKoPlace = new JRadioButton(strings.get("SelectPlaceKnockout"));
        selectKoPlace.setActionCommand(SELECT);
        selectKoPlace.addActionListener(al);
        selectKoTransition = new JRadioButton(strings.get("SelectTransitionKnockout"));
        selectKoTransition.setActionCommand(SELECT);
        selectKoTransition.addActionListener(al);

        ButtonGroup group = new ButtonGroup();
        group.add(singleKoPlace);
        group.add(singleKoTransition);
        group.add(doubleKoPlace);
        group.add(doubleKoTransition);
        group.add(selectKoPlace);
        group.add(selectKoTransition);

        calculate = new JCheckBox(strings.get("Calculate"));
        calculate.setActionCommand(ACTION_CALCULATE);
        calculate.addActionListener(al);

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

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(koPanel)
                .addComponent(calculate));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(koPanel)
                .addComponent(calculate));
    }

    private KnockoutAlgorithm getAlgorithm() {
        KnockoutAlgorithm algorithm = null;
        PetriNetFacade pnf = project.getPNFacade();
        if (singleKoPlace.isSelected()) {
            algorithm = new SinglePlaceKnockout(pnf);
        } else if (singleKoTransition.isSelected()) {
            algorithm = new SingleTransitionKnockout(pnf);
        } else if (doubleKoPlace.isSelected()) {
            algorithm = new DoublePlaceKnockout(pnf);
        } else if (doubleKoTransition.isSelected()) {
            algorithm = new DoubleTransitionKnockout(pnf);
        } else if (selectKoPlace.isSelected()) {
            KnockoutDialog dialog = new KnockoutDialog(MonaLisa.appMainWindow(), pnf.places());
            algorithm = new MultiPlaceKnockout(pnf, dialog.<Place>knockouts());
        } else if (selectKoTransition.isSelected()) {
            KnockoutDialog dialog = new KnockoutDialog(MonaLisa.appMainWindow(), pnf.transitions());
            algorithm = new MultiTransitionKnockout(pnf, dialog.<Transition>knockouts());
        }
        return algorithm;
    }

    /**
     *
     * @return new KnockoutConfiguration
     */
    @Override
    public KnockoutConfiguration getConfig() {
        KnockoutAlgorithm algorithm = getAlgorithm();
        if (algorithm.getClass().equals(MultiPlaceKnockout.class) || algorithm.getClass().equals(MultiTransitionKnockout.class)) {
            return new KnockoutConfiguration(algorithm, algorithm.getKnockoutEntities(), algorithm.getClass().getSimpleName());
        } else {
            return new KnockoutConfiguration(algorithm, algorithm.getClass().getSimpleName());
        }
    }

    /**
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
        // TODO Auto-generated method stub
    }

    @Override
    public List<Pair<Class<? extends Tool>, Configuration>> getRequirements() {
        return Collections.emptyList();
    }

    @Override
    public boolean finishedState(ToolManager toolMan) {
        return false;
    }
}
