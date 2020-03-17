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
import java.util.Iterator;
import java.util.List;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.data.pn.UniquePetriNetEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SinglePlaceKnockout extends KnockoutAlgorithm {

    private final Iterator<Place> iterator;
    private List<UniquePetriNetEntity> currentKnockouts;
    private static final Logger LOGGER = LogManager.getLogger(SinglePlaceKnockout.class);

    public SinglePlaceKnockout(PetriNetFacade pn){
        super(pn);
        LOGGER.info("Initializing SinglePlaceKnockout algorithm");
        iterator = pn.places().iterator();
        LOGGER.info("Successfully initialized SinglePlaceKnockout algorithm");
    }

    public SinglePlaceKnockout(PetriNetFacade pn, List<Place> places) {
        super(pn);
        LOGGER.info("Initializing SinglePlaceKnockout algorithm for a subset of places");
        iterator = places.iterator();
        LOGGER.info("Successfully initialized SinglePlaceKnockout algorithm for a subset of places");
    }

    @Override
    protected PetriNetFacade getNextKnockOutNetwork() {
        LOGGER.debug("Getting next KnockoutNetwork for SinglePlaceKnockout algorithm");
        PetriNet copy = getPetriNetFacade().getPNCopy();
        Place place = iterator.next();

        currentKnockouts = new ArrayList<>();
        currentKnockouts.add(place);

        for(Transition t : place.outputs())
            copy.removeTransition(t);

        for(Transition t : place.inputs())
            copy.removeTransition(t);

        copy.removePlace(place);
        LOGGER.debug("Successfully got next KnockoutNetwork for SinglePlaceKnockout algorithm");
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
