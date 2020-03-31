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

import monalisa.data.pn.PetriNetFacade;
import monalisa.results.Configuration;
import monalisa.results.Result;

public final class ToolRunner extends Thread {
    private final Tool tool;
    private final PetriNetFacade pnf;
    private final ErrorLog log;
    private Map<Configuration, Result> results;
    
    public ToolRunner(Tool tool, PetriNetFacade pnf, ErrorLog log) {
        this.tool = tool;
        this.pnf = pnf;
        this.log = log;
    }
    
    @Override
    public void run() {
        try {
            results = tool.start(pnf, log);
        } catch (InterruptedException e) {
            results = Collections.emptyMap();
            Thread.currentThread().interrupt();
        }
    }
    
    public Map<Configuration, Result> results() {
        return results;
    }
}
