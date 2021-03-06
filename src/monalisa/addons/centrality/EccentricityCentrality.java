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
 * Calculates the eccentricity centrality of every node in a Petri net
 *
 * @author Lilya Mirzoyan
 */
public class EccentricityCentrality extends CentralityAbstract {

    private static final Logger LOGGER = LogManager.getLogger(EccentricityCentrality.class);
    private final int[][] splMatrixPlaces;
    private final int[][] splMatrixTransitions;
    Collection<Place> places;
    Collection<Transition> transitions;

    /**
     * Executes the Floyd-Warshall algorithm to compute the distance matrices
     * for places and transitions
     *
     * @param petriNet
     */
    public EccentricityCentrality(PetriNetFacade petriNet) {
        super(petriNet);
        LOGGER.info("Initializing for eccentricity centrality");
        places = petriNet.places();
        transitions = petriNet.transitions();

        splMatrixPlaces = new FloydWarshall(adjMatrixPlaces).getDistMatrix();
        splMatrixTransitions = new FloydWarshall(adjMatrixTransitions).getDistMatrix();
        LOGGER.info("Finished initialization for eccentricity centrality)");
    }

    /**
     * Calculates the ranking for every place and every transition in a Petri
     * net on the basis of eccentricity centrality
     */
    @Override
    public void calculate() {
        LOGGER.info("Beginning calculation of eccentricity centrality");
        LOGGER.debug("Calculating eccentricity centrality for places");
        for (Place p : places) {
            int max = 0;
            double eccentricity;
            for (int i = 0; i < splMatrixPlaces.length; i++) {
                if (splMatrixPlaces[adjMatrixPlaces.getIndexForId(p.id())][i] > max) {
                    max = splMatrixPlaces[adjMatrixPlaces.getIndexForId(p.id())][i];
                }
            }
            if (max == 0) {
                eccentricity = 1.0;
            } else {
                eccentricity = 1 / (double) max;
            }
            rankingPlaces.put(p.id(), eccentricity);
        }
        LOGGER.debug("Finished calculating eccentricity centrality for places");
        LOGGER.debug("Calculating eccentricity centrality for transitions");
        for (Transition t : transitions) {
            int max = 0;
            double eccentricity;
            for (int i = 0; i < splMatrixTransitions.length; i++) {

                if (splMatrixTransitions[adjMatrixTransitions.getIndexForId(t.id())][i] > max) {
                    max = splMatrixTransitions[adjMatrixTransitions.getIndexForId(t.id())][i];
                }
            }
            if (max == 0) {
                eccentricity = 1.0;
            } else {
                eccentricity = 1 / (double) max;
            }
            rankingTransitions.put(t.id(), eccentricity);
        }
        LOGGER.info("Finished calculating eccentricity centrality for transitions");
    }
}
