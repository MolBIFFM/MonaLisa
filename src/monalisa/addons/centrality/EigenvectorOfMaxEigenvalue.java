/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.centrality;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class computes the eigenvector of the maximal eigenvalue of a given
 * adjacency matrix by using the method of power iteration, established by von
 * Mises
 *
 * @author Lilya Mirzoyan
 */
public class EigenvectorOfMaxEigenvalue {

    private final static Logger LOGGER = LogManager.getLogger(EigenvectorOfMaxEigenvalue.class);
    private final AdjacencyMatrix adjMatrix;
    private final int[][] corMatrix;
    private final double[] startVector;
    private double[] newVector;
    private double[] resultingVector;
    private double[] subtractedVector;
    private final List<double[]> vectorList;
    private final int maxNumOfIt;

    /**
     * Initializes the start vector and the correlation matrix for the power
     * iteration
     *
     * @param adjMatrix
     */
    public EigenvectorOfMaxEigenvalue(AdjacencyMatrix adjMatrix) {
        LOGGER.info("Initializing start vector for eigenvector centrality");
        this.adjMatrix = adjMatrix;
        int x = adjMatrix.getLength();
        startVector = new double[x];
        maxNumOfIt = 1000;   // maximal number of iterations

        for (int i = 0; i < x; i++) {
            startVector[i] = 1.0;
        }
        corMatrix = adjMatrix.getMatrix();
        vectorList = new ArrayList<>();
        vectorList.add(startVector);
        LOGGER.info("Finished initializing start vector for eigenvector centrality");
    }

    /**
     * Returns the resulting vector by multiplying the correlation matrix with
     * the suitable vector and normalizing the result iteratively, until a
     * convergence criterion is reached. If convergence is not ensured, then all
     * values of the vector are set to -1, because in this case the eigenvector
     * corresponding to the largest eigenvalue is not computable
     *
     * @return resulting vector
     */
    public double[] getVector() {
        LOGGER.info("Computing eigenvector normalized to largest eigenvalue");
        int count = 0;
        double tolerance = 0.05;
        int y = startVector.length;
        for (int i = 1; i < maxNumOfIt; i++) {
            count++;    // counts the number of iterations
            resultingVector = matrixVectorProduct(vectorList.get(i - 1));
            double size = getLengthOfVector(resultingVector);
            // normalization of the vector
            for (int j = 0; j < y; j++) {
                if (size == 0) {
                    resultingVector[j] = 0;
                } else {
                    resultingVector[j] = resultingVector[j] / size;
                }
            }
            vectorList.add(resultingVector);
            if (!vectorList.isEmpty()) {

                double[] last = vectorList.get(vectorList.size() - 1);
                double[] penultimate = vectorList.get(vectorList.size() - 2);
                double[] subtractionResult = vectorSubtraction(penultimate, last);

                // stop, if the convergence criterion is reached
                if (getLengthOfVector(subtractionResult) <= tolerance) {
                    count = Integer.MAX_VALUE;
                    LOGGER.info("Finished computation of eigenvector normalized to largest eigenvalue");
                    break;
                }

            }

        }
        // if the convergence criterion cannot be reached, the vector is assigned
        // a default value
        if (count < Integer.MAX_VALUE) {
            LOGGER.warn("Convergence could not be reached, setting vector to -1");
            for (int k = 0; k < y; k++) {
                resultingVector[k] = -1;
            }
        }
        return resultingVector;
    }

    /**
     * Computes the length of a vector according to the second vector norm
     *
     * @param vector
     * @return length of a given vector
     */
    private double getLengthOfVector(double[] vector) {
        LOGGER.debug("Computing length of vector");
        int n = startVector.length;
        double vectorSize = 0.0;
        for (int i = 0; i < n; i++) {
            vectorSize += Math.pow(vector[i], 2.0);
        }
        LOGGER.debug("Successfully computed length of vector");
        return Math.sqrt(vectorSize);
    }

    /**
     * Returns the result of a matrix-vector multiplication
     *
     * @param vector
     * @return matrix-vector product
     */
    private double[] matrixVectorProduct(double[] vector) {
        LOGGER.debug("Computing matrix-vector product");
        int x = adjMatrix.getLength();
        int y = vector.length;
        newVector = new double[y];
        for (int i = 0; i < y; i++) {
            newVector[i] = 0.0;
            for (int j = 0; j < x; j++) {
                newVector[i] += corMatrix[i][j] * vector[j];
            }
        }
        LOGGER.debug("Successfully computed matrix-vector product");
        return newVector;
    }

    /**
     * Performs subtraction of two vectors
     *
     * @param penultimateVector
     * @param lastVector
     * @return vector as a result of subtraction
     */
    private double[] vectorSubtraction(double[] penultimateVector, double[] lastVector) {
        LOGGER.debug("Computing vector subtraction");
        int y = lastVector.length;
        subtractedVector = new double[y];

        for (int j = 0; j < y; j++) {
            subtractedVector[j] = lastVector[j] - penultimateVector[j];
        }
        LOGGER.debug("Successfully computed vector subtraction");
        return subtractedVector;
    }

}
