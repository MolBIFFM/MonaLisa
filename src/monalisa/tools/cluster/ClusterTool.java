/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.cluster;

import de.molbi.mjcl.clustering.distancemeasure.DistanceFunction;
import de.molbi.mjcl.clustering.ds.Cluster;
import de.molbi.mjcl.clustering.ds.ClusterTree;
import de.molbi.mjcl.clustering.ds.Leaf;
import de.molbi.mjcl.clustering.ds.Properties;
import de.molbi.mjcl.clustering.hcl.AverageLinkageSettings;
import de.molbi.mjcl.clustering.hcl.SingleLinkageSettings;
import de.molbi.mjcl.clustering.hcl.CompleteLinkageSettings;
import de.molbi.mjcl.clustering.hcl.HierarchicSettings;
import de.molbi.mjcl.clustering.hcl.HierarchicalClustering;

import monalisa.data.pn.TInvariant;
import monalisa.results.ClusterConfiguration;
import monalisa.results.Clustering;
import monalisa.results.Configuration;
import monalisa.results.TInvariants;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.AbstractTool;
import monalisa.tools.ErrorLog;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.Project;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class ClusterTool extends AbstractTool {

    private static final Logger LOGGER = LogManager.getLogger(ClusterTool.class);

    @Override
    public void run(Project project, ErrorLog log, Configuration config) {
        LOGGER.info("Running ClusterTool");
        ClusterConfiguration clusterConfig = (ClusterConfiguration) config;
        // Config
        float percent = clusterConfig.getThreshold();
        String clusterAlgorithm = clusterConfig.getClusterAlgorithm();
        boolean includeTrivialTInvariants = clusterConfig.isIncludeTrivialTInvariants();

        // Building the input for the cluster library
        TInvariants tinvs = project.getToolManager().getResult(TInvariantTool.class, new TInvariantsConfiguration());
        int nbrOfTransitions = project.getPNFacade().transitions().size();
        int nbrOfTInvs = 0;
        // Determine the number of TInvariants
        for (TInvariant tinv : tinvs) {
            if ((tinv.isTrivial() && includeTrivialTInvariants) || !tinv.isTrivial()) {
                nbrOfTInvs++;
            }
        }
        double[][] data = new double[nbrOfTInvs][nbrOfTransitions];
        int[] iDs = new int[nbrOfTInvs];
        double[] tmp;
        int j, i = 0;
        // Fill the array
        for (TInvariant tinv : tinvs) {
            iDs[i] = tinv.id();
            if ((tinv.isTrivial() && includeTrivialTInvariants) || !tinv.isTrivial()) {
                tmp = new double[nbrOfTransitions];
                j = 0;
                for (Transition t : project.getPNFacade().transitions()) {
                    tmp[j] = tinv.factor(t);
                    j++;
                }
                data[i] = tmp;
                i++;
            }
        }

        // Create a Setting for the Cluster Library
        DistanceFunction distance = clusterConfig.getNewDistanceFunction();

        // Create a Setting for the Cluster Library
        HierarchicSettings hs = null;
        // Which Algorithm should be used?
        if (clusterAlgorithm.equalsIgnoreCase(ClusterFunctions.UPGMA)) {
            hs = new AverageLinkageSettings(distance, false, true, null, null);
        } else if (clusterAlgorithm.equalsIgnoreCase(ClusterFunctions.WPGMA)) {
            hs = new AverageLinkageSettings(distance, true, true, null, null);
        } else if (clusterAlgorithm.equalsIgnoreCase(ClusterFunctions.SingleLinkage)) {
            hs = new SingleLinkageSettings(distance, true, null, null);
        } else if (clusterAlgorithm.equalsIgnoreCase(ClusterFunctions.CompleteLinkage)) {
            hs = new CompleteLinkageSettings(distance, true, null, null);
        }

        // Start the clustering
        HierarchicalClustering hc = new HierarchicalClustering(data, iDs, hs);
        ClusterTree<ClusterTreeNodeProperties, Properties> tree = hc.execute();

        for (Cluster c : tree.getCluster()) {
            if (c instanceof Leaf) {
                tree.addNodeProperty(c, new ClusterTreeNodeProperties(tinvs.getTInvariant(c.getID())));
            } else {
                TInvariant[] t = new TInvariant[c.getLeavesCount()];
                i = 0;
                for (Leaf l : c.getLeaves()) {
                    t[i] = tinvs.getTInvariant(l.getID());
                    i++;
                }
                tree.addNodeProperty(c, new ClusterTreeNodeProperties(t));
            }
        }
        LOGGER.info("Successfully ran ClusterTool, adding result");
        // Save the result
        addResult(clusterConfig, new Clustering(tree));
        LOGGER.info("Successfully added result");
    }

    @Override
    public void saveSettings(Project p) {
        // TODO Auto-generated method stub
    }
}
