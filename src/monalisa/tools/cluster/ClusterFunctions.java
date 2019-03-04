/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.cluster;

import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;

public final class ClusterFunctions {
    public static final String UPGMA = "UPGMA";
    public static final String WPGMA = "WPGMA";
    public static final String NeighborJoining = "NeighborJoining";
    public static final String SingleLinkage = "SingleLinkage";
    public static final String CompleteLinkage = "CompleteLinkage";
    
    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();
    
    /**
     * Return a human-readable, localized name of the cluster function.
     * @param clusterFunction The cluster function.
     * @return A human-readable text representing the function's name.
     */
    public static String getName(String clusterFunction) {
        return strings.get("ClusterFunction" + clusterFunction);
    }
}
