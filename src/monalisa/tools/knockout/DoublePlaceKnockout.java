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

import monalisa.data.Pair;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.data.pn.UniquePetriNetEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DoublePlaceKnockout extends KnockoutAlgorithm {

    private final List<Pair<Place, Place>> pairs;
    private final Iterator<Pair<Place, Place>> pairIterator;
    private List<UniquePetriNetEntity> currentKnockouts;
    private static final Logger LOGGER = LogManager.getLogger(DoublePlaceKnockout.class);

    DoublePlaceKnockout(PetriNetFacade pn) {
        super(pn);
        LOGGER.info("Initializing DoublePlaceKnockout algorithm");
        this.pairs = new ArrayList<>();

        List<Place> places = new ArrayList<>();
        for (Place p : pn.places()) {
            places.add(p);
        }
        for (int i = 0; i < places.size(); i++) {
            for (int j = i + 1; j < places.size(); j++) {
                pairs.add(Pair.of(places.get(i), places.get(j)));
            }
        }

        pairIterator = pairs.iterator();
        LOGGER.info("Successfully initialized DoublePlaceKnockout algorithm");
    }

    @Override
    protected PetriNetFacade getNextKnockOutNetwork() {
        LOGGER.debug("Getting next KnockoutNetwork for DoublePlaceKnockout algorithm");
        Pair<Place, Place> p = pairIterator.next();
        PetriNet copy = getPetriNetFacade().getPNCopy();

        currentKnockouts = new ArrayList<>();
        currentKnockouts.add(p.first());
        currentKnockouts.add(p.second());

        for (Transition t : p.first().outputs()) {
            copy.removeTransition(t);
        }

        for (Transition t : p.first().inputs()) {
            copy.removeTransition(t);
        }

        for (Transition t : p.second().outputs()) {
            copy.removeTransition(t);
        }

        for (Transition t : p.second().inputs()) {
            copy.removeTransition(t);
        }

        copy.removePlace(p.first());
        copy.removePlace(p.second());
        LOGGER.debug("Successfully got next KnockoutNetwork for DoublePlaceKnockout algorithm");
        return new PetriNetFacade(copy);
    }

    @Override
    protected boolean hasNextKnockOutNetwork() {
        return pairIterator.hasNext();
    }

    protected List<UniquePetriNetEntity> getKnockoutEntities() {
        return currentKnockouts;
    }

    @Override
    protected int getTotalKnockouts() {
        return pairs.size();
    }
}
