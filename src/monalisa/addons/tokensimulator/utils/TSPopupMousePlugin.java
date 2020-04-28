/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.utils;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerEdge;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.addons.tokensimulator.AbstractTokenSim;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.exceptions.PlaceConstantException;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the popup which appears when a place is selected by right-clicking on
 * it.
 */
public class TSPopupMousePlugin extends AbstractPopupGraphMousePlugin implements MouseListener {

    private final VisualizationViewer<NetViewerNode, NetViewerEdge> vv;
    private final PetriNetFacade pnf;
    private final static Logger LOGGER = LogManager.getLogger(TSPopupMousePlugin.class);
    private final AbstractTokenSim abstTS;

    public TSPopupMousePlugin (VisualizationViewer<NetViewerNode, NetViewerEdge> vv, PetriNetFacade pnf, AbstractTokenSim abstTS) {
        this.vv = vv;
        this.pnf = pnf;
        this.abstTS = abstTS;
    }

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
                final Place place = pnf.findPlace(node.getMasterNode().getId());
                /*
                * If the place is not constant, set the number of tokens.
                 */
                if (!place.isConstant()) {
                    popup.add(new AbstractAction(SimulationManager.strings.get("TSSetTokens")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                long tokens = Long.parseLong(JOptionPane.showInputDialog(SimulationManager.strings.get("TSSetTokenPT")));
                                //negative integers are ignored
                                if (tokens >= 0) {
                                    abstTS.getSimulationMan().setTokens(place.id(), tokens);
                                }
                            } catch (NumberFormatException E) {
                                LOGGER.error("NumberFormatException while checking input" + E);
                                JOptionPane.showMessageDialog(null, SimulationManager.strings.get("TSNumberFormatExceptionM"));
                            } catch (PlaceConstantException ex) {
                                LOGGER.error("ConstantPlaceException while checking for mouseaction " + ex);
                            }
                        }
                    });
                } /*
                If the place is constant, show window for editing mathematical expression.
                 */ else {
                    popup.add(new AbstractAction(SimulationManager.strings.get("TSSetTokens")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            /*
                             Create new frame for math exp.
                             */
                            MathExpFrame frame;
                            frame = new MathExpFrame(pnf.places(), abstTS.getSimulationMan().getMathematicalExpression(place.id()), abstTS);
                            frame.addToTitle(place.getProperty("name").toString());
                            /*
                             The information is the id of the place for which the mathematical expression is created. Used in the listener.
                             */
                            frame.addPlaceInformation(place.id());
                            frame.setVisible(true);
                        }
                    });
                }

                /*
                * Add checkbox item for selecting whether the place is constant (i.e. the number of tokens on it will not be modified by simulators)
                 */
                final JMenuItem cbItem = new JCheckBoxMenuItem(SimulationManager.strings.get("PlaceConstant"));
                cbItem.setToolTipText(SimulationManager.strings.get("PlaceConstantTT"));
                cbItem.setSelected(place.isConstant());
                cbItem.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        abstTS.getSimulationMan().setPlaceConstant(place.id(), cbItem.isSelected());
                    }
                });
                popup.add(cbItem);
                popup.show(vv, me.getX(), me.getY());
            }
        }
    }
}
