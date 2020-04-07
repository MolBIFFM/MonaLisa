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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import layout.TableLayout;
import monalisa.util.MonaLisaHyperlinkListener;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;

/**
 *
 * @author Jens Einloft
 */
public class HelpDialog extends JFrame implements ActionListener {

    private final static String CLOSE_ACTION = "CLOSE_ACTION";

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private JButton close;
    private JEditorPane jep;

    public HelpDialog() {
        setSize(new Dimension(350, 200));
        setMinimumSize(getSize());
        setIconImage(resources.getImage("icon-16.png"));
        setTitle(strings.get("HelpTitle"));

        initComponent();
    }

    private void initComponent() {
        double size[][]
                = {{15, TableLayout.FILL, 5, TableLayout.PREFERRED, 5, TableLayout.FILL, 15,},
                {15, TableLayout.FILL, 5, TableLayout.PREFERRED, 15}};

        setLayout(new TableLayout(size));

        close = new JButton("Close");
        close.setActionCommand(CLOSE_ACTION);
        close.addActionListener(this);

        jep = new JEditorPane("text/html", "<html>The documentation of MonaLisa is available at "
                + "Sourceforge: <br /><br /><a href=\"https://sourceforge.net"
                + "/p/monalisa4pn/wiki/Home/\">Help</a></html>");
        jep.setEditable(false);
        jep.addHyperlinkListener(new MonaLisaHyperlinkListener());

        add(jep, "1,1,5,1");
        add(close, "3,3");
    }

    public void actionPerformed(ActionEvent e) {
        final String action = e.getActionCommand();

        if (action == CLOSE_ACTION) {
            closeDialog();
        }
    }

    private void closeDialog() {
        this.dispose();
        this.setVisible(false);
    }

}
