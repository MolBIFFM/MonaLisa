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
import monalisa.data.pn.Transition;
import monalisa.data.pn.UniquePetriNetEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SingleTransitionKnockout extends KnockoutAlgorithm {

    private final Iterator<Transition> iterator;
    private List<UniquePetriNetEntity> currentKnockouts;
    private static final Logger LOGGER = LogManager.getLogger(SingleTransitionKnockout.class);

    SingleTransitionKnockout(PetriNetFacade pn) {
        super(pn);
        LOGGER.info("Initializing SingleTransitionKnockout algorithm");
        iterator = pn.transitions().iterator();
        LOGGER.info("Successfully initialized SingleTransitionKnockout algorithm");
    }

    SingleTransitionKnockout(PetriNetFacade pn, List<Transition> transitions) {
        super(pn);
        LOGGER.info("Initializing SingleTransitionKnockout algorithm for a subset of transitions");
        iterator = transitions.iterator();
        LOGGER.info("Successfully initialized SingleTransitionKnockout algorithm for a subset of transitions");
    }

    @Override
    protected PetriNetFacade getNextKnockOutNetwork() {
        LOGGER.debug("Getting next KnockoutNetwork for SingleTransitionKnockout algorithm");
        PetriNet copy = getPetriNetFacade().getPNCopy();
        Transition transition = iterator.next();

        currentKnockouts = new ArrayList<>();
        currentKnockouts.add(transition);

        copy.removeTransition(transition);
        LOGGER.debug("Successfully got next KnockoutNetwork for SingleTransitionKnockout algorithm");
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
        return getPetriNetFacade().transitions().size();
    }
}
