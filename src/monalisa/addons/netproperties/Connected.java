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
 *
 * @author daniel
 */
public class Connected extends NetPropertyAlgorithm<Boolean> {

    private static final Logger LOGGER = LogManager.getLogger(Connected.class);
    private ArrayList<Place> placeArray;

    public Connected(PetriNetFacade pn) {
        super(pn);
    }

    /**
     * A net is connected, if there exists an undirected path from any node to
     * every other node.
     *
     */
    @Override
    public void runAlgorithm() {
        LOGGER.info("Checking whether net is connected");
        algorithmName = "connected";
        algorithmValue = true;
        placeArray = new ArrayList();
        for (Place p : petriNet.places()) {
            placeArray.add(p);
        }


        while (!placeArray.isEmpty()) {
            LOGGER.debug("Starting depth first search");
            Place a = placeArray.get(0);
            tSearch(a);
            if (!placeArray.isEmpty()) {
                algorithmValue = false;
                //    for (Place p : placeArray) {                     shows the other places
                //        System.out.print(p.getProperty("name") + ", ");
                //    }
                //    System.out.print("\n\n");                        you can see how many independent graphs you have.
            }
        }
        LOGGER.info("Successfully checked whether net is connected");
    }

    /*
     * the tSearch looks for neighbours and checks if they are already visited.
     */
    private void tSearch(Place p) {

        placeArray.remove(p);
        if (p.outputs().isEmpty()) {
            return;
        }
        for (Transition t : p.outputs()) {
            for (Place p2 : t.outputs()) {
                if (placeArray.contains(p2)) {
                    tSearch(p2);
                }
            }
            for (Place p2 : t.inputs()) {
                if (placeArray.contains(p2)) {
                    tSearch(p2);
                }
            }
        }
        for (Transition t : p.inputs()) {
            for (Place p2 : t.outputs()) {
                if (placeArray.contains(p2)) {
                    tSearch(p2);
                }
            }
            for (Place p2 : t.inputs()) {
                if (placeArray.contains(p2)) {
                    tSearch(p2);
                }
            }
        }
    }
}