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
import monalisa.data.pn.Place;
import monalisa.data.pn.UniquePetriNetEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Knocks out a set of places
 * @author Jens Einloft
 */
public class MultiPlaceKnockout extends KnockoutAlgorithm {

    private List<UniquePetriNetEntity> currentKnockouts;
    private final List<Place> toKnockout;
    private int knockOutCounter = 0;
    private static final Logger LOGGER = LogManager.getLogger(MultiPlaceKnockout.class);

    public MultiPlaceKnockout(PetriNetFacade pn, List<Place> places){
        super(pn);
        LOGGER.info("Initializing MultiPlaceKnockout algorithm");
        toKnockout = places;
        LOGGER.info("Successfully initialized MultiPlaceKnockout algorithm");
    }


    @Override
    protected PetriNetFacade getNextKnockOutNetwork() {
        LOGGER.debug("Getting next KnockoutNetwork for MultiPlaceKnockout algorithm");
        PetriNet copy = getPetriNetFacade().getPNCopy();

        currentKnockouts = new ArrayList<>();
        for(Place p : toKnockout) {
            copy.removePlace(p);
            currentKnockouts.add(p);
        }
        LOGGER.debug("Successfully got next KnockoutNetwork for MultiPlaceKnockout algorithm");
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
