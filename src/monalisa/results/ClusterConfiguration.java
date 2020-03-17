/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.results;

import monalisa.resources.StringResources;
import monalisa.tools.cluster.ClusterFunctions;

public final class ClusterConfiguration implements Configuration {
    private static final long serialVersionUID = 6553322655138235537L;
    
    private final String distanceFunction;
    private final String clusterAlgorithm;
    private final float threshold;
    private final boolean includeTrivialTInvariants;

    public ClusterConfiguration(String distanceFunction, String clusterAlgorithm, float threshold, boolean includeTrivialTInvariants) {
        this.distanceFunction = distanceFunction;
        this.clusterAlgorithm = clusterAlgorithm;
        this.threshold = threshold;
        this.includeTrivialTInvariants = includeTrivialTInvariants;
    }
    
    public float getThreshold() {
        return this.threshold;
    }
    
    @Override
    public String toString(StringResources strings) {
        String cluster = ClusterFunctions.getName(clusterAlgorithm);
        String with = strings.get(includeTrivialTInvariants ? "With" : "Without");
        String percent = String.format("%.0f", threshold);
        return strings.get("ClusterTable", cluster, distanceFunction, percent, with, "");
    }
    
    @Override
    public String toString() {
        return String.format("%s with %s (%d%%) %s",
            clusterAlgorithm, distanceFunction, (int)threshold,
            (includeTrivialTInvariants ? "with" : "without") +  " trivial EM");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ClusterConfiguration))
            return false;
        ClusterConfiguration other = (ClusterConfiguration) obj;
        return distanceFunction.equals(other.distanceFunction) &&
            clusterAlgorithm.equals(other.clusterAlgorithm) &&
            threshold == other.threshold &&
            includeTrivialTInvariants == other.includeTrivialTInvariants;
    }
    
    @Override
    public int hashCode() {
        return distanceFunction.hashCode() ^ clusterAlgorithm.hashCode();
    }

    @Override
    public Boolean isExportable() {
        return true;
    }
}
