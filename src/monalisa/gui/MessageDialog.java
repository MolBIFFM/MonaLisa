/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.tools.ErrorLog;

public final class MessageDialog extends JDialog implements ActionListener {
    @SuppressWarnings("serial")
    private static class LineControl extends Component {
        @Override
        public void paint(Graphics g) {
            g.setColor(SystemColor.controlShadow);
            g.drawLine(0, 0, getWidth(), 0);
            g.setColor(SystemColor.controlLtHighlight);
            g.drawLine(0, 1, getWidth(), 1);
        }
    }
    
    private static final long serialVersionUID = 8901011954168823254L;

    public static final String OK = "OK";
    
    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();
    
    private final ErrorLog messages;
    private List<JLabel> messageLabels;
    private JPanel messagePanel;
    private JButton buttonOk;
    
    public static void show(JFrame owner, ErrorLog messages) {
        new MessageDialog(owner, messages);
    }
    
    public MessageDialog(JFrame owner, ErrorLog messages) {
        super(owner, true);
        this.messages = messages;
        initComponents();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void initComponents() {
        List<String> errors = messages.getAll(ErrorLog.Severity.ERROR);
        List<String> warnings = messages.getAll(ErrorLog.Severity.WARNING);
        
        String titleResource =
            errors.size() == 0 ? "WarningDialogTitle" : "ErrorDialogTitle";

        messageLabels = new ArrayList<>();
        
        Icon errorIcon = resources.getIcon("error.png");
        Icon warningIcon = resources.getIcon("warning.png");
        
        for (String error : errors) {
            JLabel label = new JLabel(error, errorIcon, SwingConstants.LEFT);
            messageLabels.add(label);
        }
        
        for (String  warning : warnings) {
            JLabel label = new JLabel(warning, warningIcon, SwingConstants.LEFT);
            messageLabels.add(label);
        }
        
        messagePanel = new JPanel();
        GroupLayout panelLayout = new GroupLayout(messagePanel);
        messagePanel.setLayout(panelLayout);
        panelLayout.setAutoCreateContainerGaps(true);
        panelLayout.setAutoCreateGaps(true);
        
        ParallelGroup pg = panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup sg = panelLayout.createSequentialGroup();
        
        boolean first = true;
        for (JLabel label : messageLabels) {
            if (first)
                first = false;
            else {
                LineControl filler = new LineControl();
                filler.setPreferredSize(new Dimension(filler.getPreferredSize().width, 2));
                pg.addComponent(filler);
                sg.addComponent(filler);
            }
            pg.addComponent(label);
            sg.addComponent(label);
        }
        
        panelLayout.setHorizontalGroup(pg);
        panelLayout.setVerticalGroup(sg);
        
        buttonOk = new JButton(strings.get("OK"));
        buttonOk.setActionCommand(OK);
        buttonOk.addActionListener(this);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        
        GroupLayout layout = new GroupLayout(getContentPane());
        layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
            .addComponent(messagePanel)
            .addComponent(buttonOk));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(messagePanel)
            .addComponent(buttonOk));
        
        getContentPane().setLayout(layout);
        getRootPane().setDefaultButton(buttonOk);
        setTitle(strings.get(titleResource));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        setResizable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == OK)
            setVisible(false);
    }
}
