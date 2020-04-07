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

import de.molbi.mjcl.clustering.distancemeasure.DistanceFunction;
import monalisa.resources.StringResources;
import monalisa.tools.cluster.ClusterFunctions;
import monalisa.tools.cluster.distances.SimpleMatching;
import monalisa.tools.cluster.distances.SumOfDifferences;
import monalisa.tools.cluster.distances.TanimotoDistance;

public final class ClusterConfiguration implements Configuration {
    private static final long serialVersionUID = 6553322655138235537L;
    
    private final Class <? extends DistanceFunction> distanceFunction;
    private final String clusterAlgorithm;
    private final float threshold;
    private final boolean includeTrivialTInvariants;

    public ClusterConfiguration(Class <? extends DistanceFunction> distanceFunction, String clusterAlgorithm, float threshold, boolean includeTrivialTInvariants) {
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
        String cluster = ClusterFunctions.getName(getClusterAlgorithm());
        String with = strings.get(isIncludeTrivialTInvariants() ? "With" : "Without");
        String percent = String.format("%.0f", threshold);
        return strings.get("ClusterTable", cluster, getDistanceFunction(), percent, with, "");
    }
    
    @Override
    public String toString() {
        return String.format("%s with %s (%d%%) %s", getClusterAlgorithm(), getDistanceFunction(), (int)threshold,
            (isIncludeTrivialTInvariants() ? "with" : "without") +  " trivial EM");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ClusterConfiguration))
            return false;
        ClusterConfiguration other = (ClusterConfiguration) obj;
        return getDistanceFunction().equals(other.getDistanceFunction()) &&
            getClusterAlgorithm().equals(other.getClusterAlgorithm()) &&
            threshold == other.threshold &&
            isIncludeTrivialTInvariants() == other.isIncludeTrivialTInvariants();
    }
    
    @Override
    public int hashCode() {
        return getDistanceFunction().hashCode() ^ getClusterAlgorithm().hashCode();
    }

    @Override
    public Boolean isExportable() {
        return true;
    }

    /**
     * @return the clusterAlgorithm
     */
    public String getClusterAlgorithm() {
        return clusterAlgorithm;
    }

    /**
     * @return whether to includeTrivialTInvariants
     */
    public boolean isIncludeTrivialTInvariants() {
        return includeTrivialTInvariants;
    }

    public String getDistanceFunction() {
        return distanceFunction.getSimpleName();
    }

    public DistanceFunction getNewDistanceFunction(){
        if (distanceFunction.equals(SimpleMatching.class)) {
            return new SimpleMatching();
        } else if (distanceFunction.equals(SumOfDifferences.class)) {
            return new SumOfDifferences();
        } else if (distanceFunction.equals(TanimotoDistance.class)){
            return new TanimotoDistance();
        } else {
            return null;
        }
    }
}
