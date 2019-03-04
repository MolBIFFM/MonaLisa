/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.knockout;

import java.util.ArrayList;
import java.util.List;

import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.UniquePetriNetEntity;

/**
 * Knockes out a set of places
 * @author Jens Einloft
 */
public class MultiPlaceKnockout extends KnockoutAlgorithm {

    private List<UniquePetriNetEntity> currentKnockouts;
    private final List<Place> toKnockout;
    private int knockOutCounter = 0;
    
    public MultiPlaceKnockout(PetriNetFacade pn, List<Place> places){
        super(pn);
        toKnockout = places;
    }

    
    @Override
    protected PetriNetFacade getNextKnockOutNetwork() {
        PetriNet copy = getPetriNetFacade().getPNCopy();

        currentKnockouts = new ArrayList<>();
        for(Place p : toKnockout) {
            copy.removePlace(p);
            currentKnockouts.add(p);
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
    protected List<UniquePetriNetEntity> getKnockoutEntities() {
        return currentKnockouts;
    }

    @Override
    protected int getTotalKnockouts() {
        return 1;
    }
}
