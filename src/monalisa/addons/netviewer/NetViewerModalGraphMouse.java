/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer;

import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.annotations.AnnotatingModalGraphMouse.ModeKeyAdapter;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import java.awt.event.InputEvent;

/**
 * GraphMouse with inverted zoom directions
 *
 * @author JUNG Library, modified by Jens Einloft
 */
public final class NetViewerModalGraphMouse<V, E> extends AbstractModalGraphMouse {

    private GraphPopupMousePlugin gpmp;
    private Boolean simulatorMode;
    private NetViewer owner;

    public NetViewerModalGraphMouse(GraphPopupMousePlugin gpmp, NetViewer owner) {
        this(1.1F, 0.9090909F, gpmp, owner);
    }

    public NetViewerModalGraphMouse(float in, float out, GraphPopupMousePlugin gpmp, NetViewer owner) {
        super(in, out);
        this.owner = owner;
        this.gpmp = gpmp;
        loadPlugins();
        setModeKeyListener(new ModeKeyAdapter(this));
        simulatorMode = false;
    }

    @Override
    protected void loadPlugins() {
        pickingPlugin = new NetViewerPickingGraphMousePlugin(this, this.gpmp);
        animatedPickingPlugin = new AnimatedPickingGraphMousePlugin();
        translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
        scalingPlugin = new NetViewerScalingGraphMousePlugin(new NetViewerCrossoverScalingControl(), 0, 1 / 1.1f, 1.1f, this.owner);
        rotatingPlugin = new RotatingGraphMousePlugin();
        shearingPlugin = new ShearingGraphMousePlugin();

        add(scalingPlugin);
        setMode(Mode.PICKING);
    }

    public NetViewerScalingGraphMousePlugin getScalingPlugin() {
        return (NetViewerScalingGraphMousePlugin) scalingPlugin;
    }

    public void enableSimulatorMode(Boolean value) {
        simulatorMode = value;
    }

    public Boolean getSimulatorMode() {
        return this.simulatorMode;
    }

}
