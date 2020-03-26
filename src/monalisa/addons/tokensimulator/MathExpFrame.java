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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import monalisa.data.pn.Place;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Creates and shows a frame for input of a mathematical expression. Places of the Petri net can be used as variables.
 * @author Pavel Balazki.
 */
public class MathExpFrame extends javax.swing.JFrame {
    //BEGIN VARIABLES DECLARATION
    /**
     * Map of the listeners. Keys are listeners, values can be some information the listener can give with while registering.
     */
    private final Map<ChangeListener, Object> listeners = new HashMap<>();
    /*
     * Input text.
     */
    private String text = "";
    /**
     * Mathematical expression which is build from the entered text.
     */
    private MathematicalExpression mathExp;
    /**
     * Maps the name of the place to its ID in the Petri net.
     */
    private final Map<String, Integer> variables = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(MathExpFrame.class);
    
    //END VARIABLES DECLARATION
    
    //BEGIN INNER CLASSES
    private class PlaceCheckBox extends JCheckBox{
        Place place;
        PlaceCheckBox(Place p){
            super((String) p.getProperty("name"));
            this.place = p;
        }
        public Integer getID(){
            return place.id();
        }
        public String getPlaceName(){
            return place.getProperty("name");
        }
    }
    //END INNER CLASSES
    
    //BEGIN CONSTRUCTORS
    /**
     * Creates new form MathExpFrame.
     */
    private MathExpFrame() {
        initComponents();
    }
    
    /**
     * Creates a new frame for editing a mathematical expression.
     * @param places List of places which can be used as variables.
     * @param exp Mathematical expression which will be edited. If no old math exp exists, create new one with a "0" string.
     */
    public MathExpFrame(Collection<Place> places, MathematicalExpression exp) {
        LOGGER.info("Creating a new frame to edit a mathematical expression");
        setIconImage(TokenSimulator.resources.getImage("icon-16.png"));
        //set the name of this window
        this.mathExp = exp;
        this.setTitle("Mathematical expression");
        this.text = exp.toString();
        initComponents();
        this.variables.putAll(exp.getVariables());
        allPlacesList.setName(TokenSimulator.strings.get("Place"));
        DefaultListModel allPlacesListModel = new DefaultListModel();
        DefaultListModel varSelectListModel = new DefaultListModel();
        //Put the places in the list. Only non-constant places can be used as variables.
        for (Place place : places){
            if (!place.isConstant()){
                allPlacesListModel.addElement(place);
                PlaceCheckBox pcb = new PlaceCheckBox(place);
                if (variables.containsKey((String) place.getProperty("name"))){
                    pcb.setSelected(true);
                }
                varSelectListModel.addElement(pcb);
            }
        }
        allPlacesListModel.addElement(MathematicalExpression.TIME_VAR);
        allPlacesList.setModel(allPlacesListModel);
        // If a place in the search bar is selected, it will be pasted to the expTextArea.
        allPlacesList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                //get the selected object. If it is a place, put it to the variables list.
                Object value = allPlacesList.getSelectedValue();
                String name;
                if (value instanceof Place){
                    Place p = (Place) allPlacesList.getSelectedValue();
                    name = p.getProperty("name");
                    //Add the id of picked place to the variables map.
                    variables.put(name, p.id());
                    PlaceCheckBox pcb = ((PlaceCheckBox) varSelectList.getModel().getElementAt(allPlacesList.getSelectedIndex()));
                    pcb.setSelected(true);
                }
                else{
                    name = value.toString();
                }
                expTextArea.insert(name, expTextArea.getCaretPosition());
                expTextArea.requestFocusInWindow();
                varSelectList.repaint();
            }
        });
        varSelectList.setModel(varSelectListModel);
        varSelectList.setCellRenderer(new ListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JCheckBox checkbox = (JCheckBox) value;
                checkbox.setBackground(UIManager.getColor("List.background"));
                return checkbox;
            }
        });
        varSelectList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                //get the selected object. If it is a place, put it to the variables list.
                PlaceCheckBox value = (PlaceCheckBox) varSelectList.getSelectedValue();
                String name = value.getPlaceName();
                int id = value.getID();
                
                /*
                If the checkbox is already selected, deselect it and remove the place from the variables list.
                */
                if (value.isSelected()){
                    value.setSelected(false);
                    variables.remove(name);
                }
                /*
                If the checkbox is not selected, select it and add the place to the variables list.
                */
                else{
                    value.setSelected(true);
                    variables.put(name, id);
                }
                expTextArea.requestFocusInWindow();
            }
        });
        
        /*
         * Make frame disappear when ESC pressed and save settings when ENTER pressed.
         */
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Save");
        this.getRootPane().getActionMap().put("Cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MathExpFrame.this.dispose();
            }
        });       
    }
    //END CONSTRUCTORS
    
    /**
     * Register a listener. It can provide some additional information.
     * @param l
     * @param information 
     */
    public void addListener(ChangeListener l, Object information){
        this.listeners.put(l, information);
    }
    
    /**
     * Get the mathematical expression which was produced by this input frame.
     * @return 
     */
    public MathematicalExpression getMathematicalExpression(){
        return this.mathExp;
    }
    
    /**
     * Get the information from the listener, if it was provided.
     * @param l
     * @return 
     */
    public Object getInformation(ChangeListener l){
        return this.listeners.get(l);
    }
    
    /**
     * Add a string to existing title of the frame.
     * @param str 
     */
    public void addToTitle(String str){
        String title = this.getTitle().concat("; ");
        this.setTitle(title.concat(str));
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

        jPanel1 = new javax.swing.JPanel();
        saveBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        varSelectList = new javax.swing.JList();
        jScrollPane1 = new javax.swing.JScrollPane();
        expTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        allPlacesList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(150, 150));
        setPreferredSize(new java.awt.Dimension(650, 315));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(650, 900));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        saveBtn.setText(TokenSimulator.strings.get("Save"));
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(saveBtn, gridBagConstraints);

        jLabel1.setText("Used variables:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        jScrollPane3.setMinimumSize(new java.awt.Dimension(200, 200));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(200, 200));

        varSelectList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(varSelectList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
        jPanel1.add(jScrollPane3, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 200));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 200));

        expTextArea.setColumns(10);
        expTextArea.setRows(5);
        expTextArea.setText(text);
        jScrollPane1.setViewportView(expTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(200, 200));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(200, 200));

        allPlacesList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        allPlacesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        allPlacesList.setToolTipText(TokenSimulator.strings.get("MathExpPlaceListTT"));
        jScrollPane2.setViewportView(allPlacesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jScrollPane2, gridBagConstraints);

        jLabel2.setText("Reaction constant");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Variables");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        this.text = expTextArea.getText();
        /*
         * Try to build the mathematical expression from entered string.
         */
        try {
            this.mathExp = new MathematicalExpression(text, variables);
            for (ChangeListener l : this.listeners.keySet()){
                l.stateChanged(new ChangeEvent(this));
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Unknown function or unparsable expression found while trying to build a mathematical expression out of the input in the frame");
            JOptionPane.showMessageDialog(rootPane, TokenSimulator.strings.get("MathExpError"), "Warning", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList allPlacesList;
    private javax.swing.JTextArea expTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton saveBtn;
    private javax.swing.JList varSelectList;
    // End of variables declaration//GEN-END:variables
}
