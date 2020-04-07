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

import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

import monalisa.Project;
import monalisa.ToolManager;
import monalisa.data.Pair;
import monalisa.data.pn.PetriNetFacade;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.Result;

/**
 * The basic interface for a tool working on Petri nets.
 * @author Anja Thormann
 * @author Konrad Rudolph
 */
public interface Tool {
    /**
     * <p>Execute the tool.</p>
     * <p><strong>Note to implementors:</strong> To make a tool's execution
     * cancellable, it has to test the {@link Thread#isInterrupted()} status
     * and throw an {@link InterruptedException} accordingly, as well as handle
     * such exceptions from other sources (e.g. {@link Object#wait()}. For a
     * comprehensive guide, refer to
     * <a href="http://www.ibm.com/developerworks/java/library/j-jtp05236.html">Java theory and practice: Dealing with InterruptedException</a>.
     * </p>
     * @param project The {@link org.monalisa.Project} that provides all input
     *          and receives all output from executing the tool.
     * @param log A logger instance to receive any error messages and warnings
     *          encountered while running the tool.
     *          An error number &gt; 0 means that running the tool failed.
     * @return Returns a map of results, along with their configuration.
     * @throws InterruptedException Thrown when the user interrupts the
     *          execution of the tools.
     */
    Map<Configuration, Result> start(Project project, ErrorLog log, Configuration config) throws InterruptedException;

    /**
     * Add a new {@link ProgressListener}.
     * @param pl The listener.
     */
    void addProgressListener(ProgressListener pl);
    
    /**
     * Remove a {@link ProgressListener} from the listeners list.
     * @param pl The listener.
     */
    void removeProgressListener(ProgressListener pl);    
    
    /**
     * Save all the changes made to the properties via the GUI to the project.
     * @param p The project.
     */
    void saveSettings(Project p);
}
