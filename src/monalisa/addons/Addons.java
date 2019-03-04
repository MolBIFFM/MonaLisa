/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons;

import java.util.ArrayList;
import java.util.List;
import monalisa.addons.annotations.AnnotationsPanel;
import monalisa.addons.centrality.CentralityPanel;
import monalisa.addons.tokensimulator.TokenSimPanel;
import monalisa.addons.topological.TopologyPanel;

/**
 * A list of all available Addons for MonaLisa 
 * @author Jens Einloft
 */
public class Addons {
    
    public static final List<Class<? extends AddonPanel>> addons = new ArrayList<>();
    
    static {
        addons.add(TokenSimPanel.class);
        addons.add(CentralityPanel.class);
        addons.add(AnnotationsPanel.class);
        addons.add(TopologyPanel.class);  
    }    
    
}
