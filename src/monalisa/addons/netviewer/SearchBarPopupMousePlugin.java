/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
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

/**
 * Context menu for SearchBar
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

    public SearchBarPopupMousePlugin(NetViewer nv, Synchronizer synchronizer, JList owner) {
        this.nv = nv;
        this.vv = nv.vv;
        this.owner = owner;
        this.synchronizer = synchronizer;
        this.type = owner.getName();
    }

    @Override
    protected void handlePopup(final MouseEvent me) {
        JPopupMenu popup = new JPopupMenu();
        final List<NetViewerNode> selectedValues = owner.getSelectedValuesList();
        String menuName = null;

        // Places + Tansitions
        if(selectedValues.size() == 1) {
            final NetViewerNode nvNode = selectedValues.get(0);

            // Logical places
            if(nvNode.getLogicalPlaces().size() > 1) {
                JMenu zoomAtChildMenu = new JMenu(strings.get("NVZoomAtVertex"));
                int i = 1;
                for(final NetViewerNode n : nvNode.getLogicalPlaces()) {
                    zoomAtChildMenu.add(new AbstractAction(strings.get("NVZoomAtVertex")+" child "+(i++)) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            nv.zoomToVertex(n);
                        }
                    });
                }
                popup.add(zoomAtChildMenu);
            }
            // Non-logical place or transition
            else {
                popup.add(new AbstractAction(strings.get("NVZoomAtVertex")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                       nv.zoomToVertex(nvNode);
                    }
                });
            }

            popup.add(new AbstractAction(strings.get("NVProperties")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    nv.showVertexSetup(nvNode, me.getX(), me.getY());
                }
            });

        }

        // Only for places
        if(type.equalsIgnoreCase(NetViewer.PLACE)) {

            if(selectedValues.size() == 1) {
                NetViewerNode nvNode = selectedValues.get(0);

                if(nvNode.getLogicalPlaces().size() > 1) {
                    popup.add(new AbstractAction(strings.get("NVMeltAllLogicalPlaces")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            List<NetViewerNode> pickedVerticesList = new ArrayList<>();
                            for(NetViewerNode n : vv.getRenderContext().getPickedVertexState().getPicked())
                                pickedVerticesList.add(n);
                            synchronizer.mergeLogicalPlaces(pickedVerticesList);
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
                        synchronizer.reverseTransition(nvNode, me.getX(), me.getY());
                        nv.modificationActionHappend();                        
                    }
                });
            }
        }

        // Melting places / transitions
        if(selectedValues.size() > 1) {
            if(type.equalsIgnoreCase(NetViewer.TRANSITION))
                menuName = strings.get("NVMeltingTransitions");
            else if(type.equalsIgnoreCase(NetViewer.PLACE))
                menuName = strings.get("NVMeltingPlaces");

            popup.add(new AbstractAction(menuName) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronizer.mergeVertices(owner.getSelectedValuesList());
                    nv.modificationActionHappend();
                }
            });

            popup.add(new AbstractAction(strings.get("NVProperties")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<NetViewerNode> selectedNodes = new ArrayList<>();
                    for(Object o : selectedValues)
                        selectedNodes.add((NetViewerNode) o);
                    nv.showVertexSetup(selectedNodes, me.getX(), me.getY());
                }
            });
        }

        popup.show(owner, me.getX(), me.getY());
    }
}
