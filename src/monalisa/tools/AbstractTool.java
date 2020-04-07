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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import monalisa.Project;
import monalisa.results.Configuration;
import monalisa.results.Result;

/**
 * Abstract base class for tools that already implements some of the {@link Tool}
 * interface's methods in a convenient way to allow easy event handling.
 * @author Konrad Rudolph
 */
public abstract class AbstractTool implements Tool {
    private final List<ProgressListener> progressListeners = new ArrayList<>();
    private final Map<Configuration, Result> results = new HashMap<>();

    @Override
    public final Map<Configuration, Result> start(Project project, ErrorLog log, Configuration config)
            throws InterruptedException {
        results.clear();
        run(project, log, config);
        return Collections.unmodifiableMap(results);
    }
    
    @Override
    public synchronized void addProgressListener(ProgressListener listener) {
        if (!progressListeners.contains(listener))
            progressListeners.add(listener);
    }

    @Override
    public synchronized void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    /**
     * Fire a {@link ProgressListener} update.
     * @param percent The new progress percentage.
     */
    protected final void fireProgressUpdated(int percent) {
        List<ProgressListener> listeners = getProgressListenersCopy(progressListeners);
        ProgressEvent event = new ProgressEvent(this, percent);
        
        for (ProgressListener listener : listeners)
            listener.progressUpdated(event);
    }

    /**
     * Add a new result to the list of results.
     * This method should be called from inside the {@link #run} method to save
     * a result. Results should <em>not</em> be saved directly to the project.
     * @param config The result's configuration.
     * @param result The result.
     */
    protected final void addResult(Configuration config, Result result) {
        results.put(config, result);
    }
    
    /**
    * <p>Perform the tool's action.</p>
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
    * @throws InterruptedException Thrown when the user interrupts the
    *          execution of the tools.
    */
    protected abstract void run(Project project, ErrorLog log, Configuration config)
            throws InterruptedException;
    
    private synchronized List<ProgressListener> getProgressListenersCopy(
            List<ProgressListener> listeners) {
        return new ArrayList<>(listeners);
    }
}
