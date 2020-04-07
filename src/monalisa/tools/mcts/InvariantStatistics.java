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
import monalisa.data.pn.Transition;

public class InvariantStatistics {

    public static int[][] calculateMatrixTInvariant2TransitionOccurrence(
            List<TInvariant> invariants,
            List<Transition> transitions) {
        int[][] matrix = new int[invariants.size()][transitions.size()];
        for (int indexInvariant = 0; indexInvariant < invariants.size(); indexInvariant++) {
            TInvariant inv = invariants.get(indexInvariant);
            List<Integer> iv = inv.asVector(transitions);
            for (int i = 0; i < iv.size(); i++) {
                matrix[indexInvariant][i] = iv.get(i);
            }
        }
        return matrix;
    }

    public static List<TInvariant> getSupportMCTset(
            int[][] matrix,
            List<Transition> transitions,
            List<TInvariant> invariants,
            PetriNetFacade pnf) {
        MCTSFactory mctsf = new MCTSFactory(matrix, transitions, invariants, pnf);
        return mctsf.mctsSupport();
    }

    public static List<TInvariant> getStrongMCTset(
            int[][] matrix,
            List<Transition> transitions,
            List<TInvariant> invariants,
            PetriNetFacade pnf) {
        MCTSFactory mctsf = new MCTSFactory(matrix, transitions, invariants, pnf);
        return mctsf.strongMcts();
    }
}
