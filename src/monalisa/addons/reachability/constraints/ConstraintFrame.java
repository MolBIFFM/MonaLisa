/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package monalisa.addons.reachability.constraints;

import java.awt.Color;
import java.awt.PopupMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import static jdk.internal.org.jline.keymap.KeyMap.key;
import monalisa.addons.reachability.AlgorithmRunner;
import monalisa.addons.reachability.Pathfinder;
import monalisa.addons.reachability.ReachabilityDialog;
import monalisa.addons.reachability.ReachabilityEvent;
import static monalisa.addons.reachability.ReachabilityEvent.Status.ABORTED;
import static monalisa.addons.reachability.ReachabilityEvent.Status.EQUALNODE;
import static monalisa.addons.reachability.ReachabilityEvent.Status.FAILURE;
import static monalisa.addons.reachability.ReachabilityEvent.Status.FINISHED;
import static monalisa.addons.reachability.ReachabilityEvent.Status.PROGRESS;
import static monalisa.addons.reachability.ReachabilityEvent.Status.STARTED;
import static monalisa.addons.reachability.ReachabilityEvent.Status.SUCCESS;
import monalisa.addons.reachability.ReachabilityListener;
import monalisa.addons.reachability.ReachabilityNode;
import monalisa.addons.reachability.algorithms.AbstractReachabilityAlgorithm;
import monalisa.addons.reachability.algorithms.BreadthFirst;
import monalisa.addons.reachability.algorithms.ReachabilityAlgorithm;
import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.results.PInvariants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 *
 * @author Kristin Haas
 */
public class ConstraintFrame extends javax.swing.JFrame implements monalisa.addons.reachability.ReachabilityListener {
    private static final long serialVersionUID = -8541347764965669414L;
    BreadthFirst bf;
    public Pathfinder path;
    public PetriNetFacade pn;
    private PetriNetFacade backUFacade;
    private PetriNet backupPN;
    private static final Logger LOGGER = LogManager.getLogger(ConstraintFrame.class);
    private HashMap<Place, Long> start;
    private HashMap<Place, Long> target;
    private final HashMap<Place, Long> capacities; 
    private final PInvariants pinvs;
    
    
    private boolean pushed = false;
    private static HashSet<Transition> transitions = new HashSet<>();
    
    public boolean resetTransition = false;
    public static boolean forcedTransition = false;
    private static boolean stopProgram = false;
    private static Transition chooseTransition = null;
    private boolean possible = true;
    
    private static int spinVal = 0;
    
    public static int getSpinVal(){
        return spinVal;
    }
    
    private void setSpinVal(){
        int spin = (Integer)spinner.getValue();
        spinVal = spin;
        
    }
    
    public static HashSet<Transition> getTransitions(){
        return transitions;
    }
    
    public static Transition getChosenTransition(){
        return chooseTransition;
    }
    /**
     * 
     * @return 
     */
    public boolean getForcedTransition(){
        return forcedTransition;
    }
    
    
    /**
     * 
     * @return 
     */
    public static boolean setStopProgramTrue(){
        return stopProgram = true;
    }
    
    /**
     * 
     * @return 
     */
    public static boolean setStopProgramFalse(){
        return stopProgram = false;
    }
    
    public static boolean getStopProgram(){
        return stopProgram;
    }
    
    /**
     * 
     * @return 
     */
    public boolean setForcedTransitionTrue(){
        return forcedTransition = true;
    }
    
    /**
     * 
     * @return 
     */
    public boolean setForcedTransitionFalse(){
        return forcedTransition = false;
    }
    
