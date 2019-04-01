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

import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Stefan Marchi
 */
public class TreeViewerEdge {
    private static final long serialVersionUID = 8440480344225497981L;

    private final int id;
    private final String edgeType;
    private static final Logger LOGGER = LogManager.getLogger(TreeViewerEdge.class);

    public TreeViewerEdge(int id, String edgeType) {
        this.id = id;
        this.edgeType = edgeType;
        LOGGER.debug("Created new edge with id '" + String.valueOf(id) + "'");
    }

    public String getEdgeType() {
        return this.edgeType;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TreeViewerEdge other = (TreeViewerEdge) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }


}
