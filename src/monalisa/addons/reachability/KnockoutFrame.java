/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import javax.swing.DefaultListModel;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marcel
 */
public class KnockoutFrame extends javax.swing.JFrame {

    private final ReachabilityDialog rd;
    private static final Logger LOGGER = LogManager.getLogger(KnockoutFrame.class);

    /**
     * Creates new form KnockoutFrame
     */
    public KnockoutFrame(ReachabilityDialog rd, Collection<Transition> transitions) {
        initComponents();
        this.rd = rd;
        DefaultListModel model = (DefaultListModel) activeList.getModel();
        model.addAll(transitions);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        activeList = new javax.swing.JList<>();
        activeLabel = new java.awt.Label();
        knockedLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        knockedList = new javax.swing.JList<>();
        knockButton = new javax.swing.JButton();
        activateButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Transition knockout");

        activeList.setModel(new DefaultListModel());
        jScrollPane1.setViewportView(activeList);

        activeLabel.setText("Active Transitions");

        knockedLabel.setText("Knocked out Transitions");

        knockedList.setModel(new DefaultListModel());
        jScrollPane2.setViewportView(knockedList);

        knockButton.setText(">");
        knockButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                knockButtonActionPerformed(evt);
            }
        });

        activateButton.setText("<");
        activateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activateButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(knockButton)
                            .addComponent(activateButton)))
                    .addComponent(activeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(knockedLabel)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(activeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(knockedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(knockButton)
                        .addGap(18, 18, 18)
                        .addComponent(activateButton)))
                .addGap(18, 18, 18)
                .addComponent(saveButton)
                .addGap(22, 22, 22))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void knockButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_knockButtonActionPerformed
        DefaultListModel knockedModel = (DefaultListModel) knockedList.getModel();
        DefaultListModel activeModel = (DefaultListModel) activeList.getModel();
        ArrayList<Transition> toRemove = new ArrayList<>();
        for (int i : activeList.getSelectedIndices()) {
            knockedModel.addElement(activeModel.get(i));
            toRemove.add((Transition) activeModel.get(i));
        }
        for (Transition t : toRemove) {
            activeModel.removeElement(t);
        }
    }//GEN-LAST:event_knockButtonActionPerformed

    private void activateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activateButtonActionPerformed
        DefaultListModel knockedModel = (DefaultListModel) knockedList.getModel();
        DefaultListModel activeModel = (DefaultListModel) activeList.getModel();
        ArrayList<Transition> toRemove = new ArrayList<>();
        for (int i : knockedList.getSelectedIndices()) {
            activeModel.addElement(knockedModel.get(i));
            toRemove.add((Transition) knockedModel.get(i));
            
        }
        for (Transition t : toRemove) {
            knockedModel.removeElement(t);
        }
    }//GEN-LAST:event_activateButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        DefaultListModel knockedModel = (DefaultListModel) knockedList.getModel();
        HashSet<Transition> knockouts = new HashSet<>();
        for (int i=0; i < knockedModel.getSize(); i++) {
            knockouts.add((Transition) knockedModel.get(i));
        }
        LOGGER.info("Knocked out: " + knockouts);
        rd.setKnockouts(knockouts);
        this.dispose();
    }//GEN-LAST:event_saveButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton activateButton;
    private java.awt.Label activeLabel;
    private javax.swing.JList<Transition> activeList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton knockButton;
    private javax.swing.JLabel knockedLabel;
    private javax.swing.JList<Transition> knockedList;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}