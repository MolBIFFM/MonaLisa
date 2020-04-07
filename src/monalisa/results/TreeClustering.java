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

import java.io.File;
import java.io.IOException;
import monalisa.Project;

/**
 *
 * @author Jens Einloft
 */
public class TreeClustering implements Result {

    private static final long serialVersionUID = -8649514526288697494L;

    @Override
    public String filenameExtension() {
        return "";
    }

    @Override
    public void export(File path, Configuration config, Project project) throws IOException {
    }

}
