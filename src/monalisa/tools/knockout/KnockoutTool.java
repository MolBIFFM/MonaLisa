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

import java.util.List;
import java.util.Map;

import monalisa.Project;
import monalisa.results.Configuration;
import monalisa.results.Knockout;
import monalisa.results.KnockoutConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.ProgressEvent;
import monalisa.tools.ProgressListener;
import monalisa.tools.tinv.TInvariantCalculationFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnockoutTool extends AbstractTool implements ProgressListener {

    private static final Logger LOGGER = LogManager.getLogger(KnockoutTool.class);

    @Override
    protected void run(Project project, ErrorLog log, Configuration config)
            throws InterruptedException {
        LOGGER.info("Running KnockoutTool");
        KnockoutConfiguration knockConfig = (KnockoutConfiguration) config;
        KnockoutAlgorithm algorithm = knockConfig.getAlgorithm();

        if (algorithm == null) {
            LOGGER.error("Failed to initialize algorithm for KnockoutTool");
            throw new RuntimeException("Unreachable code, this should never happen.");
        }
        try {
            algorithm.run(this, log);
        } catch (TInvariantCalculationFailedException ex) {
            LOGGER.error("Caught TInvariantCalculationFailedException while running KnockoutTool: ", ex);
        }
        Map<List<String>, List<String>> results = algorithm.getResults();
        addResult(knockConfig, new Knockout(results));
        LOGGER.info("Successfully ran KnockoutTool");
    }

    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub

    }

    @Override
    public void progressUpdated(ProgressEvent e) {
        // KnockoutAlgorithm progress notification.
        fireProgressUpdated(e.getPercent());
    }
}
