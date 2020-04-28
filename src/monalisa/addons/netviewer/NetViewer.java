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

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import monalisa.addons.netviewer.gui.ColorOptionsFrame;
import monalisa.addons.netviewer.transformer.MyEdgeRenderer;
import monalisa.addons.netviewer.listener.NetViewerWindowsListener;
import monalisa.addons.netviewer.wrapper.PinvWrapper;
import monalisa.addons.netviewer.wrapper.MctsWrapper;
import monalisa.addons.netviewer.wrapper.TinvWrapper;
import monalisa.addons.netviewer.wrapper.MinvWrapper;
import monalisa.addons.netviewer.gui.EdgeSetupFrame;
import monalisa.addons.netviewer.gui.VertexSetupFrame;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.swing.*;
import layout.TableLayout;
import monalisa.Project;
import monalisa.Settings;
import monalisa.ToolFrame;
import monalisa.addons.netviewer.listener.NetChangedListener;
import monalisa.addons.netviewer.transformer.AdvancedVertexLabelRenderer;
import monalisa.addons.netviewer.transformer.EdgeArrowTransformer;
import monalisa.addons.netviewer.transformer.EdgeIncludePredicate;
import monalisa.addons.netviewer.transformer.EdgeLabelTransformer;
import monalisa.addons.netviewer.transformer.EdgePaintTransformer;
import monalisa.addons.netviewer.transformer.EdgeStrokeTransformer;
import monalisa.addons.netviewer.transformer.EdgeToolTipTransformer;
import monalisa.addons.netviewer.transformer.EdgeFontTransformer;
import monalisa.addons.netviewer.transformer.VertexDrawPaintTransformer;
import monalisa.addons.netviewer.transformer.VertexFontTransformer;
import monalisa.addons.netviewer.transformer.VertexLabelTransformer;
import monalisa.addons.netviewer.transformer.VertexPaintTransformer;
import monalisa.addons.netviewer.transformer.VertexShapeTransformer;
import monalisa.addons.netviewer.transformer.VertexStrokeTransformer;
import monalisa.addons.netviewer.transformer.VertexToolTipTransformer;
import monalisa.addons.netviewer.wrapper.McsWrapper;
import monalisa.data.output.OutputHandler;
import monalisa.data.output.PetriNetOutputHandlers;
import monalisa.data.pn.*;
import monalisa.gui.MainDialog;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.*;
import monalisa.synchronisation.Synchronizer;
import monalisa.tools.mcs.McsTool;
import monalisa.tools.mcts.MctsTool;
import monalisa.tools.pinv.PInvariantTool;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.tools.minv.MInvariantTool;
import monalisa.util.MonaLisaFileChooser;
import monalisa.util.MonaLisaFileFilter;
import monalisa.util.OutputFileFilter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Graphical Viewer for Petri nets and visualization of analysis results
 *
 * @author Jens Einloft
 */
