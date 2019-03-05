/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer.gui;

import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.util.List;
import javax.swing.JOptionPane;
import monalisa.addons.netviewer.listener.MyColorOptionsMouseListener;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.data.pn.Compartment;
import monalisa.data.pn.UniquePetriNetEntity;
import monalisa.util.MonaLisaWindowListener;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;

/**
 *
 * @author Jens Einloft
 */
public class VertexSetupFrame extends javax.swing.JFrame {
    private static final long serialVersionUID = -1522812473959407238L;
        
    /**
     * Create a new setup frame for a single NetViewerNode at a given point on the screen
     * @param netViewer
     * @param nvNode 
     * @param x
     * @param y
     */
    public VertexSetupFrame(NetViewer netViewer, List<NetViewerNode> nvNodes) {
        this.nvNodes = nvNodes;   
        this.netViewer = netViewer;  
        
        setAlwaysOnTop(true);            
        
        initComponents();          
        
        loadProperties();
                
        addWindowListener(new MonaLisaWindowListener(netViewer));        
    }

    private void loadProperties() {
        if(nvNodes.size() == 1) {
            loadPropertiesForSingleNode();
        } else if(nvNodes.size() > 1)
            loadPropertiesForNodeSet();
    }
    
    private void loadPropertiesForNodeSet() {
        vertexNameLabel.setText("Multi selection");
        vertexTypeLabel.setText("");
        vertexTypeLabel.setEnabled(false);
        circleRadioButon.setEnabled(false);
        circleRadioButon.setSelected(false);
        polygoneRadioButton.setEnabled(false);
        polygoneRadioButton.setSelected(false);
        cornerSpinner.setEnabled(false);
        
        showColorLabel.setForeground(nvNodes.get(0).getColor());
        showColorLabel.setBackground(nvNodes.get(0).getColor());   
        showStrokeColorLabel.setForeground(nvNodes.get(0).getStrokeColor());
        showStrokeColorLabel.setBackground(nvNodes.get(0).getStrokeColor());  
        
        SE.setSelected(true);        
        
        vertexNameTextField.setText("");
        vertexNameTextField.setEnabled(false);
        tokensTextField.setText("0");
        tokensTextField.setEnabled(false);        
        vertexNoteTextArea.setText("");
        vertexNoteTextArea.setEnabled(false);
   
        if(netViewer.getProject().getPetriNet().getCompartments().size() > 0) {
            for(Compartment c : netViewer.getProject().getPetriNet().getCompartments()) {
                compartmentCb.addItem(c);
            }                
        }            
    }
    
    /**
     * Load the properties from the NetViewerNode.
     */
    private void loadPropertiesForSingleNode() {
        NetViewerNode nvNode = nvNodes.get(0);
                
        vertexNameLabel.setText(nvNode.getName());
        vertexTypeLabel.setText("["+nvNode.getReadableNodeType()+"]");
        if(nvNode.getCorners() == 0) {
            circleRadioButon.setSelected(true);
        } else {
            polygoneRadioButton.setSelected(true);
        }
        cornerSpinner.setValue((nvNode.getCorners() == 0) ? 4 : nvNode.getCorners());
        showColorLabel.setForeground(nvNode.getColor());
        showColorLabel.setBackground(nvNode.getColor());        
        showStrokeColorLabel.setForeground(nvNode.getStrokeColor());
        showStrokeColorLabel.setBackground(nvNode.getStrokeColor());            
        vertexNameTextField.setText(nvNode.getName());
        
        if(nvNode.getNodeType().equals(NetViewer.PLACE)) {
            tokensTextField.setText(Long.toString(nvNode.getTokens()));
            tokensTextField.setEnabled(true);
        } else {
            tokensTextField.setText("0");
            tokensTextField.setEnabled(false);
        }
        
        if(nvNode.hasProperty("toolTip")) {
            vertexNoteTextArea.setText((String) nvNode.getProperty("toolTip"));
        }
        else {
            vertexNoteTextArea.setText("");
        }

        Renderer.VertexLabel.Position position = nvNode.getLabelPosition();

        if(position == null)
            SE.setSelected(true);
        else {
            switch (position) {
                case CNTR:
                    C.setSelected(true);
                    break;
                case E:
                    E.setSelected(true);
                    break;
                case N:
                    N.setSelected(true);
                    break;
                case NE:
                    NE.setSelected(true);
                    break;
                case NW:
                    NW.setSelected(true);
                    break;
                case S:
                    S.setSelected(true);
                    break;
                case SE:
                    SE.setSelected(true);
                    break;
                case SW:
                    SW.setSelected(true);
                    break;
                case W:
                    W.setSelected(true);
                    break;
                default:
                    break;
            }
        }

        if(netViewer.getProject().getPetriNet().getCompartments().size() > 0) {
            for(Compartment c : netViewer.getProject().getPetriNet().getCompartments()) {
                compartmentCb.addItem(c);
            }                

            UniquePetriNetEntity upne = netViewer.getProject().getPetriNet().findPlace(nvNode.getId());
            if(upne == null) {
                upne = netViewer.getProject().getPetriNet().findTransition(nvNode.getId());
            }                
            if(netViewer.getProject().getPetriNet().getCompartmentMap().containsKey(upne)) {                    
                compartmentCb.setSelectedItem(netViewer.getProject().getPetriNet().getCompartmentMap().get(upne));
            }
        }    
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        shapeBG = new javax.swing.ButtonGroup();
        labelPositionButtons = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        vertexNameLabel = new javax.swing.JLabel();
        vertexTypeLabel = new javax.swing.JLabel();
        shapeLabel = new javax.swing.JLabel();
        circleRadioButon = new javax.swing.JRadioButton();
        polygoneRadioButton = new javax.swing.JRadioButton();
        cornerSpinner = new javax.swing.JSpinner();
        sytleLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        vertexNameTextField = new javax.swing.JTextField();
        colorLabel = new javax.swing.JLabel();
        showColorLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        vertexNoteTextArea = new javax.swing.JTextArea();
        hintLabel = new javax.swing.JLabel();
        lablePositionLabel = new javax.swing.JLabel();
        rbPanel = new javax.swing.JPanel();
        W = new javax.swing.JRadioButton();
        C = new javax.swing.JRadioButton();
        NW = new javax.swing.JRadioButton();
        N = new javax.swing.JRadioButton();
        SW = new javax.swing.JRadioButton();
        SE = new javax.swing.JRadioButton();
        S = new javax.swing.JRadioButton();
        NE = new javax.swing.JRadioButton();
        E = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        noteLabel = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        compartmentCb = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        showStrokeColorLabel = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        tokenLabel = new javax.swing.JLabel();
        tokensTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Vertex Setup");
        setIconImage(resources.getImage("icon-16.png"));

        jPanel1.setPreferredSize(new java.awt.Dimension(290, 290));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        vertexNameLabel.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        vertexNameLabel.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(19, 0, 10, 20);
        jPanel1.add(vertexNameLabel, gridBagConstraints);
        vertexNameLabel.getAccessibleContext().setAccessibleName("vertexName");

        vertexTypeLabel.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        vertexTypeLabel.setForeground(java.awt.Color.red);
        vertexTypeLabel.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 20, 10, 0);
        jPanel1.add(vertexTypeLabel, gridBagConstraints);

