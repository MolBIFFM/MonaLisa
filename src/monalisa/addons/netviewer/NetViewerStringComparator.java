/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer;

import java.util.Comparator;

/**
 * Sorts a List of NetViewerNodes by her LabelNames
 * @author Jens Einloft
 */
public class NetViewerStringComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
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
        return n1 - n2;
    }
}
