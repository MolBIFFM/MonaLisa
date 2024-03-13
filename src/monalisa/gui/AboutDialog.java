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
public class AboutDialog extends JFrame implements ActionListener {

    private final static String CLOSE_ACTION = "CLOSE_ACTION";

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private JButton close;
    private JEditorPane jep;
    private String text;

    public AboutDialog() {
        setSize(new Dimension(450, 430));
        setMinimumSize(getSize());
        setIconImage(resources.getImage("icon-16.png"));
        setTitle(strings.get("AboutTitle"));

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

        text = "<html> MonaLisa is licensed under the <a href=\"http://www.perlfoundation.org/artistic_license_2_0\">Artistic License 2.0</a>"
                + " and depence on non-free software."
                + " This is a free software license which is <a href=\"http://www.gnu.org/licenses/license-list.html#ArtisticLicense2\">compatible with the GPL according to the FSF</a>."
                + " It is the same license that is used by the Perl programming language.<br /><br />"
                + "Version: " + strings.get("CurrentVersion") + "<br /><br />"
                + "(c) <a href=\"http://www.bioinformatik.uni-frankfurt.de/\">Molekulare Bioinformatik</a>, Goethe University Frankfurt, Frankfurt am Main, Germany<br /><br />"
                + "Project members in alphabetical order (recent and former):<br /><br />"
                + "Anja Thormann, Daniel Noll, Heiko Giese, Ina Koch, Jens Einloft, Jennifer Scheidel, Joachim Nöthen, Jörg Ackermann, Lara Klemt, Lilya Mirzoyan, Marcel Gehrmann, Marius Kirchner, Pavel Balazki, Stefan Marchi, Tim Stadager<br /><br />"
                + "<a href=\"http://www.bioinformatik.uni-frankfurt.de/tools/monalisa/index.html\">Project homepage</a>"
                + "</html>";

        jep = new JEditorPane("text/html", text);
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
