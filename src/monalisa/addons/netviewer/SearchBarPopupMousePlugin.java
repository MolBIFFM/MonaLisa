/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.synchronisation.Synchronizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Context menu for SearchBar
 *
 * @author Jens Einloft
 */
public class SearchBarPopupMousePlugin extends AbstractPopupGraphMousePlugin implements MouseListener {

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private final NetViewer nv;
    private final VisualizationViewer<NetViewerNode, NetViewerEdge> vv;
    private final JList owner;
    private final String type;

    private final Synchronizer synchronizer;
    private static final Logger LOGGER = LogManager.getLogger(SearchBarPopupMousePlugin.class);

    public SearchBarPopupMousePlugin(NetViewer nv, Synchronizer synchronizer, JList owner) {
        this.nv = nv;
        this.vv = nv.vv;
        this.owner = owner;
        this.synchronizer = synchronizer;
        this.type = owner.getName();
    }

    @Override
    protected void handlePopup(final MouseEvent me) {
        LOGGER.debug("Handling popup for SearchBar");
        JPopupMenu popup = new JPopupMenu();
        final List<NetViewerNode> selectedValues = owner.getSelectedValuesList();
        String menuName = null;

        // Places + Tansitions
        if (selectedValues.size() == 1) {
            final NetViewerNode nvNode = selectedValues.get(0);

            // Logical places
            if (nvNode.getLogicalPlaces().size() > 1) {
                JMenu zoomAtChildMenu = new JMenu(strings.get("NVZoomAtVertex"));
                int i = 1;
                for (final NetViewerNode n : nvNode.getLogicalPlaces()) {
                    zoomAtChildMenu.add(new AbstractAction(strings.get("NVZoomAtVertex") + " child " + (i++)) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LOGGER.info("Zooming to vertex from SearchBar for logical places");
                            nv.zoomToVertex(n);
                        }
                    });
                }
                popup.add(zoomAtChildMenu);
            } // Non-logical place or transition
            else {
                popup.add(new AbstractAction(strings.get("NVZoomAtVertex")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LOGGER.info("Zooming to vertex from SearchBar for non-logical nodes");
                        nv.zoomToVertex(nvNode);
                    }
                });
            }

            popup.add(new AbstractAction(strings.get("NVProperties")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LOGGER.info("Entering node setup from SearchBar");
                    nv.showVertexSetup(nvNode); //, me.getX(), me.getY());
                    LOGGER.info("Leaving node setup from SearchBar");
                }
            });

        }

        // Only for places
        if (type.equalsIgnoreCase(NetViewer.PLACE)) {

            if (selectedValues.size() == 1) {
                NetViewerNode nvNode = selectedValues.get(0);

                if (nvNode.getLogicalPlaces().size() > 1) {
                    popup.add(new AbstractAction(strings.get("NVMeltAllLogicalPlaces")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LOGGER.info("Melting all logical places");
                            List<NetViewerNode> pickedVerticesList = new ArrayList<>();
                            for (NetViewerNode n : vv.getRenderContext().getPickedVertexState().getPicked()) {
                                pickedVerticesList.add(n);
                            }
                            nv.mergeLogicalPlaces(pickedVerticesList);
                            nv.nonModificationActionHappend();
                        }
                    });
                }

            }
        }

        // only transitions
        if (type.equalsIgnoreCase(NetViewer.TRANSITION)) {

            if (selectedValues.size() == 1) {
                final NetViewerNode nvNode = selectedValues.get(0);

                popup.add(new AbstractAction(strings.get("NVReverseTransition")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LOGGER.info("Reverse Transitions");
                        if (nv.tb.getEnableGrid()) {
                            nv.reverseTransition(nvNode, (int) nv.formatCoordinates(me.getX()), (int) nv.formatCoordinates(me.getY()));
                        } else {
                            nv.reverseTransition(nvNode, me.getX(), me.getY());
                        }
                        nv.modificationActionHappend();
                    }
                });
            }
        }

        // Melting places / transitions
        if (selectedValues.size() > 1) {
            if (type.equalsIgnoreCase(NetViewer.TRANSITION)) {
                menuName = strings.get("NVMeltingTransitions");
            } else if (type.equalsIgnoreCase(NetViewer.PLACE)) {
                menuName = strings.get("NVMeltingPlaces");
            }

            popup.add(new AbstractAction(menuName) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LOGGER.info("Merging vertices from SearchBar");
                    nv.mergeVertices(owner.getSelectedValuesList());
                    nv.modificationActionHappend();
                }
            });

            popup.add(new AbstractAction(strings.get("NVProperties")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LOGGER.info("Entering vertex setup for several vertices");
                    List<NetViewerNode> selectedNodes = new ArrayList<>();
                    for (Object o : selectedValues) {
                        selectedNodes.add((NetViewerNode) o);
                    }
                    nv.showVertexSetup(selectedNodes); //, me.getX(), me.getY());
                    LOGGER.info("Leaving vertex setup for several vertices");
                }
            });
        }

        popup.show(owner, me.getX(), me.getY());
        LOGGER.debug("Done handling popup for SearchBar");
    }
}
