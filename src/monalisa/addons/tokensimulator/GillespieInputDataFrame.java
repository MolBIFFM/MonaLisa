/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;


import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;

/**
 * A frame which is used to enter biologically relevant data such as molecule concentrations, volume of the solution
 * and reaction rate constants and transforming them to petri net data.
 * @author Pavel Balazki
 */
public class GillespieInputDataFrame extends javax.swing.JFrame implements ChangeListener{
    //BEGIN VARIABLES DECLARATION
    private static final String TRANSITION = "TRANSITION";
    private static final String PLACE = "PLACE";
    private static final String CONCENTRATION = "CONCENTRATION";
    private static final String TOKENS = "TOKENS";
    private static final String DETERMINISTIC = "K";
    private static final String STOCHASTIC = "C";
    //table model variables
    //classes of data in columns
    private Class[] reactantTypes = new Class [] {Place.class, java.lang.String.class};
    private Class[] reactionTypes = new Class [] {Transition.class, java.lang.String.class};
    //columns namens    
    private String[] reactionColumnNames = new String [] {"Reaction", "Reaction rate constant[M][s]"};
    private GillespieTokenSim tokenSim;
    /**
     * Gives the user number of tokens or concentrations.
     */
    private String inputModePlaces = CONCENTRATION;
    /**
     * Gives the user number of tokens or concentrations.
     */
    private String inputModeTransitions = DETERMINISTIC;    
    /**
     * This HashMap links the given concentration of chemical compounds to the correspondent place. Unit: M
     */
    private HashMap<Place, Double> concentrations = new HashMap<>();
    /**
     * Map of constant places. Unit of mathematical expression: M
     */
    private HashMap<Place, MathematicalExpression> constantPlaces = new HashMap<>();
    /**
     * This HashMap links the equation of reaction rate constant of chemical reaction to the correspondent transition.
     * == k
     * Unit: M, sec.
     */
    private HashMap<Transition, MathematicalExpression> reactionRates = new HashMap<>();
    /*
     * Volume of the reaction environment, in liter.
     */
    private double volume;     
    /**
     * Volume multiplied with the Avogadro constant.
     */
    private double volMol = volume * 6E23;    
    //END VARIABLES DECLARATION
    
