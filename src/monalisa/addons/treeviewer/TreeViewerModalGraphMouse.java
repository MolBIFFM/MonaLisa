/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Molekulare Bioinformatik, Goethe University Frankfurt, Frankfurt am Main, Germany
 *
 */

package monalisa.addons.treeviewer;

import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import java.awt.event.InputEvent;

/**
 * A modified DefaultModalGraphMouse. Picking mode without the possibility to drag the nodes
 * @author Jens Einloft
 */
public class TreeViewerModalGraphMouse<V, E> extends AbstractModalGraphMouse {

    public TreeViewerModalGraphMouse() {
        super(1.1F, 0.9090909F);
        loadPlugins();
    }    
    
    @Override
    protected void loadPlugins() {
        pickingPlugin = new PickingGraphMousePlugin<V,E>();
        ((PickingGraphMousePlugin) pickingPlugin).setLocked(true);
        animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V,E>();
        translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
        scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
        rotatingPlugin = new RotatingGraphMousePlugin();
        shearingPlugin = new ShearingGraphMousePlugin();

        add(scalingPlugin);
        setMode(Mode.PICKING);
    }

}
