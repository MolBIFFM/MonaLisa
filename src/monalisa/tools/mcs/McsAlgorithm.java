/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.mcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.Transition;
import monalisa.results.TInvariants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class McsAlgorithm {
    private final List<TInvariant> tInvariants;
    private final List<Transition> transitions;
    private List<Set<Transition>> mcs;
    private List<Set<Transition>> precutsets;
    private static final Logger LOGGER = LogManager.getLogger(McsAlgorithm.class);

    public McsAlgorithm(PetriNetFacade petriNet, TInvariants allTInvariants, Transition objective) {
        LOGGER.info("Initializing MCS algorithm");
        this.tInvariants = new ArrayList<>();
        for (TInvariant tinv : allTInvariants)
            if (tinv.contains(objective))
                tInvariants.add(tinv);

        transitions = new ArrayList<>(petriNet.transitions());
        Collections.sort(transitions);
        initialize();
        LOGGER.info("Successfully initialized MCS algorithm");
    }

    public List<Set<Transition>> findMcs(int maxCutSetSize) {
        LOGGER.info("Determining all MCS");
        List<Set<Transition>> newPrecutsets = null;

        for(int k = 2; k <= maxCutSetSize; k++) {
            newPrecutsets = new ArrayList<>();

            for (Transition j : transitions) {
                precutsets = removeSetsContaining(j, precutsets);
                List<Set<Transition>> tempPrecutsets =
                    calculatePreliminaryCutsets(precutsets, j);
                tempPrecutsets = removeNonMinimalSets(tempPrecutsets);
                newPrecutsets.addAll(yieldIncompleteSets(tempPrecutsets));
            }

            if (newPrecutsets.isEmpty())
                break;
            else
                precutsets = newPrecutsets;
        }
        LOGGER.info("Successfully determined all MCS");
        return mcs;
    }

    private void initialize() {
        // mcs = {{j} | \forall i : em[i][j] != 0 }
        mcs = new ArrayList<>();
        // precutsets = {{j} | {j} \not\in mcs }
        precutsets = new ArrayList<>();

        for (Transition t : transitions) {
            Set<Transition> tSet = new HashSet<>();
            tSet.add(t);
            if (coversAllTInvariants(t))
                mcs.add(tSet);
            else
                precutsets.add(tSet);
        }
    }

    private List<Set<Transition>> calculatePreliminaryCutsets(
            List<Set<Transition>> precutsets, Transition j) {
        LOGGER.debug("Calculating preliminary cutsets");
        List<Set<Transition>> newPrecutsets = new ArrayList<>();

        for (Set<Transition> precutset : precutsets) {
            if (intersectsAnyInvariantContaining(j, precutset))
                continue;

            Set<Transition> newPrecutset = new HashSet<>(precutset);
            newPrecutset.add(j);
            newPrecutsets.add(newPrecutset);
        }
        LOGGER.debug("Successfully calculated preliminary cutsets");
        return newPrecutsets;
    }

    private boolean intersectsAnyInvariantContaining(Transition j, Set<Transition> set) {
        for (TInvariant tinv : tInvariants)
            if (tinv.contains(j) && !intersection(tinv, set).isEmpty())
                return true;
        return false;
    }

    private List<Set<Transition>> removeSetsContaining(Transition t, List<Set<Transition>> sets) {
        LOGGER.debug("Removing all sets containing transition '" + t.getProperty("name") + "'");
        List<Set<Transition>> ret = new ArrayList<>();

        for (Set<Transition> set : sets)
            if (!set.contains(t))
                ret.add(set);
        LOGGER.debug("Successfully removed all sets containing transition '" + t.getProperty("name") + "'");
        return ret;
    }

    private List<Set<Transition>> removeNonMinimalSets(List<Set<Transition>> precutsets) {
        LOGGER.debug("Removing all non-minimal sets");
        List<Set<Transition>> minimalPreCutSets = new ArrayList<>();

        for (Set<Transition> precutset : precutsets) {
            boolean minimal = true;
            for (Set<Transition> mincutset : mcs) {
                if (precutset.containsAll(mincutset)) {
                    minimal = false;
                    break;
                }
            }

            if (minimal)
                minimalPreCutSets.add(precutset);
        }
        LOGGER.debug("Successfully removed all non-minimal sets");
        return minimalPreCutSets;
    }

    private List<Set<Transition>> yieldIncompleteSets(List<Set<Transition>> sets) {
        List<Set<Transition>> incomplete = new ArrayList<>();

        for (Set<Transition> set : sets) {
            if (coversAllTInvariants(set))
                mcs.add(set);
            else
                incomplete.add(set);
        }

        return incomplete;
    }

    private boolean coversAllTInvariants(Transition t) {
        for (TInvariant tinv : tInvariants)
            if (!tinv.contains(t))
                return false;
        return true;
    }

    private boolean coversAllTInvariants(Set<Transition> set) {
        for (TInvariant tinv : tInvariants) {
            if (intersection(tinv, set).isEmpty())
                return false;
        }

        return true;
    }

    private <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<>(a);
        result.retainAll(b);
        return result;
    }
}
