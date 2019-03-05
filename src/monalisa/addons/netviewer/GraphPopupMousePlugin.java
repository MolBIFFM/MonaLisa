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

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import monalisa.data.pn.Arc;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.synchronisation.Synchronizer;

/**
 * Generates the PopUp Menu which occurs on a right click in the NetViewer
 * @author jens
 */
public class GraphPopupMousePlugin extends AbstractPopupGraphMousePlugin implements MouseListener {

    public static final String NORMAL = "NORMAL"; // Normal mouse mode
    public static final String SINGLE_EDGE = "SINGLE_EDGE"; // right click to create a new edge
    public static final String EDGE = "EDGE"; // add new edge mode (constant)
    public static final String DOUBLE_EDGE = "DOUBLE_EDGE"; // add new edge mode (constant)
    public static final String IN_VERTEX = "IN_VERTEX";
    public static final String OUT_VERTEX = "OUT_VERTEX";
    public static final String DELETE = "DELTE";
    public static final String DELETE_BEND = "DELETE_BEND";
    public static final String ADD_BEND = "ADD_BEND";
    public static final String ALIGN_X = "ALIGN_X";
    public static final String ALIGN_Y = "ALIGN_Y";

    private final List<NetViewerNode> knockOutNodes = new ArrayList<>();

    private final NetViewer nv;
    private final VisualizationViewer<NetViewerNode, NetViewerEdge> vv;
    private Graph<NetViewerNode, NetViewerEdge> g;

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private String mouseMode = NORMAL;
    private NetViewerNode source;

    private Set<NetViewerNode> pickedVertices = new HashSet<>();
    private final List<NetViewerNode> pickedVerticesList = new ArrayList<>();
    private Set<NetViewerEdge> pickedEdges = new HashSet<>();
    private final List<NetViewerEdge> pickedEdgesList = new ArrayList<>();
    
    private final Synchronizer synchronizer;

    public GraphPopupMousePlugin(NetViewer nv, Synchronizer synchronizer) {
        this.nv = nv;
        this.vv = nv.vv;
        this.g = nv.g;
        this.synchronizer = synchronizer;
    }

    public void changeGraph(Graph<NetViewerNode, NetViewerEdge> g) {
        this.g = g;
    }

    @Override
    protected void handlePopup(final MouseEvent me) {
        JPopupMenu popup = new JPopupMenu();

        pickedVertices = vv.getPickedVertexState().getPicked();
        pickedVerticesList.clear();
        for(NetViewerNode n : pickedVertices)
            pickedVerticesList.add(n);
        pickedEdges = vv.getPickedEdgeState().getPicked();
        pickedEdgesList.clear();
        for(NetViewerEdge edge : pickedEdges)
            pickedEdgesList.add(edge);
        int numberOfPickedVertices = pickedVertices.size();
        int numberOfPickedEdges = pickedEdges.size();
        boolean bendedEdge = true;
        
        if(this.mouseMode.equals(NORMAL)) {

            // Mutli Vertex Popup Menu
            if(numberOfPickedVertices > 1) {

                // Vertext Setup
                popup.add(new AbstractAction(strings.get("NVProperties")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nv.showVertexSetup(pickedVerticesList, me.getX(), me.getY());
                    }
                });

                Boolean meltLogicalPlaces = false;
                NetViewerNode firstNode = pickedVerticesList.get(0);
                if(firstNode.isLogical()) {
                    for(NetViewerNode nvNode : pickedVerticesList) {
                        if(nvNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
                            if(nvNode.isLogical() && !nvNode.isMasterNode() && firstNode.getMasterNode().equals(nvNode.getMasterNode()))
                                meltLogicalPlaces = true;
                            else
                                meltLogicalPlaces = false;
                        }
                    }
                }

                // Melt logical places
                if(meltLogicalPlaces) {
                    popup.add(new AbstractAction(strings.get("NVMeltLogicalPlaces")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {;
                            synchronizer.mergeLogicalPlaces(pickedVerticesList);
                            nv.nonModificationActionHappend();
                        }
                    });
                }
                
                // Align X
                popup.add(new AbstractAction(strings.get("NVAlignXPopUp")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nv.alignXMouseAction();
                    }
                });

                // Align Y
                popup.add(new AbstractAction(strings.get("NVAlignYPopUp")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nv.alignYMouseAction();
                    }
                });
                
                popup.addSeparator();
                
