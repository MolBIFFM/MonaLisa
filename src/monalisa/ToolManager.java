/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.Result;
import monalisa.tools.ErrorLog;
import monalisa.tools.ProgressEvent;
import monalisa.tools.ProgressListener;
import monalisa.tools.Tool;
import monalisa.tools.ToolRunner;
import monalisa.tools.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manager for Tools implemented in MonaLisa.
 * @author Marcel Gehrmann
 */
public class ToolManager implements ProgressListener, Serializable {

    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();
    private static final long serialVersionUID = -3642254005926990462L;

    transient private List<Tool> tools;
    transient private Map<Class<? extends Tool>, Map<Configuration, Result>> results;
    transient private ErrorLog toolMessages;
    transient Thread toolsThread;
    transient List<ToolStatusUpdateListener> toolStatusUpdateListeners;

    private static final Logger LOGGER = LogManager.getLogger(ToolManager.class);

    /**
     * Initializes a new ToolManager instance.
     */
    public ToolManager() {
        LOGGER.info("Initializing new ToolManager.");
        this.toolStatusUpdateListeners = new ArrayList<>();
        initTools();
        LOGGER.info("Successfully initialized new ToolManager.");
    }

    /**
     * Initializes all tools registered in monaLisa.tools.Tools.
     */
    private void initTools() {
        this.tools = new ArrayList<>();
        this.results = new HashMap<>();
        for (Class<? extends Tool> toolType : Tools.toolTypes()) {
            try {
                this.tools.add(toolType.newInstance());
                this.results.put(toolType, new HashMap<Configuration, Result>());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(
                        "An unexpected error has occurred while trying to load "
                        + "the tools. Sorry! Please report this error.", e);
            }
        }
    }

    /**
     * Reset all results of all tools.
     */
    public void resetTools() {
        LOGGER.info("Resetting tool results");
        results.clear();
        for (Class<? extends Tool> toolType : Tools.toolTypes()) {
            results.put(toolType, new HashMap<Configuration, Result>());
        }
    }

    /**
     * Returns all tools.
     * @return tools
     */
    public List<Tool> getTools() {
        return tools;
    }

    /**
     * Returns the name of the panel of a given tool.
     *
     * @param tool
     * @return
     */
    public String getToolName(Tool tool) {
        return Tools.name(tool);
    }

    /**
     * Returns the instance of a given Tool.
     *
     * @param toolType The class of the requested tool.
     * @return
     */
    public Tool getTool(Class<? extends Tool> toolType) {
        for (Tool tool : tools) {
            if (toolType.isInstance(tool)) {
                return tool;
            }
        }
        return null;
    }

    /**
     * Updates the results for all tools.
     */
    public void updateTools() {
        for (Class<? extends Tool> toolType : Tools.toolTypes()) {
            if (results.containsKey(toolType)) {
                results.put(toolType, new HashMap<Configuration, Result>());
            }
        }
    }

    /**
     * Returns all calculated results of all tools
     *
     * @return Returns a map from configurations to results, off all tool
     */
    public Map<Class<? extends Tool>, Map<Configuration, Result>> getAllResults() {
        return results;
    }

    /**
     * Retrieves the result specified by a tool and a configuration.
     *
     * @param <T> The type of the result.
     * @param tool The type of the tool class that calculated the result.
     * @param config The configuration that calculated the result.
     * @return Returns the requested result, or <code>null</code> if
     * <code>{@link #hasResult}(tool, config)</code> returns <code>false</code>.
     */
    @SuppressWarnings(value = "unchecked")
    public <T> T getResult(Class<? extends Tool> tool, Configuration config) {
        if (!results.containsKey(tool)) {
            putResult(tool, config, null);
        }
        return (T) results.get(tool).get(config);
    }

    /**
     * Retrieves the result specified by a tool and a configuration.
     *
     * @param <T> The type of the result.
     * @param tool The tool that calculated the result.
     * @param config The configuration that calculated the result.
     * @return Returns the requested result, or <code>null</code> if
     * <code>{@link #hasResult}(tool, config)</code> returns <code>false</code>.
     * @see #getResult(Class, Configuration)
     */
    public <T> T getResult(Tool tool, Configuration config) {
        return (T) getResult(tool.getClass(), config);
    }

