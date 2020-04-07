/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.minv;

import monalisa.results.Configuration;
import monalisa.results.MInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.Project;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author daniel
 */
public final class MInvariantTool extends AbstractTool {

    private static final Logger LOGGER = LogManager.getLogger(MInvariantTool.class);

    @Override
    public void run(Project project, ErrorLog log, Configuration config) throws InterruptedException {
        MInvariantCalculator calculator = null;
        try {
            LOGGER.info("Running MInvariantTool");
            calculator = new MInvariantCalculator(project.getPNFacade(), log);
            addResult(new MInvariantsConfiguration(), calculator.minvariants(log));
            LOGGER.info("Successfully ran MInvariantTool");
        } catch (MInvariantCalculationFailedException e) {
            // Error already handled in calculator.
        }
    }

    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub

    }
}
