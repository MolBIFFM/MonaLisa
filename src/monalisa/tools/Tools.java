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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import monalisa.tools.cluster.ClusterTool;
import monalisa.tools.knockout.KnockoutTool;
import monalisa.tools.mcs.McsTool;
import monalisa.tools.mcts.MctsTool;
import monalisa.tools.pinv.PInvariantTool;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.tools.minv.MInvariantTool;

@SuppressWarnings("unchecked")
public final class Tools {
    private static final List<Class<? extends Tool>> toolTypes =
        Arrays.<Class<? extends Tool>>asList(
            TInvariantTool.class,
            MInvariantTool.class,
            PInvariantTool.class,
            MctsTool.class,
            ClusterTool.class,
            KnockoutTool.class,
            McsTool.class
        );

    public static List<Class<? extends Tool>> toolTypes() {
        return Collections.unmodifiableList(toolTypes);
    }
        
    public static String name(Class<? extends Tool> toolType) {
        return toolType.getSimpleName();
    }
    
    public static String name(Tool tool) {
        return name(tool.getClass());
    }
}