    /**
     * Returns all error messages and warnings generated by the last run of the
     * tools, or {@code null} if the tools have not yet been run.
     *
     * @return error messages from tools
     */
    public ErrorLog getToolMessages() {
        return toolMessages;
    }

    /**
     * Stores a new result calculated by the given tool class and configuration.
     *
     * @param tool The type of the tool class that calculated the result.
     * @param config The configuration that calculated the result.
     * @param result The result.
     */
    public void putResult(Class<? extends Tool> tool, Configuration config, Result result) {
        LOGGER.debug("Putting result for tool " + tool.getSimpleName());
        if (results.containsKey(tool)) {
            results.get(tool).put(config, result);
        } else {
            Map<Configuration, Result> map = new HashMap<>();
            map.put(config, result);
            results.put(tool, map);
        }
        LOGGER.debug("Finished putting result");
    }

    /**
     * Stores a new result calculated by the given tool class and configuration.
     *
     * @param tool The tool that calculated the result.
     * @param config The configuration that calculated the result.
     * @param result The result.
     * @see #putResult(Class, Configuration, Result)
     */
    public void putResult(Tool tool, Configuration config, Result result) {
        putResult(tool.getClass(), config, result);
    }

    /**
     * Tests whether a specific result has already been calculated. A result is
     * identified by the {@link Tool} calculating it, as well as a
     * {@link Configuration}.
     *
     * @param tool The type of the tool class that calculated the result.
     * @param config The configuration to reproduce the result.
     * @return Returns <code>true</code> if the result exists, else
     * <code>false</code>.
     */
    public boolean hasResult(Class<? extends Tool> tool, Configuration config) {
        return results.get(tool).containsKey(config);
    }

    /**
     * Tests whether a specific result has already been calculated. A result is
     * identified by the {@link Tool} calculating it, as well as a
     * {@link Configuration}.
     *
     * @param tool The tool that calculated the result.
     * @param config The configuration to reproduce the result.
     * @return Returns <code>true</code> if the result exists, else
     * <code>false</code>.
     * @see #hasResult(Class, Configuration)
     */
    public boolean hasResult(Tool tool, Configuration config) {
        return hasResult(tool.getClass(), config);
    }

    /**
     * Test whether all possible results have been calculated for a given tool.
     * Since the project doesn't know which results are possible, the expected
     * number of results is passed to this method and is compared against the
     * number of available results.
     *
     * @param tool The type of the tool class that calculated the results.
     * @param expectedNumber The expected number of overall results for that
     * tool.
     * @return Returns <code>true</code> if the number of results matches the
     * expected number, else <code>false</code>.
     */
    public boolean hasAllResults(Class<? extends Tool> tool, int expectedNumber) {
        return results.get(tool).size() == expectedNumber;
    }

    /**
     * Test whether all possible results have been calculated for a given tool.
     * Since the project doesn't know which results are possible, the expected
     * number of results is passed to this method and is compared against the
     * number of available results.
     *
     * @param tool The tool that calculated the results.
     * @param expectedNumber The expected number of overall results for that
     * tool.
     * @return Returns <code>true</code> if the number of results matches the
     * expected number, else <code>false</code>.
     * @see #hasAllResults(Class, int)
     */
    public boolean hasAllResults(Tool tool, int expectedNumber) {
        return hasAllResults(tool.getClass(), expectedNumber);
    }

    /**
     * Returns all results calculated by the specified tool.
     *
     * @param tool The type of the tool class that calculated the results.
     * @return Returns a map from configurations to results, or
     * <code>null</code> if <code>{@link #hasResults}(tool)</code> returns
     * <code>false</code>.
     */
    public Map<Configuration, Result> getResults(Class<? extends Tool> tool) {
        return Collections.unmodifiableMap(results.get(tool));
    }

    /**
     * Returns all results calculated by the specified tool.
     *
     * @param tool The tool that calculated the results.
     * @return Returns a map from configurations to results, or
     * <code>null</code> if <code>{@link #hasResults}(tool)</code> returns
     * <code>false</code>.
     * @see #getResults(Class)
     */
    public Map<Configuration, Result> getResults(Tool tool) {
        return getResults(tool.getClass());
    }

