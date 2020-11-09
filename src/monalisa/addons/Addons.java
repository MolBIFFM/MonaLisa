/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons;

import java.util.ArrayList;
import java.util.List;
import monalisa.addons.annotations.AnnotationsPanel;
import monalisa.addons.centrality.CentralityPanel;
import monalisa.addons.tokensimulator.SimulationPanel;
import monalisa.addons.topological.TopologyPanel;
import monalisa.addons.netproperties.NetPropertiesPanel;

/**
 * A list of all available Addons for MonaLisa
 *
 * @author Jens Einloft
 */
public class Addons {

    public static final List<Class<? extends AddonPanel>> addons = new ArrayList<>();

    static {
        addons.add(SimulationPanel.class);
        addons.add(CentralityPanel.class);
        addons.add(AnnotationsPanel.class);
        addons.add(TopologyPanel.class);
        addons.add(NetPropertiesPanel.class);
    }

}
