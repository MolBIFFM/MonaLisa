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

import java.util.ArrayList;
import java.util.List;

import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Transition;
import monalisa.data.pn.UniquePetriNetEntity;

/**
 * Knock-outs a set of transition
 * @author Jens Einloft
 */
public class MultiTransitionKnockout extends KnockoutAlgorithm {

    private List<UniquePetriNetEntity> currentKnockouts;
    private final List<Transition> toKnockout;
    private int knockOutCounter = 0;
        
    MultiTransitionKnockout(PetriNetFacade pn, List<Transition> transitions) {
        super(pn);
        toKnockout = transitions;
    }

    @Override
    protected PetriNetFacade getNextKnockOutNetwork() {
        PetriNet copy = getPetriNetFacade().getPNCopy();

        currentKnockouts = new ArrayList<>();
        for(Transition t : toKnockout) {
            copy.removeTransition(t);
            currentKnockouts.add(t);
        }

        return new PetriNetFacade(copy);
    }

    @Override
    protected boolean hasNextKnockOutNetwork() {
        knockOutCounter++;
        if(knockOutCounter == 1)
            return true;
        else
            return false;
    }   

    @Override
    protected int getTotalKnockouts() {
        return 1;
    }

    @Override
    protected List<UniquePetriNetEntity> getKnockoutEntities() {
        return currentKnockouts;
    }
}
