/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package monalisa.addons.reachability.constraints;

/**
 *
 * @author khaas
 */
public class HelpPanel extends javax.swing.JFrame {

    /**
     * Creates new form HelpPanel
     */
    public HelpPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel1.setText("Helpdesk");

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setText("--------------------------------------------------- Target marking ---------------------------------------------------\n\nTargetmarking: Edit tabel column 3 to wanted marking.\n\t   \n\n---------------------------------------------- Deaktivate transitions ----------------------------------------------\n\nTransition on/off: you can choose to turn every transition on or off.\n\n\n------------------------------------------------- Restore petri net --------------------------------------------------\n\nRestore PN: at any time it is possible to undo all your choices and \n                     return to the original petri net.                \n\n\n\n--------------------------------------------    Force transition to fire   --------------------------------------------\n\nForce transition: it is possible to force a transition to get fired,\n\t             iff the transition will get enabled within the computation.\n\t             => to choose a transition you have to select the transition \n                                           and push the button >>choose transition<<\n\t             => it is also possible to reset your choice.\n\t             => if already computed without a selected transition, you can check\n                                          if it already has been used.\n\n----------------------------------------------------- Used/Not Used -----------------------------------------------------------\n\nWhen computing a marking with a chosen transition, it is possible that the feedback might \nbe: Transition HAS NOT been used, but the counter equals 1 or/and the transition is listed.\nThis appears when the program can not reach the target marking, but tried a certain way.\n\n----------------------------------------------------- Spinner -----------------------------------------------------------\n\nWhen a transition is chosen to fire, it is also possible to choose a maximum number \nof which that transition is allowed to fire.\n\nIt's also possible to choose an nuber of built reachability nodes\n \n\n              \n\n\n                 ");
        jTextArea1.setMaximumSize(new java.awt.Dimension(540, 450));
        jTextArea1.setMinimumSize(new java.awt.Dimension(540, 450));
        jTextArea1.setPreferredSize(new java.awt.Dimension(540, 450));
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(207, 207, 207)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(244, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(HelpPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HelpPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HelpPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HelpPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HelpPanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
