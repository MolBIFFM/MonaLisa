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
    Map<Configuration, Result> start(PetriNetFacade pnf, ErrorLog log) throws InterruptedException;

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
     * Returns whether this tool should be executed (i.e. whether the user has
     * activated it on the UI).
     */
    boolean isActive();
    
    /**
     * Set whether this tool should be (re-)executed.
     * @param active The new value for the activity.
     */
    void setActive(boolean active);
    
    /**
     * Activates the tool, and sets its settings to calculate the required
     * {@link Configuration}s {@code configs}. This does not modify any other
     * of the tool's settings, if the tool is able to calculate several results
     * at once.
     * @param configs The required configurations.
     */
    void setActive(Configuration... configs);
 
    /**
     * Determines whether a tool has calculated results for all possible
     * configurations. As a consequence, this method disables the tool if all
     * possible configurations have been exhausted. The tool may also disable
     * all visual components associated with exhausted configurations, even if
     * other configurations are still choose-able.
     * @param project The associated project that stores the results.
     * @return Returns <code>true</code> if the tool has exhausted <em>all</em>
     *          configurations, otherwise <code>false</code>.
     */
    boolean finishedState(Project project);
    
    /**
     * Return a panel that holds the GUI necessary to control the settings of
     * this tool.
     * <b>Note to implementors:</b> This panel should be created only once and
     * cached internally.
     * @param project The associated project.
     * @param strings A {@link StringResources} that holds the localization
     * strings to populate the user interface.
     * @return A panel holding the user interface.
     */
    JPanel getUI(Project project, StringResources strings);
    
    /**
     * Save all the changes made to the properties via the GUI to the project.
     * @param p The project.
     */
    void saveSettings(Project p);
    
    /**
     * Add a new {@link BooleanChangeListener} that gets fired whenever the
     * tool's {@link #isActive()} value changes.
     * @param bcl The change listener to add.
     */
    void addActivityChangedListener(BooleanChangeListener bcl);
    
    /**
     * Remove a change listener added by
     * {@link #addActivityChangedListener(BooleanChangeListener)}.
     * @param bcl The change listener to remove.
     */
    void removeActivityChangedListener(BooleanChangeListener bcl);
    
    /**
     * Determine the requirements for the tool in its current configuration.
     * The requirements of a tool may change depending on what options are set.
     * @return A read-only list of pairs that denote 1. the required tool, and
     *          2. the required {@link Configuration} that will yield the
     *          required result.
     */
    List<Pair<Class<? extends Tool>, Configuration>> getRequirements();

}
