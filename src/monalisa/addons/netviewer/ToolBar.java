/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.netviewer;

import monalisa.addons.netviewer.listener.MctsItemListener;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.DefaultListModel;
import monalisa.addons.netviewer.listener.McsItemListener;
import monalisa.addons.netviewer.wrapper.MctsWrapper;
import monalisa.addons.reachability.ReachabilityDialog;
import monalisa.data.pn.Compartment;
import monalisa.data.pn.PInvariant;
import monalisa.data.pn.Place;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.Transition;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.PInvariants;
import monalisa.results.PInvariantsConfiguration;
import monalisa.tools.pinv.PInvariantTool;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.tools.minv.MInvariantTool;
import static monalisa.tools.pinv.PInvariantTool.setplaceborder;
import static monalisa.tools.pinv.PInvariantTool.settransborder;
import monalisa.util.ColorCollection;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author jens
 */
public class ToolBar extends javax.swing.JPanel {

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private final NetViewer netViewer;
    private final NetViewerKeyListener nvkl;

    protected DefaultListModel allInvList;
    protected DefaultListModel trivialInvList;
    protected DefaultListModel cyclicInvList;
    protected DefaultListModel ioInvList;
    protected DefaultListModel inputInvList;
    protected DefaultListModel outputInvList;

    protected DefaultListModel MinvList;

    protected DefaultListModel PinvList;

    private int lastValue;
    private boolean blockSpinner, blockMenuPaneChangeListener;
    private static final Logger LOGGER = LogManager.getLogger(ToolBar.class);

