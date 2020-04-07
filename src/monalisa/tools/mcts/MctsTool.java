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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

import monalisa.data.pn.TInvariant;
import monalisa.data.pn.Transition;
import monalisa.results.Configuration;
import monalisa.results.Mcts;
import monalisa.results.MctsConfiguration;
import monalisa.results.TInvariants;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.Project;
import monalisa.data.pn.PetriNetFacade;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class MctsTool extends AbstractTool {

    private static final Logger LOGGER = LogManager.getLogger(MctsTool.class);

    @Override
    public void run(Project project, ErrorLog log, Configuration config) {
        LOGGER.info("Running MctsTool");
        MctsConfiguration mctsConfig = (MctsConfiguration) config;
        computeMCTSets(project, mctsConfig);
        LOGGER.info("Successfully ran MctsTool");
    }

    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub
    }

    private static List<Transition> sortedTransitions(PetriNetFacade petriNet) {
        ArrayList<Transition> result = new ArrayList<>(petriNet.transitions());
        Collections.sort(result);
        return result;
    }

    private void computeMCTSets(Project project, MctsConfiguration config) {
        LOGGER.info("Computing MCTSets");
        boolean includeTrivialInvariants = config.hasTrivialTInvariants();
        boolean supportOriented = !config.isStrong();
        TInvariants allTinvariants = project.getToolManager().getResult(
            TInvariantTool.class, new TInvariantsConfiguration());
        List<TInvariant> tinvariants = new ArrayList<>(
            includeTrivialInvariants ? allTinvariants :
                allTinvariants.nonTrivialTInvariants());
        List<Transition> transitions = sortedTransitions(project.getPNFacade());

        int[][] matrix = InvariantStatistics.calculateMatrixTInvariant2TransitionOccurrence(
            tinvariants, transitions);
        List<TInvariant> mcts = supportOriented ?
            InvariantStatistics.getSupportMCTset(matrix, transitions, tinvariants, project.getPNFacade()) :
            InvariantStatistics.getStrongMCTset(matrix, transitions, tinvariants, project.getPNFacade());
        if(mcts != null) {
            addResult(config, new Mcts(mcts));
        }
        LOGGER.info("Successfully computed MCTSets");
    }

    /**
     * For Manatees
     * @param includeTrivialInvariants
     * @param supportOriented
     * @return
     */
    public List<TInvariant> computeMCTSets(TInvariants allTinvariants, PetriNetFacade pnf, boolean includeTrivialInvariants) {
        LOGGER.info("Computing MCTSets for Manatees");
        List<TInvariant> tinvariants = new ArrayList<>(
            includeTrivialInvariants ? allTinvariants :
                allTinvariants.nonTrivialTInvariants());
        List<Transition> transitions = sortedTransitions(pnf);

        int[][] matrix = InvariantStatistics.calculateMatrixTInvariant2TransitionOccurrence(tinvariants, transitions);
        List<TInvariant> mcts = InvariantStatistics.getSupportMCTset(matrix, transitions, tinvariants, pnf);
        LOGGER.info("Successfully computed MCTSets for Manatess");
        if(mcts != null) {
            return mcts;
        }

        return new ArrayList<>();
    }
}