    //BEGIN INNER CLASSES
    //custom table model for the reactant tabel
    private class ReactantTableModel extends DefaultTableModel{              
        @Override
        public void setValueAt(Object value, int row, int col){
            double molVol = volume * 6E23;
            double conecentration = 0;
            
            if (col == 1){
                //non-constant places
                if (row < concentrations.size()){
                    //put the given concentration of the compound to the concentrations-map.
                    try{                                                                                                          
                        if(inputModePlaces.equals(CONCENTRATION)) {
                            conecentration = Double.parseDouble(value.toString());
                        } else if(inputModePlaces.equals(TOKENS)) {
                            conecentration = Double.parseDouble(value.toString()) / molVol;      
                        }
                        concentrations.put(concentrations.keySet().toArray(new Place[0])[row], conecentration);
                    }
                    catch (NumberFormatException ex){
                        Logger.getLogger(GillespieInputDataFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //constant places.
                else{
                    int index = row - concentrations.size();
                    try{
                        constantPlaces.put(constantPlaces.keySet().toArray(new Place[0])[index], (MathematicalExpression) value);
                    }
                    catch(Exception ex){
                        Logger.getLogger(GillespieInputDataFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }            
        }
                
        @Override
        public int getRowCount() {
            //This table lists non-constant places first, followed by the constant.
            return concentrations.size() + constantPlaces.size();
        }       
        
        @Override
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            double molVol = volume * 6E23;
            //the second column represents the concentrations of corresponding reactant
            //non-constant places
            if (row < concentrations.size()){
                //the first column represents the names of reactants (places)
                if (col == 0){
                    return concentrations.keySet().toArray(new Place[0])[row].getProperty("name");
                }
                if(inputModePlaces.equals(CONCENTRATION)) {
                    return concentrations.values().toArray(new Double[concentrations.size()])[row];
                } else {
                    return Math.round(concentrations.values().toArray(new Double[concentrations.size()])[row] * molVol);
                }                
            }
            //constant places
            else{
                int index = row - concentrations.size();
                if (col == 0){
                    return constantPlaces.keySet().toArray(new Place[0])[index].getProperty("name");
                }
                return constantPlaces.values().toArray(new MathematicalExpression[constantPlaces.size()])[index];
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            return reactantTypes [columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1 && rowIndex < concentrations.size();
        }
    }
    
    //custom table model for the reaction tabel
    private class ReactionTableModel extends DefaultTableModel{
        @Override
        public void setValueAt(Object value, int row, int col){
            if (col == 1){
                Transition t = reactionRates.keySet().toArray(new Transition[0])[row];
                if (value instanceof MathematicalExpression){                    
                    reactionRates.put(t, (MathematicalExpression) value);
                }
                else{
                    try {
                        MathematicalExpression exp;
                        if(inputModeTransitionSelection.equals(DETERMINISTIC)) {
                            exp = new MathematicalExpression((String) value);
                        } else {
                            exp = new MathematicalExpression(Double.toString(tokenSim.convertCToK(t, Double.valueOf((String) value))));
                        }
                        reactionRates.put(t, exp);
                    } catch (RuntimeException ex) {
                        Logger.getLogger(GillespieInputDataFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }       
        }
        
        @Override
        public String getColumnName(int col) {
            return reactionColumnNames[col];
        }
        
        @Override
        public int getRowCount() {
            return tokenSim.petriNet.transitions().size();
        }
        
        @Override
        public int getColumnCount() {
            return reactionColumnNames.length;
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            Transition t = reactionRates.keySet().toArray(new Transition[0])[row];
            //the first column represents the names of reactants (places)
            if (col == 0)
                return t.getProperty("name");
            //the second column represents the concentrations of corresponding reactant
            if(inputModeTransitions.equals(DETERMINISTIC)) {
                return reactionRates.values().toArray(new MathematicalExpression[0])[row].toString();
            } else {
                try {
                    return tokenSim.convertKToC(t, new Double(reactionRates.values().toArray(new MathematicalExpression[0])[row].toString()));
                } catch (NumberFormatException e) {                                      
                    return Double.NaN;
                }
            }            
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            return reactionTypes [columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if(inputModeTransitions.equals(STOCHASTIC)) {
                if(((Double)getValueAt(rowIndex, columnIndex)).isNaN()) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
    }
    //END INNER CLASSES
    
    //BEGIN CONSTRUCTORS
    private GillespieInputDataFrame(){
    }
    
    /**
     * Creates new form GillespieInputDataFrame
     * @param tokenSimN
     */
    public GillespieInputDataFrame(GillespieTokenSim tokenSimN) {
        this.tokenSim = tokenSimN;
        /*
         * Get the volume
         */
        this.volume = tokenSim.volume;
        /*
         * Put every place of the petri net in the concentrations-HashMap
         */
        for (Place p : this.tokenSim.petriNet.places()){
            if (!p.isConstant()){
                /*
                 * The number of tokens (number of molecules) must be converted to concentration in M first.
                 */
                this.concentrations.put(p, this.tokenSim.getTokens(p.id())/this.tokenSim.volMol);
            }
            else{
                /*
                 * The mathematical expression should describe concentrations in M.
                 */
                this.constantPlaces.put(p, this.tokenSim.tokenSim.getMathematicalExpression(p.id()));
            }
        }
        
        /*
         * Put every transition of the petri net in the reactionRates-HashMap and initialize with the reaction rate which is stored in
         * simulator.
         */
        for (Transition t : this.tokenSim.petriNet.transitions()){
            this.reactionRates.put(t, this.tokenSim.deterministicReactionConstants.get(t.id()));
        }
        initComponents();
        
        reactantTable.getTableHeader().setReorderingAllowed(false);
        reactantTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Reaction compound");
        reactantTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Concentration [M]");
        
        reactantTable.getTableHeader().setReorderingAllowed(false);
        reactantTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Reaction");
        reactantTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Reaction rate constant [M][s]");        
                
        this.concentrationMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    inputModePlaces = CONCENTRATION;        
                    reactantTable.repaint();
                    reactantTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Concentration [M]");
                    reactantTable.getTableHeader().repaint();
                    
                }    
            }            
        });
        
        this.tokenMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    inputModePlaces = TOKENS;                                                        
                    reactantTable.repaint();
                    reactantTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Number of Tokens");
                    reactantTable.getTableHeader().repaint();                    
                }
            }
        });
        
        this.kMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    inputModeTransitions = DETERMINISTIC;                                                        
                    reactionTable.repaint();
                    reactionTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Reaction rate constant [M][s]");
                    reactionTable.getTableHeader().repaint();                    
                }
            }
        });
        
        this.cMode.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    inputModeTransitions = STOCHASTIC;                                                        
                    reactionTable.repaint();
                    reactionTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Stocastic rate constant 1/[s]");
                    reactionTable.getTableHeader().repaint();     
                    
                    String message = "Mathematical expression can not be converted to stochastic rate constants. All reactions with amathematical expressions are blocked to edit.";            
                    JOptionPane.showMessageDialog(GillespieInputDataFrame.this, message, "Error", JOptionPane.WARNING_MESSAGE);                        
                }
            }
        });        

        this.volumeTextField.setText(Double.toString(this.volume));                      
        this.reactantTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer(){
            private final DecimalFormat formatter = new DecimalFormat("0.###E0");
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                if (row < concentrations.size()){
                    try{
                        if(inputModePlaces.equals(CONCENTRATION)) {
                            value = formatter.format((Number)value);
                        }
                    }
                    catch(Exception ex){
                        Logger.getLogger(GillespieInputDataFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        /*
         * Assign a mouse listener to the reactionTable. When a cell is double clicked, a window is opened where user can input
         * the reaction rate constant (also as a mathematical expression) for the selected reaction.
         */
        this.reactionTable.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                //get the selected column   
                int col = reactionTable.getSelectedColumn();               
                //Process double-clicks                
                if (e.getClickCount() == 2){                                     
                    //respond only of second column is selected
                    if (col == 1){
                        //get the selected row
                        int row = reactionTable.getSelectedRow();
                        int rowModel = reactionTable.convertRowIndexToModel(row);
                        
                        if(inputModeTransitions.equals(DETERMINISTIC)) {
                            MathExpFrame  frame = new MathExpFrame(tokenSim.petriNet.places(), reactionRates.values().toArray(new MathematicalExpression[reactionRates.size()])[rowModel]);
                            frame.addToTitle(reactionTable.getValueAt(row, 0).toString());
                            
                            Object[] information = {row, col, TRANSITION};
                            frame.addListener(GillespieInputDataFrame.this, information);
                            frame.setVisible(true);                            
                        }
                        

                    }
                }
            }
        });
        /*
        Create a sorter which sorts rows according to their string-values.
        */
        TableRowSorter<TableModel> reactionRowSorter = new TableRowSorter<>(this.reactionTable.getModel());
        this.reactionTable.setRowSorter(reactionRowSorter);
        List reactionSortKeys = new ArrayList();
        reactionSortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));        
        reactionRowSorter.setComparator(0, new Comparator<String>(){
            @Override
            public int compare(String t, String t1) {
                return (t.compareTo(t1));
            }
        });
        reactionRowSorter.setSortKeys(reactionSortKeys);
        
        /*
         * Assign a mouse listener to the reactantTable. When a cell is double clicked, a window is opened where user can input
         * the concentration (also as a mathematical expression) for the selected reaction.
         */
        this.reactantTable.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                //get the selected row
                int row = reactantTable.getSelectedRow();
                int rowModel = reactantTable.convertRowIndexToModel(row);
                //proceed only for constant places.
                if (rowModel >= concentrations.size()){
                    if (e.getClickCount() == 2){
                        //respond only if second column is selected
                        int col = reactantTable.getSelectedColumn();
                        int index = rowModel - concentrations.size();
                        if (col == 1) {
                            MathExpFrame  frame = new MathExpFrame(tokenSim.petriNet.places(), 
                            constantPlaces.values().toArray(new MathematicalExpression[constantPlaces.size()])[index]);
                            frame.addToTitle(reactantTable.getValueAt(row, 0).toString());
                            Object[] information = {row, col, PLACE};
                            frame.addListener(GillespieInputDataFrame.this, information);
                            frame.setVisible(true);
                        }
                    }
                }
            }
        });
        /*
        Create a sorter which sorts rows according to their string-values.
        */
        TableRowSorter<TableModel> reactantRowSorter = new TableRowSorter<>(this.reactantTable.getModel());
        this.reactantTable.setRowSorter(reactantRowSorter);
        reactionSortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));        
        List reactantSortKeys = new ArrayList();
        reactantRowSorter.setComparator(0, new Comparator<String>(){
            @Override
            public int compare(String t, String t1) {
                return (t.compareTo(t1));
            }
        });
        reactantRowSorter.setComparator(1, new Comparator<Object>(){
            @Override
            public int compare(Object t, Object t1) {
                return (t.toString().compareTo(t1.toString()));
            }
        });
        reactantRowSorter.setSortKeys(reactantSortKeys);   
    }
    //END CONSTRUCTORS
    
    /**
     * This method is called from the MathExpFrame, when the "Save"-button has been activated.
     * @param e 
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source instanceof MathExpFrame){
            //Get the new mathematical expression.
            MathematicalExpression exp = ((MathExpFrame) source).getMathematicalExpression();
            Object[] inf = (Object[]) ((MathExpFrame) source).getInformation(this);
            if (inf[2].toString().equalsIgnoreCase(TRANSITION)){
                this.reactionTable.setValueAt(exp, Integer.parseInt(inf[0].toString()), Integer.parseInt(inf[1].toString()));
            }
            else if (inf[2].toString().equalsIgnoreCase(PLACE)){
                this.reactantTable.setValueAt(exp, Integer.parseInt(inf[0].toString()), Integer.parseInt(inf[1].toString()));
            }
            ((MathExpFrame) source).dispose();
        }
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

        inputModePlaceSelection = new javax.swing.ButtonGroup();
        inputModeTransitionSelection = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        volumeLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        reactantTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        reactionTable = new javax.swing.JTable();
        saveButton = new javax.swing.JButton();
        volumeTextField = new javax.swing.JTextField();
        discardButton = new javax.swing.JButton();
        tokenMode = new javax.swing.JRadioButton();
        concentrationMode = new javax.swing.JRadioButton();
        kMode = new javax.swing.JRadioButton();
        cMode = new javax.swing.JRadioButton();

        setTitle(TokenSimulator.strings.get("GilTSInputDataFrameTitle"));
        setAutoRequestFocus(false);
        setIconImage(TokenSimulator.resources.getImage("icon-16.png"));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        volumeLabel.setText(TokenSimulator.strings.get("GilTSInputDataFrameVolume"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(volumeLabel, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 350));

        reactantTable.setModel(new ReactantTableModel());
        jScrollPane1.setViewportView(reactantTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(400, 350));

        reactionTable.setModel(new ReactionTableModel());
        jScrollPane2.setViewportView(reactionTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(jScrollPane2, gridBagConstraints);

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(saveButton, gridBagConstraints);

        volumeTextField.setText("1.0");
        volumeTextField.setToolTipText(TokenSimulator.strings.get("GilTSInputDataFrameVolumeTT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanel1.add(volumeTextField, gridBagConstraints);

        discardButton.setText("Discard");
        discardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discardButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(discardButton, gridBagConstraints);

        inputModePlaceSelection.add(tokenMode);
        tokenMode.setText("Tokens");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(tokenMode, gridBagConstraints);

        inputModePlaceSelection.add(concentrationMode);
        concentrationMode.setSelected(true);
        concentrationMode.setText("Concentraions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel1.add(concentrationMode, gridBagConstraints);

        inputModeTransitionSelection.add(kMode);
        kMode.setSelected(true);
        kMode.setText("Reaction constant");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        jPanel1.add(kMode, gridBagConstraints);

        inputModeTransitionSelection.add(cMode);
        cMode.setText("Stoachstic rate constant");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        jPanel1.add(cMode, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Convert input data to PN-data and save it in GillespieTokenSim
     */
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        
        if(reactantTable.isEditing()) {
            reactantTable.getCellEditor().stopCellEditing();
        }
        
        if(reactionTable.isEditing()) {
            reactionTable.getCellEditor().stopCellEditing();
        }
                
        try{
            //Parse the volume.
            this.volume = Double.parseDouble(this.volumeTextField.getText());
            this.tokenSim.volume = volume;
            double molVol = volume * 6E23;
            this.tokenSim.volMol = molVol;
            /*
             * Convert concentrations to molecule numbers
             */
            for (Place p : this.tokenSim.petriNet.places()){
                /*
                 * If the place is non-constant, get the entered concentration and convert it to molecule (token) number.
                 */
                if (!p.isConstant()){
                    long tokens = Math.round(this.concentrations.get(p) * molVol);
                    if (tokens == Long.MAX_VALUE){
                        String error = "Concentration for " + p.getProperty("name") + " is too high,possible loss of information,  please enter smaller concentration or lower"
                            + " the volume.";
                        JOptionPane.showMessageDialog(null, error);
                    }
                    try {
                        this.tokenSim.tokenSim.setTokens(p.id(), tokens);
                    } catch (TokenSimulator.PlaceConstantException ex) {
                        Logger.getLogger(GillespieInputDataFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                /*
                 * If the place is constant, assign a mathematical expression to it. The unit stays M and sec, no conversion needed.
                 */
                else{
                    MathematicalExpression exp = this.constantPlaces.get(p);
                    try {
                        this.tokenSim.tokenSim.setMathExpression(p.id(), exp);
                    } catch (TokenSimulator.PlaceNonConstantException ex) {
                        Logger.getLogger(GillespieInputDataFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            /*
             * Store deterministic reaction rate constants. Do not perform conversion to stochastic reaction rate constant!.
             */
            for (Transition t : this.reactionRates.keySet()){
                this.tokenSim.deterministicReactionConstants.put(t.id(), this.reactionRates.get(t));
            }
            tokenSim.tokenSim.netViewer.hideMenu();
        }
        catch(NumberFormatException E){
            JOptionPane.showMessageDialog(null, "Invalid volume! Allowed are real numbers, separated by a '.'");
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_saveButtonActionPerformed

    private void discardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discardButtonActionPerformed
        tokenSim.tokenSim.netViewer.hideMenu();
    }//GEN-LAST:event_discardButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton cMode;
    private javax.swing.JRadioButton concentrationMode;
    private javax.swing.JButton discardButton;
    private javax.swing.ButtonGroup inputModePlaceSelection;
    private javax.swing.ButtonGroup inputModeTransitionSelection;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JRadioButton kMode;
    private javax.swing.JTable reactantTable;
    private javax.swing.JTable reactionTable;
    private javax.swing.JButton saveButton;
    private javax.swing.JRadioButton tokenMode;
    private javax.swing.JLabel volumeLabel;
    private javax.swing.JTextField volumeTextField;
    // End of variables declaration//GEN-END:variables
}
