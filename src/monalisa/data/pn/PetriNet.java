/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.data.pn;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import monalisa.data.Pair;
import net.sourceforge.spargel.interfaces.bipartite.BidirectionalBipartiteGraph;
import net.sourceforge.spargel.interfaces.bipartite.BipartiteGraph;
import net.sourceforge.spargel.util.collections.collection.ConcatCollection;
import net.sourceforge.spargel.util.collections.set.TransformedSet;
import net.sourceforge.spargel.util.collections.transformer.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * A Petri net model.</p>
 * <p>
 * <strong>Note</strong>: For performance reasons, minimal sanity checks are
 * performed. As a consequence, many forbidden operations yield undefined
 * behavior rather than failing gracefully with an appropriate exception. As an
 * example, trying to retrieve the tokens of a non-existing place will yield an
 * undefined result, as will calling <code>addArc</code> etc.</p>
 * <p>
 * <strong>Attention!</strong> The API of this class is subject to change!
 * Future versions of this class will be immutable to change, a builder class
 * will provide functionality to build and change Petri nets. Do not rely on
 * this preliminary API in external projects.</p>
 *
 * @author Konrad Rudolph & Jens Einloft
 */
public final class PetriNet
        extends AbstractPetriNetEntity
        implements BidirectionalBipartiteGraph<Object, Arc, Place, Transition, Arc, Arc>,
        BipartiteGraph<Object, Arc, Place, Transition, Arc, Arc> {

    private static final long serialVersionUID = -8266249373834650762L;
    private Map<Integer, Place> places = new HashMap<>();
    private Map<Integer, Transition> transitions = new HashMap<>();
    private Map<Place, Long> marking = new HashMap<>();
    private Map<Place, List<Transition>> postTransitions = new HashMap<>();
    private Map<Transition, List<Place>> postPlaces = new HashMap<>();
    private Map<Pair<Place, Transition>, Arc> transitionInArcs = new HashMap<>();
    private Map<Pair<Transition, Place>, Arc> transitionOutArcs = new HashMap<>();
    // Inverse lookup tables.
    private final Map<Place, List<Transition>> preTransitions = new HashMap<>();
    private final Map<Transition, List<Place>> prePlaces = new HashMap<>();

    private static final Logger LOGGER = LogManager.getLogger(PetriNet.class);

    /**
     * Creates a new, empty Petri net.
     */
    public PetriNet() {
    }

    /**
     * Creates a copy of the specified Petri net. Performs a deep copy, i.e.
     * creates copies of all entities in the Petri net. Properties, on the other
     * hand, are shared between the entities of this Petri net and the other
     * Petri net since there is no generic way of copying properties efficiently
     * (i.e. without serialization).
     * <strong>Note:</strong> This behavior is subject to change since it may
     * prove impractical.
     *
     * @param other The Petri net to copy.
     */
    public PetriNet(PetriNet other) {
        super(other);
        LOGGER.debug("Creating new deep copy of a Petri net");

        for (Place oldPlace : other.places()) {
            Place newPlace = new Place(oldPlace);
            addPlace(newPlace);
            setTokens(newPlace, other.getTokens(oldPlace));
        }

        for (Transition oldTransition : other.transitions()) {
            Transition newTransition = new Transition(oldTransition);
            addTransition(newTransition);

            for (Place oldPlace : oldTransition.inputs()) {
                Place newPlace = findPlace(oldPlace.id());
                Arc newArc = new Arc(other.getArc(oldPlace, oldTransition));
                addArc(newPlace, newTransition, newArc);
            }

            for (Place oldPlace : oldTransition.outputs()) {
                Place newPlace = findPlace(oldPlace.id());
                Arc newArc = new Arc(other.getArc(oldTransition, oldPlace));
                addArc(newTransition, newPlace, newArc);
            }
        }
        LOGGER.debug("Successfully created deep copy of another Petri net");
    }

    /**
     * Add a new place to the Petri net.
     *
     * @param place The place.
     */
    public void addPlace(Place place) {
        LOGGER.debug("Adding place to Petri net");
        places.put(place.id(), place);
        place.setContainer(this);
        postTransitions.put(place, new ArrayList<Transition>());
        preTransitions.put(place, new ArrayList<Transition>());
        marking.put(place, 0L);
        LOGGER.debug("Successfully added place to Petri net");
    }

    /**
     * Add a list of new places to the Petri net.
     *
     * @param places The places.
     */
    public void addPlaces(Iterable<Place> places) {
        for (Place place : places) {
            addPlace(place);
        }
    }

    /**
     * Add a list of new places to the Petri net.
     *
     * @param places The places.
     */
    public void addPlaces(Place... places) {
        for (Place place : places) {
            addPlace(place);
        }
    }

    /**
     * Add a new transition to the Petri net.
     *
     * @param transition The transition.
     */
    public void addTransition(Transition transition) {
        LOGGER.debug("Adding transition to Petri net");
        transitions.put(transition.id(), transition);
        transition.setContainer(this);
        postPlaces.put(transition, new ArrayList<Place>());
        prePlaces.put(transition, new ArrayList<Place>());
        LOGGER.debug("Successfully added new transition to Petri net");
    }

    /**
     * Add a list of new transitions to the Petri net.
     *
     * @param transitions The transitions.
     */
    public void addTransitions(Iterable<Transition> transitions) {
        for (Transition transition : transitions) {
            addTransition(transition);
        }
    }

    /**
     * Add a list of new transitions to the Petri net.
     *
     * @param transitions The transitions.
     */
    public void addTransitions(Transition... transitions) {
        for (Transition transition : transitions) {
            addTransition(transition);
        }
    }

    /**
     * Add a new default arc from {@code from} to {@code to} the Petri net.
     * Corresponds to the following call:
     * <pre>addArc(from, to, new Arc());</pre>
     *
     * @param from The starting place.
     * @param to The target transition.
     */
    public void addArc(Place from, Transition to) {
        addArc(from, to, new Arc(from, to));
    }

    public void addArc(Place from, Transition to, int weight) {
        addArc(from, to, new Arc(from, to, weight));
    }

    /**
     * Add a new custom arc from {code from} to {@code to} the Petri net.
     *
     * @param from The starting place.
     * @param to The target transition.
     * @param arc The custom arc to add.
     */
    public void addArc(Place from, Transition to, Arc arc) {
        LOGGER.debug("Adding arc (P->T) to Petri net");
        postTransitions.get(from).add(to);
        transitionInArcs.put(Pair.of(from, to), arc);
        prePlaces.get(to).add(from);
        LOGGER.debug("Successfully added arc (P->T) to Petri net");
    }

    /**
     * Add a new default arc from {code from} to {@code to} the Petri net.
     * Corresponds to the following call:
     * <pre>addArc(from, to, new Arc());</pre>
     *
     * @param from The starting transition.
     * @param to The target place.
     */
    public void addArc(Transition from, Place to) {
        addArc(from, to, new Arc(from, to));
    }

    public void addArc(Transition from, Place to, int weight) {
        addArc(from, to, new Arc(from, to, weight));
    }

    /**
     * Add a new custom arc from {@code from} to {@code to} the Petri net.
     *
     * @param from The starting transition.
     * @param to The target place.
     * @param arc The custom arc to add.
     */
    public void addArc(Transition from, Place to, Arc arc) {
        LOGGER.debug("Adding arc (T->P) to Petri net");
        postPlaces.get(from).add(to);
        transitionOutArcs.put(Pair.of(from, to), arc);
        preTransitions.get(to).add(from);
        LOGGER.debug("Successfully added arc (T->P) to Petri net");
    }

    public void removeArc(Place source, Transition aim) {
        LOGGER.debug("Removing arc (P->T) from Petri net");
        if (postTransitions.containsKey(source)) {
            postTransitions.get(source).remove(aim);
        }
        transitionInArcs.remove(new Pair<>(source, aim));
        if (prePlaces.containsKey(aim)) {
            prePlaces.get(aim).remove(source);
        }
        LOGGER.debug("Successfully removed arc (P->T) from Petri net");
    }

    public void removeArc(Transition source, Place aim) {
        LOGGER.debug("Removing arc (T->P) from Petri net");
        if (postPlaces.containsKey(source)) {
            postPlaces.get(source).remove(aim);
        }
        transitionOutArcs.remove(new Pair<>(source, aim));
        if (preTransitions.containsKey(aim)) {
            preTransitions.get(aim).remove(source);
        }
        LOGGER.debug("Successfully removed arc (T->P) from Petri net");
    }

    public void removePlace(Place place) {
        LOGGER.debug("Removing place from Petri net");
        places.remove(place.id());
        postTransitions.remove(place);

        for (List<Place> p : postPlaces.values()) {
            p.remove(place);
        }

        Iterator<Pair<Place, Transition>> inarciter
                = transitionInArcs.keySet().iterator();
        while (inarciter.hasNext()) {
            if (inarciter.next().first().equals(place)) {
                inarciter.remove();
            }
        }

        Iterator<Pair<Transition, Place>> outarciter
                = transitionOutArcs.keySet().iterator();
        while (outarciter.hasNext()) {
            if (outarciter.next().second().equals(place)) {
                outarciter.remove();
            }
        }

        marking.remove(place);
        preTransitions.remove(place);

        for (List<Place> p : prePlaces.values()) {
            p.remove(place);
        }
        LOGGER.debug("Sucessfully removed place from Petri net");
    }

    public void removeTransition(Transition transition) {
        LOGGER.debug("Removing transition from Petri net");
        transitions.remove(transition.id());
        postPlaces.remove(transition);

        for (List<Transition> t : postTransitions.values()) {
            t.remove(transition);
        }

        Iterator<Pair<Place, Transition>> inarciter = transitionInArcs.keySet().iterator();
        while (inarciter.hasNext()) {
            if (inarciter.next().second().equals(transition)) {
                inarciter.remove();
            }
        }

        Iterator<Pair<Transition, Place>> outarciter = transitionOutArcs.keySet().iterator();
        while (outarciter.hasNext()) {
            if (outarciter.next().first().equals(transition)) {
                outarciter.remove();
            }
        }

        prePlaces.remove(transition);

        for (List<Transition> t : preTransitions.values()) {
            t.remove(transition);
        }
        LOGGER.debug("Successfully removed transition from Petri net");
    }

    /**
     * Returns the token count associated with a place. Default is 0.
     *
     * @param place The place.
     * @return A nonnegative integer.
     */
    public Long getTokens(Place place) {
        return marking.get(place);
    }

    /**
     * Change the token count of a place in the Petri net.
     *
     * @param place The place.
     * @param tokens A nonnegative integer indicating the new token count.
     */
    public void setTokens(Place place, Long tokens) {
        marking.put(place, tokens);
    }

    /**
     * Returns all places in the Petri net.
     *
     * @return A read-only list of places.
     */
    public Collection<Place> places() {
        return Collections.unmodifiableCollection(places.values());
    }

    /**
     * Returns all transitions in the Petri net.
     *
     * @return A read-only list of transitions.
     */
    public Collection<Transition> transitions() {
        return Collections.unmodifiableCollection(transitions.values());
    }

    /**
     * Searches the Petri net for a place with the given ID.
     *
     * @param id The ID to search for.
     * @return Returns the {@link Place} instance if the ID was found,
     * <code>null</code> otherwise.
     */
    public Place findPlace(int id) {
        return places.get(id);
    }

    /**
     * Searches the Petri net for a transition with the given ID.
     *
     * @param id The ID to search for.
     * @return Returns the {@link Transition} instance if the ID was found,
     * <code>null</code> otherwise.
     */
    public Transition findTransition(int id) {
        return transitions.get(id);
    }

    /**
     * Returns the (initial) marking of the Petri net.
     *
     * @return A map that associates an integer counter with each place to
     * denote its token count. Each place in the Petri net has an associated
     * counter, even if it is 0.
     */
    public Map<Place, Long> marking() {
        return Collections.unmodifiableMap(marking);
    }

    /**
     * Returns all transitions directly reachable from place {@code from}, i.e.
     * all transitions to which {code from} is an <em>input place</em>.
     *
     * @param from The starting place.
     * @return A read-only list of transitions.
     */
    public List<Transition> getTransitionsFor(Place from) {
        return Collections.unmodifiableList(postTransitions.get(from));
    }

    /**
     * Returns all <em>output places</em> of transition {code from}.
     *
     * @param from The starting transition.
     * @return A read-only list of places.
     */
    public List<Place> getPlacesFor(Transition from) {
        return Collections.unmodifiableList(postPlaces.get(from));
    }

    /**
     * Returns all transitions for which {@code to} is an output place.
     *
     * @param to The output place.
     * @return A read-only list of transitions.
     */
    public List<Transition> getInputTransitionsFor(Place to) {
        return Collections.unmodifiableList(preTransitions.get(to));
    }

    /**
     * Returns all input places to transition {@code to}.
     *
     * @param to The output transition.
     * @return A read-only list of places.
     */
    public List<Place> getInputPlacesFor(Transition to) {
        return Collections.unmodifiableList(prePlaces.get(to));
    }

    /**
     * Retrieves an input arc, identified by the associated place and
     * transition.
     *
     * @param from A place.
     * @param to A transition.
     * @return The arc from {@code from} to {@code to}.
     */
    public Arc getArc(Place from, Transition to) {
        return transitionInArcs.get(Pair.of(from, to));
    }

    /**
     * Retrieves an output arc, identified by the associated transition and
     * place.
     *
     * @param from A transition.
     * @param to A place.
     * @return The arc from {@code from} to {@code to}.
     */
    public Arc getArc(Transition from, Place to) {
        return transitionOutArcs.get(Pair.of(from, to));
    }

    /**
     * Returns the number of edges of the PetriNet.
     *
     * @return
     */
    public int getNumberOfEdges() {
        return transitionInArcs.size() + transitionOutArcs.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        places = (Map<Integer, Place>) in.readObject();
        transitions = (Map<Integer, Transition>) in.readObject();
        postTransitions = (Map<Place, List<Transition>>) in.readObject();
        postPlaces = (Map<Transition, List<Place>>) in.readObject();
        transitionInArcs = (Map<Pair<Place, Transition>, Arc>) in.readObject();
        transitionOutArcs = (Map<Pair<Transition, Place>, Arc>) in.readObject();
        marking = (Map<Place, Long>) in.readObject();

        // Revive all Petri net components by setting their container.
        // At the same time, initialize the inverse lookup tables to save one
        // loop.
        for (Place p : places()) {
            p.setContainer(this);
            preTransitions.put(p, new ArrayList<Transition>());
        }
        for (Transition t : transitions()) {
            t.setContainer(this);
            prePlaces.put(t, new ArrayList<Place>());
        }

        // Build up inverse lookup tables.
        // CONSIDER: This may be slow.
        for (Map.Entry<Place, List<Transition>> entry : postTransitions.entrySet()) {
            for (Transition transition : entry.getValue()) {
                prePlaces.get(transition).add(entry.getKey());
            }
        }

        for (Map.Entry<Transition, List<Place>> entry : postPlaces.entrySet()) {
            for (Place place : entry.getValue()) {
                preTransitions.get(place).add(entry.getKey());
            }
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(places);
        out.writeObject(transitions);
        out.writeObject(postTransitions);
        out.writeObject(postPlaces);
        out.writeObject(transitionInArcs);
        out.writeObject(transitionOutArcs);
        out.writeObject(marking);
    }

    // Spargel bridging stuff **************************************************
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getRInEdges(final Transition rv) {
        final Set<Place> predecessors = new AbstractSet<Place>() {
            @Override
            public Iterator<Place> iterator() {
                return getInputPlacesFor(rv).iterator();
            }

            @Override
            public int size() {
                return getInputPlacesFor(rv).size();
            }
        };
        return new TransformedSet<Place, Arc>(predecessors, new Transformer<Place, Arc>() {
            @Override
            public Arc transform(Place k) {
                return getArc(k, rv);
            }
        }) {
            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                return a.aim().equals(rv) && containsEdge(a.source(), a.aim());
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                if (!contains(a)) {
                    return false;
                }
                if (!(a.source() instanceof Place) || !(a.aim() instanceof Transition)) {
                    removeArc((Place) a.source(), (Transition) a.aim());
                    return true;
                } else if (!(a.source() instanceof Transition) || !(a.aim() instanceof Place)) {
                    removeArc((Transition) a.source(), (Place) a.aim());
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getLInEdges(final Place lv) {
        final Set<Transition> predecessors = new AbstractSet<Transition>() {
            @Override
            public Iterator<Transition> iterator() {
                return getInputTransitionsFor(lv).iterator();
            }

            @Override
            public int size() {
                return getInputTransitionsFor(lv).size();
            }
        };
        return new TransformedSet<Transition, Arc>(predecessors,
                new Transformer<Transition, Arc>() {
            @Override
            public Arc transform(Transition k) {
                return getArc(k, lv);
            }
        }) {
            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                return a.aim().equals(lv) && containsEdge(a.source(), a.aim());
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                if (!contains(a)) {
                    return false;
                }
                if (!(a.source() instanceof Place)
                        || !(a.aim() instanceof Transition)) {
                    removeArc((Place) a.source(), (Transition) a.aim());
                    return true;
                } else if (!(a.source() instanceof Transition)
                        || !(a.aim() instanceof Place)) {
                    removeArc((Transition) a.source(), (Place) a.aim());
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Place getLRStart(Arc lre) {
        if (lre.source() instanceof Place) {
            return (Place) lre.source();
        }
        throw new IllegalArgumentException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transition getRLStart(Arc rle) {
        if (rle.source() instanceof Transition) {
            return (Transition) rle.source();
        }
        throw new IllegalArgumentException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transition getLREnd(Arc lre) {
        if (lre.aim() instanceof Transition) {
            return (Transition) lre.aim();
        }
        throw new IllegalArgumentException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Place getRLEnd(Arc rle) {
        if (rle.aim() instanceof Place) {
            return (Place) rle.aim();
        }
        throw new IllegalArgumentException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getLOutEdges(final Place lv) {
        final Set<Transition> successors = new AbstractSet<Transition>() {
            @Override
            public Iterator<Transition> iterator() {
                return getTransitionsFor(lv).iterator();
            }

            @Override
            public int size() {
                return getTransitionsFor(lv).size();
            }
        };
        return new TransformedSet<Transition, Arc>(successors,
                new Transformer<Transition, Arc>() {
            @Override
            public Arc transform(Transition k) {
                return getArc(lv, k);
            }
        }) {
            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                return a.source().equals(lv) && containsEdge(a.source(), a.aim());
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                if (!contains(a)) {
                    return false;
                }
                if (!(a.source() instanceof Place)
                        || !(a.aim() instanceof Transition)) {
                    removeArc((Place) a.source(), (Transition) a.aim());
                    return true;
                } else if (!(a.source() instanceof Transition)
                        || !(a.aim() instanceof Place)) {
                    removeArc((Transition) a.source(), (Place) a.aim());
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getROutEdges(final Transition lv) {
        final Set<Place> successors = new AbstractSet<Place>() {
            @Override
            public Iterator<Place> iterator() {
                return getPlacesFor(lv).iterator();
            }

            @Override
            public int size() {
                return getPlacesFor(lv).size();
            }
        };
        return new TransformedSet<Place, Arc>(successors, new Transformer<Place, Arc>() {
            @Override
            public Arc transform(Place k) {
                return getArc(lv, k);
            }
        }) {
            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                return a.source().equals(lv) && containsEdge(a.source(), a.aim());
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                if (!contains(a)) {
                    return false;
                }
                if (!(a.source() instanceof Place) || !(a.aim() instanceof Transition)) {
                    removeArc((Place) a.source(), (Transition) a.aim());
                    return true;
                } else if (!(a.source() instanceof Transition) || !(a.aim() instanceof Place)) {
                    removeArc((Transition) a.source(), (Place) a.aim());
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Place> getLVertices() {
        return new AbstractSet<Place>() {
            @Override
            public Iterator<Place> iterator() {
                return places().iterator();
            }

            @Override
            public int size() {
                return places().size();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Place)) {
                    return false;
                }
                Place p = (Place) o;
                return places.containsKey(p.id());
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Place)) {
                    return false;
                }
                Place p = (Place) o;
                if (!contains(p)) {
                    return false;
                }
                removePlace(p);
                return true;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Transition> getRVertices() {
        return new AbstractSet<Transition>() {
            @Override
            public Iterator<Transition> iterator() {
                return transitions().iterator();
            }

            @Override
            public int size() {
                return transitions().size();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Transition)) {
                    return false;
                }
                Transition t = (Transition) o;
                return transitions.containsKey(t.id());
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Transition)) {
                    return false;
                }
                Transition t = (Transition) o;
                if (!contains(t)) {
                    return false;
                }
                removeTransition(t);
                return true;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getLREdges() {
        return new AbstractSet<Arc>() {
            @Override
            public Iterator<Arc> iterator() {
                return transitionInArcs.values().iterator();
            }

            @Override
            public int size() {
                return transitionInArcs.values().size();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                final Arc a = (Arc) o;
                if (a.source() instanceof Place
                        && a.aim() instanceof Transition) {
                    return transitionInArcs.containsKey(
                            new Pair<>((Place) a.source(), (Transition) a.aim()));
                }
                return false;
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                final Arc a = (Arc) o;
                if (contains(a)) {
                    return false;
                }
                removeArc((Place) a.source(), (Transition) a.aim());
                return true;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getRLEdges() {
        return new AbstractSet<Arc>() {
            @Override
            public Iterator<Arc> iterator() {
                return transitionOutArcs.values().iterator();
            }

            @Override
            public int size() {
                return transitionOutArcs.values().size();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                if (a.source() instanceof Transition
                        && a.aim() instanceof Place) {
                    return transitionOutArcs.containsKey(
                            new Pair<>((Transition) a.source(), (Place) a.aim()));
                }
                return false;
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                final Arc a = (Arc) o;
                if (contains(a)) {
                    return false;
                }
                removeArc((Transition) a.source(), (Place) a.aim());
                return true;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Arc getLREdge(Place lv, Transition rv) {
        return getArc(lv, rv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Arc getRLEdge(Transition rv, Place lv) {
        return getArc(rv, lv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLREdge(Place lv, Transition rv) {
        return getArc(lv, rv) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsRLEdge(Transition rv, Place lv) {
        return getArc(rv, lv) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getLREdges(Place lv, Transition rv) {
        if (containsLREdge(lv, rv)) {
            return Collections.singleton(getLREdge(lv, rv));
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getRLEdges(Transition rv, Place lv) {
        if (containsRLEdge(rv, lv)) {
            return Collections.singleton(getRLEdge(rv, lv));
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Map<Place, T> createLVertexMap() {
        return new WeakHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Map<Transition, T> createRVertexMap() {
        return new WeakHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Map<Arc, T> createLREdgeMap() {
        return new WeakHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Map<Arc, T> createRLEdgeMap() {
        return new WeakHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getOutEdges(Object v) {
        if (v instanceof Place) {
            return getLOutEdges((Place) v);
        } else if (v instanceof Transition) {
            return getOutEdges((Transition) v);
        }
        throw new IllegalArgumentException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getStart(Arc e) {
        return e.source();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getEnd(Arc e) {
        return e.aim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Object> getVertices() {
        class VertexSet
                extends ConcatCollection<Object>
                implements Set<Object> {

            public VertexSet() {
                super(getLVertices(), getRVertices());
            }

            @Override
            public boolean contains(Object o) {
                if (o instanceof Place) {
                    return getLVertices().contains(o);
                } else if (o instanceof Transition) {
                    return getRVertices().contains(o);
                } else {
                    return false;
                }
            }
        }
        return new VertexSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getEdges() {
        class EdgeSet
                extends ConcatCollection<Arc>
                implements Set<Arc> {

            EdgeSet() {
                super(getLREdges(), getLREdges());
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Arc)) {
                    return false;
                }
                Arc a = (Arc) o;
                return containsEdge(a.aim(), a.source());
            }
        }
        return new EdgeSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Arc getEdge(Object v, Object v1) {
        if (v instanceof Place && v1 instanceof Transition) {
            return getArc((Place) v, (Transition) v1);
        } else if (v instanceof Transition && v1 instanceof Place) {
            return getArc((Transition) v, (Place) v1);
        }
        throw new IllegalArgumentException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsEdge(Object v, Object v1) {
        if (v instanceof Transition && v1 instanceof Place) {
            return containsRLEdge((Transition) v, (Place) v1);
        } else if (v instanceof Place && v1 instanceof Transition) {
            return containsLREdge((Place) v, (Transition) v1);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getEdges(Object v, Object v1) {
        if (containsEdge(v, v1)) {
            return Collections.singleton(getEdge(v, v1));
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Map<Object, T> createVertexMap() {
        return new WeakHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Map<Arc, T> createEdgeMap() {
        return new WeakHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Arc> getInEdges(Object v) {
        if (v instanceof Place) {
            return getLInEdges((Place) v);
        } else if (v instanceof Transition) {
            return getRInEdges((Transition) v);
        }
        throw new IllegalArgumentException();
    }
}
