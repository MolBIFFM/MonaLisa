/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools;

import java.util.Collections;
import java.util.Map;

import monalisa.Project;
import monalisa.results.Configuration;
import monalisa.results.Result;

public final class ToolRunner extends Thread {
    private final Tool tool;
    private final Project project;
    private final ErrorLog log;
    private Map<Configuration, Result> results;
    
    public ToolRunner(Tool tool, Project project, ErrorLog log) {
        this.tool = tool;
        this.project = project;
        this.log = log;
    }
    
    @Override
    public void run() {
        try {
            results = tool.start(project.getPNFacade(), log);
        } catch (InterruptedException e) {
            results = Collections.emptyMap();
            Thread.currentThread().interrupt();
        }
    }
    
    public Map<Configuration, Result> results() {
        return results;
    }
}
