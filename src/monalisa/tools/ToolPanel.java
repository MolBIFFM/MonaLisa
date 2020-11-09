/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.tools;

import java.util.List;
import monalisa.ToolManager;
import monalisa.data.Pair;
import monalisa.results.Configuration;

/**
 * A panel that holds the GUI necessary to control the settings of a
 * corresponding tool.<b>Note to implementors:</b> A panel should be created
 * only once and cached internally.
 *
 * @author Marcel Gehrmann
 */
public interface ToolPanel {

    /**
     * The configuration provided by this function is intended to be used with
     * the associated tool's 'run' function. Any ToolPanel implementing this
     * needs to provide a valid configuration for the respective tool.
     *
     * @return the corresponding tool's configuration.
     */
    Configuration getConfig();

    /**
     * Should always return the class of the tool the ToolPanel is associated
     * with.
     *
     * @return Class of the associated tool.
     */
    Class<? extends Tool> getToolType();

    /**
     * Returns whether this panel's tool should be executed (i.e.whether the
     * user has activated it on the UI).
     *
     * @return whether the corresponding tool should be executed
     */
    boolean isActive();

    /**
     * Set whether this panel's tool should be (re-)executed.
     *
     * @param active The new value for the activity.
     */
    void setActive(boolean active);

    /**
     * Activates the tool, and sets its settings to calculate the required
     * {@link Configuration}s {@code configs}. This does not modify any other of
     * the tool's settings, if the tool is able to calculate several results at
     * once.
     *
     * @param configs The required configurations.
     */
    void setActive(Configuration... configs);

    /**
     * Add a new {@link BooleanChangeListener} that gets fired whenever the
     * toolpanel's {@link #isActive()} value changes.
     *
     * @param bcl The change listener to add.
     */
    void addActivityChangedListener(BooleanChangeListener bcl);

    /**
     * Remove a change listener added by
     * {@link #addActivityChangedListener(BooleanChangeListener)}.
     *
     * @param bcl The change listener to remove.
     */
    void removeActivityChangedListener(BooleanChangeListener bcl);

    /**
     * Determine the requirements for the tool in its current configuration. The
     * requirements of a tool may change depending on what options are set.
     *
     * @return A read-only list of pairs that denote 1. the required tool, and
     * 2. the required {@link Configuration} that will yield the required
     * result.
     */
    List<Pair<Class<? extends Tool>, Configuration>> getRequirements();

    /**
     * Determines whether a tool has calculated results for all possible
     * configurations. As a consequence, this method disables the tool if all
     * possible configurations have been exhausted. The tool may also disable
     * all visual components associated with exhausted configurations, even if
     * other configurations are still choose-able.
     *
     * @param toolMan The associated ToolManager that stores the results.
     * @return Returns <code>true</code> if the tool has exhausted <em>all</em>
     * configurations, otherwise <code>false</code>.
     */
    boolean finishedState(ToolManager toolMan);
}
