/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.cluster.distances;

import de.molbi.mjcl.clustering.distancemeasure.DistanceFunction;

/**
 *
 * @author stefan (b+c)/(b+c+d) oder 1-(d/(b+c+d))
 * The Tanimoto Coefficient is a statsitic used for comparing the similarity and diversity
 * of sample sets. Given two objects, A and B, each with n binary attributes, SMC
 * is defined as: SMC = Number of Matching Attributes/ Number of Attributes =
 * (M01 + M10) / (M01+M10+M11)
 * M00 represents the total number of attributes where A and B both have a value of 1
 * M01 -"- of A is 0 and B is 1
 * M10 -"- of A is 1 and B is 0
 * M11 -"- of A and B bothe have a value of 0
 */

public class TanimotoDistance extends DistanceFunction {

    public final static int id = 101;
    public final static String name = "Tanimoto Distance";
    public final static int version = 1;
    private static final long serialVersionUID = 3510602176831570214L;

    @Override
    public double calcDistance(double[] x, double[] y) {
        double a = 0, b = 0, c = 0, d = 0;
        for (int i = 0; i < x.length; i++) {
            if (x[i] == 0 && y[i] == 0) {
                a += 1;
            }
            if (x[i] == 0 && y[i] > 0) {
                b += 1;
            }
            if (x[i] > 0 && y[i] == 0) {
                c += 1;
            }
            if (x[i] > 0 && y[i] > 0) {
                d += 1;
            }
        }

        //value is too small to draw it in graph therefore * 1000 (0,01 = 10,0)
        double tani = ((b + c) / (b + c + d)) * 1000;
        return tani;
    }

    @Override
    public double getFurthestDistance() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public Integer getID() {
        return id;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public String getName() {
        return name;
    }

}
