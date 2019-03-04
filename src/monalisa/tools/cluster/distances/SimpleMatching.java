/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.cluster.distances;

import de.molbi.mjcl.clustering.distancemeasure.DistanceFunction;

/**
 *
 * @author stefan
 * 
 * The Simple Matching Coefficient is a statsitic used for comparing the similarity and diversity
 * of sample sets. Given two objects, A and B, each with n binary attributes, SMC
 * is defined as: SMC = Number of Matching Attributes/ Number of Attributes =
 * M00 + M11 / M00+M01+M10+M11
 * M00 represents the total number of attributes where A and B both have a value of 1
 * M01 -"- of A is 0 and B is 1
 * M10 -"- of A is 1 and B is 0
 * M11 -"- of A and B bothe have a value of 0
 */
public class SimpleMatching extends DistanceFunction {

    public final static int id = 102;
    public final static String name = "Simple Matching";
    public final static int version = 2;
    private static final long serialVersionUID = -6397892360858527021L;
    
    @Override
    public double calcDistance(double[] x, double[] y) {
        double a = 0,b = 0,c = 0,d = 0;
        for (int i = 0; i < x.length; i++) {
            if (x[i] == 0 && y[i] == 0) {
                a +=1;
            }
            if (x[i] == 0 && y[i] > 0) {
                b +=1;
            }
            if (x[i] > 0 && y[i] == 0) {
                c +=1;                
            }
            if (x[i] > 0 && y[i] > 0) {
                d +=1;
            }
        }
        double res = a+b+c+d;
        double simple = (a+d)/res;
        //value is too small to draw it in graph therefore * 1000 (0,01 = 10,0)
        return (1 - simple) * 1000;   
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
