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
import monalisa.data.pn.Transition;
import monalisa.data.pn.UniquePetriNetEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DoubleTransitionKnockout extends KnockoutAlgorithm {
    private final List<Pair<Transition, Transition>> pairs;
    private final Iterator<Pair<Transition, Transition>> pairIterator;
    private List<UniquePetriNetEntity> currentKnockouts;
    private static final Logger LOGGER = LogManager.getLogger(DoubleTransitionKnockout.class);

    DoubleTransitionKnockout(PetriNetFacade pn) {
        super(pn);
        LOGGER.info("Initializing DoubleTransitionKnockout algorithm");
        this.pairs = new ArrayList<>();
        List<Transition> transitions = new ArrayList<>();
        for (Transition t : pn.transitions())
            transitions.add(t);
        for (int i = 0; i < transitions.size(); i++)
            for (int j = i + 1; j < transitions.size(); j++)
                pairs.add(Pair.of(transitions.get(i), transitions.get(j)));

        pairIterator = pairs.iterator();
        LOGGER.info("Successfully initialized DoubleTransitionKnockout algorithm");
    }

    @Override
    protected PetriNetFacade getNextKnockOutNetwork() {
        LOGGER.debug("Getting next KnockoutNetwork for DoubleTransitionKnockout algorithm");
        Pair<Transition, Transition> p = pairIterator.next();
        PetriNet copy = getPetriNetFacade().getPNCopy();

        currentKnockouts = new ArrayList<>();
        currentKnockouts.add(p.first());
        currentKnockouts.add(p.second());

        copy.removeTransition(p.first());
        copy.removeTransition(p.second());
        LOGGER.debug("Successfully got next KnockoutNetwork for DoubleTransitionKnockout algorithm");
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
