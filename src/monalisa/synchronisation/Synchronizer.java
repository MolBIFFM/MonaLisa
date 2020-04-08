/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.synchronisation;

import java.util.Set;
import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class to synchronize NetViewer and PetriNet class. This class synchronize
 * the structure and properties of the PetriNet and the Graph used by the
 * NetViewer. Changes will always take part in the graph and needs applied to
 * the PetriNet
 *
 * @author Jens Einloft
 */
public class Synchronizer {

    private final PetriNet pn;
    // Have been moved, but need to be serialized: latestVertexID, latestEdgeID, transitionsMap, placeMap, g, map

    private static final Logger LOGGER = LogManager.getLogger(Synchronizer.class);

    /**
     * Create a new Synchronizer for a given PetriNet
     *
     * @param pn The PetriNet to synchronize
     */
    public Synchronizer(PetriNet pn) {
        LOGGER.info("Initializing new synchronizer.");
        this.pn = pn;
        LOGGER.info("Successfully initialized new synchronizer.");
    }

    /**
     * Return the PetriNet which is synchronized by the instance of the
     * Synchronizer
     *
     * @return The PetriNet which is synchronized
     */
    public PetriNet getPetriNet() {
        return this.pn;
    }

    /**
     * Returns the corresponding Place for an id.
     *
     * @param id
     * @return The corresponding Place or NULL, if no place with that id exists.
     */
    public Place getPlaceFromId(int id) {
        return pn.findPlace(id);
    }

    /**
     * Returns the corresponding Transition for an id.
     *
     * @param id
     * @return The corresponding Transition or NULL, if no place with that id
     * exists.
     */
    public Transition getTransitionFromId(int id) {
        return pn.findTransition(id);
    }

    /**
     * Returns the arc from Place from to Transition to
     *
     * @param from
     * @param to
     * @return the arc from Place from to Transition to, if it exists.
     */
    public Arc getArc(Place from, Transition to) {
        return pn.getArc(from, to);
    }

    /**
     * Returns the arc from Transition from to Place to
     *
     * @param from
     * @param to
     * @return the arc from Transition from to Place to, if it exists.
     */
    public Arc getArc(Transition from, Place to) {
        return pn.getArc(from, to);
    }

    // START: Section for manipulating the Net
    /**
     * Creates a new place and adds it to the Petri net.
     *
     * @param id
     * @param name
     * @param x x coordinate in layout
     * @param y y coordinate in layout
     * @return the newly created place
     */
    public Place addPlace(int id, String name, double x, double y) {
        Place place = new Place(id);
        place.putProperty("name", name);
        place.putProperty("posX", x);
        place.putProperty("posY", y);
        pn.addPlace(place);
        LOGGER.info("Added place " + place.getProperty("name") + " to Petri net");
        return place;
    }

    /**
     * Removes a place p from the Petri net.
     *
     * @param p the place to remove
     */
    public void removePlace(Place p) {
        pn.removePlace(p);
        LOGGER.info("Removed place " + p.getProperty("name") + " from Petri net");
    }

    /**
     * Creates a new transition and adds it to the Petri net.
     *
     * @param id
     * @param name
     * @param x x coordinate in layout
     * @param y y coordinate in layout
     * @return the newly created transition
     */
    public Transition addTransition(int id, String name, double x, double y) {
        Transition transition = new Transition(id);
        transition.putProperty("name", name);
        transition.putProperty("posX", x);
        transition.putProperty("posY", y);
        pn.addTransition(transition);
        LOGGER.info("Added transition " + transition.getProperty("name") + " to Petri net");
        return transition;
    }

    /**
     * Removes a transition t from the Petri net.
     *
     * @param t the transition to remove
     */
    public void removeTransition(Transition t) {
        pn.removeTransition(t);
        LOGGER.info("Removed transition " + t.getProperty("name") + " from Petri net");
    }

    /**
     * Adds an arc from Place from to Transition to to the Petri net.
     *
     * @param from Source Place
     * @param to Target Transition
     * @param weight Arc weight
     * @return the newly created arc
     */
    public Arc addArc(Place from, Transition to, int weight) {
        Arc arc = new Arc(from, to, weight);
        pn.addArc(from, to, arc);
        LOGGER.info("Added edge from place " + from.getProperty("name") + " to transition " + to.getProperty("name") + ".");
        return arc;
    }

    /**
     * Adds an arc from Transition from to Place to to the Petri net.
     *
     * @param from Source Transition
     * @param to Target Place
     * @param weight Arc weight
     * @return the newly created arc
     */
    public Arc addArc(Transition from, Place to, int weight) {
        Arc arc = new Arc(from, to, weight);
        pn.addArc(from, to, arc);
        LOGGER.info("Added edge from transition " + from.getProperty("name") + " to place " + to.getProperty("name") + ".");
        return arc;
    }

    /**
     * Removes an arc from Place from to Transition to to the Petri net, if it
     * exists.
     *
     * @param from Source Place
     * @param to Target Transition
     */
    public void removeArc(Place from, Transition to) {
        if (pn.getArc(from, to) != null) {
            this.pn.removeArc(from, to);
            LOGGER.info("Removed edge from place " + from.getProperty("name") + " to transition " + to.getProperty("name") + ".");
        }
    }

    /**
     * Removes an arc from Transition from to Place to to the Petri net, if it
     * exists.
     *
     * @param from Source Transition
     * @param to Target Place
     */
    public void removeArc(Transition from, Place to) {
        if (pn.getArc(from, to) != null) {
            this.pn.removeArc(from, to);
            LOGGER.info("Removed edge from transition " + from.getProperty("name") + " to place " + to.getProperty("name") + ".");
        }
    }

    // END: Section for manipulating the Net *
    public PetriNet getSubNetwork(Set<Place> places, Set<Transition> transitions, Set<Arc> arcs) {
        PetriNet subNetwork = new PetriNet();
        for (Place p : places) {
            subNetwork.addPlace(p);
        }
        for (Transition t : transitions) {
            subNetwork.addTransition(t);
        }
        for (Arc a : arcs) {
            if (a.source().getClass() == Place.class) {
                subNetwork.addArc((Place) a.source(), (Transition) a.aim(), a.weight());
            }
            if (a.source().getClass() == Transition.class) {
                subNetwork.addArc((Transition) a.source(), (Place) a.aim(), a.weight());
            }
        }
        return subNetwork;
    }
    /*
    private void writeObject(ObjectOutputStream objectOutput) throws IOException {
        map = layout.persist();
        objectOutput.defaultWriteObject();
    }

    private void readObject(ObjectInputStream objectInput) throws IOException, ClassNotFoundException {
        System.out.println(map);
        objectInput.defaultReadObject();

        System.out.println(map);
        this.layout = new MonaLisaLayout<>(new FRLayout<>(g));
        this.layout.setSize(new Dimension(1024 * 2, 768 * 2));
        // Happens, if the user try to load an older project format

        if (map != null) {
            this.layout.restore(map);
        }
    }
     */
}
