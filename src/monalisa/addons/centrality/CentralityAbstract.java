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

import java.util.HashMap;
import java.util.Map;
import monalisa.data.pn.PetriNetFacade;

/**
 * Abstract class for calculating centralities of nodes in a Petri net
 *
 * @author Lilya Mirzoyan
 */
public abstract class CentralityAbstract implements CentralityInterface {

    protected PetriNetFacade pn;
    protected Map<Integer, Double> rankingPlaces;
    protected Map<Integer, Double> rankingTransitions;

    protected AdjacencyMatrix adjMatrixPlaces, adjMatrixTransitions;

    public CentralityAbstract(PetriNetFacade petriNet) {
        this.pn = petriNet;

        rankingPlaces = new HashMap<>();
        rankingTransitions = new HashMap<>();

        adjMatrixPlaces = new AdjacencyMatrix(pn.places(), pn.transitions(), AdjacencyMatrix.PLACES);
        adjMatrixTransitions = new AdjacencyMatrix(pn.places(), pn.transitions(), AdjacencyMatrix.TRANSITIONS);
    }

    /**
     * Calculates centralities
     */
    @Override
    public void calculate() {

    }

    /**
     * Returns the ranking for places of a certain Petri net
     *
     * @return ranking for places
     */
    @Override
    public Map<Integer, Double> getRankingForPlaces() {
        return rankingPlaces;
    }

    /**
     * Returns the ranking for transitions of a certain Petri net
     *
     * @return ranking for transitions
     */
    @Override
    public Map<Integer, Double> getRankingForTransitions() {
        return rankingTransitions;
    }

}
