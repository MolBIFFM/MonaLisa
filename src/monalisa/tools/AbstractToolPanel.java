/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.tools;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Abstract base class for toolpanels that already implements some of the
 * {@link ToolPanel} interface's methods in a convenient way to allow easy event
 * handling.
 *
 * @author Marcel Gehrmann
 */
public abstract class AbstractToolPanel extends JPanel implements ToolPanel {

    private final List<BooleanChangeListener> activityChangedListeners = new ArrayList<>();

    @Override
    public synchronized void addActivityChangedListener(
            BooleanChangeListener listener) {
        if (!activityChangedListeners.contains(listener)) {
            activityChangedListeners.add(listener);
        }
    }

    @Override
    public synchronized void removeActivityChangedListener(
            BooleanChangeListener listener) {
        activityChangedListeners.remove(listener);
    }

    /**
     * Fire a {@link BooleanChangeListener} update.
     *
     * @param newValue The new value.
     */
    protected final void fireActivityChanged(boolean newValue) {
        List<BooleanChangeListener> listeners
                = getActivityChangedListenersCopy(activityChangedListeners);
        BooleanChangeEvent event = new BooleanChangeEvent(this, newValue);

        for (BooleanChangeListener listener : listeners) {
            listener.changed(event);
        }
    }

    private synchronized List<BooleanChangeListener> getActivityChangedListenersCopy(
            List<BooleanChangeListener> listeners) {
        return new ArrayList<>(listeners);
    }
}
