/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.treeviewer;

import monalisa.results.ClusterConfiguration;
import monalisa.results.Clustering;

/**
 * A Wrapper for the class Clustering, needed for the ComboBox in the TreeViewer.
 * @author Jens Einloft
 */
public class ClusteringWrapper {

    private final Clustering clustering;
    private final ClusterConfiguration config;
    
    public ClusteringWrapper(Clustering clustering, ClusterConfiguration config) {
        this.clustering = clustering;
        this.config = config;
    }
    
    public Clustering getClustering() {
        return this.clustering;
    }
    
    public float getTreshold() {
        return this.config.getThreshold();
    }
    
    @Override
    public String toString() {
        return this.config.toString();
    }
    
}
