/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.gillespie;

import java.util.Set;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import monalisa.util.HighQualityRandom;

/**
 *
 * @author Marcel
 */
public class SimulationStorage {

    private final Set<Integer>[] reactionsConstantEducts;
    private final Set<Integer>[] reactionsNonConstantEducts;
    private final int[][] eductStoichMatrix;
    private final Set<Integer>[] compoundsInfluence;
    private final double volMol;
    private final MathematicalExpression[] constantPlacesExp;
    private double maxSimTime;
    private final String[] constantPlaceNames;
    private final Set<Integer> constantPlacesPostTransitions;
    private final MathematicalExpression[] reactionRateConstants;
    private final String[] nonConstantPlaceNames;
    private final int[] nonConstantPlaceIDs;
    private double updateInterval;
    private final long[] nonConstantInitialMarking;
    private final int[] reactionIDs;
    private final String[] reactionNames;
    private final int[][] productStoichMatrix;
    private final int[][] reactionOrder;
    private final int[] constantPlaceIDs;
    private final Set<Integer>[] reactionsNonConstantProducts;

    public SimulationStorage(
            double volMol,
            double maxSimTime,
            double updateInterval,
            int[] nonConstantPlaceIDs,
            int[] constantPlaceIDs,
            MathematicalExpression[] constantPlacesExp,
            MathematicalExpression[] reactionRateConstants,
            long[] nonConstantInitialMarking,
            String[] nonConstantPlaceNames,
            String[] constantPlaceNames,
            int[] reactionIDs,
            Set<Integer> constantPlacesPostTransitions,
            Set<Integer>[] reactionsNonConstantEducts,
            int[][] eductStoichMatrix,
            Set<Integer>[] compoundsInfluence,
            Set<Integer>[] reactionsNonConstantProducts,
            int[][] productStoichMatrix,
            int[][] reactionOrder,
            Set<Integer>[] reactionsConstantEducts,
            String[] reactionNames) {
        this.volMol = volMol;
        this.maxSimTime = maxSimTime;
        this.updateInterval = updateInterval;

        this.nonConstantPlaceIDs = nonConstantPlaceIDs;
        this.nonConstantPlaceNames = nonConstantPlaceNames;
        this.constantPlaceNames = constantPlaceNames;
        this.constantPlaceIDs = constantPlaceIDs;
        this.constantPlacesExp = constantPlacesExp;
        this.constantPlacesPostTransitions = constantPlacesPostTransitions;
        this.reactionIDs = reactionIDs;
        this.reactionNames = reactionNames;
        this.reactionRateConstants = reactionRateConstants;
        this.reactionOrder = reactionOrder;
        this.reactionsConstantEducts = reactionsConstantEducts;
        this.reactionsNonConstantEducts = reactionsNonConstantEducts;
        this.reactionsNonConstantProducts = reactionsNonConstantProducts;
        this.nonConstantInitialMarking = nonConstantInitialMarking;
        this.eductStoichMatrix = eductStoichMatrix;
        this.productStoichMatrix = productStoichMatrix;
        this.compoundsInfluence = compoundsInfluence;
    }

    /**
     * @return the reactionsConstantEducts
     */
    public Set<Integer>[] getReactionsConstantEducts() {
        return reactionsConstantEducts;
    }

    /**
     * @return the reactionsNonConstantEducts
     */
    public Set<Integer>[] getReactionsNonConstantEducts() {
        return reactionsNonConstantEducts;
    }

    /**
     * @return the eductStoichMatrix
     */
    public int[][] getEductStoichMatrix() {
        return eductStoichMatrix;
    }

    /**
     * @return the compoundsInfluence
     */
    public Set<Integer>[] getCompoundsInfluence() {
        return compoundsInfluence;
    }

    /**
     * @return the volMol
     */
    public double getVolMol() {
        return volMol;
    }

    /**
     * @return the constantPlacesExp
     */
    public MathematicalExpression[] getConstantPlacesExp() {
        return constantPlacesExp;
    }

    /**
     * @return the maxSimTime
     */
    public double getMaxSimTime() {
        return maxSimTime;
    }

    /**
     * @return the constantPlaceNames
     */
    public String[] getConstantPlaceNames() {
        return constantPlaceNames;
    }

    /**
     * @return the constantPlacesPostTransitions
     */
    public Set<Integer> getConstantPlacesPostTransitions() {
        return constantPlacesPostTransitions;
    }

    /**
     * @return the reactionRateConstants
     */
    public MathematicalExpression[] getReactionRateConstants() {
        return reactionRateConstants;
    }

    /**
     * @return the nonConstantPlaceNames
     */
    public String[] getNonConstantPlaceNames() {
        return nonConstantPlaceNames;
    }

    /**
     * @return the nonConstantPlaceIDs
     */
    public int[] getNonConstantPlaceIDs() {
        return nonConstantPlaceIDs;
    }

    /**
     * @return the updateInterval
     */
    public double getUpdateInterval() {
        return updateInterval;
    }

    /**
     * @return the nonConstantInitialMarking
     */
    public long[] getNonConstantInitialMarking() {
        return nonConstantInitialMarking;
    }

    /**
     * @return the reactionIDs
     */
    public int[] getReactionIDs() {
        return reactionIDs;
    }

    /**
     * @return the reactionNames
     */
    public String[] getReactionNames() {
        return reactionNames;
    }

    /**
     * @return the productStoichMatrix
     */
    public int[][] getProductStoichMatrix() {
        return productStoichMatrix;
    }

    /**
     * @return the reactionOrder
     */
    public int[][] getReactionOrder() {
        return reactionOrder;
    }

    /**
     * @return the constantPlaceIDs
     */
    public int[] getConstantPlaceIDs() {
        return constantPlaceIDs;
    }

    /**
     * @return the reactionsNonConstantProducts
     */
    public Set<Integer>[] getReactionsNonConstantProducts() {
        return reactionsNonConstantProducts;
    }
}
