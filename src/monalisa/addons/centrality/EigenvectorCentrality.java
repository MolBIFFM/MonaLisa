/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.centrality;

import java.util.Collection;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;

/**
 * This class computes the eigenvector centrality for every node in a Petri net
 * @author Lilya Mirzoyan
 */
public class EigenvectorCentrality extends CentralityAbstract{
    Collection<Place> places;
    Collection<Transition> transitions;
    
    private final EigenvectorOfMaxEigenvalue vectorForPlaces;
    private final EigenvectorOfMaxEigenvalue vectorForTransitions;
    private final AdjacencyMatrix adjMatrixPlaces;
    private final AdjacencyMatrix adjMatrixTransitions;
    
    /**
     * Calls some methods to compute the adjacency matrix for places and transitions
     * in a Petri net and the eigenvector of the maximal eigenvalue of this matrix
     * @param petriNet 
     */
    public EigenvectorCentrality(PetriNetFacade petriNet){
        super(petriNet);
       
        places = petriNet.places();
        transitions = petriNet.transitions();
        adjMatrixPlaces = new AdjacencyMatrix(places, transitions, AdjacencyMatrix.PLACES);
        adjMatrixTransitions = new AdjacencyMatrix(places, transitions, AdjacencyMatrix.TRANSITIONS);
        vectorForPlaces = new EigenvectorOfMaxEigenvalue(adjMatrixPlaces);
        vectorForTransitions = new EigenvectorOfMaxEigenvalue(adjMatrixTransitions);
    }
    
    /**
     * Calculates the ranking for every place and every transition in a Petri net
     * on the basis of eigenvector centrality
     */
    @Override
    public void calculate(){
        double eigenvector;
        double[] placeVector = vectorForPlaces.getVector();
        double[] transitionVector = vectorForTransitions.getVector();
         for (Place p : places){
             eigenvector = placeVector[adjMatrixPlaces.getIndexForId(p.id())];
             rankingPlaces.put(p.id(), eigenvector);
         }
         for (Transition t : transitions){
             eigenvector = transitionVector[adjMatrixTransitions.getIndexForId(t.id())];
             rankingTransitions.put(t.id(), eigenvector);
         }
    }
}