    /**
     * ConstraintFrame
     * @param pn
     * @param start
     * @param pinvs
     * @param target
    */
    public ConstraintFrame(PetriNetFacade pn, HashMap<Place, Long> start, HashMap<Place, Long> target, PInvariants pinvs) throws InterruptedException {
        this.start = new HashMap<>();
        this.start.putAll(start);
        this.pn = pn;
        this.backUFacade = pn;
        this.backupPN = pn.getPNCopy();
        this.target = new HashMap<>();
        this.target.putAll(start);
        this.capacities = new HashMap<>();
        this.pinvs = pinvs;
        initComponents();
        // Fill combo boxes with places
        DefaultTableModel model = (DefaultTableModel) markingTable.getModel();
        for (Place p : pn.places()) {
            
            model.addRow(new Object[]{
                p,
                start.get(p),
                //pn.getArc(p, t).weight()+"  ["+t+"]",
                p.outputs(),
                target.get(p)
               
            });
            /**if(p.outputs().isEmpty()){
                model.addRow(new Object[]{
                p,
                start.get(p),
                "No arc",
                target.get(p)
               
            });
            }
            for(Transition t : p.outputs()){
                
            model.addRow(new Object[]{
                p,
                start.get(p),
                pn.getArc(p, t).weight()+"  ["+t+"]",
                target.get(p)
               
            });
            
            
               
            }*/
            
        }
        HashSet<Transition> transitionSet = new HashSet<>();
        for (Place p : pn.places()) {
            if(p.inputs().size()>=1){
                for(Transition t : p.inputs()){
                    //onTransition.add(t.toString());
                    transitionSet.add(t);
                }
            }
            if(p.outputs().size()>=1){
                for(Transition t : p.outputs()){
                    transitionSet.add(t);
                }
            }
        }
        //Fills list [transition musst be used] with transitions
        for(Transition t : transitionSet){
                onTransition.add(t.toString());
                transitionList.add(t.toString());
            }
       
        
        transitions = transitionSet;
        algoSelect.setActionCommand("Breadth First Search");
        
  
        LOGGER.info("Successfully initialized ConstraintFrame.");
    
    }
    
