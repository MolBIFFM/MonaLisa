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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Wrapper for the class Clustering, needed for the ComboBox in the TreeViewer.
 * @author Jens Einloft
 */
public class ClusteringWrapper {

    private final Clustering clustering;
    private final ClusterConfiguration config;
    private static final Logger LOGGER = LogManager.getLogger(ClusteringWrapper.class);

    public ClusteringWrapper(Clustering clustering, ClusterConfiguration config) {
        LOGGER.debug("New clustering wrapper with config '" + config.toString() + "'");
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
