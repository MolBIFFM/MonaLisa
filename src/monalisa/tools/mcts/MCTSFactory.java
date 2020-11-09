/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.mcts;

import java.util.*;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.InvariantBuilder;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MCTSFactory {

    private final int[][] occMatrix;
    private final List<Transition> transitions;
    private final List<TInvariant> invariants;
    private final PetriNetFacade pnf;
    private static final Logger LOGGER = LogManager.getLogger(MCTSFactory.class);

    public MCTSFactory(
            int occMatrix[][],
            List<Transition> transitions,
            List<TInvariant> invariants,
            PetriNetFacade pnf) {
        this.transitions = transitions;
        this.invariants = invariants;
        this.occMatrix = occMatrix;
        this.pnf = pnf;
    }

    public List<TInvariant> mctsSupport() {
        LOGGER.debug("Getting MCTS support");
        Set<Integer> alreadyDone = new HashSet<>();
        List<TInvariant> ret = new ArrayList<>();
        int mctsId = 0;

        int nbr_of_transitions = transitions.size();
        int nbr_of_invariants = invariants.size();

        if (nbr_of_invariants == 0) {
            return null;
        }

        boolean sameDts, hasI, hasN, allwaysNull;
        for (int i = 0; i < nbr_of_transitions; i++) {
            if (alreadyDone.contains(i)) {
                continue;
            }

            alreadyDone.add(i);
            Transition transition = transitions.get(i);

            InvariantBuilder builder = new InvariantBuilder(pnf, "TI");
            builder.setId(mctsId++);
            builder.add(transition, 1);

            for (int n = i + 1; n < nbr_of_transitions; n++) {
                sameDts = true;
                allwaysNull = true;
                for (int j = 0; j < nbr_of_invariants; j++) {
                    // Does this transition belong in the same dependent transition sets?
                    hasI = occMatrix[j][i] >= 1;
                    hasN = occMatrix[j][n] >= 1;

                    if (hasI == hasN && hasI == true) {
                        allwaysNull = false;
                    }

                    if (hasI != hasN) {
                        sameDts = false;
                        break;
                    }
                }
                if (sameDts && !allwaysNull) {
                    builder.add(transitions.get(n), 1);
                    alreadyDone.add(n);
                }
            }

            if (builder.getSize() > 1) {
                ret.add(builder.buildAndClear());
            } else {
                mctsId--;
            }
        }
        LOGGER.debug("Successfully got MCTS support");
        return ret;
    }

    public List<TInvariant> strongMcts() {
        LOGGER.debug("Getting strong MCTS");
        List<TInvariant> ret = new ArrayList<>();
        Set<Integer> alreadyDone = new HashSet<>();
        int mctsId = 0;
        List<TInvariant> supports = mctsSupport();

        if (invariants.isEmpty()) {
            return null;
        }

        for (TInvariant support : supports) {
            alreadyDone.clear();

            for (Transition transT : support) {
                if (alreadyDone.contains(transT.id())) {
                    continue;
                }

                alreadyDone.add(transT.id());
                InvariantBuilder builder = new InvariantBuilder(pnf, "TI");
                builder.setId(mctsId++);
                int transNum = 0;
                builder.add(transT, 1);

                TInvariant invWithNewMcts = null;
                boolean sameDts, allwaysNull;

                for (Transition transS : support) {
                    if (transS == transT) {
                        continue;
                    }

                    sameDts = true;
                    float relation = 0;
                    for (TInvariant inv : invariants) {
                        int occ = inv.factor(transT);
                        if (occ == 0) {
                            continue;
                        }
                        invWithNewMcts = inv;
                        float actRel = (float) occ / inv.factor(transS);
                        if (relation == 0) {
                            relation = actRel;
                        } else if (relation != actRel) {
                            sameDts = false;
                            break;
                        }
                    }

                    if (sameDts) {
                        builder.add(transS, 1);
                        transNum++;
                        alreadyDone.add(transS.id());
                    }
                }

                //builder.setName(transNum + " elements");
                TInvariant resultInvariant = builder.build();

                if (invWithNewMcts != null) {
                    for (Transition trans : resultInvariant) {
                        builder.add(trans, invWithNewMcts.factor(trans));
                    }

                    resultInvariant = builder.build();
                    int theGcd = gcdVector(resultInvariant.asVector());
                    if (theGcd > 1) {
                        for (Transition trans : resultInvariant) {
                            builder.add(trans, resultInvariant.factor(trans) / theGcd);
                        }
                    }

                    resultInvariant = builder.build();
                }

                ret.add(resultInvariant);
            }
        }
        LOGGER.debug("Successfully got strong MCTS");
        return ret;
    }

    private static int gcdVector(List<Integer> vector) {
        int ret = 0;

        for (int i : vector) {
            ret = gcd(ret, i);
        }

        return ret;
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
