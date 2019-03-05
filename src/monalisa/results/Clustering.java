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

import de.molbi.mjcl.clustering.ds.ClusterTree;
import java.io.File;
import java.io.IOException;

import monalisa.Project;

public final class Clustering implements Result {
    private static final long serialVersionUID = -995041259896104069L;
    
    private final ClusterTree tree;

    public Clustering(ClusterTree tree) {
        this.tree = tree;
    }
        
    public ClusterTree getClusterTree() {
        return this.tree;
    }
    
    @Override
    public void export(File path, Configuration config, Project project) throws IOException {
        // TODO
    }

    @Override
    public String filenameExtension() {
        return "txt";
    }

}
