/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.knockout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import monalisa.data.pn.UniquePetriNetEntity;
import monalisa.gui.MainDialog;
import monalisa.gui.components.SortedListModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnockoutDialog extends JDialog implements ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String ACTION_OK = "ACTION_OK";
    private static final String ACTION_CANCEL = "ACTION_CANCEL";
    private static final String IN = "IN";
    private static final String OUT = "OUT";

    private JScrollPane allScrollPane;
    private JScrollPane koScrollPane;

    private JList allList = new JList();
    private JList koList = new JList();

    private DefaultListModel allListModel = new DefaultListModel();
    private DefaultListModel koListModel = new DefaultListModel();

    private SortedListModel sortedAllListModel = new SortedListModel(allListModel);
    private SortedListModel sortedKoListModel = new SortedListModel(koListModel);

    private JPanel allKoPanel = new JPanel();
    private JPanel panel = new JPanel();

    private JButton inButton;
    private JButton outButton;
    private JButton okButton;
    private JButton cancelButton;

    private JLabel allLabel;
    private JLabel koLabel;

    private Map<String, UniquePetriNetEntity> name2Entity = new HashMap<>();
    private List<UniquePetriNetEntity> koEntities = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger(KnockoutDialog.class);

    public KnockoutDialog(MainDialog owner, Collection<? extends UniquePetriNetEntity> entities) {
        super(owner, true);
        LOGGER.info("Initializing KnockoutDialog");
        for (UniquePetriNetEntity e : entities) {
            String name = e.<String>getProperty("name");
            allListModel.addElement(name);
            name2Entity.put(name, e);
        }

        allList = new JList(sortedAllListModel);
        koList = new JList(sortedKoListModel);
        initComponents();
        setLocationRelativeTo(owner);
        setVisible(true);
        LOGGER.info("Successfully initialized KnockoutDialog");
    }

    private void initComponents() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        okButton = new JButton("Ok");
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);

        inButton = new JButton("»");
        inButton.setActionCommand(IN);
        inButton.addActionListener(this);

        outButton = new JButton("«");
        outButton.setActionCommand(OUT);
        outButton.addActionListener(this);

        allLabel = new JLabel("All");
        koLabel = new JLabel("Knockout");

        allScrollPane
                = new JScrollPane(allList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        allScrollPane.setPreferredSize(new Dimension(150, 200));

        koScrollPane
                = new JScrollPane(koList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        koScrollPane.setPreferredSize(new Dimension(150, 200));

        GroupLayout allKoLayout = new GroupLayout(allKoPanel);
        allKoPanel.setLayout(allKoLayout);
        allKoLayout.setAutoCreateGaps(true);
        allKoLayout.setAutoCreateContainerGaps(true);

        allKoLayout.setHorizontalGroup(allKoLayout.createSequentialGroup()
                .addGroup(allKoLayout.createParallelGroup()
                        .addComponent(allLabel)
                        .addComponent(allScrollPane, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(allKoLayout.createParallelGroup()
                        .addComponent(inButton)
                        .addComponent(outButton))
                .addGroup(allKoLayout.createParallelGroup()
                        .addComponent(koLabel)
                        .addComponent(koScrollPane, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        allKoLayout.setVerticalGroup(allKoLayout.createSequentialGroup()
                .addGroup(allKoLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup(allKoLayout.createSequentialGroup()
                                .addComponent(allLabel)
                                .addComponent(allScrollPane))
                        .addGroup(allKoLayout.createSequentialGroup()
                                .addComponent(inButton)
                                .addComponent(outButton))
                        .addGroup(allKoLayout.createSequentialGroup()
                                .addComponent(koLabel)
                                .addComponent(koScrollPane))));

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(allKoPanel)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addComponent(okButton)));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(allKoPanel)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelButton)
                        .addComponent(okButton)));

        layout.linkSize(SwingConstants.HORIZONTAL, cancelButton, okButton);

        add(panel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String action = e.getActionCommand();
        switch (action) {
            case ACTION_OK:
                submit();
                break;
            case ACTION_CANCEL:
                setVisible(false);
                break;
            case IN:
                for (Object o : allList.getSelectedValues()) {
                    koListModel.addElement(o.toString());
                    allListModel.removeElement(o);
                }
                break;
            case OUT:
                for (Object o : koList.getSelectedValues()) {
                    allListModel.addElement(o.toString());
                    koListModel.removeElement(o);
                }
                break;
        }
    }

    private void submit() {
        for (int i = 0; i < koListModel.getSize(); i++) {
            koEntities.add(name2Entity.get(koListModel.get(i).toString()));
        }
        setVisible(false);
    }

    @SuppressWarnings("unchecked")
    public <T extends UniquePetriNetEntity> List<T> knockouts() {
        return (List<T>) this.koEntities;
    }

}
