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
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Calculates the closeness centrality of every node in a Petri net
 * @author Lilya Mirzoyan
 */
public class ClosenessCentrality extends CentralityAbstract {

    private static final Logger LOGGER = LogManager.getLogger(ClosenessCentrality.class);
    private final int[][] splMatrixPlaces;
    private final int[][] splMatrixTransitions;
    Collection<Place> places;
    Collection<Transition> transitions;

    /**
     * Executes the Floyd-Warshall algorithm to compute the distance matrices for
     * places and transitions
     * @param petriNet
     */
    public ClosenessCentrality(PetriNetFacade petriNet) {
       super(petriNet);

       LOGGER.info("Initializing for closeness centrality");
       places = petriNet.places();
       transitions = petriNet.transitions();

       splMatrixPlaces = new FloydWarshall(adjMatrixPlaces).getDistMatrix();
       splMatrixTransitions = new FloydWarshall(adjMatrixTransitions).getDistMatrix();
       LOGGER.info("Finished initialization for closeness centrality");
    }

    /**
     * Calculates the ranking for every place and every transition in a Petri net
     * on the basis of closeness centrality
     */
    @Override
    public void calculate() {
        LOGGER.info("Beginning calculation of closeness centrality");
        LOGGER.info("Calculating closeness centrality for places");
        for (Place p : places){
            int sum = 0;
            double closeness;
            for (int i = 0; i < splMatrixPlaces.length; i++){
                sum += splMatrixPlaces[adjMatrixPlaces.getIndexForId(p.id())][i];
            }
            if (sum == 0){
                closeness = 1.0;
            }
            else {
                closeness = 1.0 /(double) sum;
            }

            rankingPlaces.put(p.id(), closeness);
        }
        LOGGER.info("Finished calculating closeness centrality for places");
        LOGGER.info("Calculating closeness centrality for transitions");
        for (Transition t : transitions){
            int sum = 0;
            double closeness;
            for (int i = 0; i < splMatrixTransitions.length; i++){
                sum += splMatrixTransitions[adjMatrixTransitions.getIndexForId(t.id())][i];
            }
            if (sum == 0){
                closeness = 1.0;
            }
            else {
                closeness = 1.0 /(double) sum;
            }

            rankingTransitions.put(t.id(), closeness);
        }
        LOGGER.info("Finished calculating closeness centrality for transitions");
    }
  }
