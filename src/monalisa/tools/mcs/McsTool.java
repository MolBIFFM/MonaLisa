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

import java.util.List;
import java.util.Set;

import monalisa.Project;
import monalisa.data.pn.Transition;
import monalisa.results.Configuration;
import monalisa.results.McsConfiguration;
import monalisa.results.Mcs;
import monalisa.results.TInvariants;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.tinv.TInvariantTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class McsTool extends AbstractTool {

    private static final Logger LOGGER = LogManager.getLogger(McsTool.class);

    @Override
    protected void run(Project project, ErrorLog log, Configuration config) throws InterruptedException {
        LOGGER.info("Running McsTool");
        McsConfiguration mcsConfig = (McsConfiguration) config;
        final TInvariants tinv = project.getToolManager().getResult(TInvariantTool.class, new TInvariantsConfiguration());
        final Transition objective = mcsConfig.getObjective();
        final int maxCutSetSize = mcsConfig.getMaxCutSetSize();

        McsAlgorithm algorithm = new McsAlgorithm(project.getPNFacade(), tinv, objective);
        List<Set<Transition>> mcs = algorithm.findMcs(maxCutSetSize);

        addResult(mcsConfig, new Mcs(mcs));
        LOGGER.info("Successfully ran McsTool");
    }

    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub
    }
}
