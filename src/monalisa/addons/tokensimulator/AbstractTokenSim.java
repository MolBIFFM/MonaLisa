/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.HighQualityRandom;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * The implementation of this class defines the rules and performs the
 * simulation.
 *
 * @author Pavel Balazki
 */
public abstract class AbstractTokenSim implements ChangeListener {

    //BEGIN VARIABLES DECLARATION
    private VisualizationViewer<NetViewerNode, NetViewerEdge> vv;
    /**
     * Stores all transitions that should be checked for their active state. In
     * the default implementation of computing the active transitions, all
     * transitions in this list will be checked.
     */
    protected Set<Transition> transitionsToCheck;
    protected PetriNetFacade petriNet;
    protected TokenSimulator tokenSim;
    /**
     * Random number generator for simulation tasks.
     */
    protected HighQualityRandom random = new HighQualityRandom();
    /**
     * Number of steps simulated so far.
     */
    protected int stepsSimulated = 0;
    private static final Logger LOGGER = LogManager.getLogger(AbstractTokenSim.class);

    //END VARIABLES DECLARATION
    //BEGIN INNER CLASSES
    /**
     * Handles the popup which appears when a place is selected by
     * right-clicking on it.
     */
    private class PopupMousePlugin extends AbstractPopupGraphMousePlugin implements MouseListener {