                // Export Subgraph
                popup.add(new AbstractAction(strings.get("NVExportSubGraph")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            nv.exportSelectedSubGraphMouseAction();
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(GraphPopupMousePlugin.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });                                 
            }

            // Edge Popup Menu
            if(numberOfPickedEdges > 1) {
                List<NetViewerEdge> bendList = pickedEdgesList.get(0).getMasterEdge().getBendEdges();
                for (NetViewerEdge picked : pickedEdges) {
                    if (!bendList.contains(picked)) {
                        bendedEdge = false;
                        break;
                    }
                }
            } 
            else {
                bendedEdge = false;
            }

            if( (numberOfPickedEdges == 1 || bendedEdge)  && numberOfPickedVertices == 0) {
                final NetViewerEdge edge = pickedEdgesList.get(0);

                // Edge Setup
                popup.add(new AbstractAction(strings.get("NVProperties")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nv.showEdgeSetup(edge, me.getX(), me.getY());
                    }
                });

                popup.addSeparator();

                // Insert Bend
                popup.add(new AbstractAction(strings.get("NVInsertBend")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        synchronizer.addBend(edge, (double) me.getX(), (double) me.getY());                        
                    }
                });

                // Remove Bend
                Boolean removeEdge = (!edge.getAim().getNodeType().equals(NetViewer.BEND) && !edge.getSource().getNodeType().equals(NetViewer.BEND));
                if(!removeEdge) {
                    popup.add(new AbstractAction(strings.get("NVRemoveBend")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            synchronizer.removeBend(edge);
                        }
                    });
                }

                // Plane Edge
                if(!removeEdge) {
                    popup.add(new AbstractAction(strings.get("NVPlaneEdge")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            synchronizer.removeAllBends(edge);
                            nv.nonModificationActionHappend();
                        }
                    });
                }

                popup.addSeparator();

                // Delete Edge
                popup.add(new AbstractAction(strings.get("NVDeleteEdge")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        synchronizer.removeEdge(edge);
                        nv.modificationActionHappend();
                    }
                });
            }

            // Vertex Popup Menu
            else if(numberOfPickedEdges == 0 && numberOfPickedVertices == 1) {
                final NetViewerNode node = pickedVerticesList.get(0);

                if(node.getNodeType().equals(NetViewer.BEND))
                    return;

                // Vertext Setup
                popup.add(new AbstractAction(strings.get("NVProperties")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nv.showVertexSetup(node, me.getX(), me.getY());
                    }
                });

                // Hide Label
                String itemName = (node.showLabel()) ? strings.get("NVHideLabel") : strings.get("NVShowLabel");
                popup.add(new AbstractAction(itemName) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nv.hideLabel(node);
                    }
                });

                popup.addSeparator();
                switch (node.getNodeType()) {
                    case NetViewer.PLACE:
                        // Add Transition
                        JMenu addTransitionMenu = new JMenu(strings.get("NVCreateTransition"));
                        addTransitionMenu.add(new AbstractAction(strings.get("NVIn")) {
                            @Override
                            public void actionPerformed(ActionEvent e) {                                
                                synchronizer.addNode(node, NetViewer.INPUT);
                                nv.modificationActionHappend();
                            }
                        });
                        addTransitionMenu.add(new AbstractAction(strings.get("NVOut")) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                synchronizer.addNode(node, NetViewer.OUTPUT);
                                nv.modificationActionHappend();
                            }
                        });
                        popup.add(addTransitionMenu);
                        // Delete Place
                        popup.add(new AbstractAction(strings.get("NVDeleteVertex")) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                synchronizer.removeNode(node);  
                                nv.modificationActionHappend();
                            }
                        });
                        break;
                    case NetViewer.TRANSITION:
                        // Add Place
                        JMenu addPlaceMenu = new JMenu(strings.get("NVCreateVertex"));
                        addPlaceMenu.add(new AbstractAction(strings.get("NVIn")) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                synchronizer.addNode(node, NetViewer.INPUT);
                                nv.modificationActionHappend();
                            }
                        });
                        addPlaceMenu.add(new AbstractAction(strings.get("NVOut")) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                synchronizer.addNode(node, NetViewer.OUTPUT);
                                nv.modificationActionHappend();
                            }
                        });
                        popup.add(addPlaceMenu);
                        // Delete Transition
                        popup.add(new AbstractAction(strings.get("NVDeleteTransition")) {
                            @Override
                    
                            public void actionPerformed(ActionEvent e) {
                                synchronizer.removeNode(node);
                                nv.modificationActionHappend();
                            }
                        });
                        break;
                }

                // Insert Edge
                popup.add(new AbstractAction(strings.get("NVCreateEdge")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        source = node;
                        setMouseModeToSingleEdge();
                    }
                });

                // Create Locical Place
                Boolean addLocicalPlace = (node.getNodeType().equals(NetViewer.PLACE) && g.getNeighborCount(node) > 1);
                if(addLocicalPlace) {
                    popup.add(new AbstractAction(strings.get("NVCreateNewNodeButton")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            nv.showCreateLogicalPlace(node, me.getX(), me.getY());
                        }
                    });
                }

                // Reverse Transition
                Boolean reverseTransition = (node.getNodeType().equals(NetViewer.TRANSITION));
                if(reverseTransition) {
                    popup.add(new AbstractAction(strings.get("NVReverseTransition")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            synchronizer.reverseTransition(node, me.getX(), me.getY());
                            nv.modificationActionHappend();
                        }
                    });
                }

                // Delete Locical Place
                Boolean removeLocicalPlace = (node.getNodeType().equals(NetViewer.PLACE) && node.isLogical() && !node.isMasterNode());
                if(removeLocicalPlace) {
                    popup.add(new AbstractAction(strings.get("NVRemoveNodeButton")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            synchronizer.removeNode(node);
                            nv.modificationActionHappend();
                        }
                    });

                    popup.add(new AbstractAction(strings.get("NVMeltAllLogicalPlaces")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            List<NetViewerNode> allPlaces = new ArrayList<>();
                            for(NetViewerNode n : node.getMasterNode().getLogicalPlaces())
                                allPlaces.add(n);
                            for(NetViewerNode n : allPlaces) {
                                synchronizer.removeNode(node);
                            }
                            nv.modificationActionHappend();
                        }
                    });
                }

                // Transition knock out
                JMenu knockOutMenu = new JMenu(strings.get("NVKnockoutTransition"));

                Boolean knockOutTransition = (node.getNodeType().equals(NetViewer.TRANSITION) && nv.hasTinvs());
                if(knockOutTransition) {
                    popup.addSeparator();

                    knockOutMenu.add(new AbstractAction(strings.get("NVKnockOutStandart")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            knockOutNodes.add(node);
                            nv.knockOut(knockOutNodes, 0);
                        }
                    });

                    knockOutMenu.add(new AbstractAction(strings.get("NVKnockOutByOccurrence")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            knockOutNodes.add(node);
                            nv.knockOut(knockOutNodes, 1);
                        }
                    }).setEnabled(knockOutTransition);

                    knockOutMenu.add(new AbstractAction(strings.get("NVResetKnockOut")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            knockOutNodes.clear();
                            nv.resetKnockOut();
                        }
                    });
                    popup.add(knockOutMenu);
                }

                popup.addSeparator();

                // Align X
                popup.add(new AbstractAction(strings.get("NVAlignXPopUp")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nv.alignXMouseAction();
                    }
                });

                // Align Y
                popup.add(new AbstractAction(strings.get("NVAlignYPopUp")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nv.alignYMouseAction();
                    }
                });
            }

            // Generell Options
            else if(numberOfPickedEdges == 0 && numberOfPickedVertices == 0) {

                // MouseMode
                if(nv.getMouseMode()) {
                    popup.add(new AbstractAction(strings.get("NVGMTransforming")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            nv.changeMouseModeToTransforming();
                        }
                    });
                }
                else {
                    popup.add(new AbstractAction(strings.get("NVGMPicking")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            nv.changeMouseModeToPicking();
                        }
                    });
                }

                popup.add(new AbstractAction(strings.get("NVCenterPetriNet")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nv.center();
                    }
                });
            }
        }

        popup.show(vv, me.getX(), me.getY());
    }

    /**
     * Handles the picking of a new node
     * @param clickedNode
     */
    public void handlePicking(NetViewerNode clickedNode) {
        switch (this.mouseMode) {
            case EDGE:
            case SINGLE_EDGE:
                if(!clickedNode.getNodeType().equalsIgnoreCase(source.getNodeType()) && !clickedNode.getNodeType().equals(NetViewer.BEND)) {
                    Arc arc;
                    boolean error = false;
                    if(source.getNodeType().equals(NetViewer.PLACE)) {
                        arc = synchronizer.getPetriNet().getArc(synchronizer.getPetriNet().findPlace(source.getMasterNode().getId()), synchronizer.getPetriNet().findTransition(clickedNode.getMasterNode().getId()));
                        if(arc != null)
                            error = true;
                    }
                    else if(source.getNodeType().equals(NetViewer.TRANSITION) && (clickedNode.isLogical() || clickedNode.isMasterNode())) {
                        arc = synchronizer.getPetriNet().getArc(synchronizer.getPetriNet().findTransition(source.getMasterNode().getId()), synchronizer.getPetriNet().findPlace(clickedNode.getMasterNode().getId()));
                        if(arc != null)
                            error = true;
                    }                                       
                    if(!error) {
                        synchronizer.addEdge(1, source, clickedNode);
                        nv.modificationActionHappend();
                    } else {
                        nv.cancelMouseAction();
                        nv.displayMessage(strings.get("NVEdgeAlwaysHere"), Color.RED);
                        return;
                    }
                    nv.edgeMouseAction();                    
                }
                else {
                    nv.displayMessage("<html>"+strings.get("NVInsertEdgeMessage") + "<br>" + strings.get("NVFalseAimType")+"</html>", Color.BLACK);
                }
                break;
            case DOUBLE_EDGE:
                this.source = clickedNode;
                setMouseModeToEdge();
                break;
            case IN_VERTEX:
                if(!clickedNode.getNodeType().equals(NetViewer.BEND)) {
                    synchronizer.addNode(clickedNode, NetViewer.INPUT);
                    nv.modificationActionHappend();
                }
                break;
            case OUT_VERTEX:
                if(!clickedNode.getNodeType().equals(NetViewer.BEND)) {
                    synchronizer.addNode(clickedNode, NetViewer.OUTPUT);
                    nv.modificationActionHappend();                    
                }
                break;
            case DELETE:
                if(!clickedNode.getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
                    synchronizer.removeNode(clickedNode);
                    nv.modificationActionHappend();
                }
                break;
            case ALIGN_X:
                if (!clickedNode.getNodeType().equalsIgnoreCase(NetViewer.BEND))
                    nv.alignVertices("X", clickedNode);
                break;
            case ALIGN_Y:
                if (!clickedNode.getNodeType().equalsIgnoreCase(NetViewer.BEND))
                    nv.alignVertices("Y", clickedNode);
                break;
        }
    }

    /**
     * Handles the picking of a new edge
     * @param clickedEdge
     */
    public void handlePicking(NetViewerEdge clickedEdge) {
        switch (this.mouseMode) {
            case DELETE:
                synchronizer.removeEdge(clickedEdge.getMasterEdge());
                nv.modificationActionHappend();
                break;
            case ADD_BEND:
                Point mousePoint = vv.getMousePosition();
                synchronizer.addBend(clickedEdge, (double) mousePoint.x, (double) mousePoint.y);              
                break;
            case DELETE_BEND:
                if(clickedEdge.getAim().getNodeType().equalsIgnoreCase(NetViewer.BEND) || clickedEdge.getSource().getNodeType().equalsIgnoreCase(NetViewer.BEND))
                    synchronizer.removeBend(clickedEdge);
                break;
        }
     }

    public void setMouseModeToNormal() {
        this.mouseMode = NORMAL;
    }

    public void setMouseModeToEdge() {
        this.mouseMode = EDGE;
        nv.displayMessage(strings.get("NVInsertEdgeMessage"), Color.BLACK);
    }

    public void setMouseModeToDoubleEdge() {
        this.mouseMode = DOUBLE_EDGE;
    }

    public void setMouseModeToInVertex() {
        this.mouseMode = IN_VERTEX;
    }

    public void setMouseModeToOutVertex() {
        this.mouseMode = OUT_VERTEX;
    }

    public void setMouseModeToDelete() {
        this.mouseMode = DELETE;
    }

    public void setMouseModeToDeleteBend() {
        this.mouseMode = DELETE_BEND;
    }

    public void setMouseModeToAddBend() {
        this.mouseMode = ADD_BEND;
    }

    private void setMouseModeToSingleEdge() {
        this.mouseMode = SINGLE_EDGE;
        nv.displayMessage(strings.get("NVInsertEdgeMessage"), Color.BLACK);
    }
    
    public void setMouseModeToAlignX() {
        this.mouseMode = ALIGN_X;
    }
        
    public void setMouseModeToAlignY() {
        this.mouseMode = ALIGN_Y;
    }

    public String getMouseMode() {
        return this.mouseMode;
    }

}
