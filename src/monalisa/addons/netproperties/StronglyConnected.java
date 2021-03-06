/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netproperties;

import java.util.ArrayList;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Checks if a Net is strongly Connected.
 *
 * @author daniel
 */
public class StronglyConnected extends NetPropertyAlgorithm<Boolean> {

    private static final Logger LOGGER = LogManager.getLogger(StronglyConnected.class);
    private ArrayList<Place> placeArray;

    public StronglyConnected(PetriNetFacade pn) {
        super(pn);
    }

    /**
     * A net is connected, if there exist a undirected path from any node to
     * every other node.
     *
     */
    @Override
    public void runAlgorithm() {
        LOGGER.info("Checking whether net is strongly connected");
        algorithmName = "strongly connected";
        algorithmValue = true;
        placeArray = new ArrayList();
        for (Place p : petriNet.places()) {
            placeArray.add(p);
        }

        LOGGER.debug("Starting depth first search");
        while (!placeArray.isEmpty()) {
            Place a = placeArray.get(0);
            tSearch(a);
            if (!placeArray.isEmpty()) {
                algorithmValue = false;
            }
        }
        LOGGER.info("Successfully checked whether net is strongly connected");
    }

    private void tSearch(Place p) {

        placeArray.remove(p);

        if (p.outputs().isEmpty()) {
            return;
        }
        for (Transition t : p.outputs()) {
            if (t.outputs().isEmpty()) {
                continue;
            }
            for (Place p2 : t.outputs()) {
                if (placeArray.contains(p2)) {
                    tSearch(p2);
                }
            }
        }
    }
}