    /**
     * Creates new form ToolBar
     */
    public ToolBar(final NetViewer netViewer, NetViewerKeyListener nvkl) {
        LOGGER.info("Initializing ToolBar");
        this.netViewer = netViewer;
        this.nvkl = nvkl;

        this.lastValue = 100;
        this.blockSpinner = false;

        this.addKeyListener(nvkl);

        allInvList = new DefaultListModel();
        trivialInvList = new DefaultListModel();
        cyclicInvList = new DefaultListModel();
        ioInvList = new DefaultListModel();
        inputInvList = new DefaultListModel();
        outputInvList = new DefaultListModel();

        MinvList = new DefaultListModel();

        PinvList = new DefaultListModel();

        blockMenuPaneChangeListener = true;
        initComponents();

        this.showCompartmentsCb.addItemListener(new ItemListener() {

            Boolean showMe;

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    netViewer.getVertexDrawPaintTransformer().setShowCompartments(false);
                } else if (e.getStateChange() == ItemEvent.SELECTED) {
                    netViewer.getVertexDrawPaintTransformer().setShowCompartments(true);
                }
                netViewer.repaint();
            }
        });

        this.compartmentCb.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    editCompartmentButton.setEnabled(true);
                    deleteCompartmentButton.setEnabled(true);
                }
            }
        });

        if (!this.netViewer.getProject().getPetriNet().getCompartments().isEmpty()) {
            for (Compartment c : this.netViewer.getProject().getPetriNet().getCompartments()) {
                this.compartmentCb.addItem(c);
            }
        }

        if (this.netViewer.getProject().hasProperty("fontSize")) {
            int fontSize = (int) this.netViewer.getProject().getProperty("fontSize");
            this.netViewer.setFontSize(fontSize);
            this.fontSizeSpinner.setValue(fontSize);
        }

        if (this.netViewer.getProject().hasProperty("arrowSize")) {
            double arrowSize = (double) this.netViewer.getProject().getProperty("arrowSize");
            this.arrowSizeSpinner.setValue(arrowSize);
        }

        if (this.netViewer.getProject().hasProperty("edgeSize")) {
            int edgeSize = (int) this.netViewer.getProject().getProperty("edgeSize");
            this.edgeSizeSpinner.setValue(edgeSize);
        }

        if (this.netViewer.getProject().hasProperty("iconSize")) {
            int iconSize = (int) this.netViewer.getProject().getProperty("iconSize");
            this.iconSizeSpinner.setValue(iconSize);
        }

        blockMenuPaneChangeListener = false;
        LOGGER.info("Successfully initialized ToolBar");
    }

    public void setZoomSpinnerValue(int value) {
        LOGGER.debug("Setting ZoomSpinner value");
        this.blockSpinner = true;
        zoomSpinner.setValue(value);
        lastValue = value;
        this.blockSpinner = false;
        LOGGER.debug("Succssfully set ZoomSpinner value");
    }

    /**
     * Add a new Tab to the Menu Bar of the NetViewer
     *
     * @param name The name of the tab, shown in the header of the tab
     * @param tab The Component which is shown in the Tab. (Panel or ToolBar).
     * Please use a TableLayout.
     */
    public void addTabToMenuBar(String name, Component tab) {
        LOGGER.debug("Adding tab to MenuBar");
        this.menuPane.addTab(name, new JScrollPane(tab));
        LOGGER.debug("Successfully added tab to MenuBar");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sp = new javax.swing.JScrollPane();
        menuPane = new javax.swing.JTabbedPane();
        controlPane = new javax.swing.JPanel();
        controlButtonPanel = new javax.swing.JPanel();
        enableHighlightingButton = new javax.swing.JButton();
        enableLabelsButton = new javax.swing.JButton();
        saveImageButton = new javax.swing.JButton();
        zoomLabel = new javax.swing.JLabel();
        zoomSpinner = new javax.swing.JSpinner();
        addPlacePanel = new javax.swing.JPanel();
        addPlaceButton = new javax.swing.JButton();
        addTransitionPanel = new javax.swing.JPanel();
        addTransitionButton = new javax.swing.JButton();
        addEdgePanel = new javax.swing.JPanel();
        addEdgeButton = new javax.swing.JButton();
        deletePanel = new javax.swing.JPanel();
        deleteButton = new javax.swing.JButton();
        removeBendPanel = new javax.swing.JPanel();
        removeBendButton = new javax.swing.JButton();
        addBendPanel = new javax.swing.JPanel();
        addBendButton = new javax.swing.JButton();
        inEdgePanel = new javax.swing.JPanel();
        inEdgeButton = new javax.swing.JButton();
        outEdgePanel = new javax.swing.JPanel();
        outEdgeButton = new javax.swing.JButton();
        allignYPanel = new javax.swing.JPanel();
        allignYButton = new javax.swing.JButton();
        allignXPanel = new javax.swing.JPanel();
        allignXButton = new javax.swing.JButton();
        mousePickingPanel = new javax.swing.JPanel();
        mousePickingButton = new javax.swing.JButton();
        mouseTransformingPanel = new javax.swing.JPanel();
        mouseTransformingButton = new javax.swing.JButton();
        saveProjectButton = new javax.swing.JButton();
        styleButtonPanel = new javax.swing.JPanel();
        fontSizeLabel = new javax.swing.JLabel();
        iconSizeLabel = new javax.swing.JLabel();
        arrowSizeLabel = new javax.swing.JLabel();
        edgeSizeLabel = new javax.swing.JLabel();
        fontSizeSpinner = new javax.swing.JSpinner();
        iconSizeSpinner = new javax.swing.JSpinner();
        arrowSizeSpinner = new javax.swing.JSpinner();
        edgeSizeSpinner = new javax.swing.JSpinner();
        compartmentPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        compartmentCb = new javax.swing.JComboBox();
        newCompartmentButton = new javax.swing.JButton();
        editCompartmentButton = new javax.swing.JButton();
        deleteCompartmentButton = new javax.swing.JButton();
        showCompartmentsCb = new javax.swing.JCheckBox();
        analysisPane = new javax.swing.JPanel();
        InvPanel = new javax.swing.JPanel();
        emLabel = new javax.swing.JLabel();
        heatmapButton = new javax.swing.JButton();
        computeInvsButton = new javax.swing.JButton();
        TinvCheckBox = new javax.swing.JCheckBox();
        MinvCheckBox = new javax.swing.JCheckBox();
        PinvCheckBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        InvTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        Tinv_list = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        Minv_list = new javax.swing.JList<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        Pinv_list = new javax.swing.JList<>();
        CTILabel = new javax.swing.JLabel();
        CPILabel = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        mctsPanel = new javax.swing.JPanel();
        mctsCb = new javax.swing.JComboBox();
        allMctsButton = new javax.swing.JButton();
        mctsLabel = new javax.swing.JLabel();
        optionsPanel = new javax.swing.JPanel();
        stackSelection = new javax.swing.JCheckBox();
        manuellColorSelection = new javax.swing.JCheckBox();
        reset_color_button = new javax.swing.JButton();
        heatMap_CheckBox = new javax.swing.JCheckBox();
        reachabilityButton = new javax.swing.JButton();
        mcsPanel = new javax.swing.JPanel();
        mcsLabel = new javax.swing.JLabel();
        mcsCb = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(300, 550));
        setLayout(new java.awt.GridBagLayout());

        sp.setPreferredSize(new java.awt.Dimension(290, 540));
        sp.addKeyListener(nvkl);

        menuPane.setPreferredSize(new java.awt.Dimension(290, 540));
        menuPane.addKeyListener(nvkl);
        menuPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(!blockMenuPaneChangeListener) {
                    netViewer.changeMouseModeToPicking();
                }
            }
        });

        controlPane.setSize(new java.awt.Dimension(350, 512));
        controlPane.setPreferredSize(new java.awt.Dimension(350, 512));
        controlPane.addKeyListener(nvkl);
        controlPane.setLayout(new java.awt.GridBagLayout());

        controlButtonPanel.setLayout(new java.awt.GridBagLayout());

        enableHighlightingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/icon-16.png"))); // NOI18N
        enableHighlightingButton.setToolTipText(strings.get("NVHideColor"));
        enableHighlightingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableHighlightingButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        controlButtonPanel.add(enableHighlightingButton, gridBagConstraints);

        enableLabelsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/hide_labels.png"))); // NOI18N
        enableLabelsButton.setToolTipText(strings.get("NVHideAllLabels"));
        enableLabelsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableLabelsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        controlButtonPanel.add(enableLabelsButton, gridBagConstraints);

        saveImageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/save_picture.png"))); // NOI18N
        saveImageButton.setToolTipText(strings.get("NVMakePicButton"));
        saveImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        controlButtonPanel.add(saveImageButton, gridBagConstraints);

        zoomLabel.setFont(new java.awt.Font("Cantarell", 0, 13)); // NOI18N
        zoomLabel.setText("Zoom: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        controlButtonPanel.add(zoomLabel, gridBagConstraints);

        zoomSpinner.setModel(new SpinnerNumberModel(100, 1, 6000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        controlButtonPanel.add(zoomSpinner, gridBagConstraints);
        zoomSpinner.addChangeListener(new ChangeListener() {
            int value, inOrOut;
            @Override
            public void stateChanged(ChangeEvent e) {
                if(!blockSpinner) {
                    value = (Integer)zoomSpinner.getValue();
                    inOrOut = (value >= lastValue) ? 1 : -1;
                    netViewer.zommToValue(inOrOut);
                }
            }
        });
        zoomSpinner.setEditor(new JSpinner.NumberEditor(zoomSpinner, "0'%'"));

        addPlacePanel.setLayout(new java.awt.GridBagLayout());

        addPlaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/place.png"))); // NOI18N
        addPlaceButton.setToolTipText(strings.get("NVCreateVertex"));
        addPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPlaceButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        addPlacePanel.add(addPlaceButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        controlButtonPanel.add(addPlacePanel, gridBagConstraints);

        addTransitionPanel.setLayout(new java.awt.GridBagLayout());

        addTransitionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/transition.png"))); // NOI18N
        addTransitionButton.setToolTipText(strings.get("NVTransition"));
        addTransitionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTransitionButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        addTransitionPanel.add(addTransitionButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        controlButtonPanel.add(addTransitionPanel, gridBagConstraints);

        addEdgePanel.setLayout(new java.awt.GridBagLayout());

        addEdgeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/add_edge.png"))); // NOI18N
        addEdgeButton.setToolTipText(strings.get("NVCreateEdge"));
        addEdgeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEdgeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        addEdgePanel.add(addEdgeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        controlButtonPanel.add(addEdgePanel, gridBagConstraints);

        deletePanel.setLayout(new java.awt.GridBagLayout());

        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/rubber.png"))); // NOI18N
        deleteButton.setToolTipText(strings.get("NVDeleteVertex"));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        deletePanel.add(deleteButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        controlButtonPanel.add(deletePanel, gridBagConstraints);

        removeBendPanel.setLayout(new java.awt.GridBagLayout());

        removeBendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/delete_bend.png"))); // NOI18N
        removeBendButton.setToolTipText(strings.get("NVDeleteBend"));
        removeBendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBendButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        removeBendPanel.add(removeBendButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        controlButtonPanel.add(removeBendPanel, gridBagConstraints);

        addBendPanel.setLayout(new java.awt.GridBagLayout());

        addBendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/add_bend.png"))); // NOI18N
        addBendButton.setToolTipText(strings.get("NVAddBend"));
        addBendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBendButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        addBendPanel.add(addBendButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        controlButtonPanel.add(addBendPanel, gridBagConstraints);

        inEdgePanel.setLayout(new java.awt.GridBagLayout());

        inEdgeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/out_node.png"))); // NOI18N
        inEdgeButton.setToolTipText(strings.get("NVInNodeToolTip"));
        inEdgeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inEdgeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        inEdgePanel.add(inEdgeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        controlButtonPanel.add(inEdgePanel, gridBagConstraints);

        outEdgePanel.setLayout(new java.awt.GridBagLayout());

        outEdgeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/in_node.png"))); // NOI18N
        outEdgeButton.setToolTipText(strings.get("NVOutNodeToolTip"));
        outEdgeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outEdgeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        outEdgePanel.add(outEdgeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        controlButtonPanel.add(outEdgePanel, gridBagConstraints);

        allignYPanel.setLayout(new java.awt.GridBagLayout());

        allignYButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/alignY.png"))); // NOI18N
        allignYButton.setToolTipText(strings.get("NVAlignmentY"));
        allignYButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allignYButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        allignYPanel.add(allignYButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        controlButtonPanel.add(allignYPanel, gridBagConstraints);

        allignXPanel.setLayout(new java.awt.GridBagLayout());

        allignXButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/alignX.png"))); // NOI18N
        allignXButton.setToolTipText(strings.get("NVAlignmentX"));
        allignXButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allignXButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        allignXPanel.add(allignXButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        controlButtonPanel.add(allignXPanel, gridBagConstraints);

        mousePickingPanel.setLayout(new java.awt.GridBagLayout());

        mousePickingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/mouse_picking.png"))); // NOI18N
        mousePickingButton.setToolTipText(strings.get("NVGMPicking"));
        mousePickingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mousePickingButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mousePickingPanel.add(mousePickingButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        controlButtonPanel.add(mousePickingPanel, gridBagConstraints);

        mouseTransformingPanel.setLayout(new java.awt.GridBagLayout());

        mouseTransformingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/mouse_transforming.png"))); // NOI18N
        mouseTransformingButton.setToolTipText(strings.get("NVGMTransforming"));
        mouseTransformingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mouseTransformingButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mouseTransformingPanel.add(mouseTransformingButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        controlButtonPanel.add(mouseTransformingPanel, gridBagConstraints);

        saveProjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/save_project.png"))); // NOI18N
        saveProjectButton.setToolTipText("Save Project");
        saveProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        controlButtonPanel.add(saveProjectButton, gridBagConstraints);

        controlPane.add(controlButtonPanel, new java.awt.GridBagConstraints());

        styleButtonPanel.setLayout(new java.awt.GridBagLayout());

        fontSizeLabel.setFont(new java.awt.Font("Cantarell", 0, 13)); // NOI18N
        fontSizeLabel.setText("Font size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        styleButtonPanel.add(fontSizeLabel, gridBagConstraints);

        iconSizeLabel.setFont(new java.awt.Font("Cantarell", 0, 13)); // NOI18N
        iconSizeLabel.setText("Icon size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        styleButtonPanel.add(iconSizeLabel, gridBagConstraints);

        arrowSizeLabel.setFont(new java.awt.Font("Cantarell", 0, 13)); // NOI18N
        arrowSizeLabel.setText("Arrow size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        styleButtonPanel.add(arrowSizeLabel, gridBagConstraints);

        edgeSizeLabel.setFont(new java.awt.Font("Cantarell", 0, 13)); // NOI18N
        edgeSizeLabel.setText("Edge size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        styleButtonPanel.add(edgeSizeLabel, gridBagConstraints);

        fontSizeSpinner.setModel(new SpinnerNumberModel(12, 5, 100, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        styleButtonPanel.add(fontSizeSpinner, gridBagConstraints);
        fontSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int fontSize = ((Integer)fontSizeSpinner.getValue());
                netViewer.getProject().putProperty("fontSize", fontSize);
                netViewer.setFontSize(fontSize);
            }
        });

        iconSizeSpinner.setModel(new SpinnerNumberModel(12, 5, 150, 1));
        iconSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int iconSize = ((Integer)iconSizeSpinner.getValue());
                netViewer.getProject().putProperty("iconSize", iconSize);
                netViewer.setIconSize(iconSize);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        styleButtonPanel.add(iconSizeSpinner, gridBagConstraints);

        arrowSizeSpinner.setModel(new SpinnerNumberModel(1.0, 1.0, 100.0, 0.5));
        arrowSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double arrowSize = ((Double)arrowSizeSpinner.getValue());
                netViewer.getProject().putProperty("arrowSize", arrowSize);
                netViewer.setArrowSize(arrowSize);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        styleButtonPanel.add(arrowSizeSpinner, gridBagConstraints);

        edgeSizeSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        edgeSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int edgeSize = ((Integer)edgeSizeSpinner.getValue());
                netViewer.getProject().putProperty("edgeSize", edgeSize);
                netViewer.setEdgeSize(edgeSize);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        styleButtonPanel.add(edgeSizeSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        controlPane.add(styleButtonPanel, gridBagConstraints);

        compartmentPanel.setMinimumSize(new java.awt.Dimension(215, 108));
        compartmentPanel.setPreferredSize(new java.awt.Dimension(215, 108));
        compartmentPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Cantarell", 0, 13));
        jLabel1.setText("Compartments:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        compartmentPanel.add(jLabel1, gridBagConstraints);

        compartmentCb.setModel(new DefaultComboBoxModel());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        compartmentPanel.add(compartmentCb, gridBagConstraints);

        newCompartmentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/new_project.png"))); // NOI18N
        newCompartmentButton.setToolTipText("Create a new compartment");
        newCompartmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCompartmentButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        compartmentPanel.add(newCompartmentButton, gridBagConstraints);

        editCompartmentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/edit.png"))); // NOI18N
        editCompartmentButton.setToolTipText("Edit current compartment");
        editCompartmentButton.setEnabled(false);
        editCompartmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCompartmentButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        compartmentPanel.add(editCompartmentButton, gridBagConstraints);

        deleteCompartmentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/delete.png"))); // NOI18N
        deleteCompartmentButton.setToolTipText("Delete current compartment");
        deleteCompartmentButton.setEnabled(false);
        deleteCompartmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteCompartmentButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        compartmentPanel.add(deleteCompartmentButton, gridBagConstraints);

        showCompartmentsCb.setText("Show compartements");
        showCompartmentsCb.setToolTipText("Species or reactions get color coded by there compartments. (The color assigned to the compartments are used)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        compartmentPanel.add(showCompartmentsCb, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        controlPane.add(compartmentPanel, gridBagConstraints);

        menuPane.addTab("Control", controlPane);

        analysisPane.addKeyListener(nvkl);
        analysisPane.setLayout(new java.awt.GridBagLayout());

        InvPanel.setLayout(new java.awt.GridBagLayout());

        emLabel.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        emLabel.setText("Invariants");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        InvPanel.add(emLabel, gridBagConstraints);

        heatmapButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/monalisa/resources/map_color.png"))); // NOI18N
        heatmapButton.setToolTipText(strings.get("NVHeadMapButtonOff"));
        heatmapButton.setToolTipText(strings.get("NVHeadMapButton"));
        heatmapButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heatmapButtonActionPerformed(evt);
            }
        });
        heatmapButton.setVisible(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        InvPanel.add(heatmapButton, gridBagConstraints);

        computeInvsButton.setText("Compute Invariants");
        computeInvsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                computeInvsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        InvPanel.add(computeInvsButton, gridBagConstraints);

        TinvCheckBox.setText("Transition Invariants");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        InvPanel.add(TinvCheckBox, gridBagConstraints);

        MinvCheckBox.setText("Manatee Invariants");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        InvPanel.add(MinvCheckBox, gridBagConstraints);

        PinvCheckBox.setText("Place Invariants");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        InvPanel.add(PinvCheckBox, gridBagConstraints);

        jLabel2.setText("Select the Invariants you want to compute:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        InvPanel.add(jLabel2, gridBagConstraints);

        InvTabbedPane.setMinimumSize(new java.awt.Dimension(370, 280));
        InvTabbedPane.setPreferredSize(new java.awt.Dimension(370, 280));

        Tinv_list.setModel(allInvList);
        Tinv_list.addListSelectionListener(new monalisa.addons.netviewer.listener.TinvSelectionListener(this.netViewer, this, Tinv_list, true));
        jScrollPane1.setViewportView(Tinv_list);

        InvTabbedPane.addTab("T - Invariants", jScrollPane1);

        Minv_list.setModel(MinvList);
        Minv_list.addListSelectionListener(new monalisa.addons.netviewer.listener.MinvSelectionListener(this.netViewer, this, Minv_list, true));
        jScrollPane2.setViewportView(Minv_list);

        InvTabbedPane.addTab("M - Invariants", jScrollPane2);

        Pinv_list.setModel(PinvList);
        Pinv_list.addListSelectionListener(new monalisa.addons.netviewer.listener.PinvSelectionListener(this.netViewer, this, Pinv_list));
        jScrollPane4.setViewportView(Pinv_list);

        InvTabbedPane.addTab("P - Invariants", jScrollPane4);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        InvPanel.add(InvTabbedPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        InvPanel.add(CTILabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        InvPanel.add(CPILabel, gridBagConstraints);

        jCheckBox1.setText("Place bordered");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        InvPanel.add(jCheckBox1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        analysisPane.add(InvPanel, gridBagConstraints);

        mctsPanel.setLayout(new java.awt.GridBagLayout());

        mctsCb.addItemListener(new MctsItemListener(this.netViewer, this, mctsCb));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        mctsPanel.add(mctsCb, gridBagConstraints);

        allMctsButton.setText("Highlight all MCTS");
        allMctsButton.setToolTipText("Highlights all MCT-Set at once");
        allMctsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allMctsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        mctsPanel.add(allMctsButton, gridBagConstraints);

        mctsLabel.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        mctsLabel.setText("MCT Sets");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        mctsPanel.add(mctsLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        analysisPane.add(mctsPanel, gridBagConstraints);

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        stackSelection.setText("Highlight multiple selections");
        stackSelection.setToolTipText("If this checkbox is selcted, a selection of a new elementary mode, MCT-Set or place invariant will not overwrite the current highlighting. Instead the new selected lementary mode, MCT-Set or place invariant will be added to the highlighting.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        optionsPanel.add(stackSelection, gridBagConstraints);

        manuellColorSelection.setText("Choose a new color for every selection");
        manuellColorSelection.setToolTipText("If this checkbox is selected, a new color for the highlighting can be chosen, if a new elementary mode, MCT-set or place invariant is selected.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        optionsPanel.add(manuellColorSelection, gridBagConstraints);

        reset_color_button.setText("Reset Coloring");
        reset_color_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reset_color_buttonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        optionsPanel.add(reset_color_button, gridBagConstraints);

        heatMap_CheckBox.setText("Show Heatmap with Computation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        optionsPanel.add(heatMap_CheckBox, gridBagConstraints);

        reachabilityButton.setText("Reachability");
        reachabilityButton.setActionCommand("Reach");
        reachabilityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reachabilityButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        optionsPanel.add(reachabilityButton, gridBagConstraints);
        reachabilityButton.getAccessibleContext().setAccessibleParent(analysisPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 0);
        analysisPane.add(optionsPanel, gridBagConstraints);

        mcsPanel.setLayout(new java.awt.GridBagLayout());

        mcsLabel.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        mcsLabel.setText("Minimal Cut Sets");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        mcsPanel.add(mcsLabel, gridBagConstraints);

        mcsCb.addItemListener(new McsItemListener(this.netViewer, this, mcsCb));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        mcsPanel.add(mcsCb, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        analysisPane.add(mcsPanel, gridBagConstraints);

        menuPane.addTab("Analysis", analysisPane);

        sp.setViewportView(menuPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(sp, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void enableHighlightingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableHighlightingButtonActionPerformed
        LOGGER.debug("Highlighting Button in ToolBar used");
        Boolean value = this.netViewer.hideColor();

        if (value) {
            LOGGER.debug("Changing ToolBar button to ShowColor");
            enableHighlightingButton.setIcon(resources.getIcon("sw.png"));
            enableHighlightingButton.setToolTipText(strings.get("NVShowColor"));
        } else {
            LOGGER.debug("Changing ToolBar button to HideColor");
            enableHighlightingButton.setIcon(resources.getIcon("color.png"));
            enableHighlightingButton.setToolTipText(strings.get("NVHideColor"));
        }
        LOGGER.debug("Done handling use of highlighting button in ToolBar");
    }//GEN-LAST:event_enableHighlightingButtonActionPerformed

    private void enableLabelsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableLabelsButtonActionPerformed
        LOGGER.debug("Label button in ToolBar used");
        Boolean value = this.netViewer.showLabels();
        if (value) {
            LOGGER.debug("Changing ToolBar button to HideAllLabels");
            enableLabelsButton.setIcon(resources.getIcon("hide_labels.png"));
            enableLabelsButton.setToolTipText(strings.get("NVHideAllLabels"));
        } else {
            LOGGER.debug("Changing ToolBar button to ShowAllLabels");
            enableLabelsButton.setIcon(resources.getIcon("show_labels.png"));
            enableLabelsButton.setToolTipText(strings.get("NVShowAllLabels"));
        }
        LOGGER.debug("Done handling use of label button in ToolBar");
    }//GEN-LAST:event_enableLabelsButtonActionPerformed

    private void saveImageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveImageButtonActionPerformed
        try {
            LOGGER.debug("Image button in ToolBar used");
            this.netViewer.makePic();
            LOGGER.debug("Done handling use of image button in ToolBar");
        } catch (IOException ex) {
            LOGGER.error("Issue while making picture: ", ex);
        }
    }//GEN-LAST:event_saveImageButtonActionPerformed

    private void addPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPlaceButtonActionPerformed
        this.netViewer.placeMouseAction();
    }//GEN-LAST:event_addPlaceButtonActionPerformed

    private void addTransitionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTransitionButtonActionPerformed
        this.netViewer.transitionMouseAction();
    }//GEN-LAST:event_addTransitionButtonActionPerformed

    private void addEdgeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEdgeButtonActionPerformed
        this.netViewer.edgeMouseAction();
    }//GEN-LAST:event_addEdgeButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        this.netViewer.deleteMouseAction();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void removeBendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBendButtonActionPerformed
        this.netViewer.deleteBendMouseAction();
    }//GEN-LAST:event_removeBendButtonActionPerformed

    private void addBendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBendButtonActionPerformed
        this.netViewer.addBendMouseAction();
    }//GEN-LAST:event_addBendButtonActionPerformed

    private void inEdgeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inEdgeButtonActionPerformed
        this.netViewer.inVertexMouseAction();
    }//GEN-LAST:event_inEdgeButtonActionPerformed

    private void outEdgeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outEdgeButtonActionPerformed
        this.netViewer.outVertexMouseAction();
    }//GEN-LAST:event_outEdgeButtonActionPerformed

    private void allignYButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allignYButtonActionPerformed
        this.netViewer.alignYMouseAction();
    }//GEN-LAST:event_allignYButtonActionPerformed

    private void allignXButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allignXButtonActionPerformed
        this.netViewer.alignXMouseAction();
    }//GEN-LAST:event_allignXButtonActionPerformed

    private void mousePickingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mousePickingButtonActionPerformed
        this.netViewer.changeMouseModeToPicking();
    }//GEN-LAST:event_mousePickingButtonActionPerformed

    private void mouseTransformingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mouseTransformingButtonActionPerformed
        this.netViewer.changeMouseModeToTransforming();
    }//GEN-LAST:event_mouseTransformingButtonActionPerformed

    private void saveProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveProjectButtonActionPerformed
        this.netViewer.saveProject();
    }//GEN-LAST:event_saveProjectButtonActionPerformed

    private void newCompartmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCompartmentButtonActionPerformed
        LOGGER.debug("AddCompartment button used from ToolBar");
        CompartmentSetupFrame csf = new CompartmentSetupFrame(this.netViewer);
        csf.setVisible(true);
        LOGGER.debug("Done handling use of AddCompartment button from ToolBar");
    }//GEN-LAST:event_newCompartmentButtonActionPerformed

    private void editCompartmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCompartmentButtonActionPerformed
        LOGGER.debug("EditCompartment button used from ToolBar");
        CompartmentSetupFrame csf = new CompartmentSetupFrame(this.netViewer, (Compartment) this.compartmentCb.getSelectedItem());
        csf.setVisible(true);
        LOGGER.debug("Done handling use of EditCompartment button from ToolBar");
    }//GEN-LAST:event_editCompartmentButtonActionPerformed

    private void deleteCompartmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteCompartmentButtonActionPerformed
        LOGGER.debug("DeleteCompartment button used from ToolBar");
        Compartment c = (Compartment) this.compartmentCb.getSelectedItem();
        this.compartmentCb.removeItem(c);
        this.netViewer.getProject().getPetriNet().removeCompartment(c);

        if (this.compartmentCb.getItemCount() == 0) {
            LOGGER.debug("No Compartments left, disabling EditCompartment and DeleteCompartment button");
            this.editCompartmentButton.setEnabled(false);
            this.deleteCompartmentButton.setEnabled(false);
        }

        this.repaint();
        LOGGER.debug("Done handling use of DeleteCompartment button from ToolBar");
    }//GEN-LAST:event_deleteCompartmentButtonActionPerformed

    private void heatmapButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heatmapButtonActionPerformed
        LOGGER.debug("HeatMap button used from ToolBar");
        if (this.netViewer.heatMap()) {
            LOGGER.debug("Changing ToolBar button to HeatMapOff");
            heatmapButton.setIcon(resources.getIcon("map_sw.png"));
            heatmapButton.setToolTipText(strings.get("NVHeadMapButtonOff"));
        } else {
            LOGGER.debug("Changing ToolBar button to HeatMapOn");
            heatmapButton.setIcon(resources.getIcon("map_color.png"));
            heatmapButton.setToolTipText(strings.get("NVHeadMapButtonOn"));
        }
        LOGGER.debug("Done handling use of HeatMap button from ToolBar");
    }//GEN-LAST:event_heatmapButtonActionPerformed

    private void allMctsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allMctsButtonActionPerformed
        LOGGER.debug("AllMcts button used from ToolBar");
        int numberOfElements = mctsCb.getItemCount();

        if (numberOfElements > ColorCollection.colors.size()) {
            JOptionPane.showMessageDialog(netViewer.vv, "Too many MCTS");
            return;
        }

        TInvariant mcts;
        Set<Transition> transitions;
        Iterator<Transition> it;
        for (int i = 0; i < numberOfElements; i++) {
            if (mctsCb.getItemAt(i).getClass().equals(MctsWrapper.class)) {
                netViewer.colorTransitions(((MctsWrapper) mctsCb.getItemAt(i)).getMcts().transitions(), ColorCollection.colors.get(i));
            }
        }

        netViewer.vv.repaint();
        LOGGER.debug("Done handling use of AllMcts button from ToolBar");
    }//GEN-LAST:event_allMctsButtonActionPerformed

    private void computeInvsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_computeInvsButtonActionPerformed
        if (heatMap_CheckBox.isSelected()) {
            netViewer.heatMap = true;
        } else {
            netViewer.heatMap = false;
        }

        ArrayList<String> toolslist = new ArrayList<>();

        if (TinvCheckBox.isSelected()) {
            toolslist.add(TInvariantTool.class.getName());
        }
        if (MinvCheckBox.isSelected()) {
            toolslist.add(MInvariantTool.class.getName());
        }
        if (PinvCheckBox.isSelected() && !jCheckBox1.isSelected()){//!pinvariantbutton.isSelected()) {           
            settransborder();
            toolslist.add(PInvariantTool.class.getName());
            
            //System.out.println("PinvList: " + PinvList);
        }
        if (PinvCheckBox.isSelected() && jCheckBox1.isSelected()){//pinvariantbutton.isSelected()) {
            setplaceborder();
            toolslist.add(PInvariantTool.class.getName());
            //System.out.println("PinvList: " + PinvList);
        }

        netViewer.calcTools(toolslist);
        //System.out.println("PinvList: " + PinvList);
        
        /*System.out.println("Tabs are refresht");
        InvTabbedPane.removeAll();
        InvTabbedPane.addTab("T - Invariants", jScrollPane1);
        InvTabbedPane.addTab("M - Invariants", jScrollPane2);
        InvTabbedPane.addTab("P - Invariants", jScrollPane4);*/

        // check if the net is CTI
        //    TInvariantTool TTool  = new TInvariantTool();
        //    CTILabel.setText(TTool.cti.getText());

    }//GEN-LAST:event_computeInvsButtonActionPerformed

    private void reset_color_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reset_color_buttonActionPerformed
        netViewer.resetColor();
        Tinv_list.clearSelection();
        Pinv_list.clearSelection();
    }//GEN-LAST:event_reset_color_buttonActionPerformed

    private void reachabilityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reachabilityButtonActionPerformed
        HashMap<Place, Long> marking = new HashMap<>();
        marking.putAll(netViewer.getProject().getPNFacade().marking());
        if (netViewer.getProject().getToolManager().hasResult(PInvariantTool.class, new PInvariantsConfiguration())) {
            PInvariants pinvs = netViewer.getProject().getToolManager().getResult(PInvariantTool.class, new PInvariantsConfiguration());      
            ReachabilityDialog rd = new ReachabilityDialog(netViewer.getProject().getPNFacade(), marking, pinvs);
            rd.setVisible(true);
        } else {
            LOGGER.warn("Results for place invariants not found. Reachability analysis aborted.");
            JOptionPane.showMessageDialog(this, "No results for place invariants have been found. Please compute place invariants before starting the reachability analysis.");
        }
    }//GEN-LAST:event_reachabilityButtonActionPerformed

    public boolean stackSelection() {
        return this.stackSelection.isSelected();
    }

    public boolean manuellColorSelection() {
        return this.manuellColorSelection.isSelected();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JLabel CPILabel;
    protected javax.swing.JLabel CTILabel;
    private javax.swing.JPanel InvPanel;
    protected javax.swing.JTabbedPane InvTabbedPane;
    private javax.swing.JCheckBox MinvCheckBox;
    private javax.swing.JList<String> Minv_list;
    private javax.swing.JCheckBox PinvCheckBox;
    private javax.swing.JList<String> Pinv_list;
    private javax.swing.JCheckBox TinvCheckBox;
    private javax.swing.JList<String> Tinv_list;
    protected javax.swing.JButton addBendButton;
    protected javax.swing.JPanel addBendPanel;
    protected javax.swing.JButton addEdgeButton;
    protected javax.swing.JPanel addEdgePanel;
    protected javax.swing.JButton addPlaceButton;
    protected javax.swing.JPanel addPlacePanel;
    protected javax.swing.JButton addTransitionButton;
    protected javax.swing.JPanel addTransitionPanel;
    protected javax.swing.JButton allMctsButton;
    protected javax.swing.JButton allignXButton;
    protected javax.swing.JPanel allignXPanel;
    protected javax.swing.JButton allignYButton;
    protected javax.swing.JPanel allignYPanel;
    private javax.swing.JPanel analysisPane;
    private javax.swing.JLabel arrowSizeLabel;
    protected javax.swing.JSpinner arrowSizeSpinner;
    protected javax.swing.JComboBox compartmentCb;
    private javax.swing.JPanel compartmentPanel;
    private javax.swing.JButton computeInvsButton;
    private javax.swing.JPanel controlButtonPanel;
    private javax.swing.JPanel controlPane;
    protected javax.swing.JButton deleteButton;
    private javax.swing.JButton deleteCompartmentButton;
    protected javax.swing.JPanel deletePanel;
    private javax.swing.JLabel edgeSizeLabel;
    protected javax.swing.JSpinner edgeSizeSpinner;
    private javax.swing.JButton editCompartmentButton;
    private javax.swing.JLabel emLabel;
    protected javax.swing.JButton enableHighlightingButton;
    protected javax.swing.JButton enableLabelsButton;
    private javax.swing.JLabel fontSizeLabel;
    protected javax.swing.JSpinner fontSizeSpinner;
    private javax.swing.JCheckBox heatMap_CheckBox;
    protected javax.swing.JButton heatmapButton;
    private javax.swing.JLabel iconSizeLabel;
    protected javax.swing.JSpinner iconSizeSpinner;
    protected javax.swing.JButton inEdgeButton;
    protected javax.swing.JPanel inEdgePanel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    protected javax.swing.JCheckBox manuellColorSelection;
    protected javax.swing.JComboBox mcsCb;
    private javax.swing.JLabel mcsLabel;
    private javax.swing.JPanel mcsPanel;
    protected javax.swing.JComboBox mctsCb;
    private javax.swing.JLabel mctsLabel;
    private javax.swing.JPanel mctsPanel;
    protected javax.swing.JTabbedPane menuPane;
    protected javax.swing.JButton mousePickingButton;
    protected javax.swing.JPanel mousePickingPanel;
    protected javax.swing.JButton mouseTransformingButton;
    protected javax.swing.JPanel mouseTransformingPanel;
    private javax.swing.JButton newCompartmentButton;
    private javax.swing.JPanel optionsPanel;
    protected javax.swing.JButton outEdgeButton;
    protected javax.swing.JPanel outEdgePanel;
    private javax.swing.JButton reachabilityButton;
    protected javax.swing.JButton removeBendButton;
    protected javax.swing.JPanel removeBendPanel;
    private javax.swing.JButton reset_color_button;
    protected javax.swing.JButton saveImageButton;
    private javax.swing.JButton saveProjectButton;
    private javax.swing.JCheckBox showCompartmentsCb;
    private javax.swing.JScrollPane sp;
    protected javax.swing.JCheckBox stackSelection;
    private javax.swing.JPanel styleButtonPanel;
    protected javax.swing.JLabel zoomLabel;
    protected javax.swing.JSpinner zoomSpinner;
    // End of variables declaration//GEN-END:variables
}