public class NetViewer extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1113813463292202573L;

    // Constants declaration
    public final static String VVPANEL = "VVPANEL";
    public static final String TRANSITION = "TRANSITION";
    public static final String PLACE = "PLACE";
    public static final String BEND = "BEND";
    public static final Color DEFAULT_COLOR_PLACES = Color.WHITE;
    public static final Color DEFAULT_COLOR_TRANSITIONS = Color.BLACK;
    public static final Boolean INPUT = true;
    public static final Boolean OUTPUT = false;
    private static final String EXIT = "EXIT";
    private static final String HIDE_COLOR = "HIDE_COLOR";
    private static final String MAKE_PIC = "MAKE_PIC";
    private static final String CTI_TRANSITIONS = "CTI_TRANSITIONS";
    private static final String BY_OCCURRENCE = "BY_OCCURRENCE";
    private static final String BY_FACTOR = "BY_FACTOR";
    private static final String SHOW_LABELS = "SHOW_LABELS";
    private static final String CENTER_NET = "CENTER_NET";
    private static final String RESET_COLORING = "RESET_COLORING";
    private static final String SHOW_COLOR_OPTION = "SHOW_COLOR_OPTION";

    private static final Color defaultPanelColor = new Color(238, 238, 238);

    public static Color TINV_COLOR;
    public static Color PINV_COLOR;
    public static Color MCTS_COLOR;
    public static Color HEATMAP_COLOR;
    public static Color KNOCKEDOUT_COLOR;
    public static Color ALSOKNOCKEDOUT_COLOR;
    public static Color NOTKNOCKEDOUTCOLOR;
    public static Color BACKGROUND_COLOR;
    public static Color MCS_COLOR;
    public static Color MCSOBJECTIV_COLOR;

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private final Project project;
    private final MainDialog mainDialog;
    private TInvariants tinvs;
    private PInvariants pinvs;
    private MInvariants minvs;
    private Map<Configuration, Result> mctsResults;
    private Map<Configuration, Result> mcsResults;
    public Graph<NetViewerNode, NetViewerEdge> g;
    private MonaLisaLayout<NetViewerNode, NetViewerEdge> layout;
    public final Map<Integer, NetViewerNode> placeMap;
    public final Map<Integer, NetViewerNode> transitionMap;
    private int latestEdgeID;
    private int latestVertexID;
    protected VisualizationViewer<NetViewerNode, NetViewerEdge> vv;
    private NetViewerModalGraphMouse gm;
    private Boolean mouseMode;                                                  // true = picking ; false = transforming
    protected Boolean heatMap;                                                    // true = show , false = dont show
    private Boolean netChanged = false, lastChanged = false;
    private final List<NetViewerNode> alignmentList;                            // List to save selected nodes for a layout alignment

    private GraphPopupMousePlugin gpmp;
    private NetViewerMouseListener mml;
    private NetViewerKeyListener nvkl;

    protected int placeCount, transitionCount;

    // GUI Section
    private JMenuBar menuBar;
    private JMenu fileMenu, visualizationMenu, importantTransitonsMenu, optionsMenu, addonMenu;
    private JMenuItem exitItem, ctiItem, byOccurrenceItem, byFactorItem, centerItem,
            resetColoringItem, colorOptionsItem, colorItem, labelItem, saveAsPictureItem;
    private JLabel messageLabel, infoBarLabel;
    private DefaultListModel<NetViewerNode> neighborsListModel;
    private DefaultListModel allPlacesModel, allTransitionsModel;

    private JSplitPane mainSplitPane;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    protected ToolBar tb;
    private SearchBar sb;
    private LogicalPlacesFrame lpf;
    private final ToolFrame toolUI;

    private final List<JPanel> mouseModePanels = new ArrayList<>();

    private final Map<DefaultListModel, String> tinvModelMap = new HashMap<>();
    private final List<JFrame> framesList = new ArrayList<>();
    private final Map<String, JScrollPane> spMap = new HashMap<>();

    private final List<NetChangedListener> netChangedListener;

    private final Synchronizer synchronizer;
    private final NetViewerStorage nvs;
    private static final Logger LOGGER = LogManager.getLogger(NetViewer.class);

    /**
     *
     * @param owner
     * @param project
     * @param toolUI
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InterruptedException
     */
    public NetViewer(MainDialog owner, Project project, ToolFrame toolUI) throws IOException, ClassNotFoundException, InterruptedException {
        LOGGER.info("Initializing NetViewer");
        this.toolUI = toolUI;
        this.alignmentList = new ArrayList<>();
        this.project = project;
        this.mainDialog = owner;
        this.synchronizer = project.getSynchronizer();
        this.nvs = project.getNvs();

        // Load NVS values
        this.latestVertexID = nvs.getLatestVertexID();
        this.latestEdgeID = nvs.getLatestEdgeID();
        this.placeMap = nvs.getPlaceMap();
        this.transitionMap = nvs.getTransitionMap();
        this.g = nvs.getGraph();
        this.layout = nvs.getLayout();

        setLocationRelativeTo(owner);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // Disable heatmaps for T-invariants
        this.heatMap = false;

        // Read the saved settings for the NetViewer out of config file
        TINV_COLOR = Settings.getAsColor("tinvColor");
        PINV_COLOR = Settings.getAsColor("pinvColor");
        MCTS_COLOR = Settings.getAsColor("mctsColor");
        HEATMAP_COLOR = Settings.getAsColor("heatMapColor");
        KNOCKEDOUT_COLOR = Settings.getAsColor("knockedOutColor");
        ALSOKNOCKEDOUT_COLOR = Settings.getAsColor("alsoKnockedOutColor");
        NOTKNOCKEDOUTCOLOR = Settings.getAsColor("notKnockedOutColor");
        BACKGROUND_COLOR = Settings.getAsColor("backgroundColor");
        MCSOBJECTIV_COLOR = Settings.getAsColor("mcsObjectivColor");
        MCS_COLOR = Settings.getAsColor("mcsColor");

        netChangedListener = new ArrayList<>();

        initComponent();

        placeCount = this.synchronizer.getPetriNet().places().size();
        transitionCount = this.synchronizer.getPetriNet().transitions().size();
        LOGGER.info("Finished initializing NetViewer");
    }

    /**
     * Init the NetViewer GUI and data structures
     */
    private void initComponent() throws ClassNotFoundException, InterruptedException {
        LOGGER.info("Initializing NetViewer frame");
        // --- Init Frame ---
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension nvDimension = new Dimension((int) (screenDimension.getWidth() * 0.95), (int) (screenDimension.getHeight() * 0.95));

        setSize(nvDimension);
        setMinimumSize(new Dimension(800, 600));
        setTitle(strings.get("NVTitle") + " - " + project.getName());
        setIconImage(resources.getImage("icon-16.png"));
        setLocation(0, 0);

        addComponentListener(new ComponentListener() {
            Dimension currentDimension;
            double sizeOfRightSide, newSplitRatio;

            @Override
            public void componentResized(ComponentEvent e) {
                currentDimension = ((JFrame) e.getSource()).getSize();
                sizeOfRightSide = currentDimension.getWidth() * 0.75;

                if (sizeOfRightSide > 400.0) {
                    newSplitRatio = 1 - (400 / currentDimension.getWidth());
                } else {
                    newSplitRatio = 0.75;
                }

                mainSplitPane.setDividerLocation(newSplitRatio);
                mainSplitPane.setResizeWeight(newSplitRatio);
                mainSplitPane.validate();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        addWindowListener(new NetViewerWindowsListener(this));
        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent we) {
                changeMouseModeToPicking();
            }

            @Override
            public void windowLostFocus(WindowEvent we) {
                changeMouseModeToPicking();
            }
        });

        // --- Graph Section ---
        if (g == null) {
            LOGGER.warn("Graph is null, initializing new graph");
            g = translatePNtoGraph();
            layout = new MonaLisaLayout<>(new FRLayout<>(g));
        } else {
            LOGGER.info("Using imported Graph and Layout");
        }

        LOGGER.debug("Initializing VisualizationViewer");
        // Init the VisualizationViewer and set all Renderer ect.
        vv = new VisualizationViewer<>(layout);//, gridSize, oo);
        vv.setPreferredSize(nvDimension);
        vv.setSize(nvDimension);
        vv.setVertexToolTipTransformer(new VertexToolTipTransformer()); // render the tooltips by mouse over
        vv.setEdgeToolTipTransformer(new EdgeToolTipTransformer());
        vv.getRenderer().setEdgeRenderer(new MyEdgeRenderer());
        vv.getRenderContext().setVertexLabelTransformer(new VertexLabelTransformer()); // render vertex label
        vv.getRenderContext().setEdgeLabelTransformer(new EdgeLabelTransformer()); // render edge label
        vv.getRenderContext().setVertexShapeTransformer(new VertexShapeTransformer(12)); // render the shape of the vertices
        vv.getRenderContext().setVertexFillPaintTransformer(new VertexPaintTransformer()); // controll the color of vertices
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line()); // render the border of vertices, because bends need no border
        vv.getRenderContext().setEdgeStrokeTransformer(new EdgeStrokeTransformer(1));
        vv.getRenderContext().setVertexFontTransformer(new VertexFontTransformer(new Font("Helvetica", Font.BOLD, 12)));
        vv.getRenderContext().setEdgeFontTransformer(new EdgeFontTransformer(new Font("Helvetica", Font.BOLD, 12)));
        vv.getRenderContext().setEdgeIncludePredicate(new EdgeIncludePredicate()); // controll the visibility of edges
        vv.getRenderContext().setVertexDrawPaintTransformer(new VertexDrawPaintTransformer()); // controll the color of the edges
        vv.getRenderContext().setVertexStrokeTransformer(new VertexStrokeTransformer());
        vv.getRenderContext().setEdgeDrawPaintTransformer(new EdgePaintTransformer(vv));
        vv.getRenderContext().setArrowDrawPaintTransformer(new EdgePaintTransformer(vv));
        vv.getRenderContext().setArrowFillPaintTransformer(new EdgePaintTransformer(vv));
        vv.getRenderContext().setEdgeArrowTransformer(new EdgeArrowTransformer(1)); // control the size of the arrows
        vv.getRenderer().setVertexLabelRenderer(new AdvancedVertexLabelRenderer());

        // Create the message label at the top of die vv
        messageLabel = new JLabel();
        vv.add(messageLabel);

        // Init all MouseListener and MousePlugins
        gpmp = new GraphPopupMousePlugin(this, this.synchronizer);
        gm = new NetViewerModalGraphMouse(gpmp, this); // Mouse for picking and transforming the graph
        gm.add(gpmp);
        vv.setGraphMouse(gm);
        mouseMode = true;

        mml = new NetViewerMouseListener(this, synchronizer); // Mouselistener for creating new places and transitions to model a Petri net
        vv.addMouseListener(mml);

        nvkl = new NetViewerKeyListener(this); // KeyListener for hotkeys
        vv.addKeyListener(nvkl);
        addKeyListener(nvkl);

        // If an vertex is selected, the MousePlugin must called for possible actions
        vv.getRenderContext().getPickedVertexState().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    gpmp.handlePicking((NetViewerNode) e.getItem());
                }
            }
        });

        // If an edge is selected, the MousePlugin must called for possible actions
        vv.getRenderContext().getPickedEdgeState().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    gpmp.handlePicking((NetViewerEdge) e.getItem());
                }
            }
        });

        vv.setBackground(BACKGROUND_COLOR);
        vv.repaint();
        LOGGER.debug("Finished initializing VisualizationViewer");
        // START --- GUI building section
        LOGGER.debug("Initializing NetViewer GUI");
        // Init MenuBar
        initMenuBar();

        // Create Frames like ToolBar etc and connect them with NetViewer
        initToolBar();

        infoBarLabel = new JLabel();
        infoBarLabel.setSize(new Dimension((int) nvDimension.getWidth(), 20));
        infoBarLabel.setPreferredSize(new Dimension((int) nvDimension.getWidth(), 20));

        cardLayout = new CardLayout();

        mainPanel = new JPanel();
        mainPanel.setLayout(cardLayout);
        mainPanel.add(vv, VVPANEL);

        mainSplitPane = new JSplitPane();
        mainSplitPane.setSize(nvDimension);
        mainSplitPane.setPreferredSize(nvDimension);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setVerifyInputWhenFocusTarget(false);

        mainSplitPane.setLeftComponent(mainPanel);
        mainSplitPane.setRightComponent(tb);

        Container contentPane = getContentPane();
        double sizeOfMainLayout[][]
                = {{TableLayout.FILL},
                {TableLayout.FILL, TableLayout.PREFERRED}};

        contentPane.setLayout(new TableLayout(sizeOfMainLayout));
        contentPane.setSize(nvDimension);
        contentPane.add(mainSplitPane, "0,0");
        contentPane.add(infoBarLabel, "0,1");

        pack();

        updateInfoBar();

        updateSearchBar(g.getVertices());
        markSelectedMouseMode(tb.mousePickingPanel);
        LOGGER.debug("Finished initializing GUI");
        // END --- GUI building section
        // If the Petri net is imported and contains information about the layout use this informations
        if (this.project.getPetriNet().hasProperty("new_imported")) {
            LOGGER.info("Using imported layout information if available");
            for (Place place : this.synchronizer.getPetriNet().places()) {
                if (place.hasProperty("posX")) {
                    layout.setLocation(getNodeFromVertex(place), new Point2D.Double((Double) place.getProperty("posX"), (Double) place.getProperty("posY")));
                }
            }

            for (Transition transition : this.synchronizer.getPetriNet().transitions()) {
                if (transition.hasProperty("posX")) {
                    layout.setLocation(getNodeFromVertex(transition), new Point2D.Double((Double) transition.getProperty("posX"), (Double) transition.getProperty("posY")));
                }
            }
            this.project.getPetriNet().removeProperty("new_imported");
        }

        // Imported from Spped?
        checkForSppedImport();

        netChanged = false;
        LOGGER.info("Finished initializing NetViewer frame");
    }

    /**
     * Which action is to perform?
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        final String action = e.getActionCommand();
        LOGGER.info("Performing action: '" + e.getActionCommand() + "'");
        switch (action) {
            case HIDE_COLOR:
                hideColor();
                break;
            case MAKE_PIC:
                try {
                makePic();
            } catch (IOException ex) {
                LOGGER.error("Issue while making picture: ", ex);
            }
            break;
            case CENTER_NET:
                center();
                break;
            case CTI_TRANSITIONS:
                colorCTITransitions();
                break;
            case BY_OCCURRENCE:
                colorImportantTransitions(true);
                break;
            case BY_FACTOR:
                colorImportantTransitions(false);
                break;
            case SHOW_LABELS:
                showLabels();
                break;
            case SHOW_COLOR_OPTION:
                showColorOptions();
                break;
            case RESET_COLORING:
                resetColor();
                break;
            case EXIT:
                exitNetViewer();
                break;
        }
    }

    public void addNetChangedListener(NetChangedListener ncl) {
        netChangedListener.add(ncl);
    }

    private void fireNetChangedEvent() {
        for (NetChangedListener ncl : netChangedListener) {
            ncl.netChanged();
        }
    }

    /**
     * Centers the Petri net in the VisualizationViewer
     */
    protected void center() {
        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).setToIdentity();
        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).setToIdentity();
        setZoomScale(100.0);
    }

    /**
     * Updates the InfoBar and the SearchBar and reset the PickedVertexState and
     * PickedEdgeState.
     */
    protected void nonModificationActionHappend() {
        LOGGER.info("Handling non-modification action");
        updateInfoBar();
        updateSearchBar(g.getVertices());
        vv.getPickedEdgeState().clear();
        vv.getPickedVertexState().clear();
        vv.repaint();
        LOGGER.info("Successfully handled non-modification action");
    }

    /**
     * Updates the InfoBar and the SearchBar and reset the PickedVertexState and
     * PickedEdgeState. Clears all calculated results and shows a message
     * regarding that.
     *
     */
    public void modificationActionHappend() {
        LOGGER.info("Handling modification action");
        netChanged = true;
        netChanged();
        updateInfoBar();
        updateSearchBar(g.getVertices());
        vv.getPickedEdgeState().clear();
        vv.getPickedVertexState().clear();
        vv.repaint();
        project.setProjectChanged(true);
        LOGGER.info("Successfully handled non-modification action");
    }

    /**
     * Save the current color settings
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void saveColorOptions(Map<String, Color> colorMap) throws FileNotFoundException, IOException {
        LOGGER.info("Saving color options");
        Settings.setColorOption("tinvColor", colorMap.get("tinvColor"));
        TINV_COLOR = colorMap.get("tinvColor");
        Settings.setColorOption("pinvColor", colorMap.get("pinvColor"));
        PINV_COLOR = colorMap.get("pinvColor");
        Settings.setColorOption("mctsColor", colorMap.get("mctsColor"));
        MCTS_COLOR = colorMap.get("mctsColor");
        Settings.setColorOption("heatMapColor", colorMap.get("heatMapColor"));
        HEATMAP_COLOR = colorMap.get("heatMapColor");
        Settings.setColorOption("knockedOutColor", colorMap.get("knockedOutColor"));
        KNOCKEDOUT_COLOR = colorMap.get("knockedOutColor");
        Settings.setColorOption("alsoKnockedOutColor", colorMap.get("alsoKnockedOutColor"));
        ALSOKNOCKEDOUT_COLOR = colorMap.get("alsoKnockedOutColor");
        Settings.setColorOption("notKnockedOutColor", colorMap.get("notKnockedOutColor"));
        NOTKNOCKEDOUTCOLOR = colorMap.get("notKnockedOutColor");
        Settings.setColorOption("backgroundColor", colorMap.get("backgroundColor"));
        BACKGROUND_COLOR = colorMap.get("backgroundColor");
        Settings.setColorOption("mcsObjectivColor", colorMap.get("mcsObjectivColor"));
        MCSOBJECTIV_COLOR = colorMap.get("mcsObjectivColor");
        Settings.setColorOption("mcsColor", colorMap.get("mcsColor"));
        MCS_COLOR = colorMap.get("mcsColor");

        Settings.writeToFile(System.getProperty("user.home") + "/.monalisaSettings");

        vv.setBackground(BACKGROUND_COLOR);
        vv.repaint();
        LOGGER.info("Successfully saved color options");
    }

    /**
     *
     * @param value
     */
    public void setNetChanged(Boolean value) {
        netChanged = value;
    }

    /**
     * Checks if the Petri net is changed and disable / enable regarding gui
     * elemets
     */
    public void netChanged() {
        if (netChanged) {
            LOGGER.debug("Petri net has changed");
            if (project.getToolManager().hasResults(new TInvariantTool()) || project.getToolManager().hasResults(new PInvariantTool()) || project.getToolManager().hasResults(new MctsTool())) {
                displayMessage(strings.get("NVNetChanged"), Color.RED);
            }
        }
        if (netChanged && !lastChanged) {
            LOGGER.debug("netChanged && !lastChanged");
            lastChanged = netChanged;

            if (project.getToolManager().hasResults(new TInvariantTool()) || project.getToolManager().hasResults(new PInvariantTool()) || project.getToolManager().hasResults(new MctsTool())) {
                displayMessage(strings.get("NVNetChanged"), Color.RED);

                tinvs = null;

                // clear all Display Lists and reset the Tab Titles
                for (DefaultListModel dl : tinvModelMap.keySet()) {
                    dl.clear();
                }

                tb.allInvList.clear();

                tb.CTILabel.setText("");

                minvs = null;
                tb.MinvList.clear();

                pinvs = null;
                tb.PinvList.clear();
                mctsResults = null;
                tb.mctsCb.removeAllItems();
                mcsResults = null;
                tb.mcsCb.removeAllItems();
                project.getToolManager().resetTools();
                mainDialog.updateAnalyzeFrame();

                tb.InvTabbedPane.setTitleAt(0, "T - Invariants");
                tb.InvTabbedPane.setTitleAt(1, "M - Invariants");
                tb.InvTabbedPane.setTitleAt(2, "P - Invariants");
            }
            fireNetChangedEvent();
        } else if (!netChanged && lastChanged) {
            LOGGER.debug("!netChanged && lastChanged");
            lastChanged = netChanged;

            resetMessageLabel();

            if (hasMcts()) {
                tb.mctsCb.setEnabled(true);
                tb.allMctsButton.setEnabled(true);
            }
            importantTransitonsMenu.setEnabled(true);
        }
        LOGGER.debug("Finished checking whether Petri net changed");
    }

    public Boolean hasNetChanged() {
        return netChanged;
    }

    /**
     * Shows the given String in the Message Label of the NetViewer in the given
     * color.
     *
     * @param text
     * @param color
     */
    public void displayMessage(String text, Color color) {
        messageLabel.setText(text);
        messageLabel.setForeground(color);
    }

    /**
     * Set the messageLabel to empty text and set color to black
     */
    public void resetMessageLabel() {
        displayMessage("", Color.BLACK);
    }

    /**
     * Sets the font size to a given value
     *
     * @param newFontSize
     */
    public void setFontSize(int newFontSize) {
        LOGGER.info("Setting new font size");
        if (this.tb != null) {
            if ((int) this.tb.fontSizeSpinner.getValue() != newFontSize) {
                this.tb.fontSizeSpinner.setValue(newFontSize);
                return;
            }
        }

        ((VertexFontTransformer) vv.getRenderContext().getVertexFontTransformer()).setFontSize(newFontSize);
        ((EdgeFontTransformer) vv.getRenderContext().getEdgeFontTransformer()).setFontSize(newFontSize);
        vv.repaint();
        LOGGER.info("Successfully set new font size");
    }

    /**
     * Sets the icon size to a given value
     *
     * @param newVertexSize
     */
    public void setIconSize(int newVertexSize) {
        LOGGER.info("Setting new icon size");
        if (this.tb != null) {
            if ((int) this.tb.iconSizeSpinner.getValue() != newVertexSize) {
                this.tb.iconSizeSpinner.setValue(newVertexSize);
                return;
            }
        }

        ((VertexShapeTransformer) vv.getRenderContext().getVertexShapeTransformer()).setSize(newVertexSize);
        vv.repaint();
        LOGGER.info("Successfully set new icon size");
    }

    /**
     * Sets the arrow size to a given value
     *
     * @param newArrowSize
     */
    public void setArrowSize(double newArrowSize) {
        LOGGER.info("Setting new arrow size");
        if (this.tb != null) {
            if ((double) this.tb.arrowSizeSpinner.getValue() != newArrowSize) {
                this.tb.arrowSizeSpinner.setValue(newArrowSize);
                return;
            }
        }

        ((EdgeArrowTransformer) vv.getRenderContext().getEdgeArrowTransformer()).setFactor(newArrowSize);
        vv.repaint();
        LOGGER.info("Successfully set new arrow size");
    }

    /**
     * Sets the edge size to a given value
     *
     * @param newEdgeSize
     */
    public void setEdgeSize(int newEdgeSize) {
        LOGGER.info("Setting new edge size");
        if (this.tb != null) {
            if ((int) this.tb.edgeSizeSpinner.getValue() != newEdgeSize) {
                this.tb.edgeSizeSpinner.setValue(newEdgeSize);
                return;
            }
        }

        ((EdgeStrokeTransformer) vv.getRenderContext().getEdgeStrokeTransformer()).setStrokeFactor(newEdgeSize);
        vv.repaint();
        LOGGER.info("Successfully set new edge size");
    }

    public double getArrowSize() {
        return ((EdgeArrowTransformer) vv.getRenderContext().getEdgeArrowTransformer()).getFactor();
    }

    public int getFontSize() {
        return ((EdgeFontTransformer) vv.getRenderContext().getEdgeFontTransformer()).getFontSize();
    }

    public int getIconSize() {
        return ((VertexShapeTransformer) vv.getRenderContext().getVertexShapeTransformer()).getSize();
    }

    public int getEdgeSize() {
        return ((EdgeStrokeTransformer) vv.getRenderContext().getEdgeStrokeTransformer()).getStrokeFactor();
    }

    /**
     * Zooms in or out by a given value.
     *
     * @param inOrOut
     * @param setSlider
     */
    protected void zommToValue(int inOrOut) {
        LOGGER.info("Changing zoom value");
        Point2D center = new Point(vv.getSize().height / 2, vv.getSize().width / 2);

        if (inOrOut > 0) {
            this.gm.getScalingPlugin().getScaler().scale(vv, 1.1F, center);
        } else if (inOrOut < 0) {
            this.gm.getScalingPlugin().getScaler().scale(vv, 0.9090909F, center);
        }

        double viewScale = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getScale();
        double layoutScale = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale();
        setZoomScale((viewScale * layoutScale) * 100);
        LOGGER.info("Successfully changed zoom value");
    }

    /**
     * Zooms to a given NetViewerNode
     *
     * @param nvNode
     */
    public void zoomToVertex(NetViewerNode nvNode) {
        LOGGER.info("Zooming to node");
        center();
        Dimension d = vv.getSize();
        Point2D viewCenter = new Point2D.Float(d.width / 2, d.height / 2);
        Point2D nodePosition = vv.getModel().getGraphLayout().transform(nvNode);
        viewCenter = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(viewCenter);
        double xdist = viewCenter.getX() - nodePosition.getX();
        double ydist = viewCenter.getY() - nodePosition.getY();
        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).translate(xdist, ydist);
        LOGGER.info("Successfully zoomed to node");
    }

    /**
     * Set the value of the JSPinner in the ToolBarFrame
     *
     * @param zoomScale
     */
    protected void setZoomScale(double zoomScale) {
        tb.setZoomSpinnerValue(new Double(zoomScale).intValue());
    }

    /**
     * Should the Invariants displayed as a heatmap?
     *
     * @return
     */
    protected Boolean heatMap() {
        if (heatMap) {
            heatMap = false;
            return heatMap;
        } else {
            heatMap = true;
            return heatMap;
        }
    }

    /**
     * true = picking, false = transforming
     *
     * @return
     */
    protected Boolean getMouseMode() {
        return mouseMode;
    }

    /**
     * Change mousemode to transforming
     */
    protected void changeMouseModeToTransforming() {
        LOGGER.info("Changing mouse mode to transforming");
        cancelMouseAction();
        mouseMode = false;
        markSelectedMouseMode(tb.mouseTransformingPanel);
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        netChanged();
        LOGGER.info("Successfully changed mouse mode to transforming");
    }

    /**
     * change mousemode to picking
     */
    protected void changeMouseModeToPicking() {
        LOGGER.info("Changing mouse mode to picking");
        cancelMouseAction();
        mouseMode = true;
        markSelectedMouseMode(tb.mousePickingPanel);
        gm.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        netChanged();
        LOGGER.info("Successfully changed mouse mode to picking");
    }

    /**
     * Returns the current project
     *
     * @return
     */
    public Project getProject() {
        return this.project;
    }

    /**
     * Runs all the given tools from the netviewer
     *
     * @param tools
     */
    public void calcTools(ArrayList<String> tools) {
        toolUI.runGivenTools(tools);
    }

    /**
     * Checks if the net is CTI and changes the Label in Toolbar correspondingly
     */
    public void checkCTI() {
        tinvs = getTInvs();
        int status = ((TInvariantTool) project.getToolManager().getTool(TInvariantTool.class)).isCTI(tinvs, project);
        switch (status) {
            case 1:
                tb.CTILabel.setText(strings.get("CTI"));
                tb.CTILabel.setForeground(new java.awt.Color(35, 132, 71));
                break;
            case 0:
                tb.CTILabel.setText(strings.get("NotCTI"));
                tb.CTILabel.setForeground(new java.awt.Color(215, 69, 19));
                break;
            case -1:
                tb.CTILabel.setText("");
                break;
            default:
                break;
        }
    }

    /**
     * Returns all t-invariants form loaded Petri net
     *
     * @return
     */
    private TInvariants getTInvs() {
        return project.getToolManager().getResult(TInvariantTool.class, new TInvariantsConfiguration());
    }

    private MInvariants getMInvs() {
        return project.getToolManager().getResult(MInvariantTool.class, new MInvariantsConfiguration());
    }

    /**
     * Returns all p-invariants form loaded Petri net
     *
     * @return
     */
    private PInvariants getPInvs() {
        return project.getToolManager().getResult(PInvariantTool.class, new PInvariantsConfiguration());
    }

    /**
     * Return all MC-sets from loaded Petri net
     *
     * @return
     */
    private Map<Configuration, Result> getMcsResults() {
        return project.getToolManager().getResults(McsTool.class);
    }

    /**
     * Return all MCT-sets from loaded Petri net
     *
     * @return
     */
    private Map<Configuration, Result> getMctsResults() {
        return project.getToolManager().getResults(MctsTool.class);
    }

    /**
     * Hide coloring of vertex
     *
     * @return
     */
    protected Boolean hideColor() {
        LOGGER.debug("Changing whether vertex color is hidden");
        VertexPaintTransformer vfpt = ((VertexPaintTransformer) vv.getRenderContext().getVertexDrawPaintTransformer());
        vfpt.setHideColor(!(vfpt.getHideColor()));
        EdgePaintTransformer edpt = ((EdgePaintTransformer) vv.getRenderContext().getEdgeDrawPaintTransformer());
        edpt.setHideColor(!(edpt.getHideColor()));

        if (!vfpt.getHideColor()) {
            colorItem.setText(strings.get("NVShowColorText"));
            vv.repaint();
            LOGGER.info("Hiding vertex color");
            return false;
        } else {
            colorItem.setText(strings.get("NVHideColorText"));
            vv.repaint();
            LOGGER.info("Showing vertex color");
            return true;
        }
    }

    /**
     * Hide or show all Labels
     *
     * @return
     */
    protected Boolean showLabels() {
        LOGGER.debug("Changing whether labels are shown or not");
        if (((VertexLabelTransformer) vv.getRenderContext().getVertexLabelTransformer()).showLabel()) {
            ((VertexLabelTransformer) vv.getRenderContext().getVertexLabelTransformer()).setShowLabel(false);
            labelItem.setText(strings.get("NVShowAllLabels"));

            vv.repaint();
            LOGGER.info("Hiding all labels");
            return false;
        } else {
            ((VertexLabelTransformer) vv.getRenderContext().getVertexLabelTransformer()).setShowLabel(true);
            labelItem.setText(strings.get("NVHideAllLabels"));

            vv.repaint();
            LOGGER.info("Showing all labels");
            return true;
        }
    }

    /**
     * Save the current layout in a PNG or SVG file
     *
     * @throws IOException
     */
    public void makePic() throws IOException {
        LOGGER.info("Saving current layout as .png or .svg file");
        messageLabel.setVisible(false);

        vv.getPickedVertexState().clear();
        vv.getPickedEdgeState().clear();

        MonaLisaFileFilter pngFilter = new MonaLisaFileFilter("png", "Portable Network Graphics");
        MonaLisaFileFilter svgFilter = new MonaLisaFileFilter("svg", "Support Vector Graphics");

        MonaLisaFileChooser imgPathChooser = new MonaLisaFileChooser();
        imgPathChooser.setDialogTitle(strings.get("NVImageLocation"));
        imgPathChooser.setAcceptAllFileFilterUsed(false);
        imgPathChooser.addChoosableFileFilter(pngFilter);
        imgPathChooser.addChoosableFileFilter(svgFilter);
        imgPathChooser.showSaveDialog(this);

        File imgFile = imgPathChooser.getSelectedFile();

        if (imgFile != null) {
            MonaLisaFileFilter selectedFileFilter = ((MonaLisaFileFilter) imgPathChooser.getFileFilter());

            // Are an ".png" or ".svg" at the end of the filename?
            imgFile = selectedFileFilter.checkFileNameForExtension(imgFile);

            if (selectedFileFilter.getExtension().equalsIgnoreCase("png")) {
                BufferedImage img = new BufferedImage(vv.getSize().width, vv.getSize().height, BufferedImage.TYPE_INT_RGB);
                vv.paintAll(img.getGraphics());
                ImageIO.write(img, "png", imgFile);
                LOGGER.info("Successfully saved current layout as .png");
            } else if (selectedFileFilter.getExtension().equalsIgnoreCase("svg")) {
                // Get a DOMImplementation
                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                // Create an instance of org.w3c.dom.Document
                Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
                // Create an instance of the SVG Generator
                SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
                vv.paint(svgGenerator);
                svgGenerator.stream(imgFile.getAbsolutePath());
                LOGGER.info("Successfully saved current layout as .svg");
            }
        }
        messageLabel.setVisible(true);
    }

    /**
     * Add the T-Invariants to the list display, if T-Invariants are there.
     */
    public void addTinvsToListDisplay() {
        if (tinvs == null) {
            tinvs = getTInvs();
        }
        if (tinvs == null) {
            return;
        }
        addTinvsToListDisplay(tinvs);
    }

    /**
     * Add the T-Invariants to the list display
     *
     * @param tinvs
     */
    protected void addTinvsToListDisplay(TInvariants tinvs) {
        if (tinvs == null) {
            return;
        }
        if (!tinvs.isEmpty()) {
            LOGGER.info("Adding T-Invariants to List Display");

            for (DefaultListModel dl : tinvModelMap.keySet()) {
                dl.clear();
            }
            minvs = getMInvs();
            System.out.println(minvs);
            System.out.println(tinvs);

            tb.allInvList.clear();

            Boolean input, output;
            int i, tinvSize;

            Iterator<Transition> it1;
            Transition t1;

            for (TInvariant tinv : tinvs) {
                tinvSize = tinv.size();
                if (tinvSize == 2) {
                    tb.trivialInvList.addElement(new TinvWrapper(tinv));
                    continue;
                }

                input = false;
                output = false;

                NetViewerNode tmp;
                it1 = tinv.transitions().iterator();
                while (it1.hasNext()) {
                    t1 = it1.next();

                    // Check for I/O TInvariant
                    tmp = getNodeFromVertex(t1);
                    if (tmp != null) {
                        if (getNodeFromVertex(t1).getInEdges().isEmpty()) {
                            input = true;
                        }
                        if (getNodeFromVertex(t1).getOutEdges().isEmpty()) {
                            output = true;
                        }
                    }
                }

                if (!output && !input) {
                    tb.cyclicInvList.addElement(new TinvWrapper(tinv));
                } else if (output && input) {
                    tb.ioInvList.addElement(new TinvWrapper(tinv));
                } else if (output && !input) {
                    tb.outputInvList.addElement(new TinvWrapper(tinv));
                } else if (input && !output) {
                    tb.inputInvList.addElement(new TinvWrapper(tinv));
                }
            }

            LOGGER.info("Generating combination of all T-Invariants for a List Display");
            // Create a T-invariant out of all t-invariantzs of a single list
            int itemCount, oldValue, counter;
            counter = 0;
            TInvariant tinvariant;
            Map<Transition, Integer> transitions;
            for (DefaultListModel dl : tinvModelMap.keySet()) {
                itemCount = dl.size();
                if (itemCount > 0) {
                    transitions = new HashMap<>();
                    for (i = 0; i < itemCount; i++) {
                        tinvariant = ((TinvWrapper) dl.getElementAt(i)).getTinv();
                        for (Transition transition : tinvariant) {
                            if (tinvariant.factor(transition) > 0) {
                                if (!transitions.containsKey(transition)) {
                                    transitions.put(transition, 0);
                                }
                                oldValue = transitions.get(transition);
                                transitions.put(transition, tinvariant.factor(transition) + oldValue);
                            }
                        }
                    }
                    String name = strings.get(tinvModelMap.get(dl), (dl.size() == 0) ? 0 : dl.size());
                    dl.insertElementAt(new TinvWrapper(new TInvariant(-1, transitions), name), 0);
                    counter++;
                }
            }
            if (!tb.ioInvList.isEmpty()) {
                tb.allInvList.addElement(strings.get("NVSeperator"));
                counter++;
                for (Object tw : tb.ioInvList.toArray()) {
                    tb.allInvList.addElement(tw);
                }
            }

            if (!tb.inputInvList.isEmpty()) {
                tb.allInvList.addElement(strings.get("NVSeperator"));
                counter++;
                for (Object tw : tb.inputInvList.toArray()) {
                    tb.allInvList.addElement(tw);
                }
            }

            if (!tb.outputInvList.isEmpty()) {
                tb.allInvList.addElement(strings.get("NVSeperator"));
                counter++;
                for (Object tw : tb.outputInvList.toArray()) {
                    tb.allInvList.addElement(tw);
                }
            }

            if (!tb.cyclicInvList.isEmpty()) {
                tb.allInvList.addElement(strings.get("NVSeperator"));
                counter++;
                for (Object tw : tb.cyclicInvList.toArray()) {
                    tb.allInvList.addElement(tw);
                }
            }

            if (!tb.trivialInvList.isEmpty()) {
                tb.allInvList.addElement(strings.get("NVSeperator"));
                counter++;
                for (Object tw : tb.trivialInvList.toArray()) {
                    tb.allInvList.addElement(tw);
                }
            }

            itemCount = tb.allInvList.size();
            if (itemCount > 0) {
                transitions = new HashMap<>();
                for (i = 0; i < itemCount; i++) {
                    if (tb.allInvList.getElementAt(i).getClass().equals(TinvWrapper.class)) {
                        tinvariant = ((TinvWrapper) tb.allInvList.getElementAt(i)).getTinv();
                        for (Transition transition : tinvariant) {
                            if (tinvariant.factor(transition) > 0) {
                                if (!transitions.containsKey(transition)) {
                                    transitions.put(transition, 0);
                                }
                                oldValue = transitions.get(transition);
                                transitions.put(transition, tinvariant.factor(transition) + oldValue);
                            }
                        }
                    }
                }
                String allTinv = strings.get("NVAllT", (tb.allInvList.size() == 0) ? 0 : tb.allInvList.size() - counter);
                tb.InvTabbedPane.setTitleAt(0, allTinv);

                TInvariant combined = new TInvariant(-1, transitions);
                String name = strings.get("NVCombinedT", (combined.isEmpty()) ? 0 : combined.size());
                tb.allInvList.insertElementAt(new TinvWrapper(combined, name), 0);
            }

            LOGGER.info("Successfully added T-Invariants to List Display");
        }
    }

    public void addMinvsToListDisplay() {
        LOGGER.info("Adding M-Invariants to List Display");
        if (minvs == null) {
            minvs = getMInvs();
        }
        if (minvs == null) {
            return;
        }
        if (!minvs.isEmpty()) {
            tb.MinvList.clear();

            for (MInvariant minv : minvs) {
                tb.MinvList.addElement(new MinvWrapper(minv));
            }
            /*
            // Create a M-invariant out of all m-invariants
            LOGGER.info("Generating combination of all M-Invariants");
            int itemCount, oldValue;
            PInvariant pinvariant;
            Map<Place, Integer> places;
            itemCount = tb.PinvList.size();
            if(itemCount > 0) {
                places = new HashMap<>();
                for(int i = 0; i < itemCount; i++) {
                    pinvariant = ((PinvWrapper)tb.PinvList.getElementAt(i)).getPinv();
                    for(Place place : pinvariant) {
                        if(pinvariant.factor(place) > 0) {
                            if(!places.containsKey(place))
                                places.put(place, 0);
                            oldValue = places.get(place);
                            places.put(place, pinvariant.factor(place)+oldValue);
                        }
                    }
                }
                tb.PinvList.insertElementAt(new PinvWrapper(new PInvariant(-1, places)), 0);
            }*/

            tb.InvTabbedPane.setTitleAt(1, strings.get("NVAllM", tb.MinvList.getSize()));
        }
        LOGGER.info("Successfully added M-Invariants to List Display");
    }

    /**
     * Add the P-Invariants to the combobox.
     */
    public void addPinvsToListDisplay() {
        LOGGER.info("Adding P-Invariants to List Display");
        if (pinvs == null) {
            pinvs = getPInvs();
        }
        if (pinvs == null) {
            return;
        }
        if (!pinvs.isEmpty()) {
            tb.PinvList.clear();

            for (PInvariant pinv : pinvs) {
                tb.PinvList.addElement(new PinvWrapper(pinv));
            }

            // Create a P-invariant out of all p-invariants
            LOGGER.info("Generating combination of all P-Invariants");
            int itemCount, oldValue;
            PInvariant pinvariant;
            Map<Place, Integer> places;
            itemCount = tb.PinvList.size();
            if (itemCount > 0) {
                places = new HashMap<>();
                for (int i = 0; i < itemCount; i++) {
                    pinvariant = ((PinvWrapper) tb.PinvList.getElementAt(i)).getPinv();
                    for (Place place : pinvariant) {
                        if (pinvariant.factor(place) > 0) {
                            if (!places.containsKey(place)) {
                                places.put(place, 0);
                            }
                            oldValue = places.get(place);
                            places.put(place, pinvariant.factor(place) + oldValue);
                        }
                    }
                }
                tb.PinvList.insertElementAt(new PinvWrapper(new PInvariant(-1, places)), 0);
            }

            tb.InvTabbedPane.setTitleAt(2, strings.get("NVAllP", tb.PinvList.getSize() - 1));
        }
        LOGGER.info("Successfully added P-Invariants to List Display");
    }

    /**
     * Adds the MC-Sets to the combobox.
     */
    public void addMcsToComboBox() {
        LOGGER.info("Adding MCS to ComboBox");
        if (mcsResults == null) {
            mcsResults = getMcsResults();
        }
        if (!mcsResults.isEmpty()) {
            tb.mcsCb.removeAllItems();
            tb.mcsCb.setEnabled(true);

            String[] configParts;
            String configName;
            McsConfiguration config;
            Mcs result;
            for (Entry<Configuration, Result> entry : mcsResults.entrySet()) {
                config = (McsConfiguration) entry.getKey();
                result = project.getToolManager().getResult(new McsTool(), config);
                if (result != null) {
                    configParts = config.toString().split("-");
                    configName = configParts[2] + " with max size of " + configParts[5];
                    tb.mcsCb.addItem(configName);

                    McsWrapper wrapper;
                    int i = 1;
                    for (Set<Transition> mcs : result.getMCS()) {
                        wrapper = new McsWrapper(mcs, config.getObjective(), i++);
                        if (!wrapper.getMcs().isEmpty()) {
                            tb.mcsCb.addItem(wrapper);
                        }
                    }
                }
            }
            tb.mcsCb.setSelectedIndex(0);
        }
        LOGGER.info("Successfully added MCS to ComboBox");
    }

    /**
     * Adds the MCTS-Sets to the combobox.
     */
    public void addMctsToComboBox() {
        LOGGER.info("Adding MCTS to ComboBox");
        if (mctsResults == null) {
            mctsResults = getMctsResults();
        }
        if (!mctsResults.isEmpty()) {
            tb.mctsCb.removeAllItems();
            tb.mctsCb.setEnabled(true);
            tb.allMctsButton.setEnabled(true);

            String[] configName;
            String withOutOrWith;
            Configuration config;
            Mcts results;
            for (Map.Entry<Configuration, Result> entry : mctsResults.entrySet()) {
                config = entry.getKey();
                results = project.getToolManager().getResult(new MctsTool(), config);
                if (results != null) {
                    configName = config.toString(strings).split(" ");
                    if (configName[2].contains("with")) {
                        withOutOrWith = configName[2];
                    } else {
                        withOutOrWith = configName[3];
                    }
                    tb.mctsCb.addItem(configName[0].split("-")[0] + " (" + withOutOrWith + " trivial T-inv)" + " - Sum: " + results.size());
                    Iterator it = results.iterator();
                    MctsWrapper wrapper;
                    while (it.hasNext()) {
                        wrapper = new MctsWrapper((TInvariant) it.next());
                        if (!wrapper.getMcts().isEmpty()) {
                            tb.mctsCb.addItem(wrapper);
                        }
                    }
                }
            }
            tb.mctsCb.setSelectedIndex(0);
        }
        LOGGER.info("Successfully added MCTS to ComboBox");
    }

    /**
     * Reset the coloring of all vertex to default.
     */
    public void resetColor() {
        LOGGER.info("Resetting color of all vertices to default");
        for (NetViewerNode n : g.getVertices()) {
            if (n.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
                n.setColor(Color.BLACK);
            }
            if (n.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
                if (n.isLogical()) {
                    n.setColor(Color.LIGHT_GRAY);
                } else if (n.isMasterNode()) {
                    n.setColor(Color.DARK_GRAY);
                } else {
                    n.setColor(Color.WHITE);
                }
            }
        }
        for (NetViewerEdge e : g.getEdges()) {
            if (e.getVisible()) {
                e.setColorForAllEdges(Color.BLACK);
            }
        }

        vv.repaint();
        LOGGER.info("Successfully set color of all vertices to default");
    }

    /**
     * Add or delete (or update) a node to the search bar
     *
     * @param vertices
     */
    protected void updateSearchBar(Collection<NetViewerNode> vertices) {
        LOGGER.info("Updating search bar");
        List<NetViewerNode> places = new ArrayList<>();
        List<NetViewerNode> transitions = new ArrayList<>();

        List<String> allPlaceNames = new ArrayList<>();
        List<String> doublePlaceNames = new ArrayList<>();
        List<String> allTransitionNames = new ArrayList<>();
        List<String> doubleTransitionNames = new ArrayList<>();

        for (NetViewerNode nvNode : vertices) {
            if (!nvNode.isLogical() && !nvNode.getNodeType().equals(BEND)) {
                if (nvNode.getNodeType().equals(PLACE)) {
                    places.add(nvNode);
                    if (!allPlaceNames.contains(nvNode.getName())) {
                        allPlaceNames.add(nvNode.getName());
                    } else {
                        doublePlaceNames.add(nvNode.getName());
                    }
                } else {
                    transitions.add(nvNode);
                    if (!allTransitionNames.contains(nvNode.getName())) {
                        allTransitionNames.add(nvNode.getName());
                    } else {
                        doubleTransitionNames.add(nvNode.getName());
                    }
                }
            }
        }

        Collections.sort(places, new NetViewerStringComparator());
        Collections.sort(transitions, new NetViewerStringComparator());

        allPlacesModel.clear();
        allTransitionsModel.clear();

        Boolean doublePlaces = false, doubleTransitions = false;

        for (NetViewerNode nvNode : places) {
            allPlacesModel.addElement(nvNode);
            if (doublePlaceNames.contains(nvNode.getName())) {
                nvNode.setSearchBarColor(Color.RED);
                doublePlaces = true;
            } else {
                nvNode.setSearchBarColor(Color.BLACK);
            }
        }

        if (doublePlaces) {
            sb.doublePlacesWarning.setText(strings.get("NVWarningDoublePlaceNames"));
            displayMessage("There are places or transitions with the same name. The SearchBar provides more informationen", Color.RED);
        } else {
            sb.doublePlacesWarning.setText("");
        }

        for (NetViewerNode nvNode : transitions) {
            allTransitionsModel.addElement(nvNode);
            if (doubleTransitionNames.contains(nvNode.getName())) {
                nvNode.setSearchBarColor(Color.RED);
                doubleTransitions = true;
            } else {
                nvNode.setSearchBarColor(Color.BLACK);
            }
        }

        if (doubleTransitions) {
            sb.doubleTransitionsWarning.setText(strings.get("NVWarningDoubleTransitionNames"));
            displayMessage("There are places or transitions with the same name. The SearchBar provides more informationen", Color.RED);
        } else {
            sb.doubleTransitionsWarning.setText("");
        }

        sb.placesLabel.setText(strings.get("NVPlaces") + " (" + allPlacesModel.getSize() + ")");
        sb.transitionsLabel.setText(strings.get("NVTransitions") + " (" + allTransitionsModel.getSize() + ")");
        LOGGER.info("Finished updating search bar");
    }

    /**
     * Updates the number of places, transitions, and edges in the InfoBar
     */
    private void updateInfoBar() {
        LOGGER.info("Updating number of places, transitions and edges in InfoBar");
        int placeCounter = synchronizer.getPetriNet().places().size();
        int transitionCounter = synchronizer.getPetriNet().transitions().size();
        int edgeCounter = 0;

        for (Transition transition : synchronizer.getPetriNet().transitions()) {
            edgeCounter += transition.inputs().size();
            edgeCounter += transition.outputs().size();
        }
        infoBarLabel.setText(" Places: " + placeCounter + " | Transitions: " + transitionCounter + " | Edges: " + edgeCounter);
        LOGGER.info("Successfully updated number of places, tranitions and edges in InfoBar");
    }

    /**
     * Are T-Invariants loaded?
     *
     * @return
     */
    public boolean hasTinvs() {
        return tb.allInvList.size() != 0;
    }

    /**
     * Are MCT-Sets loaded?
     *
     * @return
     */
    public boolean hasMcts() {
        return tb.mctsCb.getItemCount() != 0;
    }

    /**
     * Write the given properties if the NetViewerNode and to the
     * place/transition.This function is used, if a MultiSelection of vertices
     * is edited.
     *
     * @param nvNode
     * @param lablePosition
     * @param compartment
     * @param color
     * @param strokeColor
     */
    public void writeVertexSetup(NetViewerNode nvNode, Position lablePosition, Compartment compartment, Color color, Color strokeColor) {
        LOGGER.info("Multiple vertices edited, updating setup");
        nvNode.getMasterNode().setColorForAllNodes(color);
        nvNode.getMasterNode().setStrokeColorForAllNodes(strokeColor);
        nvNode.setLabelPosition(lablePosition);

        if (compartment != null) {
            nvNode.putProperty("compartment", compartment);

            if (nvNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
                Place place = synchronizer.getPetriNet().findPlace(nvNode.getMasterNode().getId());
                place.setCompartment(compartment);
            } else if (nvNode.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
                Transition transition = synchronizer.getPetriNet().findTransition(nvNode.getId());
                transition.setCompartment(compartment);
            }
        }

        vv.repaint();
        updateSearchBar(g.getVertices());
        project.setProjectChanged(true);
        LOGGER.info("Successfully updated setup for multiple vertices");
    }

    /**
     * Write the given properties the NetViewerNode and to the
     * place/transition.This function is used, if only a single vertex is
     * edited.
     *
     * @param nvNode
     * @param color
     * @param strokeColor
     * @param corners
     * @param name
     * @param tokens
     * @param toolTip
     * @param lablePosition
     * @param compartment
     * @return
     */
    public boolean writeVertexSetup(NetViewerNode nvNode, Color color, Color strokeColor, int corners, String name, Long tokens, String toolTip, Position lablePosition, Compartment compartment) {
        LOGGER.info("Single vertex edited, updating setup");
        if (!name.equals(nvNode.getName())) {
            if (nvNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
                for (Place p : this.project.getPetriNet().places()) {
                    if (name.equals(p.getProperty("name"))) {
                        return false;
                    }
                }
            } else if (nvNode.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
                for (Transition t : this.project.getPetriNet().transitions()) {
                    if (name.equals(t.getProperty("name"))) {
                        return false;
                    }
                }
            }
        }

        if (nvNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
            nvNode.getMasterNode().setTokensForAllNodes(tokens);
        }

        nvNode.getMasterNode().setColorForAllNodes(color);
        nvNode.getMasterNode().setStrokeColorForAllNodes(strokeColor);
        nvNode.getMasterNode().setCornersForAllNodes(corners);
        nvNode.getMasterNode().setNameForAllNodes(name);

        if (compartment != null) {
            nvNode.putProperty("compartment", compartment);
        }

        nvNode.setLabelPosition(lablePosition);

        if (!toolTip.isEmpty()) {
            nvNode.putProperty("toolTip", toolTip);
        }

        if (nvNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
            Place place = synchronizer.getPetriNet().findPlace(nvNode.getMasterNode().getId());
            place.putProperty("name", name);
            project.getPetriNet().setTokens(place, tokens);
            if (compartment != null) {
                place.setCompartment(compartment);
            }
            if (!toolTip.isEmpty()) {
                place.putProperty("toolTip", toolTip);
            }
        } else if (nvNode.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
            Transition transition = synchronizer.getPetriNet().findTransition(nvNode.getId());
            transition.putProperty("name", name);
            if (compartment != null) {
                transition.setCompartment(compartment);
            }
            if (!toolTip.isEmpty()) {
                transition.putProperty("toolTip", toolTip);
            }
        }

        vv.repaint();
        updateSearchBar(g.getVertices());
        project.setProjectChanged(true);
        LOGGER.info("Successfully updated setup for single vertex");
        return true;
    }

    /**
     * Write the given properties the NetViewerEdge and to the arc.
     *
     * @param nvEdge
     * @param weight
     * @param color
     * @param toolTip
     */
    public void writeEdgeSetup(NetViewerEdge nvEdge, int weight, Color color, String toolTip) {
        LOGGER.info("Edge edited, updating setup");
        nvEdge.setWeightForAllEdges(weight);
        nvEdge.setColorForAllEdges(color);

        if (nvEdge.getMasterEdge().getSource().getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
            Place from = synchronizer.getPetriNet().findPlace(nvEdge.getMasterEdge().getSource().getMasterNode().getId());
            Transition to = synchronizer.getPetriNet().findTransition(nvEdge.getMasterEdge().getAim().getId());
            synchronizer.getPetriNet().getArc(from, to).putProperty("weight", weight);

            if (!toolTip.isEmpty()) {
                synchronizer.getPetriNet().getArc(from, to).putProperty("toolTip", toolTip);
            }
        } else if (nvEdge.getMasterEdge().getSource().getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
            Transition from = synchronizer.getPetriNet().findTransition(nvEdge.getMasterEdge().getSource().getId());
            Place to = synchronizer.getPetriNet().findPlace(nvEdge.getMasterEdge().getAim().getMasterNode().getId());
            synchronizer.getPetriNet().getArc(from, to).putProperty("weight", weight);

            if (!toolTip.isEmpty()) {
                synchronizer.getPetriNet().getArc(from, to).putProperty("toolTip", toolTip);
            }
        }

        if (!toolTip.isEmpty()) {
            nvEdge.putProperty("toolTip", toolTip);
        }

        vv.repaint();
        project.setProjectChanged(true);
        LOGGER.info("Successfully updated setup for edge");
    }

    /**
     * Calculates a new point, 150 pixel around the given point
     *
     * @param oldPoint
     * @return
     */
    protected Point2D calcNewPoint(Point2D oldPoint) {
        Point.Double newPoint = new Point.Double();
        newPoint.x = oldPoint.getX() + 30;
        newPoint.y = oldPoint.getY() + 30;
        return newPoint;
    }

    /**
     * Colors all transitions which occurs in one or more T-Invariants.
     */
    private void colorCTITransitions() {
        LOGGER.info("Coloring CTI-Transitions");
        if (tinvs != null) {
            resetColor();

            Iterator<Transition> it;
            for (TInvariant tinv : tinvs) {
                it = tinv.transitions().iterator();
                while (it.hasNext()) {
                    getNodeFromVertex(it.next()).setColor(Color.YELLOW);
                }
            }
        }
        vv.repaint();
        LOGGER.info("Successfully colored CTI-Transitions");
    }

    /**
     * Counts occurrence of transitions in T-Invariants and generate a heatmap.
     */
    private void colorImportantTransitions(Boolean byOccurrence) {
        if (tinvs != null) {
            LOGGER.info("Coloring important transitions");
            float min = 0, max = 0, maxmin;
            float[] hsbvals = Color.RGBtoHSB(HEATMAP_COLOR.getRed(), HEATMAP_COLOR.getGreen(), HEATMAP_COLOR.getBlue(), null);
            float norm;
            Map<Transition, Integer> allFactors = new HashMap<>();

            resetColor();

            Iterator<Transition> it;
            Transition t;
            for (TInvariant tinv : tinvs) {
                it = tinv.transitions().iterator();
                while (it.hasNext()) {
                    t = it.next();
                    if (!allFactors.containsKey(t)) {
                        allFactors.put(t, 0);
                    }
                    if (byOccurrence) {
                        allFactors.put(t, allFactors.get(t) + 1);
                    } else {
                        allFactors.put(t, allFactors.get(t) + tinv.factor(t));
                    }
                }
            }

            Iterator<Integer> itInt = allFactors.values().iterator();
            int value;
            while (itInt.hasNext()) {
                value = itInt.next();
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
            maxmin = max - min;

            displayMessage(strings.get("NVImportantTransitionMessage", (int) min, (int) max), Color.BLACK);

            Iterator<Entry<Transition, Integer>> itEntry = allFactors.entrySet().iterator();
            Entry<Transition, Integer> entry;
            while (itEntry.hasNext()) {
                entry = itEntry.next();
                norm = (entry.getValue() - min) / maxmin;
                if (norm < 0.05) {
                    norm = (float) 0.05;
                }
                getNodeFromVertex(entry.getKey()).setColor(new Color(Color.HSBtoRGB(hsbvals[0], norm, hsbvals[2])));
            }
            vv.repaint();
            LOGGER.info("Successfully colored important transitions");
        }
    }

    /**
     * Hide label from a given node
     *
     * @param nvNode
     */
    protected void hideLabel(NetViewerNode nvNode) {
        nvNode.setShowLabel(!nvNode.showLabel());
        vv.repaint();
    }

    /**
     * Knock out a transition and show the new Petri net
     *
     * @param nvNodes
     * @param nvNode
     * @param mode 0=Standart, 1=heatmap by occurrence
     */
    public void knockOut(List<NetViewerNode> nvNodes, int mode) {
        LOGGER.info("Updating visualization to reflect knockouts");
        List<TInvariant> notKnockedOutTinvariants = new ArrayList<>();
        List<Transition> notKnockedOutTransitions = new ArrayList<>();
        Map<Transition, Integer> factors = new HashMap<>();
        int factor;
        float min, max, maxmin, norm;
        double percentTransitions, percentInvariants;
        float[] hsbvals = Color.RGBtoHSB(NOTKNOCKEDOUTCOLOR.getRed(), NOTKNOCKEDOUTCOLOR.getGreen(), NOTKNOCKEDOUTCOLOR.getBlue(), null);
        Boolean isKnockedOut;

        // Check which Invariants are knocked out
        for (TInvariant inv : tinvs) {
            isKnockedOut = false;
            for (NetViewerNode nvNode : nvNodes) {
                if (inv.factor(synchronizer.getPetriNet().findTransition(nvNode.getId())) > 0) {
                    isKnockedOut = true;
                    break;
                }
            }
            if (!isKnockedOut) {
                notKnockedOutTinvariants.add(inv);
            }
        }

        for (Transition t : synchronizer.getPetriNet().transitions()) {
            factors.put(t, 0);
        }

        for (TInvariant inv : notKnockedOutTinvariants) {
            for (Transition t : inv) {
                factor = inv.factor(t);
                if (factor > 0) {
                    if (!notKnockedOutTransitions.contains(t)) {
                        notKnockedOutTransitions.add(t);
                    }

                    factors.put(t, factors.get(t) + 1);

                }
            }
        }

        min = (float) Collections.min(factors.values());
        max = (float) Collections.max(factors.values());
        maxmin = max - min;

        percentTransitions = Math.round(100.0 - Math.round((((double) notKnockedOutTransitions.size() / (double) synchronizer.getPetriNet().transitions().size()) * 100) * 100) / 100.0);
        percentInvariants = Math.round(100.0 - Math.round(((double) notKnockedOutTinvariants.size() / (double) tinvs.size()) * 100 * 100) / 100.0);

        resetColor();

        for (Transition t : synchronizer.getPetriNet().transitions()) {
            if (notKnockedOutTransitions.contains(t)) {
                if (mode != 0) {
                    norm = (factors.get(t) - min) / maxmin;
                    if (norm < 0.05) {
                        norm = (float) 0.05;
                    }
                    getNodeFromVertex(t).setColor(new Color(Color.HSBtoRGB(hsbvals[0], norm, hsbvals[2])));
                } else {
                    getNodeFromVertex(t).setColor(NOTKNOCKEDOUTCOLOR);
                }
            } else {
                getNodeFromVertex(t).setColor(ALSOKNOCKEDOUT_COLOR);
            }
        }

        String knockOutString = strings.get("NVKnockOutMessage", percentInvariants, percentTransitions);
        String heatMapString = (mode != 0) ? "<br>" + strings.get("NVImportantTransitionMessage", (int) min, (int) max) : "";
        displayMessage("<html>" + knockOutString + "<br />" + heatMapString + "</html>", Color.BLACK);

        for (NetViewerNode nvNode : nvNodes) {
            getTransitionMap().get(nvNode.getId()).setColor(KNOCKEDOUT_COLOR);
        }

        vv.repaint();
        LOGGER.info("Successfully updated visualization to reflect knockouts");
    }

    /**
     * For Manatees
     */
    public void knockOut(List<NetViewerNode> nvNodes, TInvariants tinvs) {
        LOGGER.info("Updating visualization to reflect knockouts - manatee variant");
        List<TInvariant> notKnockedOutTinvariants = new ArrayList<>();
        List<Transition> notKnockedOutTransitions = new ArrayList<>();
        Map<Transition, Integer> factors = new HashMap<>();
        int factor;
        float min, max, maxmin, norm;
        double percentTransitions, percentInvariants;
        float[] hsbvals = Color.RGBtoHSB(NOTKNOCKEDOUTCOLOR.getRed(), NOTKNOCKEDOUTCOLOR.getGreen(), NOTKNOCKEDOUTCOLOR.getBlue(), null);
        Boolean isKnockedOut;

        // Check which Invariants are knocked out
        for (TInvariant inv : tinvs) {
            isKnockedOut = false;
            for (NetViewerNode nvNode : nvNodes) {
                if (inv.factor(synchronizer.getPetriNet().findTransition(nvNode.getId())) > 0) {
                    isKnockedOut = true;
                    break;
                }
            }
            if (!isKnockedOut) {
                notKnockedOutTinvariants.add(inv);
            }
        }

        for (Transition t : synchronizer.getPetriNet().transitions()) {
            factors.put(t, 0);
        }

        for (TInvariant inv : notKnockedOutTinvariants) {
            for (Transition t : inv) {
                factor = inv.factor(t);
                if (factor > 0) {
                    if (!notKnockedOutTransitions.contains(t)) {
                        notKnockedOutTransitions.add(t);
                    }

                    factors.put(t, factors.get(t) + 1);

                }
            }
        }

        min = (float) Collections.min(factors.values());
        max = (float) Collections.max(factors.values());
        maxmin = max - min;

        percentTransitions = Math.round(100.0 - Math.round((((double) notKnockedOutTransitions.size() / (double) synchronizer.getPetriNet().transitions().size()) * 100) * 100) / 100.0);
        percentInvariants = Math.round(100.0 - Math.round(((double) notKnockedOutTinvariants.size() / (double) tinvs.size()) * 100 * 100) / 100.0);

        resetColor();

        for (Transition t : synchronizer.getPetriNet().transitions()) {
            if (notKnockedOutTransitions.contains(t)) {
                getNodeFromVertex(t).setColor(NOTKNOCKEDOUTCOLOR);
            } else {
                getNodeFromVertex(t).setColor(ALSOKNOCKEDOUT_COLOR);
            }
        }

        for (NetViewerNode nvNode : nvNodes) {
            getTransitionMap().get(nvNode.getId()).setColor(KNOCKEDOUT_COLOR);
        }

        vv.repaint();
        LOGGER.info("Successfully updated visualization to reflect knockouts - manatee variant");
    }

    protected void resetKnockOut() {
        LOGGER.info("Resetting Knockouts");
        resetColor();
        resetMessageLabel();
    }

    /**
     * Action on adding a new inVertex.
     */
    protected void inVertexMouseAction() {
        LOGGER.info("Adding new inVertex");
        changeMouseModeToPicking();
        vv.getPickedVertexState().clear();
        markSelectedMouseMode(tb.inEdgePanel);
        displayMessage(strings.get("NVInsertInVertexMessage"), Color.BLACK);
        gpmp.setMouseModeToInVertex();
        mouseMode = false;
    }

    /**
     * Action on adding a new outVertex.
     */
    protected void outVertexMouseAction() {
        LOGGER.info("Adding new outVertex");
        changeMouseModeToPicking();
        vv.getPickedVertexState().clear();
        markSelectedMouseMode(tb.outEdgePanel);
        displayMessage(strings.get("NVInsertOutVertexMessage"), Color.BLACK);
        gpmp.setMouseModeToOutVertex();
        mouseMode = false;
    }

    /**
     * Action on adding a new edge.
     */
    protected void edgeMouseAction() {
        vv.getPickedVertexState().clear();
        // Adding only one edge
        if (gpmp.getMouseMode().equalsIgnoreCase(GraphPopupMousePlugin.SINGLE_EDGE)) {
            LOGGER.info("Adding new edge");
            cancelMouseAction();
        } // or more edges?
        else {
            LOGGER.info("Adding new edges");
            changeMouseModeToPicking();
            markSelectedMouseMode(tb.addEdgePanel);
            displayMessage(strings.get("NVInsertDoubleEdgeMessage"), Color.BLACK);
            gpmp.setMouseModeToDoubleEdge();
        }
        mouseMode = false;
    }

    /**
     * Action on adding a new place.
     */
    protected void placeMouseAction() {
        LOGGER.info("Adding new place");
        changeMouseModeToPicking();
        vv.getPickedVertexState().clear();
        mml.setMouseModeToPlace();
        markSelectedMouseMode(tb.addPlacePanel);
        displayMessage(strings.get("NVPlaceMessage"), Color.BLACK);
        mouseMode = false;
    }

    /**
     * Action on adding a new transition.
     */
    protected void transitionMouseAction() {
        LOGGER.info("Adding new transition");
        changeMouseModeToPicking();
        vv.getPickedVertexState().clear();
        mml.setMouseModeToTransition();
        markSelectedMouseMode(tb.addTransitionPanel);
        displayMessage(strings.get("NVTransitionMessage"), Color.BLACK);
        mouseMode = false;
    }

    /**
     * Action on remove an edge.
     */
    protected void deleteMouseAction() {
        LOGGER.info("Deleting edge");
        changeMouseModeToPicking();
        vv.getPickedEdgeState().clear();
        vv.getPickedVertexState().clear();
        gpmp.setMouseModeToDelete();
        markSelectedMouseMode(tb.deletePanel);
        displayMessage(strings.get("NVDeleteMessage"), Color.BLACK);
        mouseMode = false;
    }

    /**
     * Action on adding a bend to an edge.
     */
    protected void addBendMouseAction() {
        LOGGER.info("Adding bend to edge");
        changeMouseModeToPicking();
        vv.getPickedEdgeState().clear();
        gpmp.setMouseModeToAddBend();
        markSelectedMouseMode(tb.addBendPanel);
        displayMessage(strings.get("NVAddBendMessage"), Color.BLACK);
        mouseMode = false;
    }

    /**
     * Action on removing a bend from an edge.
     */
    protected void deleteBendMouseAction() {
        LOGGER.info("Removing bend from edge");
        changeMouseModeToPicking();
        vv.getPickedEdgeState().clear();
        gpmp.setMouseModeToDeleteBend();
        markSelectedMouseMode(tb.removeBendPanel);
        displayMessage(strings.get("NVDeleteBendMessage"), Color.BLACK);
        mouseMode = false;
    }

    /**
     * Action on aligning vertices on x-axis.
     */
    protected void alignXMouseAction() {
        LOGGER.info("Aligning vertices on X-axis");
        changeMouseModeToPicking();
        gpmp.setMouseModeToAlignX();
        markSelectedMouseMode(tb.allignXPanel);
        saveSelectedVertices();
        displayMessage(strings.get("NVAlignmentText"), Color.BLACK);
        mouseMode = false;
    }

    /**
     * * Action on aligning vertices on y-axis.
     */
    protected void alignYMouseAction() {
        LOGGER.info("Aligning vertices on Y-axis");
        changeMouseModeToPicking();
        gpmp.setMouseModeToAlignY();
        markSelectedMouseMode(tb.allignYPanel);
        saveSelectedVertices();
        displayMessage(strings.get("NVAlignmentText"), Color.BLACK);
        mouseMode = false;
    }

    /**
     * Exports a selected sub-graph to a file.
     *
     * @throws FileNotFoundException
     */
    protected void exportSelectedSubGraphMouseAction() throws FileNotFoundException {
        MonaLisaFileChooser petriNetFileChooser = new MonaLisaFileChooser();

        petriNetFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        petriNetFileChooser.setDialogTitle(strings.get("ChooseAPetriNetFileName"));
        petriNetFileChooser.setAcceptAllFileFilterUsed(false);

        for (final OutputHandler handler : PetriNetOutputHandlers.getHandlers()) {
            petriNetFileChooser.addChoosableFileFilter(new OutputFileFilter(handler));
        }

        if (petriNetFileChooser.showDialog(this, "Export") != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File petriNetFile = petriNetFileChooser.getSelectedFile();
        OutputFileFilter selectedFileFilter = ((OutputFileFilter) petriNetFileChooser.getFileFilter());
        petriNetFile = selectedFileFilter.checkFileNameForExtension(petriNetFile);
        LOGGER.info("Exporting selected sub-graph to file: " + petriNetFile.getAbsolutePath());
        exportSubGraph(petriNetFile, selectedFileFilter.getHandler(), synchronizer, vv.getRenderContext().getPickedVertexState().getPicked());
        LOGGER.info("Successfully exported selected sub-graph to file: " + petriNetFile.getAbsolutePath());
    }

    private void exportSubGraph(File petriNetFile, OutputHandler handler, Synchronizer sync, Set<NetViewerNode> picked) throws FileNotFoundException {
        Set<Place> places = new HashSet<>();
        Set<Transition> transitions = new HashSet<>();
        Set<Arc> arcs = new HashSet<>();
        for (NetViewerNode n : picked) {
            if (n.getNodeType().equalsIgnoreCase("PLACE")) {
                places.add(sync.getPetriNet().findPlace(n.getMasterNode().getId()));
            } else if (n.getNodeType().equalsIgnoreCase("TRANSITION")) {
                transitions.add(sync.getPetriNet().findTransition(n.getMasterNode().getId()));
            }
        }
        for (Place p : places) {
            for (Transition t : transitions) {
                if (sync.getPetriNet().getArc(p, t) != null) {
                    arcs.add(sync.getPetriNet().getArc(p, t));
                }
            }
        }
        for (Transition t : transitions) {
            for (Place p : places) {
                if (sync.getPetriNet().getArc(t, p) != null) {
                    arcs.add(sync.getPetriNet().getArc(t, p));
                }
            }
        }
        handler.save(new FileOutputStream(petriNetFile), sync.getSubNetwork(places, transitions, arcs));
    }

    /**
     * Cancel mouse action and set mouse to picking mode.
     */
    protected void cancelMouseAction() {
        LOGGER.info("Canceling mouse action and setting to picking mode");
        gpmp.setMouseModeToNormal();
        mml.setMouseModeToNormal();
        markSelectedMouseMode(tb.mousePickingPanel);
        resetMessageLabel();
        netChanged();
        mouseMode = true;
    }

    /**
     * Marks the button of the current MouseMode
     *
     * @param activatedPanel
     */
    private void markSelectedMouseMode(JPanel activatedPanel) {
        LOGGER.debug("Marking button of current MouseMode");
        for (JPanel p : mouseModePanels) {
            if (p.equals(activatedPanel)) {
                p.setBackground(Color.RED);
            } else {
                p.setBackground(defaultPanelColor);
            }
        }
        LOGGER.debug("Successfully marked button of current MouseMode");
    }

    /**
     * Save properties of a set of vertices.
     */
    private void saveSelectedVertices() {
        LOGGER.info("Saving properties of selected vertices");
        if (vv.getRenderContext().getPickedVertexState().getPicked().isEmpty()) {
            return;
        }
        alignmentList.clear();
        for (NetViewerNode nvNode : vv.getRenderContext().getPickedVertexState().getPicked()) {
            alignmentList.add(nvNode);
        }

        vv.getRenderContext().getPickedVertexState().clear();
        LOGGER.info("Successfully saved properties of selected vertices");
    }

    /**
     * Aligns vertices to a given vertex
     *
     * @param pos x or y axis
     * @param nodeToAlign
     */
    protected void alignVertices(String pos, NetViewerNode nodeToAlign) {
        LOGGER.info("Aligning vertices to a given vertex");
        int newX = (int) layout.transform(nodeToAlign).getX();
        int newY = (int) layout.transform(nodeToAlign).getY();

        if (pos.equalsIgnoreCase("X")) {
            for (NetViewerNode nvNode : alignmentList) {
                layout.setLocation(nvNode, new Point(newX, (int) layout.transform(nvNode).getY()));
            }
        } else if (pos.equalsIgnoreCase("Y")) {
            for (NetViewerNode nvNode : alignmentList) {
                layout.setLocation(nvNode, new Point((int) layout.transform(nvNode).getX(), newY));
            }
        }
        cancelMouseAction();
        resetMessageLabel();
        vv.repaint();
        project.setProjectChanged(true);
        LOGGER.info("Successfully aligned vertices to a given vertex");
    }

    /**
     * Removes a set of Vertices.
     */
    protected void removeSelectedVertices() {
        LOGGER.info("Removing set of vertices");
        cancelMouseAction();

        for (NetViewerNode nvNode : vv.getRenderContext().getPickedVertexState().getPicked()) {
            removeNode(nvNode);
        }

        for (NetViewerEdge nvEdge : vv.getRenderContext().getPickedEdgeState().getPicked()) {
            removeEdge(nvEdge);
        }

        netChanged = true;
        netChanged();

        vv.getPickedVertexState().clear();

        updateSearchBar(g.getVertices());
        updateInfoBar();
        vv.repaint();
        project.setProjectChanged(true);
        LOGGER.info("Successfuly removed set of vertices");
    }

    /**
     * Colors a given set of transitions with a given color
     *
     * @param mcts
     * @param color
     */
    public void colorTransitions(Set<Transition> transitions, Color color) {
        LOGGER.info("Coloring transitions of a given set");
        Iterator<Transition> it = transitions.iterator();
        while (it.hasNext()) {
            getNodeFromVertex(it.next()).setColor(color);
        }
        vv.repaint();
        LOGGER.info("Successfully colored transitions of a given set");
    }

    public void colorTransitions(Color color, Transition... transitions) {
        LOGGER.info("Coloring a number of transitions");
        for (Transition t : transitions) {
            getNodeFromVertex(t).setColor(color);
        }
        vv.repaint();
        LOGGER.info("Successfully colored a number of transitions");
    }

    /**
     * Checks for Spped Import and creates bends and logical places
     */
    private void checkForSppedImport() {
        LOGGER.info("Checking for Spped Import");
        // If the Petri net is imported from Snoopy we must check for logical places
        if (synchronizer.getPetriNet().hasProperty("importtype")) {
            if (((String) synchronizer.getPetriNet().getProperty("importtype")).equals("speed")) {
                LOGGER.info("Spped Import detected");
                Map<String, Place> graphicalId2Place = new HashMap<>();
                Map<String, Place> internalId2Place = new HashMap<>();
                Map<String, Transition> internalId2Transition = new HashMap<>();
                Map<String, String> internalId2Pos = new HashMap<>();
                Map<String, List<NetViewerNode>> newLogicalPlaces = new HashMap<>();
                Map<Integer, List<Place>> net2Place = new HashMap<>();
                Map<Integer, List<Transition>> net2Transition = new HashMap<>();

                // Fill the maps for places
                LOGGER.debug("Filling map for places");
                List<String> graphicalRepresentations;
                String[] lineParts;
                Integer net;
                for (Place p : synchronizer.getPetriNet().places()) {
                    graphicalRepresentations = (List<String>) p.getProperty("graphicalRepresentations");
                    for (String s : graphicalRepresentations) {
                        lineParts = s.split("\\|");
                        graphicalId2Place.put(lineParts[2], p);
                        internalId2Pos.put(lineParts[2], lineParts[0] + "|" + lineParts[1]);
                        net = new Integer(lineParts[3]);
                        if (!net2Place.containsKey(net)) {
                            net2Place.put(net, new ArrayList<Place>());
                        }
                        net2Place.get(net).add(p);
                    }
                    internalId2Place.put(((Integer) p.getProperty("internalId")).toString(), p);
                }
                LOGGER.debug("Filling map for transitions");
                // Fill the maps for transitions
                for (Transition t : synchronizer.getPetriNet().transitions()) {
                    internalId2Transition.put(((Integer) t.getProperty("internalId")).toString(), t);

                    net = t.getProperty("net");
                    if (!net2Transition.containsKey(net)) {
                        net2Transition.put(net, new ArrayList<Transition>());
                    }
                    net2Transition.get(net).add(t);
                }
                LOGGER.debug("Relocating hierarchical transitions");
                // Relocate the hierachical transitions
                if (net2Transition.size() > 1) {
                    double minXa, minYa, x, y;
                    double minXb, minYb, maxXa, maxYa, maxXb, maxYb, lastX = 0, lastY = 0, distX, distY;
                    boolean sameX, sameY;
                    int counter;
                    Point2D pos, newPos;

                    for (Integer i : net2Transition.keySet()) {
                        if (i == 1 || i == 0) {
                            continue;
                        }

                        minXa = Double.MAX_VALUE;
                        minYa = Double.MAX_VALUE;
                        maxXa = Double.MIN_VALUE;
                        maxYa = Double.MIN_VALUE;

                        minXb = Double.MAX_VALUE;
                        minYb = Double.MAX_VALUE;
                        maxXb = Double.MIN_VALUE;
                        maxYb = Double.MIN_VALUE;

                        sameX = true;
                        sameY = true;

                        if (net2Place.containsKey(i)) {
                            for (Place p : net2Place.get(i)) {
                                pos = layout.transform(getNodeFromVertex(p));
                                x = pos.getX();
                                y = pos.getY();

                                if (x < minXa) {
                                    minXa = x;
                                }
                                if (x > maxXa) {
                                    maxXa = x;
                                }
                                if (y < minYa) {
                                    minYa = y;
                                }
                                if (y > maxYa) {
                                    maxYa = y;
                                }
                            }
                        }

                        if (net2Transition.containsKey(i)) {
                            counter = 0;
                            for (Transition t : net2Transition.get(i)) {
                                pos = layout.transform(getNodeFromVertex(t));
                                x = pos.getX();
                                y = pos.getY();

                                if (counter == 0) {
                                    lastX = x;
                                    lastY = y;
                                }

                                if (x < minXb) {
                                    minXb = x;
                                }
                                if (x > maxXb) {
                                    maxXb = x;
                                }
                                if (y < minYb) {
                                    minYb = y;
                                }
                                if (y > maxYb) {
                                    maxYb = y;
                                }

                                if (lastX != x) {
                                    sameX = false;
                                }
                                if (lastY != y) {
                                    sameY = false;
                                }

                                lastX = x;
                                lastY = y;

                                counter++;
                            }

                            for (Transition t : net2Transition.get(i)) {
                                pos = layout.transform(getNodeFromVertex(t));

                                if (sameY) {
                                    distX = 0.0 - ((maxXb - minXb) / 2.0) + (pos.getX() - minXb);
                                    distY = (maxYa - minYa) / 2.0;
                                } else if (sameX) {
                                    distX = (maxXa - minXa) / 2.0;
                                    distY = 0.0 - ((maxYb - minYb) / 2.0) + (pos.getY() - minYb);
                                } else {
                                    distX = pos.getX() - minXb;
                                    distY = pos.getY() - minYb;
                                }

                                if (distX == 0.0) {
                                    distX = 20.0;
                                }
                                if (distY == 0.0) {
                                    distY = 20.0;
                                }

                                newPos = new Point2D.Double(minXa + distX, distY + minYa);
                                layout.setLocation(getNodeFromVertex(t), vv.getRenderContext().getMultiLayerTransformer().inverseTransform(newPos));
                            }
                        }
                    }
                }
                LOGGER.debug("Finding transitions for logical places");
                // Find the transitions for the logical places
                Arc a;
                List<NetViewerNode> selectedNodes = new ArrayList<>();
                String internalID;
                for (Transition t : synchronizer.getPetriNet().transitions()) {
                    for (Place p : t.outputs()) {
                        a = synchronizer.getPetriNet().getArc(t, p);
                        internalID = (String) a.getProperty("target_graphic");
                        if (graphicalId2Place.containsKey(internalID)) {
                            if (!newLogicalPlaces.containsKey(internalID)) {
                                newLogicalPlaces.put(internalID, new ArrayList<NetViewerNode>());
                            }
                            newLogicalPlaces.get(internalID).add(getNodeFromVertex(t));
                        }
                    }
                    for (Place p : t.inputs()) {
                        a = synchronizer.getPetriNet().getArc(p, t);
                        internalID = (String) a.getProperty("source_graphic");
                        if (graphicalId2Place.containsKey(internalID)) {
                            if (!newLogicalPlaces.containsKey(internalID)) {
                                newLogicalPlaces.put(internalID, new ArrayList<NetViewerNode>());
                            }
                            newLogicalPlaces.get(internalID).add(getNodeFromVertex(t));
                        }
                    }

                }
                LOGGER.debug("Generating logical places");
                // Generate the logical places
                String[] pointParts;
                for (String k : newLogicalPlaces.keySet()) {
                    pointParts = internalId2Pos.get(k).split("\\|");
                    Point.Double newPoint = new Point.Double();
                    newPoint.x = new Double(pointParts[0]);
                    newPoint.y = new Double(pointParts[1]);
                    selectedNodes.clear();
                    selectedNodes.addAll(newLogicalPlaces.get(k));
                    addLogicalPlace(getNodeFromVertex(graphicalId2Place.get(k)), selectedNodes, newPoint);
                }
                LOGGER.debug("Detecting bends in arcs");
                //detect bends in arcs
                List<String> points;
                Arc arc;
                NetViewerEdge lastEdge;
                for (NetViewerEdge e : g.getEdges()) {
                    lastEdge = null;
                    if (e.getSource().getNodeType().equals(NetViewer.PLACE)) {
                        Place from = synchronizer.getPetriNet().findPlace(e.getSource().getMasterNode().getId());
                        Transition to = synchronizer.getPetriNet().findTransition(e.getAim().getId());
                        arc = synchronizer.getPetriNet().getArc(from, to);
                    } else {
                        Transition from = synchronizer.getPetriNet().findTransition(e.getSource().getId());
                        Place to = synchronizer.getPetriNet().findPlace(e.getAim().getMasterNode().getId());
                        arc = synchronizer.getPetriNet().getArc(from, to);
                    }

                    points = arc.getProperty("points");
                    int length = points.size();
                    if (length > 2) {
                        for (int i = 1; i < length - 1; i++) {
                            pointParts = points.get(i).split("\\|");
                            if (lastEdge == null) {
                                lastEdge = addBend(e, Double.parseDouble(pointParts[0]) - 40.0, Double.parseDouble(pointParts[1]) - 30.0);
                            } else {
                                lastEdge = addBend(lastEdge, Double.parseDouble(pointParts[0]) - 40.0, Double.parseDouble(pointParts[1]) - 30.0);

                            }
                        }
                    }
                }
            }
            synchronizer.getPetriNet().removeProperty("importtype");
            LOGGER.info("Spped Import handled");
        }
    }

    /**
     * Update all properties of the Petri net with new names etc.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void updatePetriNet() throws IOException, ClassNotFoundException {
        updatePetriNet(true);
    }

    /**
     * Update all properties of the Petri net with new names etc.
     *
     * @param saveLayout
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void updatePetriNet(Boolean saveLayout) throws IOException, ClassNotFoundException {
        LOGGER.info("Updating properties of Petri net with new names etc.");
        PetriNet pn = this.synchronizer.getPetriNet();
        NetViewerNode nvNode;
        for (Transition t : pn.transitions()) {
            nvNode = getNodeFromVertex(t);
            t.putProperty("name", nvNode.getName());
            t.putProperty("posX", layout.transform(nvNode).getX());
            t.putProperty("posY", layout.transform(nvNode).getY());
        }
        for (Place p : pn.places()) {
            nvNode = getNodeFromVertex(p);
            p.putProperty("name", nvNode.getName());
            p.putProperty("posX", layout.transform(nvNode).getX());
            p.putProperty("posY", layout.transform(nvNode).getY());
        }

        String aimType, sourceType;
        Arc arc = null;
        for (NetViewerEdge nvEdge : g.getEdges()) {
            aimType = nvEdge.getMasterEdge().getAim().getNodeType();
            sourceType = nvEdge.getMasterEdge().getSource().getNodeType();

            if (!aimType.equalsIgnoreCase(NetViewer.BEND) && !sourceType.equalsIgnoreCase(NetViewer.BEND)) {
                if (sourceType.equalsIgnoreCase(NetViewer.PLACE)) {
                    arc = pn.getArc(pn.findPlace(nvEdge.getMasterEdge().getSource().getMasterNode().getId()), pn.findTransition(nvEdge.getMasterEdge().getAim().getMasterNode().getId()));
                } else if (sourceType.equalsIgnoreCase(NetViewer.TRANSITION)) {
                    arc = pn.getArc(pn.findTransition(nvEdge.getMasterEdge().getSource().getMasterNode().getId()), pn.findPlace(nvEdge.getMasterEdge().getAim().getMasterNode().getId()));
                }

                if (arc == null) {
                    continue;
                }
                arc.setWeight(nvEdge.getWeight());
            }
        }
        LOGGER.info("Successfully updated Petri net");
    }

    public void closeAllFrames() {
        LOGGER.info("Closing all frames");
        for (JFrame frame : framesList) {
            frame.setVisible(false);
            frame.dispose();
        }
        LOGGER.info("Successfully closed all frames");
    }

    /**
     * Shows the NetViewer.
     */
    public void showMe() {
        LOGGER.info("Showing NetViewer");
        this.setVisible(true);
    }

    /**
     * Shows the properties menu for a vertex
     *
     * @param nvNode
     * @param x
     * @param y
     */
    protected void showVertexSetup(NetViewerNode nvNode, int x, int y) {
        LOGGER.info("Opening properties menu for a vertex");
        if (nvNode != null) {
            if (!nvNode.getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
                VertexSetupFrame setupFrame = new VertexSetupFrame(this, Arrays.asList(nvNode));

                setupFrame.setLocation(x, y);
                setupFrame.setVisible(true);
                this.setEnabled(false);
            }
        }
        LOGGER.info("Successfully opened properties menu for a vertex");
    }

    /**
     * Shows the properties menu for a set of vertices
     *
     * @param selectedNodes
     * @param nvNode
     * @param x
     * @param y
     */
    protected void showVertexSetup(List<NetViewerNode> selectedNodes, int x, int y) {
        if (selectedNodes != null) {
            LOGGER.info("Opening properties menu for several vertices");
            VertexSetupFrame setupFrame = new VertexSetupFrame(this, selectedNodes);

            setupFrame.setLocation(x, y);
            setupFrame.setVisible(true);
            setupFrame.setAlwaysOnTop(true);
            this.setEnabled(false);
            LOGGER.info("Successfully opened properties menu for several vertices");
        }
    }

    /**
     * Shows the properties menu of a edge
     *
     * @param nvEdge
     * @param x
     * @param y
     */
    public void showEdgeSetup(NetViewerEdge nvEdge, int x, int y) {
        LOGGER.info("Opening properties menu for an edge");
        EdgeSetupFrame setupFrame = new EdgeSetupFrame(this, nvEdge);
        setupFrame.setLocation(x, y);
        setupFrame.setVisible(true);
        this.setEnabled(false);
        LOGGER.info("Successfully opened properties menu for an edge");
    }

    /**
     * Shows the logical place menu
     *
     * @param nvNode
     * @param x
     * @param y
     */
    protected void showCreateLogicalPlace(NetViewerNode nvNode, int x, int y) {
        LOGGER.info("Opening logical place menu");
        lpf.setLocation(x, y);

        neighborsListModel.removeAllElements();
        for (NetViewerNode n : g.getNeighbors(nvNode)) {
            if (!n.getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
                neighborsListModel.addElement(n);
            }
        }

        lpf.setVisible(true);
        lpf.setAlwaysOnTop(true);
        this.setEnabled(false);
        LOGGER.info("Successfully opened logical place menu");
    }

    /**
     * Show a dialog to change the color settings.
     */
    private void showColorOptions() {
        LOGGER.info("Opening color settings");
        ColorOptionsFrame cof = new ColorOptionsFrame(this);

        cof.setEnabled(true);
        cof.setVisible(true);
        cof.setAlwaysOnTop(true);

        this.setEnabled(false);
        LOGGER.info("Successfully opened color settings");
    }

    /**
     * Hides the NetViewer.
     */
    private void exitNetViewer() {
        LOGGER.info("Closing NetViewer");
        this.setVisible(false);
    }

    /**
     * Init the ToolBar.
     */
    private void initToolBar() {
        // Create ToolBar
        LOGGER.info("Initializing ToolBar");
        tb = new ToolBar(this, nvkl);
        LOGGER.debug("Adding Invariants List Display");
        // Invariants Lists
        tinvModelMap.put(tb.ioInvList, "NVIOT");
        tinvModelMap.put(tb.trivialInvList, "NVTrivialT");
        tinvModelMap.put(tb.cyclicInvList, "NVCyclicT");
        tinvModelMap.put(tb.outputInvList, "NVOOT");
        tinvModelMap.put(tb.inputInvList, "NVOIT");

        // Fill the Boxes
        // Are T-invariants available? If so, add these to the Lists
        LOGGER.debug("Filling with T-Invariants, if available");
        tinvs = getTInvs();
        if (tinvs == null) {
            addTinvsToListDisplay();
        }

        // Are M-invariants available? If so, add these to the List
        LOGGER.debug("Filling with M-Invariants, if available");
        minvs = getMInvs();
        if (minvs != null) {
            addMinvsToListDisplay();
        }

        // Are MCTS available? If so, add these to the ComboBox
        LOGGER.debug("Filling with MCTS, if available");
        mctsResults = getMctsResults();
        if (mctsResults == null) {
            tb.mctsCb.setEnabled(false);
            tb.allMctsButton.setEnabled(false);
        } else {
            addMctsToComboBox();
        }

        // Are MC-Sets available? If so, add these to the ComboBox
        LOGGER.debug("Filling with MCS, if available");
        mcsResults = getMcsResults();
        if (mcsResults == null) {
            tb.mcsCb.setEnabled(false);
        } else {
            addMcsToComboBox();
        }

        // Are P-invariants available? If so, add these to the List
        LOGGER.debug("Filling with P-Invariants, if available");
        pinvs = getPInvs();
        if (pinvs != null) {
            addPinvsToListDisplay();
        }

        // Add all MouseMode Buttons to a list
        LOGGER.debug("Adding MouseMode buttons to list");
        mouseModePanels.add(tb.mousePickingPanel);
        mouseModePanels.add(tb.mouseTransformingPanel);
        mouseModePanels.add(tb.inEdgePanel);
        mouseModePanels.add(tb.outEdgePanel);
        mouseModePanels.add(tb.addEdgePanel);
        mouseModePanels.add(tb.deletePanel);
        mouseModePanels.add(tb.addBendPanel);
        mouseModePanels.add(tb.removeBendPanel);
        mouseModePanels.add(tb.addPlacePanel);
        mouseModePanels.add(tb.addTransitionPanel);
        mouseModePanels.add(tb.allignXPanel);
        mouseModePanels.add(tb.allignYPanel);

        // Create SearchBar
        LOGGER.debug("Creating SearchBar");
        sb = new SearchBar(this, synchronizer);
        addTabToMenuBar("SearchBar", sb.getContentPane());

        allPlacesModel = (DefaultListModel) sb.allPlacesList.getModel();
        allTransitionsModel = (DefaultListModel) sb.allTransitionsList.getModel();

        // create LocigalPlaceFrame
        LOGGER.debug("Creating LogicalPlaceFrame");
        lpf = new LogicalPlacesFrame(this, synchronizer);
        neighborsListModel = (DefaultListModel) lpf.neighborsList.getModel();

        framesList.add(sb);
        framesList.add(lpf);
        LOGGER.info("Successfully initialized ToolBar");
    }

    /**
     * Init the MenuBar.
     */
    private void initMenuBar() {
        LOGGER.info("Initializing MenuBar");
        menuBar = new JMenuBar();

        saveAsPictureItem = new JMenuItem(strings.get("NVMakePicButtonShort"), resources.getIcon("save_picture.png"));
        saveAsPictureItem.setActionCommand(MAKE_PIC);
        saveAsPictureItem.addActionListener(this);
        saveAsPictureItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));

        exitItem = new JMenuItem(strings.get("NVClose"), resources.getIcon("delete.png"));
        exitItem.setActionCommand(EXIT);
        exitItem.addActionListener(this);
        exitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));

        fileMenu = new JMenu(strings.get("File"));
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(saveAsPictureItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        colorItem = new JMenuItem(strings.get("NVHideColorText"));
        colorItem.setActionCommand(HIDE_COLOR);
        colorItem.addActionListener(this);

        labelItem = new JMenuItem(strings.get("NVHideLabel"));
        labelItem.setActionCommand(SHOW_LABELS);
        labelItem.addActionListener(this);

        centerItem = new JMenuItem(strings.get("NVCenterPetriNet"));
        centerItem.setActionCommand(CENTER_NET);
        centerItem.addActionListener(this);

        resetColoringItem = new JMenuItem(strings.get("NVResetColoring"));
        resetColoringItem.setActionCommand(RESET_COLORING);
        resetColoringItem.addActionListener(this);

        ctiItem = new JMenuItem(strings.get("NVCTITransitionen"));
        ctiItem.setActionCommand(CTI_TRANSITIONS);
        ctiItem.addActionListener(this);

        byOccurrenceItem = new JMenuItem(strings.get("NVByOccurrence"));
        byOccurrenceItem.setActionCommand(BY_OCCURRENCE);
        byOccurrenceItem.addActionListener(this);

        byFactorItem = new JMenuItem(strings.get("NVByFactor"));
        byFactorItem.setActionCommand(BY_FACTOR);
        byFactorItem.addActionListener(this);

        importantTransitonsMenu = new JMenu(strings.get("NVImportantTransitions"));
        importantTransitonsMenu.add(byOccurrenceItem);
        importantTransitonsMenu.add(byFactorItem);

        visualizationMenu = new JMenu(strings.get("NVVisualization"));
        visualizationMenu.setMnemonic(KeyEvent.VK_I);
        visualizationMenu.add(colorItem);
        visualizationMenu.add(labelItem);
        visualizationMenu.add(centerItem);
        visualizationMenu.add(resetColoringItem);
        visualizationMenu.add(ctiItem);
        visualizationMenu.add(importantTransitonsMenu);

        colorOptionsItem = new JMenuItem(strings.get("NVColorOptions"));
        colorOptionsItem.setActionCommand(SHOW_COLOR_OPTION);
        colorOptionsItem.addActionListener(this);

        optionsMenu = new JMenu(strings.get("NVOptions"));
        optionsMenu.setMnemonic(KeyEvent.VK_O);
        optionsMenu.add(colorOptionsItem);

        addonMenu = new JMenu(strings.get("NVAddonMenu"));

        menuBar.add(fileMenu);
        menuBar.add(visualizationMenu);
        menuBar.add(optionsMenu);
        menuBar.add(addonMenu);
        setJMenuBar(menuBar);
        LOGGER.info("Successfully initialized MenuBar");
    }

    // ------- START: Communication and controll with / for other addons -----------
    /**
     * Saves the current state of the project to the project file.
     */
    public void saveProject() {
        LOGGER.info("Saving project");
        try {
            mainDialog.save();
            LOGGER.info("Successfully saved project");
        } catch (IOException ex) {
            LOGGER.error("Issue while saving project: ", ex);
        }
    }

    /**
     * Updates the associated instance of NetViewerStorage with the current
     * values.
     */
    public void updateNVS() {
        nvs.updateStorage(latestVertexID, latestEdgeID, placeMap, transitionMap, this);
    }

    /**
     * Replaces the actual GraphMousePlugin with popupMouse
     *
     * @param popupMouse
     */
    public void setGraphMousePlugin(AbstractPopupGraphMousePlugin popupMouse) {
        gm.remove(gpmp);
        gm.add(popupMouse);
    }

    /**
     * Remove the given GraphMousePlugin and set the NetViewer GraphMousePlugin
     *
     * @param popupMouse
     */
    public void removeGraphMousePlugin(AbstractPopupGraphMousePlugin popupMouse) {
        gm.remove(popupMouse);
        gm.add(gpmp);
        gm.setMode(ModalGraphMouse.Mode.PICKING);
    }

    /**
     * Add a new Tab to the Menu Bar of the NetViewer
     *
     * @param name The name of the tab, shown in the header of the tab
     * @param tab The Component which is shown in the Tab. (Panel or ToolBar).
     * Please use a TableLayout.
     */
    public void addTabToMenuBar(String name, Component tab) {
        LOGGER.info("Adding new tab to MenuBar of NetViewer");
        Boolean showTab;
        if (Settings.contains(name)) {
            showTab = Settings.getBoolean(name);
        } else {
            showTab = true;
            Settings.set(name, showTab.toString());
        }

        if (showTab) {
            tb.addTabToMenuBar(name, tab);
        }

        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(name, showTab);
        menuItem.addItemListener(new AddonMenuItemListener(name, tab, tb));

        addonMenu.add(menuItem);
        LOGGER.info("Successfully added new tab to MenuBar of NetViewer");
    }

    /**
     * Returns the actual VisualizationViewer
     *
     * @return VisualizationViewer
     */
    public VisualizationViewer getVisualizationViewer() {
        return vv;
    }

    public Boolean getHeatMap() {
        return this.heatMap;
    }

    public VertexDrawPaintTransformer getVertexDrawPaintTransformer() {
        return (VertexDrawPaintTransformer) vv.getRenderContext().getVertexDrawPaintTransformer();
    }

    /**
     * Returns all vertices of the Graph (Places, Transitions, and Bends)
     *
     * @return
     */
    public Collection<NetViewerNode> getAllVertices() {
        return g.getVertices();
    }

    /**
     * Displays a component at the place of the NetViewer
     *
     * @param c
     * @param name
     */
    public void displayMenu(Component c, String name) {
        LOGGER.info("Opening pane for plugin menu");
        if (!spMap.containsKey(name)) {
            JScrollPane sp = new JScrollPane(c);
            spMap.put(name, sp);
            mainPanel.add(sp, name);
        } else {
            if (!c.equals(spMap.get(name).getComponents()[0])) {
                removeMenu(name);
                JScrollPane sp = new JScrollPane(c);
                spMap.put(name, sp);
                mainPanel.add(sp, name);
            }
        }

        cardLayout.show(mainPanel, name);
        tb.menuPane.setEnabledAt(0, false);
        LOGGER.info("Successfully opened pane for plugin menu");
    }

    /**
     * Hide the current displayed Plugin Menu and switches back to net Petri net
     */
    public void hideMenu() {
        LOGGER.info("Hiding currently displayed plugin menu");
        cardLayout.show(mainPanel, VVPANEL);
        tb.menuPane.setEnabledAt(0, true);
        LOGGER.info("Successfully hid previously displayed plugin menu");
    }

    /**
     * Removes a component from the CardLayout
     *
     * @param name
     * @param c
     */
    public void removeMenu(String name) {
        LOGGER.debug("Removing component from CardLayout");
        if (spMap.containsKey(name)) {
            mainPanel.remove(spMap.get(name));
            spMap.remove(name);
        }
        LOGGER.debug("Successfully removed component from CardLayout");
    }

    /**
     * Returns the edge that connects the outNode with the inNode
     *
     * @param outNode Source node of the edge
     * @param inNode Sink node of the edge
     * @return Edge connecting outNode with inNode
     */
    public NetViewerEdge getEdge(NetViewerNode outNode, NetViewerNode inNode) {
        LOGGER.info("Getting Edge. This function should not even be called anywhere. FATAL");
        // If outNode or inNode are a logical place, we have to find the correct edge.
        NetViewerEdge nvEdge;
        if (outNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
            if (outNode.isMasterNode() || outNode.isLogical()) {
                for (NetViewerNode nvNode : outNode.getMasterNode().getLogicalPlaces()) {
                    nvEdge = this.g.findEdge(nvNode, inNode);
                    if (nvEdge != null) {
                        return nvEdge;
                    }
                }
            } else {
                return this.g.findEdge(outNode, inNode);
            }
        } else if (inNode.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
            if (inNode.isMasterNode() || inNode.isLogical()) {
                for (NetViewerNode nvNode : inNode.getMasterNode().getLogicalPlaces()) {
                    nvEdge = this.g.findEdge(outNode, nvNode);
                    if (nvEdge != null) {
                        return nvEdge;
                    }
                }
            } else {
                return this.g.findEdge(outNode, inNode);
            }
        }

        return null;
    }

    /**
     * Displays the VisualizationViewer of the TokenSimulator and deactivate
     * some NetViewer options
     *
     * @param popupMouse
     */
    public void startTokenSimulator(AbstractPopupGraphMousePlugin popupMouse) {
        LOGGER.info("Starting TokenSimulator");
        changeMouseModeToPicking();
        gm.enableSimulatorMode(true);
        setGraphMousePlugin(popupMouse);
        tb.menuPane.setEnabledAt(0, false); // Disables network modifications
        nvkl.setActivated(false);
        LOGGER.info("Successfully started TokenSimulator");
    }

    /**
     * Hide the VisualizationViewer of the TokenSimulator and enable some
     * NetViewer options
     *
     * @param popupMouse
     */
    public void endTokenSimulator(AbstractPopupGraphMousePlugin popupMouse) {
        LOGGER.info("Closing TokenSimulator");
        gm.enableSimulatorMode(false);
        removeGraphMousePlugin(popupMouse);
        tb.menuPane.setEnabledAt(0, true);
        nvkl.setActivated(true);
        vv.getRenderContext().setVertexShapeTransformer(new VertexShapeTransformer(this.getIconSize()));
        vv.repaint();
        LOGGER.info("Successfully closed TokenSimulator");
    }
    // ------- END: Communication and control with / for other addons -----------

    // ------- START: Graph modification in NetViewer -------
    /**
     * Return a map to get the corresponding NetViewerNode of a place.
     *
     * @return
     */
    public Map<Integer, NetViewerNode> getPlaceMap() {
        return Collections.unmodifiableMap(placeMap);
    }

    /**
     * Generates the graph class for the NetViewer out of the PetriNet.
     */
    public Graph<NetViewerNode, NetViewerEdge> translatePNtoGraph() {
        Graph<NetViewerNode, NetViewerEdge> graph = new SparseGraph<>();
        LOGGER.info("Translating Petri net to graph for NetViewer");
        PetriNet pn = synchronizer.getPetriNet();
        int edgeCount = 0;
        NetViewerNode nvNode;
        // generate Places
        String labelName;
        String propertyName;
        for (Place place : pn.places()) {
            labelName = place.<String>getProperty("name");
            nvNode = new NetViewerNode(place.id(), PLACE, labelName);
            placeMap.put(place.id(), nvNode); // this map is needed to synchrone the PN with ne Netviewer graph and for coloring of the nodes
            graph.addVertex(nvNode);
            // has the place a compartment?
            if (place.getCompartment() != null) {
                nvNode.putProperty("compartment", place.getCompartment());
            }
            // save all properties from place to nvNode
            Iterator it = place.getPropertyList().iterator();
            while (it.hasNext()) {
                propertyName = (String) it.next();
                nvNode.putProperty(propertyName, place.getProperty(propertyName));
            }
            // serach for highest id, to get no conflict by creating new nodes
            if (place.id() > latestVertexID) {
                latestVertexID = place.id();
            }
        }
        // generate Transitions
        for (Transition transition : pn.transitions()) {
            labelName = (String) transition.getProperty("name");
            nvNode = new NetViewerNode(transition.id(), TRANSITION, labelName);
            transitionMap.put(transition.id(), nvNode);
            graph.addVertex(nvNode);
            // has the transition a compartment?
            if (transition.getCompartment() != null) {
                nvNode.putProperty("compartment", transition.getCompartment());
            }
            // save all properties from place to nvNode
            Iterator it = transition.getPropertyList().iterator();
            while (it.hasNext()) {
                propertyName = (String) it.next();
                nvNode.putProperty(propertyName, transition.getProperty(propertyName));
            }
            // serach for highest id, to get no conflict by creating new nodes
            if (transition.id() > latestVertexID) {
                latestVertexID = transition.id();
            }
        }
        // generate Edges
        int weight;
        NetViewerEdge edge;
        for (Place place : pn.places()) {
            // inputs
            for (Transition transition : place.inputs()) {
                weight = pn.getArc(pn.findTransition(transition.id()), pn.findPlace(place.id())).weight();
                edge = new NetViewerEdge("e" + edgeCount, weight, transitionMap.get(transition.id()), placeMap.get(place.id()));
                graph.addEdge(edge, edge.getSource(), edge.getAim(), EdgeType.DIRECTED);
                edgeCount++;
            }
            // outputs
            for (Transition transition : place.outputs()) {
                weight = pn.getArc(pn.findPlace(place.id()), pn.findTransition(transition.id())).weight();
                edge = new NetViewerEdge("e" + edgeCount, weight, placeMap.get(place.id()), transitionMap.get(transition.id()));
                graph.addEdge(edge, edge.getSource(), edge.getAim(), EdgeType.DIRECTED);
                edgeCount++;
            }
        }
        LOGGER.info("Successfully translated Petri net to graph for NetViewer");
        return graph;
    }

    /**
     * Return the Graph which is used by the NetViewer.
     *
     * @return The graph which is synchronized
     */
    public Graph<NetViewerNode, NetViewerEdge> getGraph() {
        return g;
    }

    /**
     * Returns the layout, corresponding to this NetViewer's graph.
     *
     * @return
     */
    public MonaLisaLayout<NetViewerNode, NetViewerEdge> getMLLayout() {
        return layout;
    }

    /**
     * Returns the corresponding Place of a NetViewerNode.
     *
     * @param nvNode
     * @return The corresponding Place or NULL, if nvNode is not a Place
     */
    public Place getPlaceFromNode(NetViewerNode nvNode) {
        return synchronizer.getPlaceFromId(nvNode.getMasterNode().getId());
    }

    /**
     * Return a map to get the corresponding NetViewerNode of a transition.
     *
     * @return
     */
    public Map<Integer, NetViewerNode> getTransitionMap() {
        return Collections.unmodifiableMap(transitionMap);
    }

    /**
     * Returns the corresponding Transition of a NetViewerNode.
     *
     * @param nvNode
     * @return The corresponding Transition or NULL, if nvNode is not a
     * Transition
     */
    public Transition getTransitionFromNode(NetViewerNode nvNode) {
        return synchronizer.getTransitionFromId(nvNode.getId());
    }

    /**
     * Returns the corresponding NetViewerNode to a place or transition. If the
     * given UPNE is neither a Place or Transition, the function returns NULL.
     *
     * @param upne Either a Place or a Transition
     * @return If the given UPNE is neither a Place or Transition, the function
     * returns NULL otherwise the reference of a NetViewerNode.
     */
    public NetViewerNode getNodeFromVertex(UniquePetriNetEntity upne) {
        NetViewerNode ret = null;
        if (upne == null) {
            return null;
        }
        if (upne.getClass().equals(Transition.class)) {
            ret = transitionMap.get(upne.id());
        }
        if (upne.getClass().equals(Place.class)) {
            ret = placeMap.get(upne.id());
        }
        return ret;
    }

    /**
     * Returns the corresponding NetViewerNode to ID of a transition.
     *
     * @param id
     * @return The corresponding transition, if for the given ID exist a
     * Transition, NULL otherwise.
     */
    public NetViewerNode getNodeFromTransitionId(Integer id) {
        NetViewerNode ret = null;
        if (transitionMap.containsKey(id)) {
            ret = transitionMap.get(id);
        }
        return ret;
    }

    /**
     * Returns the corresponding NetViewerNode to ID of a place.
     *
     * @param id
     * @return The corresponding place, if for the given ID exist a place, NULL
     * otherwise.
     */
    public NetViewerNode getNodeFromPlaceId(Integer id) {
        NetViewerNode ret = null;
        if (placeMap.containsKey(id)) {
            ret = placeMap.get(id);
        }
        return ret;
    }

    /**
     * Return a new node id. This ID is higher than any other, older, ID.
     *
     * @return
     */
    public int getNewNodeId() {
        return ++this.latestVertexID;
    }

    /**
     * Return a new edge id. This ID is higher than any other, older, ID.
     *
     * @return
     */
    public int getNewEdgeId() {
        return ++this.latestEdgeID;
    }

    // START: Section for manipulating the Net
    /**
     * Creates a new node and add him to the Graph and the PetriNet.
     *
     * @param type NetViewer.PLACE or NetViewer.TRANSITION
     * @param labelName String which is displayed in the NetViewer
     * @param x x coordinate
     * @param y y coordinate
     * @return the created NetViewerNode
     */
    public NetViewerNode addNode(String type, String labelName, double x, double y) {
        LOGGER.debug("Adding new node to NetViewer and Petri net");
        NetViewerNode ret = new NetViewerNode(getNewNodeId(), type, labelName);
        g.addVertex(ret);

        Point.Double point = new Point.Double(x, y);
        layout.setLocation(ret, point);

        if (ret.getNodeType().equalsIgnoreCase(PLACE)) {
            Place p = synchronizer.addPlace(ret.getId(), ret.getName(), x, y);
            placeMap.put(p.id(), ret);
        } else if (ret.getNodeType().equalsIgnoreCase(TRANSITION)) {
            Transition t = synchronizer.addTransition(ret.getId(), ret.getName(), x, y);
            transitionMap.put(t.id(), ret);
        }
        LOGGER.debug("Successfully added new node to NetViewer and Petri net");
        return ret;
    }

    /**
     * Add a new node in dependence to another node
     *
     * @param nvNode
     * @param direction 0 = input ; 1 = output
     */
    public NetViewerNode addNode(NetViewerNode nvNode, Boolean direction) {
        LOGGER.debug("Adding new node to NetViewer in dependence from another node");
        NetViewerNode ret;
        Point2D coordinates = layout.transform(nvNode);
        if (nvNode.getNodeType().equalsIgnoreCase(TRANSITION)) {
            ret = addNode(PLACE, "Created_from_" + nvNode.getName(), (int) coordinates.getX() + 50, (int) coordinates.getY() + 50);
        } else {
            ret = addNode(TRANSITION, "Created_from_" + nvNode.getName(), (int) coordinates.getX() + 50, (int) coordinates.getY() + 50);
        }
        if (direction) {
            addEdge(1, ret, nvNode);
        } else {
            addEdge(1, nvNode, ret);
        }
        LOGGER.debug("Successfully added new node to NetViewer in dependence from another node");
        return ret;
    }

    /**
     * Create a new logical place
     *
     * @param sourceNode
     * @param selectedNodes
     * @param nodePosition
     */
    public void addLogicalPlace(NetViewerNode sourceNode, List<NetViewerNode> selectedNodes, Point2D nodePosition) {
        if (!selectedNodes.isEmpty()) {
            LOGGER.debug("Adding new logical place to NetViewer");
            NetViewerNode newNode = sourceNode.getMasterNode().generateLocicalPlace(getNewNodeId());
            sourceNode.getMasterNode().setColor(Color.GRAY);
            g.addVertex(newNode);
            if (!(sourceNode.getMasterNode().getColor().equals(Color.WHITE) || sourceNode.getMasterNode().getColor().equals(Color.GRAY))) {
                newNode.setColor(sourceNode.getColor());
            }
            if (!sourceNode.getMasterNode().getStrokeColor().equals(Color.BLACK)) {
                newNode.setStrokeColor(sourceNode.getMasterNode().getStrokeColor());
            } else {
                newNode.setStrokeColor(Color.BLACK);
            }
            if (sourceNode.getCorners() != 0) {
                newNode.setCorners(sourceNode.getCorners());
            }
            NetViewerEdge oldEdge;
            NetViewerEdge newEdge;
            for (Object n : selectedNodes) {
                // in which direction are the edge?
                oldEdge = g.findEdge(sourceNode, (NetViewerNode) n);
                if (oldEdge != null) {
                    newEdge = new NetViewerEdge("L" + getNewEdgeId(), oldEdge.getWeight(), newNode, (NetViewerNode) n);
                    removeAllBends(oldEdge);
                    g.removeEdge(oldEdge);
                    g.addEdge(newEdge, newEdge.getSource(), newEdge.getAim(), EdgeType.DIRECTED);
                    sourceNode.removeOutEdge(oldEdge);
                } else {
                    oldEdge = g.findEdge((NetViewerNode) n, sourceNode);
                    newEdge = new NetViewerEdge("L" + getNewEdgeId(), oldEdge.getWeight(), (NetViewerNode) n, newNode);
                    removeAllBends(oldEdge);
                    g.removeEdge(oldEdge);
                    g.addEdge(newEdge, newEdge.getSource(), newEdge.getAim(), EdgeType.DIRECTED);
                    sourceNode.removeInEdge(oldEdge);
                }
                oldEdge.getAim().removeInEdge(oldEdge);
                oldEdge.getSource().removeOutEdge(oldEdge);
            }
            if (nodePosition == null) {
                Point2D coordinates = layout.transform(sourceNode);
                layout.setLocation(newNode, new Point.Double(coordinates.getX() + 50.0, coordinates.getY() + 50.0));
            } else {
                layout.setLocation(newNode, nodePosition);
            }
            LOGGER.debug("Successfully added new logical place to NetViewer");
        }
    }

    /**
     * Add a bend to a given edge at a given point (x,y)
     *
     * @param nvEdge
     * @param x
     * @param y
     */
    public NetViewerEdge addBend(NetViewerEdge nvEdge, double x, double y) {
        LOGGER.debug("Adding bend to edge in NetViewer");
        NetViewerEdge newEdge;
        NetViewerEdge masterEdge = nvEdge.getMasterEdge();
        NetViewerNode newNode = addNode(BEND, "B", x + 30.0, y + 30.0);
        addBendEdge(nvEdge.getSource(), newNode, masterEdge);
        newEdge = new NetViewerEdge("n" + getNewEdgeId(), nvEdge.getWeight(), newNode, nvEdge.getAim(), masterEdge);
        if (newEdge.getAim().getNodeType().equalsIgnoreCase(BEND)) {
            g.addEdge(newEdge, newEdge.getSource(), newEdge.getAim(), EdgeType.UNDIRECTED);
        } else {
            g.addEdge(newEdge, newEdge.getSource(), newEdge.getAim(), EdgeType.DIRECTED);
        }
        if (!nvEdge.getSource().getNodeType().equalsIgnoreCase(BEND) && !nvEdge.getAim().getNodeType().equalsIgnoreCase(BEND)) {
            nvEdge.setVisible(false);
        } else {
            g.removeEdge(nvEdge);
            masterEdge.removeBendEdge(nvEdge);
            nvEdge.getAim().removeInEdge(nvEdge);
            nvEdge.getSource().removeOutEdge(nvEdge);
        }
        LOGGER.debug("Successfully added bend to edge in NetViewer");
        return newEdge;
    }

    /**
     * Add a new bend edge to the Graph
     *
     * @param weight
     * @param source
     * @param aim
     * @param masterEdge
     * @return
     */
    public NetViewerEdge addBendEdge(NetViewerNode source, NetViewerNode aim, NetViewerEdge masterEdge) {
        LOGGER.debug("Adding new bended edge to NetViewer");
        NetViewerEdge ret = new NetViewerEdge("n" + getNewEdgeId(), masterEdge.getWeight(), source, aim, masterEdge);
        g.addEdge(ret, ret.getSource(), ret.getAim(), EdgeType.UNDIRECTED);
        LOGGER.debug("Successfully added new bended edge to NetViewer");
        return ret;
    }

    /**
     * Add a edge to the Graph and the PetriNet.
     *
     * @param weight Weight of the edge
     * @param source Source of the edge
     * @param aim Aim of the edge
     * @return The new created NetViewerEdge
     */
    public NetViewerEdge addEdge(int weight, NetViewerNode source, NetViewerNode aim) {
        LOGGER.debug("Adding new edge to NetViewer and Petri net");
        NetViewerEdge ret = new NetViewerEdge("n" + getNewEdgeId(), weight, source, aim);
        g.addEdge(ret, source, aim, EdgeType.DIRECTED);
        if (ret.getSource().getNodeType().equalsIgnoreCase(PLACE)) {
            Place from = synchronizer.getPlaceFromId(ret.getSource().getMasterNode().getId());
            Transition to = synchronizer.getTransitionFromId(ret.getAim().getId());
            synchronizer.addArc(from, to, weight);
        } else if (ret.getSource().getNodeType().equalsIgnoreCase(TRANSITION)) {
            Transition from = synchronizer.getTransitionFromId(ret.getSource().getId());
            Place to = synchronizer.getPlaceFromId(ret.getAim().getMasterNode().getId());
            synchronizer.addArc(from, to, weight);
        }
        LOGGER.debug("Successfully added new edge to NetViewer and Petri net");
        return ret;
    }

    /**
     * Removes a logical place. All edges a go back to master node or to a other
     * given logical places
     *
     * @param nvNode Place to remove
     * @param aim Aim for edges.
     */
    public void removeLogicalPlace(NetViewerNode nvNode, NetViewerNode aim) {
        if (!nvNode.getNodeType().equalsIgnoreCase(PLACE) || !aim.getNodeType().equalsIgnoreCase(PLACE)) {
            return;
        }
        if (nvNode.equals(nvNode.getMasterNode())) {
            return;
        }
        LOGGER.debug("Removing logical place from NetViewer");
        List<NetViewerEdge> edgesToDelete = new ArrayList<>();
        for (NetViewerEdge e : nvNode.getInEdges()) {
            addEdge(e.getWeight(), e.getSource(), aim);
            edgesToDelete.add(g.findEdge(e.getSource(), nvNode));
        }
        for (NetViewerEdge e : nvNode.getOutEdges()) {
            addEdge(e.getWeight(), aim, e.getAim());
            edgesToDelete.add(g.findEdge(nvNode, e.getAim()));
        }
        for (NetViewerEdge nvEdge : edgesToDelete) {
            nvEdge.getSource().removeOutEdge(nvEdge);
            nvEdge.getAim().removeInEdge(nvEdge);
            removeEdgeFromGraph(nvEdge);
        }
        nvNode.getMasterNode().removeLogicalNode(nvNode);
        if (!nvNode.getMasterNode().getColor().equals(Color.WHITE)) {
            nvNode.getMasterNode().setColor(nvNode.getMasterNode().getColor());
        } else {
            nvNode.getMasterNode().setColor(Color.WHITE);
        }
        if (nvNode.getMasterNode().getLogicalPlaces().size() == 1) {
            nvNode.getMasterNode().setColor(Color.WHITE);
        }
        LOGGER.debug("Successfully removed logical place from NetViewer");
    }

    /**
     * Removes the given edge from the Graph and the PetriNet.
     *
     * @param edge The edge to remove
     */
    public void removeEdge(NetViewerEdge edge) {
        LOGGER.debug("Removing edge from Graph and Petri net");
        removeEdgeFromPetriNet(edge);
        removeAllBends(edge);
        removeEdgeFromGraph(edge);
        LOGGER.debug("Successfully removed edge from Graph and Petri net");
    }

    /**
     * Removes the edge from the graph
     *
     * @param edge
     */
    private void removeEdgeFromGraph(NetViewerEdge edge) {
        LOGGER.debug("Removing edge from graph");
        if (edge.getMasterEdge().equals(edge)) {
            edge.getAim().removeInEdge(edge);
            edge.getSource().removeOutEdge(edge);
        }
        edge.getMasterEdge().getAim().removeInEdge(edge.getMasterEdge());
        edge.getMasterEdge().getSource().removeOutEdge(edge.getMasterEdge());
        g.removeEdge(edge.getMasterEdge());
        LOGGER.debug("Successfully removed edge from graph");
    }

    /**
     * Melt multiple logical places to the master node. The master node is
     * detected automatically
     *
     * @param nvNodes The list of logical places to be melted
     */
    public void mergeLogicalPlaces(List<NetViewerNode> nvNodes) {
        LOGGER.debug("Merging logical places with master node in NetViewer");
        NetViewerNode masterNode = nvNodes.get(0).getMasterNode();
        for (NetViewerNode nvNode : nvNodes) {
            if (!nvNode.getNodeType().equalsIgnoreCase(PLACE)) {
                return;
            }
        }
        if (nvNodes.contains(masterNode)) {
            nvNodes.remove(masterNode);
        }
        for (NetViewerNode nvNode : nvNodes) {
            removeNode(nvNode);
        }
        LOGGER.debug("Successfully merged logical places with master node in NetViewer");
    }

    /**
     * Removes the edge from the Petri net.
     *
     * @param edge
     */
    public void removeEdgeFromPetriNet(NetViewerEdge edge) {
        if (edge == null) {
            return;
        }
        LOGGER.debug("Removing edge from Petri net");
        if (edge.getSource().getNodeType().equals(PLACE)) {
            Place from = synchronizer.getPlaceFromId(edge.getSource().getMasterNode().getId());
            Transition to = synchronizer.getTransitionFromId(edge.getAim().getId());
            synchronizer.removeArc(from, to);
        } else {
            Transition from = synchronizer.getTransitionFromId(edge.getSource().getId());
            Place to = synchronizer.getPlaceFromId(edge.getAim().getMasterNode().getId());
            synchronizer.removeArc(from, to);
        }
        LOGGER.debug("Successfully removed edge from Petri net");
    }

    /**
     * Remove all bends of a gives edge
     *
     * @param edge
     */
    public void removeAllBends(NetViewerEdge edge) {
        LOGGER.debug("Removing all bends from edge in NetViewer");
        List<NetViewerEdge> edgeList = edge.getMasterEdge().getBendEdges();
        for (NetViewerEdge e : edgeList) {
            e.getSource().removeOutEdge(e);
            e.getAim().removeInEdge(e);
            g.removeEdge(e);
            if (e.getAim().getNodeType().equalsIgnoreCase(BEND)) {
                g.removeVertex(e.getAim());
            }
        }
        edge.getMasterEdge().clearBendEdges();
        edge.getMasterEdge().setVisible(true);
        LOGGER.debug("Successfully removed all bends from edge in NetViewer");
    }

    /**
     * Removes a bend from the given edge
     *
     * @param edge
     */
    public void removeBend(NetViewerEdge edge) {
        LOGGER.debug("Removing one bend from an edge in NetViewer");
        NetViewerEdge invisibleEdge = null;
        NetViewerEdge otherEdge;
        // NetViewer.BEND ------ NODE
        if (edge.getSource().getNodeType().equalsIgnoreCase(BEND) && !edge.getAim().getNodeType().equalsIgnoreCase(BEND)) {
            edge.getAim().removeInEdge(edge);
            edge.getSource().removeOutEdge(edge);
            otherEdge = (NetViewerEdge) edge.getSource().getInEdges().toArray()[0];
            if (!otherEdge.getSource().getNodeType().equals(BEND)) {
                invisibleEdge = g.findEdge(otherEdge.getSource(), edge.getAim());
            } else {
                addBendEdge(otherEdge.getSource(), edge.getAim(), edge.getMasterEdge());
            }
            g.removeVertex(edge.getSource());
        } // NODE ------ NetViewer.BEND
        else if (edge.getAim().getNodeType().equalsIgnoreCase(BEND) && !edge.getSource().getNodeType().equalsIgnoreCase(BEND)) {
            edge.getSource().removeOutEdge(edge);
            edge.getAim().removeInEdge(edge);
            otherEdge = (NetViewerEdge) edge.getAim().getOutEdges().toArray()[0];
            if (!otherEdge.getAim().getNodeType().equalsIgnoreCase(BEND)) {
                invisibleEdge = g.findEdge(edge.getSource(), otherEdge.getAim());
            } else {
                addBendEdge(edge.getSource(), otherEdge.getAim(), otherEdge.getMasterEdge());
            }
            g.removeVertex(edge.getAim());
        } // NetViewer.BEND ------ NetViewer.BEND
        else {
            edge.getAim().removeInEdge(edge);
            edge.getSource().removeOutEdge(edge);
            otherEdge = ((NetViewerEdge) edge.getSource().getInEdges().toArray()[0]);
            addBendEdge(otherEdge.getSource(), edge.getAim(), edge.getMasterEdge());
            g.removeVertex(otherEdge.getAim());
        }
        otherEdge.getMasterEdge().removeBendEdge(otherEdge);
        g.removeEdge(otherEdge);
        edge.getMasterEdge().removeBendEdge(edge);
        g.removeEdge(edge);
        if (invisibleEdge != null) {
            invisibleEdge.setVisible(true);
        }
        LOGGER.debug("Successfully removed one bend from an edge in NetViewer");
    }

    /**
     * Removes the given node from the Graph and the Petri net
     *
     * @param nvNode
     */
    public void removeNode(NetViewerNode nvNode) {
        LOGGER.info("Removing node from graph and Petri net");
        boolean notLogicalAnymore = false;
        // Remove all bends
        List<NetViewerEdge> bends2remove = new ArrayList<>();
        for (NetViewerEdge edge : nvNode.getInEdges()) {
            if (!edge.getMasterEdge().equals(edge)) {
                continue;
            }
            if (!edge.getMasterEdge().getBendEdges().isEmpty()) {
                bends2remove.add(edge.getMasterEdge());
            }
        }
        for (NetViewerEdge edge : nvNode.getOutEdges()) {
            if (!edge.getMasterEdge().equals(edge)) {
                continue;
            }
            if (!edge.getMasterEdge().getBendEdges().isEmpty()) {
                bends2remove.add(edge.getMasterEdge());
            }
        }
        for (NetViewerEdge edge : bends2remove) {
            removeAllBends(edge);
        }
        if (nvNode.isMasterNode()) {
            // Needed to avoid ConcurrentModification Errors
            List<NetViewerNode> nvList = new ArrayList<>();
            for (NetViewerNode n : nvNode.getLogicalPlaces()) {
                if (n.equals(nvNode)) {
                    continue;
                }
                nvList.add(n);
            }
            for (NetViewerNode n : nvList) {
                removeNode(n);
            }
            nvNode.clearLogicalPlaces();
        } else if (nvNode.isLogical()) {
            removeLogicalPlace(nvNode, nvNode.getMasterNode());
            // If a logical place is deleted, bends and egdes have to be removed from Graph but not from Petri net!!!
            notLogicalAnymore = true;
        }
        // Now remove all edges
        for (NetViewerEdge edge : nvNode.getOutEdges()) {
            removeEdgeFromPetriNet(edge);
            edge.getAim().removeInEdge(edge.getMasterEdge());
        }
        for (NetViewerEdge edge : nvNode.getInEdges()) {
            removeEdgeFromPetriNet(edge);
            edge.getSource().removeOutEdge(edge.getMasterEdge());
        }
        // and than the node
        g.removeVertex(nvNode);
        // and from the PetriNet
        if (!nvNode.isLogical() && !notLogicalAnymore) {
            if (nvNode.getNodeType().equalsIgnoreCase(PLACE)) {
                synchronizer.removePlace(synchronizer.getPlaceFromId(nvNode.getId()));
                placeMap.remove(nvNode.getId());
            } else if (nvNode.getNodeType().equalsIgnoreCase(TRANSITION)) {
                synchronizer.removeTransition(synchronizer.getTransitionFromId(nvNode.getId()));
                transitionMap.remove(nvNode.getId());
            }
        }
        LOGGER.info("Successfully removed node from graph and Petri net");
    }

    /**
     * Merges a set of vertices to one vertex
     *
     * @param meltingPot List of NetViewerNode to be merged
     */
    public void mergeVertices(List<NetViewerNode> meltingPot) {
        LOGGER.debug("Merging vertices to a single vertex in NetViewer");
        // Define the vertex which "survive"
        NetViewerNode meltAim = meltingPot.get(0);
        // Go through all other vertices to melt these
        List<NetViewerEdge> toDelete = new ArrayList<>();
        for (NetViewerNode nvNode : meltingPot) {

            if (nvNode.equals(meltAim)) {
                continue;
            }
            // Delete old edges and create the new ones
            for (NetViewerEdge e : nvNode.getOutEdges()) {
                toDelete.add(e);
            }
            for (NetViewerEdge e : toDelete) {
                addEdge(e.getWeight(), meltAim, e.getAim());
                removeEdge(e);
            }
            toDelete.clear();
            for (NetViewerEdge e : nvNode.getInEdges()) {
                toDelete.add(e);
            }
            for (NetViewerEdge e : toDelete) {
                addEdge(e.getWeight(), e.getSource(), meltAim);
                removeEdge(e);
            }
            // Remove the old vertex
            removeNode(nvNode);
        }
        LOGGER.debug("Successfully merged vertices to a single vertex in NetViewer");
    }

    /**
     * Reverse a transition. Do nothing if a place is given
     *
     * @param nvNode
     */
    public void reverseTransition(NetViewerNode nvNode, int x, int y) {
        if (nvNode.getNodeType().equalsIgnoreCase(TRANSITION)) {
            LOGGER.debug("Reversing transition");
            NetViewerNode newNode = addNode(TRANSITION, nvNode.getName() + "_rev", x, y);
            Arc a;
            Transition t = getTransitionFromNode(nvNode);
            for (Place p : t.inputs()) {
                a = synchronizer.getArc(p, t);
                addEdge(a.weight(), newNode, getNodeFromPlaceId(p.id()));
            }
            for (Place p : t.outputs()) {
                a = synchronizer.getArc(t, p);
                addEdge(a.weight(), getNodeFromPlaceId(p.id()), newNode);
            }
            LOGGER.debug("Successfully reversed transition");
        }
    }
}
