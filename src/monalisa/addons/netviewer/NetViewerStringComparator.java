/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer;

import java.util.Comparator;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Sorts a List of NetViewerNodes by their LabelNames
 * @author Jens Einloft
 */
public class NetViewerStringComparator implements Comparator {

    private static final Logger LOGGER = LogManager.getLogger(NetViewerStringComparator.class);

    @Override
    public int compare(Object o1, Object o2) {
        LOGGER.debug("Sorting list of NetViewerNodes by LabelNames");
        String s1 = ((NetViewerNode) o1).getName();
        String s2 = ((NetViewerNode)o2).getName();
        int n1 = s1.length();
        int n2 = s2.length();
        int min = Math.min(n1, n2);

        for (int i = 0; i < min; i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 != c2) {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
                if (c1 != c2) {
                    c1 = Character.toLowerCase(c1);
                    c2 = Character.toLowerCase(c2);
                    if (c1 != c2) {
                        // No overflow because of numeric promotion
                        return c1 - c2;
                    }
                }
            }
        }
        LOGGER.debug("Successfully sorted list of NetViewerNodes by LabelNames");
        return n1 - n2;
    }
}
