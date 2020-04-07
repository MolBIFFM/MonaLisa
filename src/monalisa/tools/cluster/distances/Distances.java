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
import java.util.HashMap;
import java.util.Map;

/**
 * A List of all provided Distance Functions.
 *
 * @author Jens Einloft
 */
public class Distances {

    public static final Map<String, Class<? extends DistanceFunction>> distances = new HashMap<>();

    public static final String TANIMOTO = "Tanimoto";
    public static final String SUM_OF_DIFFERENCES = "Sum of differences";
    public static final String SIMPLEMATCHING = "Simple matching";

    static {
        distances.put(TANIMOTO, TanimotoDistance.class);
        distances.put(SUM_OF_DIFFERENCES, SumOfDifferences.class);
        distances.put(SIMPLEMATCHING, SimpleMatching.class);
    }

}
