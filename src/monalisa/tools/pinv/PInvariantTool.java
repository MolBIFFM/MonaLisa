/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.pinv;

import monalisa.data.pn.PInvariant;
import java.util.ArrayList;
import java.util.List;

import monalisa.results.Configuration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.Project;
import monalisa.ToolManager;
import monalisa.results.PInvariants;
import monalisa.results.PInvariantsConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class PInvariantTool extends AbstractTool {

    private static final Logger LOGGER = LogManager.getLogger(PInvariantTool.class);
    private static String whichcalc; 
    //if P-Invariants should be calculated for the place bordered Petri net
    public static void setplaceborder(){
        whichcalc = "PIw";
    }
    //if P-Invariants should be calculated for the transition bordered (original) Petri net
    public static void settransborder(){
        whichcalc = "PI";
    }

    @Override
    public void run(Project project, ErrorLog log, Configuration config) throws InterruptedException {
        PInvariantCalculator calculator = null;
        try {
            LOGGER.info("Running PInvariantTool");
            //System.out.println("whichcalc: " + whichcalc);
            calculator = new PInvariantCalculator(project.getPNFacade(), log, whichcalc);
            addResult(new PInvariantsConfiguration(), calculator.pinvariants(log));
            LOGGER.info("Successfully ran PInvariantTool");
        } catch (PInvariantCalculationFailedException e) {
            // Error already handled in calculator.
        }
    }

    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub

    }
    //checks whether Petri net is CPI
    public int isCPI(Project project) {
        LOGGER.info("Checking whether Petri net is CPI");
        ToolManager tm = project.getToolManager();
        LOGGER.info("ToolManager loaded");
        PInvariants pinv = tm.getResult(PInvariantTool.class, new PInvariantsConfiguration());
        if (pinv == null) {
            LOGGER.warn("P-Invariants could not be found");
            return -1;
        } else {
            int nbrOfPlaces = project.getPetriNet().places().size();
            int i = 0;
            List<Integer> counterList = new ArrayList<>();
            try {
                for (PInvariant t : pinv) {
                    i = 0;
                    for (int j : t.asVector()) {
                        if (j > 0 && !counterList.contains(i)) {
                            counterList.add(i);
                        }
                        i++;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Caught exception while checking whether Petri net is CPI", e);
                return -1;
            }
            if (counterList.size() == nbrOfPlaces && whichcalc == "PI") {
                return 1; //if CPI and original net
                
            } else if(counterList.size() == nbrOfPlaces && whichcalc == "PIw"){
                return 2;  //if CPI and place bordered net              
            } else if(counterList.size() != nbrOfPlaces && whichcalc == "PIw"){
                return 3;  //if not CPI and place bordered net               
            }
            else {
                return 0;
            }
        }
    }
    //checks whether Petri net is CPI
    public int isCPI(PInvariants pinv, Project project) {
        LOGGER.info("Checking whether Petri net is CPI");
        if (pinv == null) {
            LOGGER.warn("P-Invariants could not be found");
            return -1;
        } else {
            int nbrOfPlaces = project.getPetriNet().places().size();
            int i = 0;
            List<Integer> counterList = new ArrayList<>();
            for (PInvariant t : pinv) {
                i = 0;
                for (int j : t.asVector()) {
                    if (j > 0 && !counterList.contains(i)) {
                        counterList.add(i);
                    }
                    i++;
                }
            }
            if (counterList.size() == nbrOfPlaces && whichcalc == "PI") {
                return 1; //if CPI and original net
                
            } else if(counterList.size() == nbrOfPlaces && whichcalc == "PIw"){
                return 2;  //if CPI and place bordered net              
            } else if(counterList.size() != nbrOfPlaces && whichcalc == "PIw"){
                return 3;  //if not CPI and place bordered net               
            }else {
                return 0;
            }
        }
    }
}