    /**
     * Tests whether the specified tool already has calculated some results.
     *
     * @param tool The type of the tool class.
     * @return Returns <code>true</code> if at least one result is associated
     * with the tool, otherwise <code>false</code>.
     */
    public boolean hasResults(Class<? extends Tool> tool) {
        Map<Configuration, Result> res = results.get(tool);
        return res != null && res.size() > 0;
    }

    /**
     * Tests whether the specified tool already has calculated some results.
     *
     * @param tool The tool.
     * @return Returns <code>true</code> if at least one result is associated
     * with the tool, otherwise <code>false</code>.
     * @see #hasResults(Class)
     */
    public boolean hasResults(Tool tool) {
        return hasResults(tool.getClass());
    }

    /**
     * Add a new ToolStatusUpdateListener to the project.
     *
     * @param listener
     */
    public synchronized void addToolStatusUpdateListener(ToolStatusUpdateListener listener) {
        if (!toolStatusUpdateListeners.contains(listener)) {
            toolStatusUpdateListeners.add(listener);
        }
    }

    /**
     * Fires a status update for a tool with a given status
     * @param tool the tool to fire an update for
     * @param status the new status of the tool
     */
    private void fireToolStatusUpdate(Tool tool, ToolStatusUpdateEvent.Status status) {
        fireToolStatusUpdate(new ToolStatusUpdateEvent(this, getToolName(tool), status));
    }

    /**
     * Fires a status update for a tool with a given progress
     * @param tool the tool to fire an update for
     * @param progress the progress of the tool
     */
    private void fireToolStatusUpdate(Tool tool, int progress) {
        fireToolStatusUpdate(new ToolStatusUpdateEvent(this, getToolName(tool), progress));
    }

    /**
     * Distributes the status update event to all listeners
     * @param e the tool status update event
     */
    protected synchronized void fireToolStatusUpdate(ToolStatusUpdateEvent e) {
        List<ToolStatusUpdateListener> listeners = new ArrayList<>(toolStatusUpdateListeners);
        for (ToolStatusUpdateListener listener : listeners) {
            listener.updated(e);
        }
    }

    /**
     * Removes a ToolStatusUpdateListener from the project.
     * @param listener
     */
    public synchronized void removeToolStatusUpdateListener(ToolStatusUpdateListener listener) {
        toolStatusUpdateListeners.remove(listener);
    }

    @Override
    public void progressUpdated(ProgressEvent e) {
        fireToolStatusUpdate((Tool) e.getSource(), e.getPercent());
    }

    /**
     * Stops all running tools.
     */
    public void stopRunningTools() {
        LOGGER.warn("Aborting the running of tools.");
        toolsThread.interrupt();
        fireToolStatusUpdate(new ToolStatusUpdateEvent(this, null, ToolStatusUpdateEvent.Status.ABORTED));
        LOGGER.warn("Finished aborting the running tools.");
    }

    /**
     * Resets the messages from tools to an empty ErrorLog.
     */
    public void resetToolMessages() {
        toolMessages = new ErrorLog();
    }

    /**
     * Runs a tool with a given configuration for it.
     * @param tool tool to be run.
     * @param config config for the tool to be run.
     * @param project
     * @return
     */
    public ErrorLog runSingleTool(Tool tool, Configuration config, Project project) {
        LOGGER.info(getToolName(tool) + " started");
        tool.addProgressListener(this);
        fireToolStatusUpdate(tool, ToolStatusUpdateEvent.Status.STARTED);
        ErrorLog log = new ErrorLog();
        ToolRunner runner = new ToolRunner(tool, project, log, config);
        runner.start();
        try {
            runner.join();
        } catch (InterruptedException e) {
            runner.interrupt();
            LOGGER.warn("Interrupt caught.");
            Thread.currentThread().interrupt();
            LOGGER.warn("Interrupt propagated.");
            return log;
        } finally {
            tool.removeProgressListener(this);
        }
        for (Map.Entry<Configuration, Result> entry : runner.results().entrySet()) {
            putResult(tool, entry.getKey(), entry.getValue());
        }
        toolMessages.logAll(log);
        fireToolStatusUpdate(tool, ToolStatusUpdateEvent.Status.FINISHED);
        LOGGER.info(getToolName(tool) + " finished");
        return log;
    }
}
