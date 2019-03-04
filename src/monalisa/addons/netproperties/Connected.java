/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netproperties;

import java.util.ArrayList;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;

/**
 *
 * @author daniel
 */
public class Connected extends NetPropertieAlgorithm<Boolean> {

    private ArrayList<Place> placeArray;

    public Connected(PetriNetFacade pn) {
        super(pn);
    }

    /**
     * A net is connected, if there exist a undirected path from any node to
     * every other node.
     *
     */
    @Override
    public void runAlgorithm() {
        
        algorithmName = "connected";
        algorithmValue = true;
        placeArray = new ArrayList();
        for (Place p : petriNet.places()) {
            placeArray.add(p);
        }


        while (!placeArray.isEmpty()) {

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