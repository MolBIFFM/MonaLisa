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
import javax.swing.JList;
import javax.swing.JPopupMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class ModellersMouseListenerCompartment extends AbstractPopupGraphMousePlugin implements MouseListener {

    private static final Logger LOGGER = LogManager.getLogger(ModellersMouseListenerCompartment.class);
    private final JList owner;
    private final CompartmentAnnotationFrame caf;

    public ModellersMouseListenerCompartment(CompartmentAnnotationFrame caf, JList owner) {
        this.owner = owner;
        this.caf = caf;
    }

    @Override
    protected void handlePopup(MouseEvent me) {
        JPopupMenu popup = new JPopupMenu();
        final List<MiriamWrapper> selectedValue = owner.getSelectedValuesList();

        popup.add(new AbstractAction("Go to") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Going to MIRIAM identifier value in CompartmentAnnotationFrame");
                caf.goToMiriamIdentifier(selectedValue.get(0));
            }
        });

        popup.add(new AbstractAction("Edit") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Editing MIRIAM identifier value in CompartmentAnnotationFrame");
                caf.editMiriamIdentifier(selectedValue.get(0), owner);
            }
        });

        popup.add(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOGGER.info("Deleting MIRIAM identifier value in CompartmentAnnotationFrame");
                caf.deleteMiriamIdentifier(selectedValue.get(0), owner);
            }
        });

        popup.show(owner, me.getX(), me.getY());
    }

}