    public HashMap<Place, Long> readTable(){
        DefaultTableModel model = (DefaultTableModel) markingTable.getModel();
        HashMap<Place, Long> t = new HashMap<>();
        for(int i = 0; i < model.getRowCount(); i++){
            Long newLong = (Long)(start.get(model.getValueAt(i, 0)));
            for(Map.Entry<Place, Long> entry : target.entrySet()){
                if(model.getValueAt(i, 0).equals(entry.getKey())){
                    String sLong = model.getValueAt(i, 3).toString();
                    
                    target.replace(entry.getKey(), Long.parseLong(sLong));
                   // target.replace(entry.getKey(), Long.parseLong(sLong));
                   if(pn.getInputTransitionsFor(entry.getKey()).isEmpty()){
                       possible = false;
                       System.out.println(entry.getKey());
                   }
                }
            }
        }
        return null;
    }
    
  

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        Reachability = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        algoSelect = new javax.swing.JComboBox<>();
        onTransition = new java.awt.List();
        offTransition = new java.awt.List();
        restorePN = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        markingTable = new javax.swing.JTable();
        PlaceTitel = new javax.swing.JLabel();
        what = new javax.swing.JLabel();
        used = new java.awt.List();
        firedTransitionText = new javax.swing.JLabel();
        transitionList = new java.awt.List();
        jLabel8 = new javax.swing.JLabel();
        chooseButton = new javax.swing.JButton();
        chooseText = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        chosenAND = new javax.swing.JLabel();
        tryAgain = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        counterText = new javax.swing.JLabel();
        stopButton = new javax.swing.JButton();
        spinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(650, 900));
        setMinimumSize(getSize());
        setPreferredSize(new java.awt.Dimension(650, 900));
        setResizable(false);
        setSize(new java.awt.Dimension(650, 900));

        Reachability.setFont(new java.awt.Font("Dialog", 0, 20)); // NOI18N
        Reachability.setText("Reachability with constraints");

        jLabel1.setText("Switched OFF transitions");

        jLabel2.setText("Choose algorithm");

        jButton1.setText("SwitchOFF");
        jButton1.setMaximumSize(new java.awt.Dimension(89, 60));
        jButton1.setMinimumSize(new java.awt.Dimension(89, 60));
        jButton1.setPreferredSize(new java.awt.Dimension(150, 25));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton4.setText("SwitchON");
        jButton4.setPreferredSize(new java.awt.Dimension(150, 25));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel4.setText("Switched ON transitions");

        jButton5.setForeground(new java.awt.Color(0, 102, 51));
        jButton5.setText("Compute");
        jButton5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 51)));
        jButton5.setMaximumSize(new java.awt.Dimension(92, 25));
        jButton5.setMinimumSize(new java.awt.Dimension(92, 25));
        jButton5.setPreferredSize(new java.awt.Dimension(92, 25));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        algoSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Breadth First Search" }));
        algoSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSelectActionPerformed(evt);
            }
        });

        onTransition.setMaximumSize(new java.awt.Dimension(200, 80));
        onTransition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onTransitionActionPerformed(evt);
            }
        });

        offTransition.setMaximumSize(new java.awt.Dimension(155, 80));
        offTransition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                offTransitionActionPerformed(evt);
            }
        });

        restorePN.setText("Restore PN");
        restorePN.setMaximumSize(new java.awt.Dimension(98, 25));
        restorePN.setMinimumSize(new java.awt.Dimension(98, 25));
        restorePN.setPreferredSize(new java.awt.Dimension(98, 25));
        restorePN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restorePNActionPerformed(evt);
            }
        });

        markingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Place [Name]", "Available [#Token]", "Post [Transitions]", "Target [#Token]"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        markingTable.setMaximumSize(new java.awt.Dimension(300, 0));
        markingTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(markingTable);
        if (markingTable.getColumnModel().getColumnCount() > 0) {
            markingTable.getColumnModel().getColumn(0).setResizable(false);
            markingTable.getColumnModel().getColumn(1).setResizable(false);
            markingTable.getColumnModel().getColumn(2).setResizable(false);
            markingTable.getColumnModel().getColumn(3).setResizable(false);
        }

        PlaceTitel.setText("Place and token before fireing");

        what.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N

        used.setMaximumSize(new java.awt.Dimension(155, 80));
        used.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usedActionPerformed(evt);
            }
        });

        firedTransitionText.setText("Fired transitions:");

        transitionList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                transitionListMouseClicked(evt);
            }
        });
        transitionList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transitionListActionPerformed(evt);
            }
        });

        jLabel8.setText("Transition must be used [only one possible]:");

        chooseButton.setText("Choose transition");
        chooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseButtonActionPerformed(evt);
            }
        });

        chooseText.setText("Chosen transition:");

        jButton2.setText("Reset transition");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Help");
        jButton3.setMaximumSize(new java.awt.Dimension(98, 25));
        jButton3.setMinimumSize(new java.awt.Dimension(98, 25));
        jButton3.setPreferredSize(new java.awt.Dimension(98, 25));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        counterText.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N

        stopButton.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        stopButton.setForeground(new java.awt.Color(153, 51, 0));
        stopButton.setText("Stop");
        stopButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 51, 0)));
        stopButton.setPreferredSize(new java.awt.Dimension(50, 25));
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Max. number of chosen transition to fire:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(used, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(stopButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Reachability)
                                .addGap(29, 29, 29)
                                .addComponent(restorePN, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(counterText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(algoSelect, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(what, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(onTransition, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(31, 31, 31)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 29, Short.MAX_VALUE))
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(offTransition, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(PlaceTitel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(chooseText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                                    .addComponent(transitionList, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(chooseButton, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                                        .addComponent(chosenAND, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tryAgain, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(60, 60, 60))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(firedTransitionText, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(61, 61, 61)
                                .addComponent(spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Reachability)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(restorePN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jLabel1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(offTransition, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                    .addComponent(onTransition, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chooseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2))
                    .addComponent(transitionList, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chooseText)
                    .addComponent(chosenAND, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(tryAgain, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(PlaceTitel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(algoSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addComponent(what, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(counterText, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(firedTransitionText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(used, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(229, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Uses JList as stack.
    // JList save not necessary?!
    private DefaultListModel save = new DefaultListModel<>();
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String selectedString = onTransition.getSelectedItem();

        if(onTransition.getItemCount()==0 && selectedString == null){
            JOptionPane.showMessageDialog(null, "No transition turned on.");
        }
        if(selectedString == null && onTransition.getItemCount()>0){
            JOptionPane.showMessageDialog(null, "No transition selected.\n"
                    +                           "     Please select!");
        }
        else{
            onTransition.remove(selectedString);
            save.add(0, selectedString);
            offTransition.add(selectedString, 0);
            transitionList.remove(selectedString);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param evt 
     */
    private void algoSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoSelectActionPerformed
        // TODO add your handling code here:
        String selectedCombo = algoSelect.getSelectedItem().toString();

    }//GEN-LAST:event_algoSelectActionPerformed
            
    
    private void onTransitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onTransitionActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_onTransitionActionPerformed

    /**
     * Clears all used maps and lists directly after program terminates.
     */
    private void clearMapsAndLists(){
        BreadthFirst.clearEnabledTransitions();
        BreadthFirst.clearMarkingHashMap();
        BreadthFirst.clearNodeslist();
        BreadthFirst.clearTBacktrack();
        BreadthFirst.clearUsedTransitions();
        BreadthFirst.clearUpdatedMarking();
        BreadthFirst.clearUpdateFrame();
        BreadthFirst.clearReachabilityNodes();
        BreadthFirst.clearVisitedNodes();
        HashSet<Transition> transitionSet = new HashSet<>();
        for (Place p : backupPN.places()) {
            if(p.inputs().size()>=1){
                for(Transition t : p.inputs()){
                    transitionSet.add(t);
                }
            }
            if(p.outputs().size()>=1){
                for(Transition t : p.outputs()){
                    transitionSet.add(t);
                }
            }
        }
        for(Transition t : transitionSet){
                t.setNotUsed(); //setting all transitions back to default
                
            }
    }
    /**
     * 
     * @return 
     */
    private boolean PushButton(){
        return pushed = true;
    }

   // Reset button off transition side.
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        String selectedString = offTransition.getSelectedItem();
        if(offTransition.getItemCount()==0 && selectedString == null){
            JOptionPane.showMessageDialog(null, "No transition turned off.");
        }
        if(selectedString == null && offTransition.getItemCount()>0){
            JOptionPane.showMessageDialog(null, "No transition selected.\n"
                    +                           "     Please select!");
        }
        else{
            offTransition.remove(selectedString);
            save.add(0, selectedString);
            onTransition.add(selectedString, 0);
        }
    }//GEN-LAST:event_jButton4ActionPerformed
    
    // Compute button. Magic should happen here. Delete transitions.
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // If program has been used already, clear all outputs.
        setSpinVal();
        BreadthFirst.clearUsedTransitions();
        clearMapsAndLists();
        
        BreadthFirst.clearTBacktrack();
        readTable();
        if(pushed==true){
            tryAgain.setText("");
            chosenAND.setText("");
            chooseButton.setText("Check if already used");
            BreadthFirst.clearUsedTransitions();
            BreadthFirst.clearTBacktrack();
            
            for(int i = 0; i < used.getItemCount(); i++){
                used.remove(0);
                used.removeAll();   
            }
            pushed =false;
        }
        pushed = true;
        PushButton();
        PetriNetFacade copyPN = this.pn;
        String selectedCombo = algoSelect.getSelectedItem().toString();
        // Getting transition to delete by iterating over list of transitions in 
        // Place object
        if(onTransition.getItemCount()==0){
            JOptionPane.showMessageDialog(null, "***No transition switched on.***\n"
                    +                           "   Computation not possible!");
        }
        JList<Transition> transitionList = new JList<>();
        if(selectedCombo == "None"){
            JOptionPane.showMessageDialog(null, "No algorithm selected.\n"
                    +                           "     Please select!");
        }

        /**
         * Checks if transitions are turned off.
         * If that's the case delete them from PN.
         * TODO: check if arcs need to be deleted too.
         */
        HashSet<Transition> knockout = new HashSet<>();
        if(offTransition.getItemCount()>0){
          // iterates over transitions in copied PN.
            for(Transition t : copyPN.transitions()){
                String searchTransition = t.toString();
                //Transition objTransition = t;
                for(int i = 0; i < offTransition.getItemCount(); i++){
                    String trans = offTransition.getItem(i);
                    // If transition strings (names) are equal
                    if(searchTransition == null ? trans == null : searchTransition.equals(trans)){
                        // Delete object transition from copied PN
                        // Add deleted Transition to knockout
                        knockout.add(t);
                        copyPN.removeProperty(trans);
                    }
                }
            }
        }
        if(possible == false){
            path = new Pathfinder(copyPN, start, target, capacities, null, null);
            path.getStatus();
        }
        //  Start and Target Hashmap change in if
        LOGGER.info("Requested computation of a path from start to target marking.");
        String algo = algoSelect.getSelectedItem().toString();
        if (algo.equals("Breadth First Search")) {
            path = new Pathfinder(copyPN, start, target, capacities, knockout, algo);
        }   else {
                path = new Pathfinder(copyPN, start, target, capacities, null, algo);
        }
        // If transition deactivated 
        if (!path.checkPIs(pinvs, start, target)&& offTransition.getItemCount() == 0) {
            LOGGER.warn("Aborting reachability analysis.");
            JOptionPane.showMessageDialog(this, "Start marking and target marking are incompatible. Sums for place invariants do not match.");            
            return;
        }  
        path.addListenerToAlgorithm(this);
        path.run();
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * Resets table in GUI
     */
    private void resetTable(){
        DefaultTableModel model = (DefaultTableModel) markingTable.getModel();
        HashMap<Place, Long> placesInTable = new HashMap<>();
        placesInTable.putAll(backUFacade.marking());
        for (int i = 0; i < markingTable.getRowCount(); ++i) {
            for(int j = 0; j < placesInTable.size(); j++){
                Place p = pn.findPlace(j); //gibt place aus           
                model.setValueAt(placesInTable.get(p), j, 1);
            LOGGER.debug("Updated values for Place " + ((Place) markingTable.getValueAt(i, 0)).getProperty("name"));
            }
        }
    }
    
    /**
     * Updates table in JFrame
     */
    private void updateMarkings() {
        HashMap<Place, Long> placesInTable = new HashMap<>();
        DefaultTableModel model = (DefaultTableModel) markingTable.getModel();
        placesInTable.putAll(BreadthFirst.getUpdateFrame());
        System.out.println("Update Frame: "+BreadthFirst.getUpdateFrame()+" Updated Marking "+BreadthFirst.getUpdatedMarking());
        for (int i = 0; i < markingTable.getRowCount(); ++i) {
            for(int j = 0; j < placesInTable.size(); j++){
            //for(int j = 0; j < markingTable.getRowCount(); j++){
                Place p = pn.findPlace(j); //gibt place aus           
                model.setValueAt(placesInTable.get(p), j, 1);
               // System.out.println("Ausgabe: "+p+" table: "+placesInTable.get(p)+" row: "+j+" Model: "+model.getValueAt( j, 1));
               // System.out.println("Ausgabe: "+p+" table: "+placesInTable.get(p)+" row: "+i+" Model: "+model.getValueAt( i, 1));
                for(Map.Entry<Place, Long> entry : placesInTable.entrySet()){
                    //System.out.println("ENTRY: "+entry.getKey()+" "+entry.getValue());
                }
                LOGGER.debug("Updated values for Place " + ((Place) markingTable.getValueAt(i, 0)).getProperty("name"));
            }
        }
        LOGGER.info("Successfully updated markings from table.");
    }

    
    /**
     * Restores petri net.
     * First clears all lists then fills them with original pn components.
     * @param evt 
     */
    private void restorePNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restorePNActionPerformed
        LOGGER.info("Restore petri net.");
        resetTable();
        clearMapsAndLists();
        backUFacade = pn;
        chooseButton.setText("Choose transition");
        tryAgain.setText("");
        chosenAND.setText("");
        transitionList.removeAll();
        onTransition.removeAll();
        offTransition.removeAll();
        
        counterText.setText("");
        HashSet<Transition> transitionSet = new HashSet<>();
        for (Place p : backupPN.places()) {
            System.out.println("CLEAR: "+p.toString());
            if(p.inputs().size()>=1){
                for(Transition t : p.inputs()){
                    transitionSet.add(t);
                }
            }
            if(p.outputs().size()>=1){
                for(Transition t : p.outputs()){
                    transitionSet.add(t);
                }
            }
        }
        for(Transition t : transitionSet){
                t.setNotUsed(); //setting all transitions back to default
                onTransition.add(t.toString());
                transitionList.add(t.toString());
            }
        firedTransitionText.setText("Fired transitions: ");
        what.setText("");
        for(int i = 0; i < used.getItemCount(); i++){
            used.remove(0);
            used.removeAll();
        }
        PlaceTitel.setForeground(Color.BLACK);
        PlaceTitel.setText("Places and token before firing");
       
        for(int i = 0; i < used.getItemCount(); i++){
            used.remove(0);
            used.removeAll();   
        }
        clearMapsAndLists();
    }//GEN-LAST:event_restorePNActionPerformed

    /**
     * Sets string back.
     */
    private static String selectedTargetNodeVisible = "";
    
    /**
     * 
     * @return 
     */
    public static String getSelectedTargetNode(){
        return selectedTargetNodeVisible;
    }
    
    private void usedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usedActionPerformed
    
   
    /**
     * 
     * @param evt 
     */
    private void chooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseButtonActionPerformed
        String selectedTransition = transitionList.getSelectedItem();
        chooseText.setText("Chosen transition: "+selectedTransition);
        if(selectedTransition == ""){
            JOptionPane.showMessageDialog(null, "No transition selected.\n"
                    +                           "     Please select!");
        }
        boolean hasBeenUsed = false;
        resetTransition = false;
        setForcedTransitionTrue();
        for(Transition t : transitions){
            if(t.toString()== selectedTransition){
                chooseTransition = t;
            }
        }
        // When computed without forced transition. Check if transition has 
        // been used
        ArrayList<Transition> search = new ArrayList<>();
        if(BreadthFirst.forcedTransitionBacktrack().size() > 0){
            search.addAll(BreadthFirst.forcedTransitionBacktrack());
        }
        if(BreadthFirst.forcedTransitionBacktrack().size()== 0){
            search.addAll(BreadthFirst.getTransitions());
        }
        if(used.getItemCount() > 0){
            System.out.println("HASbeenUSED: "+hasBeenUsed+" pushed: "+pushed+" List: "+search);
            //for(Transition t : search){
                for(int i = 0; i < used.getItemCount(); i++){
               // System.out.println("Transition in GUI: "+t+" selected: "+selectedTransition);
                if(used.getItem(i).toString()==selectedTransition ){
                    hasBeenUsed = true;
                    chosenAND.setForeground(new Color(0, 102, 0));
                    chosenAND.setText("Transition has been used.");
                    tryAgain.setText("");
                }   
            }
            if(hasBeenUsed == false){
                chosenAND.setForeground(new Color(204, 0, 0));
                chosenAND.setText("Transition has NOT been used. ");
                tryAgain.setText("Compute again with musthave transition.");
            }
        }
    }//GEN-LAST:event_chooseButtonActionPerformed
    /**
     * 
     * @param evt 
     */
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        chooseText.setText("Chosen transition: ");
        chooseTransition = null;
        resetTransition = true;
        setForcedTransitionFalse();
    }//GEN-LAST:event_jButton2ActionPerformed

    
    private void transitionListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transitionListActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_transitionListActionPerformed

    private void transitionListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_transitionListMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_transitionListMouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        HelpPanel helpPanel = new HelpPanel();
        helpPanel.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void offTransitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offTransitionActionPerformed
        
    }//GEN-LAST:event_offTransitionActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        setStopProgramTrue();
    }//GEN-LAST:event_stopButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ConstraintFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ConstraintFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ConstraintFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ConstraintFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new ConstraintFrame().setVisible(true);
                
                 
            }
            
             
        });
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PlaceTitel;
    private javax.swing.JLabel Reachability;
    private javax.swing.JComboBox<String> algoSelect;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton chooseButton;
    private javax.swing.JLabel chooseText;
    private javax.swing.JLabel chosenAND;
    private javax.swing.JLabel counterText;
    private javax.swing.JLabel firedTransitionText;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable markingTable;
    private java.awt.List offTransition;
    private java.awt.List onTransition;
    private javax.swing.JButton restorePN;
    private javax.swing.JSpinner spinner;
    private javax.swing.JButton stopButton;
    private java.awt.List transitionList;
    private javax.swing.JLabel tryAgain;
    private java.awt.List used;
    private javax.swing.JLabel what;
    // End of variables declaration//GEN-END:variables

    /**
     * 
     * @param b 
     */
    private void lock(boolean b) {
        markingTable.setEnabled(!b);
        algoSelect.enableInputMethods(!b);
    }
    
    /**
     * 
     * @param tList 
     */
    public void setUsedTransitionTable(ArrayList<Transition> tList){
        for(Transition t : tList){
            used.add(t.toString());
        }
    }
    
    
    
    /**
     * Returns number of all nodes whose token changed throughout computing.
     * @return 
     */
    public int getNumberVisitedNodes(){
        return BreadthFirst.getVisitiedNodes().size();
    } 
    
    /**
     * Returns number of fired transitions.
     * @return 
     */
    public int getNumberFiredTransitions(){
        return BreadthFirst.getTransitions().size();
    }
    
    /**
     * 
     * @return 
     */
    public ArrayList<Transition> getAllTransitions(){
        ArrayList<Transition> allTransitions = new ArrayList<>();
        for(Transition t : BreadthFirst.getTransitions()){
                if(!allTransitions.contains(t)){
                    allTransitions.add(t);
                }
            }
        return allTransitions;
    }
    
    /**
     * Used to visualize all nodes (whose token changed throughout computing)
     * @return 
     */
    public HashMap<Place, Long> getAllVisitedNodes(){
        HashMap<Place, Long> allUsedNodes = new HashMap<>();
        for(Map.Entry<Place,Long> entry : BreadthFirst.getVisitiedNodes().entrySet()){
                allUsedNodes.put(entry.getKey(), entry.getValue());
            }
        return allUsedNodes;
    }
    
    /**
     * Visualizes all fired transition in GUI
     * @param transitions 
     */
    public void enumerateUsedTransitions(ArrayList<Transition> transitions){
        for(Transition t : transitions){
            used.add(t.toString());
        }
    }
  
    /**
     * @param e 
     * Visualizes results of computed calculation 
     */
    @Override
    public void update(ReachabilityEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        ArrayList<Transition> showTransition = new ArrayList<>();
        HashMap<Place, Long> showPlaces = new HashMap<>();
        String placeTextSuccess = "Transition has been used.";
        String placeTextFailure = "Transition has NOT been used.";
        showTransition.addAll(BreadthFirst.getTransitions());
        showPlaces.putAll(BreadthFirst.getVisitiedNodes());
        System.out.println("AUSGABE: "+showPlaces );
        if(getForcedTransition()== true){
            if(e.getStatus()== SUCCESS){
                chosenAND.setForeground(new Color(0, 102, 0));
                chosenAND.setText("Transition has been used.");
            }
            if(e.getStatus() ==  FAILURE){
                chosenAND.setForeground(new Color(204, 0, 0));
                chosenAND.setText("Transition has NOT been used");
            }
            if(e.getStatus() == ABORTED){
               chosenAND.setForeground(new Color(204, 0, 0));
               chosenAND.setText("Transition has NOT been used");  
            }
        }
            switch (e.getStatus()) {
               
                case STARTED: // Should be fired after Compute or either of the full-Buttons was pressed and the algorithm is started.
                    lock(true); // Ensures that only one algorithm runs at a time.
                    break;
                
                case SUCCESS: // Fired when an algorithm successfully finds the target marking.
                    lock(false);
                    // Should probably handle displaying output
                    LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before successfully finding target marking.");
                    what.setForeground(new Color(0, 102, 0));
                   
                    what.setText("[Success] Target marking found!");
                    firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                    PlaceTitel.setForeground(new Color(0, 0, 153));
                    PlaceTitel.setText("Places and token after firing.");
                    counterText.setForeground(new Color(0, 0, 153));
                    counterText.setText("#Reachabilitynodes: "+ e.getSteps()+1);
                    updateMarkings();
                    enumerateUsedTransitions(showTransition);
                    clearMapsAndLists();
                    setStopProgramFalse();
                    
                    break;
                case FAILURE: // Fired when an algorithm fails to find the target marking.
                    lock(false);
                    // Should output failure.
                    LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before failure was determined.");
                    what.setForeground(new Color(204, 0, 0));
                    what.setText("[Failure] Target marking not reachable!");
                    PlaceTitel.setForeground(new Color(0, 0, 153));
                    PlaceTitel.setText("Places and token after firing.");
                    firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                    counterText.setForeground(new Color(0, 0, 153));
                    counterText.setText("#Reachabilitynodes: "+ e.getSteps()+1);
                    updateMarkings();
                    setUsedTransitionTable(showTransition);
                    clearMapsAndLists();
                    setStopProgramFalse();
                    possible = true;
                    break;
                case PROGRESS: // Fired every 100 expanded nodes.
                    LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes so far.");
                    what.setForeground(new Color(102, 0, 253));
                    what.setText("Progress");
                    PlaceTitel.setForeground(new Color(0, 0, 153));
                    PlaceTitel.setText("Places and token after firing.");
                    firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                    updateMarkings();
                    setUsedTransitionTable(showTransition);
                    break;
                case SPINNER: // Fired when an algorithm fails to find the target marking.
                    lock(false);
                    // Should output failure.
                    LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before failure was determined.");
                    what.setForeground(new Color(204, 0, 0));
                    what.setText("[Failure] MAX number of chosen transition fired: "+spinVal+" ");
                    PlaceTitel.setForeground(new Color(0, 0, 153));
                    PlaceTitel.setText("Places and token after firing.");
                    firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                    counterText.setForeground(new Color(0, 0, 153));
                    counterText.setText("#Reachabilitynodes: "+ e.getSteps()+1);
                    updateMarkings();
                    setUsedTransitionTable(showTransition);
                    clearMapsAndLists();
                    setStopProgramFalse();
                    possible = true;
                    break;
                case FINISHED: // Fired by FullReachability and FullCoverability on completion
                    lock(false);
                    LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes to complete the graph.");
                    what.setForeground(new Color(0, 102, 0));
                    what.setText("Finished");
                   
                    firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                    updateMarkings();
                    setUsedTransitionTable(showTransition);
                    clearMapsAndLists();
                    setStopProgramFalse();
                    break;
                    
                case ABORTED: // Fired when an algorithm fails to find the target marking.
                    lock(false);
                    // Should output failure.
                    LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before failure was determined.");
                    what.setForeground(new Color(204, 0, 0));
                    what.setText("[Aborted] Target node not reachable! One or more places not reachable");
                   
                    //firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                    setStopProgramFalse();
                    updateMarkings();
                    setUsedTransitionTable(showTransition);
                    clearMapsAndLists();
                    break;
                    
                case STOPED:
                    lock(false);
                    // Should probably handle displaying output
                    what.setForeground(new Color(102, 0, 153));
                   
                    what.setText("[STOPED] Prgram stopped manually!");
                    firedTransitionText.setText("Fired transitions: #"+used.getItemCount());
                    PlaceTitel.setForeground(new Color(0, 0, 153));
                    PlaceTitel.setText("Places and token after firing.");
                    counterText.setForeground(new Color(0, 0, 153));
                    counterText.setText("#Reachabilitynodes: "+ e.getSteps()+1);
                    updateMarkings();
                    enumerateUsedTransitions(showTransition);
                    clearMapsAndLists();
                    setStopProgramFalse();
                    
                    break;
                default:
                    break;
            }

        }
    
}
