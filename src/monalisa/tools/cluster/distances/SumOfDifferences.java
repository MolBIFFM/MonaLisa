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
 * @author stefan
 *
 *
 */
public class SumOfDifferences extends DistanceFunction {

    public final static int id = 100;
    public final static String name = "Sum of Differences";
    public final static int version = 1;
    private static final long serialVersionUID = -7688831744304783824L;

    @Override
    public double calcDistance(double[] x, double[] y) {
        double sad = 0;

        for (int i = 0; i < x.length; i++) {
            sad += Math.abs(x[i] - y[i]);
        }

        //value is too small to draw it in graph therefore * 10 (0,1 = 1,0)
        return sad * 10;
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
