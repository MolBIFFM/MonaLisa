/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;

import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;

public final class ConfirmOverwriteDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 3111673729833496449L;
    
    public static final String YES = "YES";
    public static final String YES_FOR_ALL = "YES_FOR_ALL";
    public static final String NO = "NO";
    public static final String NO_FOR_ALL = "NO_FOR_ALL";
    
    private static final StringResources strings =
        ResourceManager.instance().getDefaultStrings();

    private JLabel labelMessage;
    private JButton buttonYes;
    private JButton buttonYesForAll;
    private JButton buttonNo;
    private JButton buttonNoForAll;
    
    private String result;
    private String filename;

    public ConfirmOverwriteDialog(JFrame owner, String filename) {
        super(owner, true);
        this.filename = filename;
        initComponents();
        setLocationRelativeTo(owner);
        setVisible(true);
    }
    
    public String getResult() {
        return result;
    }
    
    private void initComponents() {
        labelMessage = new JLabel(strings.get("ConfirmOverwriteDialogMessage", filename));
        
        buttonYes = new JButton(strings.get("Yes"));
        buttonYes.setActionCommand(YES);
        buttonYes.addActionListener(this);
        
        buttonYesForAll = new JButton(strings.get("YesForAll"));
        buttonYesForAll.setActionCommand(YES_FOR_ALL);
        buttonYesForAll.addActionListener(this);

        buttonNo = new JButton(strings.get("No"));
        buttonNo.setActionCommand(NO);
        buttonNo.addActionListener(this);

        buttonNoForAll = new JButton(strings.get("NoForAll"));
        buttonNoForAll.setActionCommand(NO_FOR_ALL);
        buttonNoForAll.addActionListener(this);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        
        GroupLayout layout = new GroupLayout(getContentPane());
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(labelMessage)
            .addGroup(layout.createSequentialGroup()
                .addComponent(buttonYes)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonYesForAll)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonNo)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonNoForAll)));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(labelMessage)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(buttonYes)
                .addComponent(buttonYesForAll)
                .addComponent(buttonNo)
                .addComponent(buttonNoForAll)));
        
        getContentPane().setLayout(layout);
        getRootPane().setDefaultButton(buttonNo);
        setTitle(strings.get("ConfirmOverwriteDialogTitle"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        setResizable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        result = e.getActionCommand();
        setVisible(false);
    }
}
