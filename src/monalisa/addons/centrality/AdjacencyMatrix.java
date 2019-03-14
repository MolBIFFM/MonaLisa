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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to compute respectively the adjacency matrix of transitions and places for a certain Petri net
 * @author Lilya Mirzoyan
 */
public class AdjacencyMatrix {

    public static final Logger LOGGER =  LogManager.getLogger(AdjacencyMatrix.class);
    public static final String PLACES = "PLACES";
    public static final String TRANSITIONS = "TRANSITIONS";

    private int[][] adjMatrix;
    private final int length;
    private final Map<Integer, Integer> translationMap;
    private final Map<Integer, Integer> reverseTranlationMap;


    /**
     * Looks if a node is a place or a transition and yields the adjacency matrix for both. If two places are connected by a transition, then they
     * are adjacent. Otherwise, they are not. The same applies to the transitions
     * @param places
     * @param transitions
     * @param whichOne
     */
    public AdjacencyMatrix(Collection<Place> places, Collection<Transition> transitions, String whichOne){
        LOGGER.info("Creating new adjacency matrix");
        translationMap = new HashMap<>();
        reverseTranlationMap = new HashMap<>();

        int counter = 0;
        if (whichOne.equals(PLACES)){
            LOGGER.info("Adjacency matrix based on places");
            this.length = places.size();
            initMatrix(places.size());
            LOGGER.info("Initializing translations maps");
            for(Place p : places) {
                translationMap.put(p.id(), counter);    // the nodes are numbered
                reverseTranlationMap.put(counter, p.id());  // stores the ID of a node for each value
                counter++;
            }
            LOGGER.info("Filling matrix based on transitions");
            for (Transition t : transitions){
                for (Place p1 : t.inputs()){
                    for (Place p2 : t.outputs()){
                        if (!p1.equals(p2)){    // prevents self-adjacency, if a place is connected with itself via a transition
                        adjMatrix[translationMap.get(p1.id())][translationMap.get(p2.id())] = 1;
                        }
                    }
                }
            }
        } else {
            LOGGER.info("Adjacency matrix based on transitions");
            initMatrix(transitions.size());
            this.length = transitions.size();
            LOGGER.info("Initializing translation maps");
            for(Transition t : transitions) {
                translationMap.put(t.id(), counter);
                reverseTranlationMap.put(counter, t.id());
                counter++;
            }
            for (Place p : places){
                for (Transition t1 : p.inputs()){
                    for (Transition t2 : p.outputs()){
                        if (!t1.equals(t2)){
                        adjMatrix[translationMap.get(t1.id())][translationMap.get(t2.id())] = 1;
                        }
                    }
                }
            }
        }
    }


    /**
     * Initializes the adjacency matrix by filling it with zeros
     * @param size
     */
    private void initMatrix(int size){
        LOGGER.info("Initializing adjacency matrix with zeros");
        adjMatrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++){
                adjMatrix[i][j] = 0;
            }
        }
        LOGGER.info("Successfully initialized adjacency matrix with zeros");
    }

    /**
     * Yields the desired entry of the adjacency matrix
     * @param x
     * @param y
     * @return adjMatrix
     */
    public int getEntry(Integer x, Integer y){
        LOGGER.info("Getting entry for position " + x.toString() + "," + y.toString());
        return adjMatrix[translationMap.get(x)][translationMap.get(y)];
    }

    /**
     * Allows to change entries in the adjacency matrix
     * @param x
     * @param y
     * @param value
     */
    public void setEntry(Integer x, Integer y, int value) {
        LOGGER.info("Setting entry for position " + x.toString() + "," + y.toString() + " to " + value);
        adjMatrix[translationMap.get(x)][translationMap.get(y)] = value;
    }

    /**
     * Returns the ID of a node for a certain index
     * @param index
     * @return ID of a node for a given index
     */
    public int getIdForIndex(int index) {
        return reverseTranlationMap.get(index);
    }

    /**
     * Returns the index of a node for a certain ID
     * @param id
     * @return index of a node corresponding to a given ID
     */
    public int getIndexForId(int id){
        return translationMap.get(id);
    }

    /**
     * Returns the length of the adjacency matrix
     * @return number of nodes in the adjacency matrix
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Returns the computed adjacency matrix
     * @return adjacency matrix
     */
    public int[][] getMatrix() {

        return this.adjMatrix;
    }
}