        @Override
        protected void handlePopup(final MouseEvent me) {
            Set<NetViewerNode> pickedVertices = vv.getPickedVertexState().getPicked();
            //The popup will appear if only one single place is picked. It ignores the picked transitions or arcs.
            if (pickedVertices.size() == 1) {
                //Get the selected node.
                final NetViewerNode node = pickedVertices.iterator().next();
                //if the selected node is a place, set the number of tokens for the place
                if (node.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
                    //Create new popup
                    JPopupMenu popup = new JPopupMenu();
                    //Get the place of PN represented by the selected node.
                    final Place place = petriNet.findPlace(node.getMasterNode().getId());
                    /*
                     * If the place is not constant, set the number of tokens.
                     */
                    if (!place.isConstant()) {
                        popup.add(new AbstractAction(TokenSimulator.strings.get("TSSetTokens")) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    long tokens = Long.parseLong(JOptionPane.showInputDialog(TokenSimulator.strings.get("TSSetTokenPT")));
                                    //negative integers are ignored
                                    if (tokens >= 0) {
                                        tokenSim.setTokens(place.id(), tokens);
                                    }
                                } catch (NumberFormatException E) {
                                    LOGGER.error("NumberFormatException while checking input" + E);
                                    JOptionPane.showMessageDialog(null, TokenSimulator.strings.get("TSNumberFormatExceptionM"));
                                } catch (TokenSimulator.PlaceConstantException ex) {
                                    LOGGER.error("ConstantPlaceException while checking for mouseaction " + ex);
                                }
                            }
                        });
                    } /*
                     If the place is constant, show window for editing mathematical expression.
                     */ else {
                        popup.add(new AbstractAction(TokenSimulator.strings.get("TSSetTokens")) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                /*
                                Create new frame for math exp.
                                 */
                                MathExpFrame frame;
                                frame = new MathExpFrame(petriNet.places(), tokenSim.getMathematicalExpression(place.id()));
                                frame.addToTitle(place.getProperty("name").toString());
                                /*
                                The information is the id of the place for which the mathematical expression is created. Used in the listener.
                                 */
                                frame.addListener(AbstractTokenSim.this, place.id());
                                frame.setVisible(true);
                            }
                        });
                    }

                    /*
                     * Add checkbox item for selecting whether the place is constant (i.e. the number of tokens on it will not be modified by simulators)
                     */
                    final JMenuItem cbItem = new JCheckBoxMenuItem(TokenSimulator.strings.get("PlaceConstant"));
                    cbItem.setToolTipText(TokenSimulator.strings.get("PlaceConstantTT"));
                    cbItem.setSelected(place.isConstant());
                    cbItem.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            tokenSim.setPlaceConstant(place.id(), cbItem.isSelected());
                        }
                    });
                    popup.add(cbItem);
                    popup.show(vv, me.getX(), me.getY());
                }
            }
        }
    }
    //END INNER CLASSES

    //BEGIN CONSTRUCTORS
    /**
     * Prevent using non-parameterized constructor.
     */
    private AbstractTokenSim() {

    }

    /**
     * New instance of AbstractTokenSim gets the TokenSimulator-object and uses
     * its VisualizationViewer and petriNet.
     *
     * @param tsN
     */
    public AbstractTokenSim(TokenSimulator tsN) {
        this.tokenSim = tsN;
        this.petriNet = tokenSim.getPetriNet();
        this.vv = this.tokenSim.getVisualizationViewer();
        this.transitionsToCheck = new HashSet<>();

        init();
    }
    //END CONSTRUCTORS    

    //BEGIN ABSTRACT METHODS
    /**
     * This method is always called from constructor; may be empty. Use it to
     * perform some custom initialization.
     */
    protected abstract void init();

    /**
     * Creates the GUI-Component with controls of this specific simulation mode
     * that will be used in NetViewer menu.
     *
     * @return
     */
    protected abstract JComponent getControlComponent();

    /**
     * Creates a JPanel which will be embedded in preferences-frame
     *
     * @return
     */
    protected abstract JPanel getPreferencesPanel();

    /**
     * Update preferences after they were changed in preferencesPanel and
     * save-button was pressed.
     */
    protected abstract void updatePreferences();

    /**
     * Load current settings to the Preferences-Panel.
     */
    protected abstract void loadPreferences();

    /**
     * AbstractTokenSim object receives the message that simulation mode has
     * been started. Variables can be initialized or controls can be set active.
     */
    protected abstract void startSim();

    /**
     * AbstractTokenSim object receives the message that simulation mode has
     * been stopped. Controls can be set inactive.
     */
    protected abstract void endSim();

    /**
     * This method chooses which transition will be fired next.
     *
     * @return Transition which will be fired in the next step.
     */
    protected abstract Transition getTransitionToFire();

    /**
     * Saves simulation setup, including marking, firing rates, constant-flags
     * for places etc to a XML-file.
     */
    protected abstract void exportSetup();

    /**
     * Loads simulation setup, including marking, firing rates, constant-flags
     * for places etc from a XML-file.
     */
    protected abstract void importSetup();

    /**
     * Create the mousePopupPlugin to handle mouse popups in token simulator.
     * This method can be overridden for custom popup menus.
     *
     * @return
     */
    protected AbstractPopupGraphMousePlugin getMousePopupPlugin() {
        return (new PopupMousePlugin());
    }

    /**
     * Get the time which was simulated.
     *
     * @return In timed PNs can be seconds, in non-timed PNs number of simulated
     * steps.
     */
    public abstract double getSimulatedTime();
    //END ABSTRACT METHODS

    /**
     * This method implements the determination of all transitions that can fire
     * (i.e. active and not forbidden). It takes marking-map from TokenSimulator
     * tokenSim and computes the states of transitions based on active
     * transitions rules.
     *
     * This method can be overridden to provide alternative active states
     * descriptions. Still, it should use the checkTransitions-ArrayList. Each
     * time a token number for a place changes, all post-transitions of this
     * place are added to checkTransitions.
     */
    protected void computeActiveTransitions() {
        /*
         * Iterate through all transitions that should be checked.
         */
        LOGGER.info("Checking for all transitions if they are fireable");
        for (Transition transition : this.transitionsToCheck) {
            /*
             * Assume that the transition is active.
             */
            boolean active = true;
            /*
             * Iterate through the pre-places of current transition.
             */
            for (Place place : transition.inputs()) {
                /*
                 * If any pre-palce of the transition has less tokens that the weight of the arc between the pre-place and transition,
                 * the transition cannot be active and all other pre-places must not be checked.
                 */
                long tokens = this.getTokens(place.id());
                if (this.petriNet.getArc(place, transition).weight() > tokens) {
                    active = false;
                    break;
                }
            }
            if (active) {
                this.tokenSim.activeTransitions.add(transition);
            } else {
                //Ensure that previous active-marked transition is not active any more.
                this.tokenSim.activeTransitions.remove(transition);
            }
        }
        LOGGER.info("Found all active Transitions");
        LOGGER.debug("Checking for constant places in preposition to transition, to make sure these transitions are checked for in every step");
        /*
        After active transitions were computed, clear the transitionsToCheck-list. However, post-transitions of constant
        places must be retained as they should be checked every step.
         */
        this.transitionsToCheck.retainAll(this.tokenSim.constantPlacesPostTransitions);
    }

    /**
     * Add received transitions to the checkTransitions-list, so they can be
     * checked.
     *
     * @param transitions Transitions which pre-places marking was changed.
     */
    protected void addTransitionsToCheck(Transition... transitions) {
        transitionsToCheck.addAll(Arrays.asList(transitions));
    }

    /**
     * This method is called from the MathExpFrame, when the "Save"-button has
     * been activated.
     *
     * @param e
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source instanceof MathExpFrame) {
            //Get the new mathematical expression.
            MathematicalExpression exp = ((MathExpFrame) source).getMathematicalExpression();
            try {
                /*
                Put the new mathematical expression to the constant places - map.
                 */
                this.tokenSim.setMathExpression((int) ((MathExpFrame) source).getInformation(this), exp);
            } catch (TokenSimulator.PlaceNonConstantException ex) {
                LOGGER.error("NonConstant Place Exception while experiencing a change of state" + ex);
            }

            ((MathExpFrame) source).dispose();
        }
    }

    /**
     * Return the number of tokens on the place.
     *
     * @param id ID of the place.
     * @return Number of tokens. For constant places mathematical expression is
     * evaluated first, using current marking of non-constant places and current
     * simulated time.
     */
    public long getTokens(int id) {
        if (!petriNet.findPlace(id).isConstant()) {
            return this.tokenSim.getMarking().get(id);
        } else {
            Map<Integer, Double> markingDouble = new HashMap<>();
            for (Entry<Integer, Long> entr : tokenSim.getMarking().entrySet()) {
                markingDouble.put(entr.getKey(), entr.getValue().doubleValue());
            }
            MathematicalExpression mathExp = tokenSim.getMathematicalExpression(id);
            return Math.round(mathExp.evaluateML(markingDouble, this.getSimulatedTime()));
        }
    }
}
