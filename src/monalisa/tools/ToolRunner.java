/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools;

import java.util.Collections;
import java.util.Map;
import monalisa.Project;

import monalisa.data.pn.PetriNetFacade;
import monalisa.results.Configuration;
import monalisa.results.Result;

public final class ToolRunner extends Thread {
    private final Tool tool;
    private final Project project;
    private final ErrorLog log;
    private final Configuration config;
    private Map<Configuration, Result> results;
    
    public ToolRunner(Tool tool, Project project, ErrorLog log, Configuration config) {
        this.tool = tool;
        this.project = project;
        this.log = log;
        this.config = config;
    }
    
    @Override
    public void run() {
        try {
            results = tool.start(project, log, config);
        } catch (InterruptedException e) {
            results = Collections.emptyMap();
            Thread.currentThread().interrupt();
        }
    }
    
    public Map<Configuration, Result> results() {
        return results;
    }
}
