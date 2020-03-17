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

import monalisa.data.pn.Transition;
import java.util.ArrayList;
import monalisa.data.pn.PetriNetFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * returns all transitions without pre places.
 * @author daniel
 */
public class TransitionWithoutPostPlace extends NetPropertyAlgorithm <ArrayList<Transition>>{

    private static final Logger LOGGER = LogManager.getLogger(TransitionWithoutPostPlace.class);

    public TransitionWithoutPostPlace(PetriNetFacade pn) {
        super(pn);
    }

    @Override
    public void runAlgorithm() {
        LOGGER.info("Checking whether net has transitions without post-places");
        algorithmName = "transition withouth post place";
        algorithmValue = new ArrayList();
        for(Transition t : petriNet.transitions()){
            if(t.outputs().isEmpty()){
                algorithmValue.add(t);
            }
        }
        LOGGER.info("Successfully checked whether net has transitions without post-places");
    }
}