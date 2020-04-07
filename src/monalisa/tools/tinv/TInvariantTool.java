/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.tinv;

import java.util.ArrayList;
import java.util.List;

import monalisa.results.Configuration;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.Project;
import monalisa.data.pn.TInvariant;
import monalisa.results.TInvariants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TInvariantTool extends AbstractTool {

    private static final Logger LOGGER = LogManager.getLogger(TInvariantTool.class);

    //public TInvariantTool(Project project){
    //    this.project = project;
    //}
    @Override
    public void run(Project project, ErrorLog log, Configuration config) throws InterruptedException {
        TInvariantCalculator calculator = null;
        try {
            LOGGER.info("Running TInvariantTool");
            calculator = new TInvariantCalculator(project.getPNFacade(), log);
            addResult(new TInvariantsConfiguration(), calculator.tinvariants(log));
            LOGGER.info("Successfully ran TInvariantTool");
            //setCTILabelText(calculator.tinvariants(log), this); NEEDS FIXING!
//            if (calculator.postScriptSource(log) != null)
//                addResult(new MauritiusMapConfiguration(),
//                calculator.postScriptSource(log));
        } catch (TInvariantCalculationFailedException e) {
            // Error already handled in calculator.
        }
    }

    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub
    }

    public int isCTI(Project project) {
        LOGGER.info("Checking whether Petri net is CTI");
        TInvariants tinv = project.getToolManager().getResult(TInvariantTool.class, new TInvariantsConfiguration());
        if (tinv == null) {
            LOGGER.warn("T-Invariants could not be found");
            return -1;
        } else {
            int nbrOfTransitions = project.getPetriNet().transitions().size();
            int i = 0;
            List<Integer> counterList = new ArrayList<>();
            try {
                for (TInvariant t : tinv) {
                    i = 0;
                    for (int j : t.asVector()) {
                        if (j > 0 && !counterList.contains(i)) {
                            counterList.add(i);
                        }
                        i++;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Caught exception while checking whether Petri net is CTI", e);
                return -1;
            }
            if (counterList.size() == nbrOfTransitions) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public int isCTI(TInvariants tinv, Project project) {
        LOGGER.info("Checking whether Petri net is CTI");
        if (tinv == null) {
            LOGGER.warn("T-Invariants could not be found");
            return -1;
        } else {
            int nbrOfTransitions = project.getPetriNet().transitions().size();
            int i = 0;
            List<Integer> counterList = new ArrayList<>();
            for (TInvariant t : tinv) {
                i = 0;
                for (int j : t.asVector()) {
                    if (j > 0 && !counterList.contains(i)) {
                        counterList.add(i);
                    }
                    i++;
                }
            }
            if (counterList.size() == nbrOfTransitions) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
