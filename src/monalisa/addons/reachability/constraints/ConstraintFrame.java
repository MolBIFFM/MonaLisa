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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import static jdk.internal.org.jline.keymap.KeyMap.key;
import monalisa.addons.reachability.AlgorithmRunner;
import monalisa.addons.reachability.Pathfinder;
import monalisa.addons.reachability.ReachabilityDialog;
import monalisa.addons.reachability.ReachabilityEvent;
import static monalisa.addons.reachability.ReachabilityEvent.Status.ABORTED;
import static monalisa.addons.reachability.ReachabilityEvent.Status.FAILURE;
import static monalisa.addons.reachability.ReachabilityEvent.Status.FINISHED;
import static monalisa.addons.reachability.ReachabilityEvent.Status.PROGRESS;
import static monalisa.addons.reachability.ReachabilityEvent.Status.STARTED;
import static monalisa.addons.reachability.ReachabilityEvent.Status.SUCCESS;
import monalisa.addons.reachability.ReachabilityListener;
import monalisa.addons.reachability.ReachabilityNode;
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
    // What is tht number for?
    private static final long serialVersionUID = -8541347764965669414L;
    //private AlgorithmRunner algorithmRunner;
    public Pathfinder path;
    public PetriNetFacade pn;
    private PetriNetFacade backUFacade;
    private PetriNet backupPN;
   // private BreadthFirst bf;
    private static final Logger LOGGER = LogManager.getLogger(ConstraintFrame.class);

    private HashMap<Place, Long> start;
    private HashMap<Place, Long> target;
    private final HashMap<Place, Long> capacities; 
    private final PInvariants pinvs;
    private HashMap<Place, Long> eStart = null;
    private HashMap<Place, Long> eTarget = null;
    private boolean computed = false;
    private boolean pushed = false;
    private HashSet<Transition> transitions = new HashSet<>();
    
    public static Transition chooseTransition = null;
  
    

    /**
     * ConstraintFrame
     * @param pn
     * @param start
     * @param pinvs
     * @param target
    */
    public ConstraintFrame(PetriNetFacade pn, HashMap<Place, Long> start, HashMap<Place, Long> target, PInvariants pinvs) {
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
        //reachabilityDialog = new ReachabilityDialog(this.pn, this.start, target, this.pinvs);
        //ConstraintFrame = new ConstraintFrame(this.pn, this.start, this.target, this.pinvs);
        String[] columnsNames = {"Transition", "PrePlace", "PostPlace"};
        // Fill combo boxes with places
        //DefaultListModel model = new DefaultListModel();
        DefaultTableModel model = (DefaultTableModel) markingTable.getModel();
        for (Place p : pn.places()) {
            for(Transition t : p.outputs()){
            model.addRow(new Object[]{
                p,
                start.get(p),
                pn.getArc(p, t).weight(),
               
            });
                System.out.println("C: "+ pn.getArc(p, t).weight());
        }
        }
        HashSet<Transition> transitionSet = new HashSet<>();
        for (Place p : pn.places()) {
            //model.addElement(p.inputs().getFirst());
            
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
            
        
        for(HashMap.Entry<Place, Long> entry :this.start.entrySet()){
            startNode.addItem(entry.toString());
            for(Transition in: entry.getKey().inputs()){
                System.out.println("OBEN: "+in);
               
                
            }
        }
        //DefaultListModel listMod = (DefaultListModel) off.getModel();
        for(HashMap.Entry<Place, Long> entry :this.target.entrySet()){
            sinkNode.addItem(entry.toString());
            for(Transition out : entry.getKey().outputs()){
                String sTrans = out.toString();
           }
        
        }
        
        transitions = transitionSet;
        algoSelect.setActionCommand("Breadth First Search");
        algoSelect.setActionCommand("Best First Search");
        algoSelect.setActionCommand("A*");
  
        LOGGER.info("Successfully initialized ConstraintFrame.");
        


        
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
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        startNode = new javax.swing.JComboBox<>();
        sinkNode = new javax.swing.JComboBox<>();
        onTransition = new java.awt.List();
        offTransition = new java.awt.List();
        restorePN = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        markingTable = new javax.swing.JTable();
        PlaceTitel = new javax.swing.JLabel();
        what = new javax.swing.JLabel();
        used = new java.awt.List();
        firedTransitionText = new javax.swing.JLabel();
        nodes = new java.awt.List();
        visitedNodeText = new javax.swing.JLabel();
        transitionList = new java.awt.List();
        jLabel8 = new javax.swing.JLabel();
        chooseButton = new javax.swing.JButton();
        chooseText = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        chosenAND = new javax.swing.JLabel();
        tryAgain = new javax.swing.JLabel();

        setMinimumSize(getSize());
        setPreferredSize(new java.awt.Dimension(667, 800));
        setSize(new java.awt.Dimension(667, 800));

        Reachability.setFont(new java.awt.Font("Dialog", 0, 20)); // NOI18N
        Reachability.setText("Reachability with constraints");

        jLabel1.setText("Switched OFF transitions");

        jLabel2.setText("Choose algorithm");

        jButton1.setText("SwitchOFF");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton4.setText("SwitchON");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel4.setText("Switched ON transitions");

        jButton5.setText("Compute");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        algoSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Breadth First Search", "Best First Search", "A*" }));
        algoSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSelectActionPerformed(evt);
            }
        });

        jLabel3.setText("Choose start node");

        jLabel5.setText("Choose sink node");

        startNode.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                startNodeItemStateChanged(evt);
            }
        });
        startNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startNodeActionPerformed(evt);
            }
        });

        sinkNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sinkNodeActionPerformed(evt);
            }
        });

        onTransition.setMaximumSize(new java.awt.Dimension(80, 60));
        onTransition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onTransitionActionPerformed(evt);
            }
        });

        offTransition.setMaximumSize(new java.awt.Dimension(80, 60));

        restorePN.setText("Restore PN");
        restorePN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restorePNActionPerformed(evt);
            }
        });

        markingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Place [Name]", "Available [#Token]", "Arc P=>T [Weight]"
            }
        ));
        jScrollPane1.setViewportView(markingTable);

        PlaceTitel.setText("Place and token before fireing");

        what.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N

        used.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usedActionPerformed(evt);
            }
        });

        firedTransitionText.setText("Fired transitions:");

        nodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodesActionPerformed(evt);
            }
        });

        visitedNodeText.setText("Visited Nodes [completely]:");

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Reachability)
                .addGap(212, 212, 212))
            .addGroup(layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(what, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(104, 104, 104))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(PlaceTitel, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                                .addGap(43, 43, 43)
                                .addComponent(tryAgain, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(31, 31, 31)
                                        .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                                            .addComponent(startNode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(onTransition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(offTransition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sinkNode, 0, 147, Short.MAX_VALUE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(algoSelect, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(firedTransitionText, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(used, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(nodes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(restorePN, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                                    .addComponent(visitedNodeText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(chooseText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(transitionList, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(chooseButton, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(chosenAND, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(60, 60, 60))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(88, 88, 88)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startNode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sinkNode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(Reachability)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4))
                    .addComponent(onTransition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(offTransition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(50, 50, 50)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(transitionList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chooseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chooseText)
                    .addComponent(chosenAND))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PlaceTitel)
                    .addComponent(tryAgain, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(algoSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(what, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(visitedNodeText, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(firedTransitionText))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(nodes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(used, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(restorePN))
                .addGap(41, 41, 41))
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
        }
        
    }//GEN-LAST:event_jButton1ActionPerformed
    
    
   
    /**
     * actually doesn't do anything. Hasn't been used
     * @param evt 
     */
    private void algoSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoSelectActionPerformed
        // TODO add your handling code here:
        String selectedCombo = algoSelect.getSelectedItem().toString();
        switch (selectedCombo) {
            case "Breadth First Search":
                
                System.out.println("Breadth First Search");
                break;
            case "Best First Search":
                System.out.println("Best First Search");

                break;
            case "A*":
                System.out.println("A*");
                break;
           
            default:
                throw new AssertionError();
        }

    }//GEN-LAST:event_algoSelectActionPerformed
            
    
    private void startNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startNodeActionPerformed
        // Get selected startNode
        String selectedStartNode = startNode.getSelectedItem().toString();
        // Use start Node in Algorithm
        
    }//GEN-LAST:event_startNodeActionPerformed

    private void startNodeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_startNodeItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_startNodeItemStateChanged

    private void onTransitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onTransitionActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_onTransitionActionPerformed

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
        if(pushed==true){
            //restorePNActionPerformed(evt);
            tryAgain.setText("");
            chosenAND.setText("");
            chooseButton.setText("Check if already used");

            //used.clear();
            //nodes.clear();
            for(int i = 0; i < nodes.getItemCount(); i++){
            nodes.remove(0);
            nodes.removeAll();
            
        }
            for(int i = 0; i < used.getItemCount(); i++){
                used.remove(0);
                used.removeAll();   

            }
            pushed =false;
        }
        
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
        for(int i=0; i<copyPN.marking().size();i++){
            System.out.println("copyPN: "+copyPN.findPlace(i)+", Transiton: "+copyPN.transitions().iterator().next());
        }

        //  Start and Target Hashmap change in if

        LOGGER.info("Requested computation of a path from start to target marking.");
        String algo = algoSelect.getSelectedItem().toString();
        if (algo.equals("Breadth First Search")) {
            try {
                String trimStart = startNode.getSelectedItem().toString().substring(0, (startNode.getSelectedItem().toString().indexOf("=")));
                for(HashMap.Entry<Place, Long> entry : start.entrySet()){
                    if(trimStart == null ? entry.getKey().toString() == null : trimStart.equals(entry.getKey().toString())){
                        HashMap<Place, Long> newStart = new HashMap<>();
                        newStart.put(entry.getKey(), entry.getValue());
                        eStart = newStart;
                    }

                }
                String trimEnd = sinkNode.getSelectedItem().toString().substring(0, (sinkNode.getSelectedItem().toString().indexOf("=")));
                for(HashMap.Entry<Place, Long> entry : target.entrySet()){
                    if(trimEnd == null ? entry.getKey().toString() == null : trimEnd.equals(entry.getKey().toString())){
                        HashMap<Place, Long> newTarget = new HashMap<>();
                        newTarget.put(entry.getKey(), entry.getValue());
                        eTarget = newTarget;
                    }
                }
                path = new Pathfinder(copyPN, start, target, capacities, knockout, algo, eStart, eTarget);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(ConstraintFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            path = new Pathfinder(copyPN, start, target, capacities, null, algo);
        }
        if (!path.checkPIs(pinvs, start, target)) {
            LOGGER.warn("Aborting reachability analysis.");
            JOptionPane.showMessageDialog(this, "Start marking and target marking are incompatible. Sums for place invariants do not match.");            
            return;
        }  
        path.addListenerToAlgorithm(this);
        path.run();
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * Updates table in JFrame
     */
    private void updateMarkings() {
        
        DefaultTableModel model = (DefaultTableModel) markingTable.getModel();
        
        for (int i = 0; i < markingTable.getRowCount(); ++i) {
            for(int j = 0; j < BreadthFirst.getUpdateFrame().size(); j++){
                Object keyC = markingTable.getValueAt(0, i);
                Place p = pn.findPlace(j); //gibt place aus
             
                model.setValueAt(BreadthFirst.getUpdateFrame().get(p), j, 1);
               // model.setValueAt(BreadthFirst.getUpdateFrame().get(p), j, 2);
               for(Map.Entry<Place, Long> entry : BreadthFirst.getUpdateFrame().entrySet()){
                   model.setValueAt(pn.getArc(entry.getKey(), entry.getKey().outputs().getFirst()).weight(), j, 2);
               }
            LOGGER.debug("Updated values for Place " + ((Place) markingTable.getValueAt(i, 0)).getProperty("name"));
            }
        }
        
        LOGGER.info("Successfully updated markings from table.");
    }
    
    public void useUpdateMarking(){
        updateMarkings();
    }
    
   
    
    // Restore PN.
    private void restorePNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restorePNActionPerformed
        // TODO add your handling code here:
        // First clear all panels, then fill them again
        System.out.println("RESTORE");
        backUFacade = pn;
        chooseButton.setText("Choose transition");
        tryAgain.setText("");
        chosenAND.setText("");
        for(int i = 0; i<onTransition.getItemCount();i++){
            onTransition.remove(onTransition.getItem(i));
            onTransition.removeAll();
            
        }
        
        for(int i = 0; i < offTransition.getItemCount(); i++){
            if(offTransition.getItemCount()>0){
            offTransition.removeAll();
            }
        }
        HashSet<Transition> transitionSet = new HashSet<>();
        for (Place p : backupPN.places()) {
            //model.addElement(p.inputs().getFirst());
            
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
        for(Transition t : transitionSet){
                onTransition.add(t.toString());
            }
        visitedNodeText.setText("Visited nodes [CPLT]:");
        firedTransitionText.setText("Fired transitions: ");
        what.setText("");
        
        for(int i = 0; i < used.getItemCount(); i++){
            used.remove(0);
            used.removeAll();
            
        }
        PlaceTitel.setForeground(Color.BLACK);
        PlaceTitel.setText("Places and token before firing");
        
        for(int i = 0; i < nodes.getItemCount(); i++){
            nodes.remove(0);
            nodes.removeAll();
            
        }
        for(int i = 0; i < used.getItemCount(); i++){
            used.remove(0);
            used.removeAll();   
            
        }
    
    }//GEN-LAST:event_restorePNActionPerformed

    private void sinkNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sinkNodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sinkNodeActionPerformed

    private void usedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usedActionPerformed

    private void chooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseButtonActionPerformed
        String selectedTransition = transitionList.getSelectedItem();
        chooseText.setText("Chosen transition: "+selectedTransition);
        boolean hasBeenUsed = false;

        for(Transition t : transitions){
            if(t.toString()== selectedTransition){
                chooseTransition = t;
            }
        }
        
        
        if(pushed==true){
            for(Transition t : BreadthFirst.usedTransitions){
                if(t.toString()==selectedTransition ){
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

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        chooseText.setText("Chosen transition: ");
        chooseTransition = null;
    }//GEN-LAST:event_jButton2ActionPerformed

    private void nodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nodesActionPerformed

    private void transitionListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transitionListActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_transitionListActionPerformed

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
    private javax.swing.JLabel firedTransitionText;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable markingTable;
    private java.awt.List nodes;
    private java.awt.List offTransition;
    private java.awt.List onTransition;
    private javax.swing.JButton restorePN;
    private javax.swing.JComboBox<String> sinkNode;
    private javax.swing.JComboBox<String> startNode;
    private java.awt.List transitionList;
    private javax.swing.JLabel tryAgain;
    private java.awt.List used;
    private javax.swing.JLabel visitedNodeText;
    private javax.swing.JLabel what;
    // End of variables declaration//GEN-END:variables

    private void lock(boolean b) {
        
        markingTable.setEnabled(!b);
        
        algoSelect.enableInputMethods(!b);
    }
    
    public void setUsedTransitionTable(ArrayList<Transition> tList){
        for(Transition t : tList){
            used.add(t.toString());
        }
       
    }
    
    public void setVisitedNodes(HashMap<Place, Long> map){
        for(Map.Entry<Place, Long> entry : map.entrySet()){
            nodes.add(entry.getKey().toString()+"      ID: "+entry.getKey().id());
            
        }
    }
    
    public int getNumberVisitedNodes(){
        return BreadthFirst.visitedNodes.size();
    }
    
    public int getNumberFiredTransitions(){
        return BreadthFirst.usedTransitions.size();
    }
    
    public ArrayList<Transition> getAllTransitions(){
        ArrayList<Transition> allTransitions = new ArrayList<>();
        for(ArrayList<Transition> a : BreadthFirst.returnForAllUsedTransitions()){
            for(Transition t : a){
                if(!allTransitions.contains(t)){
                    allTransitions.add(t);
                }
            }
        }
        return allTransitions;
    }
    
    public HashMap<Place, Long> getAllVisitedNodes(){
        HashMap<Place, Long> allUsedNodes = new HashMap<>();
        for(HashMap<Place, Long> v : BreadthFirst.returnForAllVisitedNodes()){
            for(Map.Entry<Place,Long> entry : v.entrySet()){
                allUsedNodes.put(entry.getKey(), entry.getValue());
            }
            
        }
        return allUsedNodes;
    }
    
    /**
     * @author Marcel Germann
     * @param e 
     */
    @Override
    public void update(ReachabilityEvent e) {
        
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        
        System.out.println("STATUS: "+e.getStatus()+" "+e.getBacktrack());
        switch (e.getStatus()) {
            case ABORTED: // Aborted should be fired after stopButton was pressed and the thread was successfully canceled.
                lock(false);
                // Do a popup that says things have been terminated at X steps?
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before execution was aborted.");
                //progressLabel.setText("Number of nodes expanded before execution was aborted: " + Integer.toString(e.getSteps()));
                what.setForeground(new Color(204, 0, 0));
                what.setText("Aborted");
                PlaceTitel.setForeground(new Color(0, 0, 153));
                PlaceTitel.setText("Places and token after firing.");
                firedTransitionText.setText("Fired transitions: "+getNumberFiredTransitions());
                visitedNodeText.setText("Visited nodes [CPLT]: "+getNumberVisitedNodes());
                if(BreadthFirst.returnForAllUsedTransitions().isEmpty()){
                    updateMarkings();
                    setUsedTransitionTable(BreadthFirst.usedTransitions);
                    setVisitedNodes(BreadthFirst.visitedNodes);
                    break; 
                }
                
                
                
            case STARTED: // Should be fired after Compute or either of the full-Buttons was pressed and the algorithm is started.
                lock(true); // Ensures that only one algorithm runs at a time.
                break;
            case EQUALNODE:
                lock(false);
               // progressLabel.setForeground(new Color(0, 0, 153));
                //progressLabel.setText("Start- and targetnode are equal!");
                what.setForeground(new Color(0, 102, 0));
                what.setText("[Success]"); 
                firedTransitionText.setText("Fired transitions: "+ getNumberFiredTransitions());
                visitedNodeText.setText("Visited nodes [CPLT]: "+getNumberVisitedNodes());
                setVisitedNodes(BreadthFirst.visitedNodes);
                setUsedTransitionTable(BreadthFirst.usedTransitions);
                break;
            case SUCCESS: // Fired when an algorithm successfully finds the target marking.
                lock(false);
                ArrayList<Transition> path = e.getBacktrack();
                updateMarkings();
                // Should probably handle displaying output
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before successfully finding target marking.");
                //progressLabel.setText("Number of nodes expanded before target marking was successfully found: " + Integer.toString(e.getSteps()));
                what.setForeground(new Color(0, 102, 0));
                what.setText("[Success] Target node reached!");
                PlaceTitel.setForeground(new Color(0, 0, 153));
                PlaceTitel.setText("Places and token after firing");
                firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                visitedNodeText.setText("Visited nodes [CPLT]: #"+getNumberVisitedNodes());
                if(BreadthFirst.returnForAllUsedTransitions().isEmpty()){
                    updateMarkings();
                    setUsedTransitionTable(BreadthFirst.usedTransitions);
                    setVisitedNodes(BreadthFirst.visitedNodes);
                    break; 
                }
                if(!BreadthFirst.returnForAllUsedTransitions().isEmpty()){
                    updateMarkings();
                    setUsedTransitionTable(getAllTransitions());
                    setVisitedNodes(getAllVisitedNodes());
                }
            case FAILURE: // Fired when an algorithm fails to find the target marking.
                lock(false);
                // Should output failure.
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before failure was determined.");
                //progressLabel.setText("Number of nodes expanded before failure was determined: " + Integer.toString(e.getSteps()));
                what.setForeground(new Color(204, 0, 0));
                what.setText("[Failure] Target node not reachable!");
                PlaceTitel.setForeground(new Color(0, 0, 153));
                PlaceTitel.setText("Places and token after firing");
                firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                visitedNodeText.setText("Visited nodes [CPLT]: #"+getNumberVisitedNodes());
                updateMarkings();
                setUsedTransitionTable(BreadthFirst.usedTransitions);
                if(eStart.equals(eTarget)){
                    nodes.add("[Startnode NOT visited as target]");
                }
                setVisitedNodes(BreadthFirst.visitedNodes);
                break;
            case PROGRESS: // Fired every 100 expanded nodes.
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes so far.");
               // progressLabel.setText("Number of nodes expanded so far: " + Integer.toString(e.getSteps()));
                what.setForeground(new Color(102, 0, 253));
                what.setText("Progress");
                PlaceTitel.setForeground(new Color(0, 0, 153));
                PlaceTitel.setText("Places and token after firing");
                firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                visitedNodeText.setText("Visited nodes [CPLT]: #"+getNumberVisitedNodes());
                setUsedTransitionTable(BreadthFirst.usedTransitions);
                break;
            case FINISHED: // Fired by FullReachability and FullCoverability on completion
                lock(false);
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes to complete the graph.");
                //progressLabel.setText("Number of nodes expanded until completion: " + Integer.toString(e.getSteps()));
                // Somehow display the graph? Otherwise this doesn't do much.
                what.setForeground(new Color(0, 102, 0));
                what.setText("Finished");
                PlaceTitel.setForeground(new Color(0, 0, 153));
                PlaceTitel.setText("Places and token after firing");
                firedTransitionText.setText("Fired transitions: #"+getNumberFiredTransitions());
                visitedNodeText.setText("Visited nodes [CPLT]: #"+getNumberVisitedNodes());
                updateMarkings();
                setVisitedNodes(BreadthFirst.visitedNodes);
                setUsedTransitionTable(BreadthFirst.usedTransitions);
                break;
            default:
                break;
        }
    
    }
}
