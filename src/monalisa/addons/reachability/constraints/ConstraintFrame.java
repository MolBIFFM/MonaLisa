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
    private PetriNet backupPN;
    private static final Logger LOGGER = LogManager.getLogger(ConstraintFrame.class);

    private HashMap<Place, Long> start;
    private HashMap<Place, Long> target;
    private final HashMap<Place, Long> capacities; 
    private final PInvariants pinvs;
    private HashMap<Place, Long> eStart = null;
    private HashMap<Place, Long> eTarget = null;
   // private HashSet<Transition> transitions;
    
    // Backtrack stil includes deleted Transition-> Deleting not working
    
    // Deleted  reachability Object -> not functioning now
  
    

    /**
     * Creates new form ConstraintFrame
     * @param pn
     * @param start
     * @param pinvs
     * @param target
    */
    public ConstraintFrame(PetriNetFacade pn, HashMap<Place, Long> start, HashMap<Place, Long> target, PInvariants pinvs) {
        this.start = new HashMap<>();
        this.start.putAll(start);
        this.pn = pn;
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
            model.addRow(new Object[]{
                p,
                start.get(p),
                target.get(p),
                capacities.put(p, pn.getTokens(p))
            });
           // capacities.put(p, 0L); -> Not sure if capacities need to be 0
            System.out.println("CAPACITIES: "+capacities);
        }
        for (Place p : pn.places()) {
            //model.addElement(p.inputs().getFirst());
            onTransition.add(p.inputs().getFirst().toString());
            System.out.println("TESTING: "+p.inputs().getFirst());
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
        

        algoSelect.setActionCommand("Breadth First Search");
        algoSelect.setActionCommand("Best First Search");
        algoSelect.setActionCommand("A*");
        
        //aStarRButton.addActionListener(this);
        //aStarRButton.setActionCommand("A*");
        //breadthRButton.addActionListener(this);
        //breadthRButton.setActionCommand("Breadth First Search");
        //bestRButton.addActionListener(this);
        //bestRButton.setActionCommand("Best First Search");
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
        jLabel6 = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();
        what = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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

        onTransition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onTransitionActionPerformed(evt);
            }
        });

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
                "Place", "Starting Token Amount", "Target Token Amount"
            }
        ));
        jScrollPane1.setViewportView(markingTable);

        jLabel6.setText("Place overview");

        progressLabel.setText("Number of nodes extended: 0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Reachability)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                                            .addComponent(startNode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(onTransition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(36, 36, 36)
                                        .addComponent(offTransition, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(0, 144, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(sinkNode, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(algoSelect, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(restorePN, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(66, 66, 66))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(what, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(Reachability)
                .addGap(49, 49, 49)
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
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(30, 30, 30)
                        .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(onTransition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(offTransition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(31, 31, 31)
                .addComponent(jLabel6)
                .addGap(7, 7, 7)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(algoSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(progressLabel)
                .addGap(18, 18, 18)
                .addComponent(what)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(restorePN))
                .addGap(39, 39, 39))
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
        // TODO delete transition out of PN
        PetriNetFacade copyPN = this.pn;
        PetriNetFacade backUpPN = this.pn;
        String selectedCombo = algoSelect.getSelectedItem().toString();
        // Getting transition to delete by iterating over list of transitions in 
        // Place object
        System.out.println("PN BEFORE: "+copyPN.transitions().iterator().next());

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
                System.out.println("TRANSITION: "+t.id()+" ; "+t);
                String searchTransition = t.toString();
                //Transition objTransition = t;
                for(int i = 0; i < offTransition.getItemCount(); i++){
                    System.out.println("TASTYTAST: "+offTransition.getItem(i));
                    String trans = offTransition.getItem(i);
                    // If transition strings (names) are equal
                    if(searchTransition == null ? trans == null : searchTransition.equals(trans)){
                        // Delete object transition from copied PN
                        // Add deleted Transition to knockout
                        knockout.add(t);
                        copyPN.removeProperty(trans);
                        // Delete arcs
                        
                        
                        // PetriNet has remove Arc 
                        System.out.println("Number of Edges before: "+copyPN.getNumberOfEdges());
                        copyPN.deletePTArc(t.inputs().getFirst(), t);
                        copyPN.deleteTPArc(t, t.outputs().getFirst());
                        System.out.println("ARC Number of Edges: "+copyPN.getNumberOfEdges());
                        
                        
                    
                }
            }
                
          }
        }
        for(int i=0; i<copyPN.marking().size();i++){
            System.out.println("copyPN: "+copyPN.findPlace(i)+", Transiton: "+copyPN.transitions().iterator().next());
        }
        //Place testPlace = (Place) startNode.getSelectedItem();;
        
        updateMarkings();
        //  Start and Target Hashmap change in if
        System.out.println("Test getting Start: "+this.start+" and Target: "+this.target+" Capacities: "+capacities);
        
        LOGGER.info("Requested computation of a path from start to target marking.");
        String algo = algoSelect.getSelectedItem().toString();
        if (algo.equals("Breadth First Search")) {
            System.out.println("FEHLER in CONSTRAINT: "+start+" Target: "+target);
        String trimStart = startNode.getSelectedItem().toString().substring(0, (startNode.getSelectedItem().toString().indexOf("=")));
        System.out.println("STRING_STRIP: "+trimStart);
        for(HashMap.Entry<Place, Long> entry : start.entrySet()){
            System.out.println("PLACE: "+entry.getKey()+" Value: "+entry.getValue());
            System.out.println("VERGLEICH: "+trimStart);
            if(trimStart == null ? entry.getKey().toString() == null : trimStart.equals(entry.getKey().toString())){
                HashMap<Place, Long> newStart = new HashMap<>();
                newStart.put(entry.getKey(), entry.getValue());
                eStart = newStart;
                System.out.println("START_FOR: "+start);
            }
            
        }
        String trimEnd = sinkNode.getSelectedItem().toString().substring(0, (sinkNode.getSelectedItem().toString().indexOf("=")));
        System.out.println("STRING_STRIP: "+trimEnd);
        for(HashMap.Entry<Place, Long> entry : target.entrySet()){
            System.out.println("PLACE: "+entry.getKey()+" Value: "+entry.getValue());
            if(trimEnd == null ? entry.getKey().toString() == null : trimEnd.equals(entry.getKey().toString())){
                HashMap<Place, Long> newTarget = new HashMap<>();
                newTarget.put(entry.getKey(), entry.getValue());
                eTarget = newTarget;
                System.out.println("TARGET_FOR: "+ target);
            }
          
        }
            
            
            
            System.out.println("Test getting Start: "+start+" and Target: "+target+" Capacities: "+capacities);
            path = new Pathfinder(copyPN, start, target, capacities, knockout, algo, eStart, eTarget);
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
        
        
        System.out.println("FEHLER in CONSTRAINT: "+start+" Target: "+target);
        String trimStart = startNode.getSelectedItem().toString().substring(0, (startNode.getSelectedItem().toString().indexOf("=")));
        System.out.println("STRING_STRIP: "+trimStart);
        for(HashMap.Entry<Place, Long> entry : start.entrySet()){
            System.out.println("PLACE: "+entry.getKey()+" Value: "+entry.getValue());
            System.out.println("VERGLEICH: "+trimStart);
            if(trimStart == null ? entry.getKey().toString() == null : trimStart.equals(entry.getKey().toString())){
                HashMap<Place, Long> newStart = new HashMap<>();
                newStart.put(entry.getKey(), entry.getValue());
                start = newStart;
                System.out.println("START_FOR: "+start);
            }
            
        }
        
        
        String trimEnd = sinkNode.getSelectedItem().toString().substring(0, (sinkNode.getSelectedItem().toString().indexOf("=")));
        System.out.println("STRING_STRIP: "+trimEnd);
        for(HashMap.Entry<Place, Long> entry : target.entrySet()){
            System.out.println("PLACE: "+entry.getKey()+" Value: "+entry.getValue());
            if(trimEnd == null ? entry.getKey().toString() == null : trimEnd.equals(entry.getKey().toString())){
                HashMap<Place, Long> newTarget = new HashMap<>();
                newTarget.put(entry.getKey(), entry.getValue());
                eTarget = newTarget;
                System.out.println("TARGET_FOR: "+ target);
            }
          
        }
        System.out.println("CHECKMAL: "+start+" "+target);
            
        
       
        
        
        
        //updateMarkings();       
        /**LOGGER.info("Requested computation of full reachability graph.");
        path = new Pathfinder(copyPN, start, target, capacities, null, "Fullreach");//Fullreach
        path.addListenerToAlgorithm(this);
        path.run();*/
        
        

        

        
        
        
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @author Marcel Germann
     */
    private void updateMarkings() {
        LOGGER.info("Updating markings from table.");
        for (int i = 0; i < markingTable.getRowCount(); ++i) {
            start.put((Place) markingTable.getValueAt(i, 0), (Long) markingTable.getValueAt(i, 1));
            target.put((Place) markingTable.getValueAt(i, 0), (Long) markingTable.getValueAt(i, 2));
            LOGGER.debug("Updated values for Place " + ((Place) markingTable.getValueAt(i, 0)).getProperty("name"));
        }
        LOGGER.info("Successfully updated markings from table.");
    }
    
   
    
    // Restore PN.
    private void restorePNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restorePNActionPerformed
        // TODO add your handling code here:
        // First clear all panels, then fill them again
        for(int i = 0; i<onTransition.getItemCount();i++){
            onTransition.remove(onTransition.getItem(i));
            onTransition.removeAll();
            
        }
        
        for(int i = 0; i < offTransition.getItemCount(); i++){
            if(offTransition.getItemCount()>0){
            offTransition.removeAll();
            }
        }
        for (Place p : backupPN.places()) {
            //model.addElement(p.inputs().getFirst());
            onTransition.add(p.inputs().getFirst().toString());
            System.out.println("TESTING: "+p.inputs().getFirst());
        }
        progressLabel.setText("Number of nodes extended: 0");
        what.setText("");
        /**
         * 
         
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
        }*/
    }//GEN-LAST:event_restorePNActionPerformed

    private void sinkNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sinkNodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sinkNodeActionPerformed

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
    private javax.swing.JLabel Reachability;
    private javax.swing.JComboBox<String> algoSelect;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable markingTable;
    private java.awt.List offTransition;
    private java.awt.List onTransition;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JButton restorePN;
    private javax.swing.JComboBox<String> sinkNode;
    private javax.swing.JComboBox<String> startNode;
    private javax.swing.JLabel what;
    // End of variables declaration//GEN-END:variables

    private void lock(boolean b) {
        
        markingTable.setEnabled(!b);
        
        algoSelect.enableInputMethods(!b);
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
                progressLabel.setText("Number of nodes expanded before execution was aborted: " + Integer.toString(e.getSteps()));
                what.setText("Aborted");
                break;
            case STARTED: // Should be fired after Compute or either of the full-Buttons was pressed and the algorithm is started.
                lock(true); // Ensures that only one algorithm runs at a time.
                break;
            case SUCCESS: // Fired when an algorithm successfully finds the target marking.
                lock(false);
                ArrayList<Transition> path = e.getBacktrack();
                // Should probably handle displaying output
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before successfully finding target marking.");
                progressLabel.setText("Number of nodes expanded before target marking was successfully found: " + Integer.toString(e.getSteps()));
                what.setForeground(Color.GREEN);
                what.setText("[Success] Target node reached!");
                break;
            case FAILURE: // Fired when an algorithm fails to find the target marking.
                lock(false);
                // Should output failure.
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes before failure was determined.");
                progressLabel.setText("Number of nodes expanded before failure was determined: " + Integer.toString(e.getSteps()));
                what.setForeground(Color.RED);
                what.setText("[Failure] Target node not reachable!");
                break;
            case PROGRESS: // Fired every 100 expanded nodes.
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes so far.");
                progressLabel.setText("Number of nodes expanded so far: " + Integer.toString(e.getSteps()));
                what.setText("Progress");
                break;
            case FINISHED: // Fired by FullReachability and FullCoverability on completion
                lock(false);
                LOGGER.info("Expanded " + Integer.toString(e.getSteps()) + " nodes to complete the graph.");
                progressLabel.setText("Number of nodes expanded until completion: " + Integer.toString(e.getSteps()));
                // Somehow display the graph? Otherwise this doesn't do much.
                what.setForeground(Color.GREEN);

                what.setText("Finished");
                break;
            default:
                break;
        }
    
    }
}
