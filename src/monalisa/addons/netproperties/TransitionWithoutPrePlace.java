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
public class TransitionWithoutPrePlace extends NetPropertyAlgorithm <ArrayList<Transition>>{

    private static final Logger LOGGER = LogManager.getLogger(TransitionWithoutPrePlace.class);

    public TransitionWithoutPrePlace(PetriNetFacade pn) {
        super(pn);
    }

    @Override
    public void runAlgorithm() {
        LOGGER.info("Checking whether net has transitions without pre-places");
        algorithmName = "transition withouth pre place";
        algorithmValue = new ArrayList();
        for(Transition t : petriNet.transitions()){
            if(t.inputs().isEmpty()){
                algorithmValue.add(t);
            }
        }
        LOGGER.info("Succesfully checked whether net has transitions without pre-places");
    }
}