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
 * Calculates the betweenness centrality of every node in a Petri net
 * @author Lilya Mirzoyan
 */
public class BetweennessCentrality extends CentralityAbstract{

    Collection<Place> places;
    Collection<Transition> transitions;
    private static final Logger LOGGER = LogManager.getLogger(BetweennessCentrality.class);
    private final FloydWarshall floydWarshallPlaces;
    private final FloydWarshall floydWarshallTransitions;
    private final NumberOfShortestPaths shortestPathsPlaces;
    private final NumberOfShortestPaths shortestPathsTransitions;

    /**
     * Executes algorithms to compute the distance matrices and shortest-paths-number
     * matrices for places and transitions
     * @param petriNet
     */
    public BetweennessCentrality(PetriNetFacade petriNet){
        super(petriNet);

        LOGGER.info("Initializing for betweenness centrality");

        places = petriNet.places();
        transitions = petriNet.transitions();
        floydWarshallPlaces = new FloydWarshall(adjMatrixPlaces);
        floydWarshallTransitions = new FloydWarshall(adjMatrixTransitions);
        shortestPathsPlaces = new NumberOfShortestPaths(adjMatrixPlaces, floydWarshallPlaces);
        shortestPathsTransitions = new NumberOfShortestPaths(adjMatrixTransitions, floydWarshallTransitions);
        LOGGER.info("Finished initialization for betweenness centrality");
    }

    /**
     * Calculates the ranking for every place and every transition in a Petri net
     * on the basis of betweenness centrality
     */
    @Override
    public void calculate(){
        LOGGER.info("Beginning calculation of betweenness centrality");
        double spp = shortestPathsPlaces.getAllShortestPaths();
        double spt = shortestPathsTransitions.getAllShortestPaths();
        double betweenness;
        double x,y;

        LOGGER.info("Calculating betweenness centrality for places");
        for (Place p : places){
            x = shortestPathsPlaces.getCounter(adjMatrixPlaces.getIndexForId(p.id()));
            if (spp == 0.0){
                betweenness = 0.0;
            }
            else{
            betweenness = x/spp;
            }

            rankingPlaces.put(p.id(), betweenness);
        }
        LOGGER.info("Finished calculating betweenness centrality for places");
        LOGGER.info("Calculating betweenness centrality for transitions");
        for (Transition t : transitions){
            y = shortestPathsTransitions.getCounter(adjMatrixTransitions.getIndexForId(t.id()));
            if (spt == 0.0){
                betweenness = 0.0;
            }
            else {
            betweenness = y/spt;
            }

            rankingTransitions.put(t.id(), betweenness);
        }
        LOGGER.info("Finished calculating betweenness centrality for transitions");
    }
 }
