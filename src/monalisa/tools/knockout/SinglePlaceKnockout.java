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
import java.util.Iterator;
import java.util.List;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.data.pn.UniquePetriNetEntity;

public class SinglePlaceKnockout extends KnockoutAlgorithm {

    private final Iterator<Place> iterator;
    private List<UniquePetriNetEntity> currentKnockouts;
    
    public SinglePlaceKnockout(PetriNetFacade pn){
        super(pn);
        iterator = pn.places().iterator();
    }
    
    public SinglePlaceKnockout(PetriNetFacade pn, List<Place> places) {
        super(pn);
        iterator = places.iterator();
    }
    
    @Override
    protected PetriNetFacade getNextKnockOutNetwork() {
        PetriNet copy = getPetriNetFacade().getPNCopy();
        Place place = iterator.next();
        
        currentKnockouts = new ArrayList<>();
        currentKnockouts.add(place);
        
        for(Transition t : place.outputs())
            copy.removeTransition(t);

        for(Transition t : place.inputs())
            copy.removeTransition(t);
        
        copy.removePlace(place);
        return new PetriNetFacade(copy);
    }

    @Override
    protected boolean hasNextKnockOutNetwork() {
        return iterator.hasNext();
    }
    
    @Override
    protected List<UniquePetriNetEntity> getKnockoutEntities() {
        return currentKnockouts;
    }

    @Override
    protected int getTotalKnockouts() {
        return getPetriNetFacade().places().size();
    }
}