        shapeLabel.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        shapeLabel.setText("Shape");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 0);
        jPanel1.add(shapeLabel, gridBagConstraints);

        shapeBG.add(circleRadioButon);
        circleRadioButon.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        circleRadioButon.setText("Circle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        jPanel1.add(circleRadioButon, gridBagConstraints);

        shapeBG.add(polygoneRadioButton);
        polygoneRadioButton.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        polygoneRadioButton.setText("Polygone");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        jPanel1.add(polygoneRadioButton, gridBagConstraints);

        cornerSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 3, 7, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(cornerSpinner, gridBagConstraints);

        sytleLabel.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        sytleLabel.setText("Style");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel1.add(sytleLabel, gridBagConstraints);

        nameLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        nameLabel.setText("Tokens:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        jPanel1.add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 20);
        jPanel1.add(vertexNameTextField, gridBagConstraints);

        colorLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        colorLabel.setText("Color:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        jPanel1.add(colorLabel, gridBagConstraints);

        showColorLabel.setText(strings.get("NVColorLabelDummy"));
        showColorLabel.addMouseListener(new MyColorOptionsMouseListener(showColorLabel));
        showColorLabel.setOpaque(true);
        showColorLabel.setPreferredSize(new java.awt.Dimension(50, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(showColorLabel, gridBagConstraints);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(50, 50));

        vertexNoteTextArea.setColumns(20);
        vertexNoteTextArea.setRows(5);
        jScrollPane2.setViewportView(vertexNoteTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 253;
        gridBagConstraints.ipady = 154;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 20);
        jPanel1.add(jScrollPane2, gridBagConstraints);

        hintLabel.setFont(new java.awt.Font("Cantarell", 0, 10)); // NOI18N
        hintLabel.setText("(click to change)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 20);
        jPanel1.add(hintLabel, gridBagConstraints);

        lablePositionLabel.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        lablePositionLabel.setText("Label position");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel1.add(lablePositionLabel, gridBagConstraints);

        rbPanel.setLayout(new java.awt.GridBagLayout());

        labelPositionButtons.add(W);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        rbPanel.add(W, gridBagConstraints);

        labelPositionButtons.add(C);
        C.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        rbPanel.add(C, gridBagConstraints);

        labelPositionButtons.add(NW);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        rbPanel.add(NW, gridBagConstraints);

        labelPositionButtons.add(N);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        rbPanel.add(N, gridBagConstraints);

        labelPositionButtons.add(SW);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        rbPanel.add(SW, gridBagConstraints);

        labelPositionButtons.add(SE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        rbPanel.add(SE, gridBagConstraints);

        labelPositionButtons.add(S);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        rbPanel.add(S, gridBagConstraints);

        labelPositionButtons.add(NE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        rbPanel.add(NE, gridBagConstraints);

        labelPositionButtons.add(E);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        rbPanel.add(E, gridBagConstraints);

        jLabel1.setText("Top left");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        rbPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setText("Top right");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        rbPanel.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Bottom left");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        rbPanel.add(jLabel3, gridBagConstraints);

        jLabel4.setText("Bottom right");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        rbPanel.add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 109;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 20);
        jPanel1.add(rbPanel, gridBagConstraints);

        noteLabel.setFont(new java.awt.Font("Cantarell", 1, 14)); // NOI18N
        noteLabel.setText("Note:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel1.add(noteLabel, gridBagConstraints);

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 20, 20);
        jPanel1.add(saveButton, gridBagConstraints);

        jLabel5.setText("Compartment:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(jLabel5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(compartmentCb, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel6.setText("Stroke color:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        jPanel1.add(jLabel6, gridBagConstraints);

        showStrokeColorLabel.setText(strings.get("NVColorLabelDummy"));
        showStrokeColorLabel.addMouseListener(new MyColorOptionsMouseListener(showStrokeColorLabel));
        showStrokeColorLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(showStrokeColorLabel, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel8.setText("(click to change)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 20);
        jPanel1.add(jLabel8, gridBagConstraints);

        tokenLabel.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        tokenLabel.setText("Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        jPanel1.add(tokenLabel, gridBagConstraints);

        tokensTextField.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 20);
        jPanel1.add(tokensTextField, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        int corners = 0;
        if(circleRadioButon.isSelected())
            corners = 0;
        else if(polygoneRadioButton.isSelected())
            corners = (Integer)cornerSpinner.getValue();        
        
        Position lablePosition = Position.CNTR;
        if(C.isSelected())
            lablePosition = Position.CNTR;
        else if(E.isSelected())
            lablePosition = Position.E;
        else if(N.isSelected())
            lablePosition = Position.N;
        else if(NE.isSelected())
            lablePosition = Position.NE;
        else if(NW.isSelected())
            lablePosition = Position.NW;
        else if(S.isSelected())
            lablePosition = Position.S;
        else if(SE.isSelected())
            lablePosition = Position.SE;
        else if(SW.isSelected())
            lablePosition = Position.SW;
        else if(W.isSelected())
            lablePosition = Position.W;        
        
        Long tokens;
        
        try {
            tokens = Long.valueOf(tokensTextField.getText());
        } catch(NumberFormatException e) {
            String message = "The given input of tokens is not a valid number";            
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);               
            return;
        }
        
        boolean error = false;
        if(nvNodes.size() == 1) {
           error = !netViewer.writeVertexSetup(nvNodes.get(0), showColorLabel.getForeground(), showStrokeColorLabel.getForeground() , corners, vertexNameTextField.getText(), tokens, vertexNoteTextArea.getText(), lablePosition, (Compartment) compartmentCb.getSelectedItem()); 
        }
        else {
            for(NetViewerNode nvNode : nvNodes) {
                if(!nvNode.getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
                    netViewer.writeVertexSetup(nvNode, lablePosition, (Compartment) compartmentCb.getSelectedItem(), showColorLabel.getForeground(), showStrokeColorLabel.getForeground());
                }
            }
        }
        
        if(!error) {
            this.dispose();
        } else {
            String message = "A place or transition with the given name already exists";            
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);            
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JRadioButton C;
    protected javax.swing.JRadioButton E;
    protected javax.swing.JRadioButton N;
    protected javax.swing.JRadioButton NE;
    protected javax.swing.JRadioButton NW;
    protected javax.swing.JRadioButton S;
    protected javax.swing.JRadioButton SE;
    protected javax.swing.JRadioButton SW;
    protected javax.swing.JRadioButton W;
    private javax.swing.JRadioButton circleRadioButon;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JComboBox compartmentCb;
    private javax.swing.JSpinner cornerSpinner;
    private javax.swing.JLabel hintLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    protected javax.swing.ButtonGroup labelPositionButtons;
    private javax.swing.JLabel lablePositionLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JRadioButton polygoneRadioButton;
    private javax.swing.JPanel rbPanel;
    private javax.swing.JButton saveButton;
    private javax.swing.ButtonGroup shapeBG;
    private javax.swing.JLabel shapeLabel;
    private javax.swing.JLabel showColorLabel;
    private javax.swing.JLabel showStrokeColorLabel;
    private javax.swing.JLabel sytleLabel;
    private javax.swing.JLabel tokenLabel;
    private javax.swing.JTextField tokensTextField;
    private javax.swing.JLabel vertexNameLabel;
    private javax.swing.JTextField vertexNameTextField;
    protected javax.swing.JTextArea vertexNoteTextArea;
    private javax.swing.JLabel vertexTypeLabel;
    // End of variables declaration//GEN-END:variables

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private final NetViewer netViewer;
    private final List<NetViewerNode> nvNodes;
}
