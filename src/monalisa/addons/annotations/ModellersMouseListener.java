/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.annotations;

import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class ModellersMouseListener extends AbstractPopupGraphMousePlugin implements MouseListener {

    private static final Logger LOGGER = LogManager.getLogger(ModellersMouseListener.class);
    private final JList owner;
    private final AnnotationsPanel ap;

    public ModellersMouseListener(AnnotationsPanel ap, JList owner) {
        this.owner = owner;
        this.ap = ap;
    }

    @Override
    protected void handlePopup(MouseEvent me) {
        JPopupMenu popup = new JPopupMenu();
        final List<ModellerWrapper> selectedValue = owner.getSelectedValuesList();

        popup.add(new AbstractAction("Edit") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Editing Modeller value in AnnotationsPanel");
                ap.editModeller(selectedValue.get(0), owner.getSelectedIndex());
            }
        });

        popup.add(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Deleting Modeller value in AnnotationsPanel");
                ((DefaultListModel)owner.getModel()).removeElement(selectedValue.get(0));
            }
        });

        popup.show(owner, me.getX(), me.getY());
    }

}
