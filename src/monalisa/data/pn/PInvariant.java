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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A P-Invariant in a Petri net. Use the {@link PInvariantBuilder} class to
 * create P-Invariants.
 *
 * @author Konrad Rudolph
 */
public class PInvariant implements Serializable, Set<Place> {

    private static final long serialVersionUID = -9192304162641653823L;

    private final int id;
    private final Map<Place, Integer> places;
    transient private List<Integer> placesVector;

    /**
     * <p>
     * Creates a new P-Invariant with an identifier and a list of weighted
     * transitions. The weight on each transition is its factor in the T-
     * invariant vector.</p>
     * <p>
     * <strong>Note:</strong> {@code transitions} must not contain transitions
     * with factor <code>0</code>!</p>
     * <p>
     * It is advised not to use this constructor directly. Instead, use the
     * {@link PInvariantBuilder} class to create instances of P-Invariants.</p>
     *
     * @param id The identifier of the P-Invariant. It should be unique for each
     * Petri net.
     * @param transitions A mapping of transitions and weights.
     */
    public PInvariant(int id, Map<Place, Integer> places) {
        if (places == null) {
            throw new NullPointerException("places");
        }
        this.id = id;
        this.places = Collections.unmodifiableMap(places);
    }

    /**
     * Returns the numeric identifier of the P-Invariant.
     */
    public int id() {
        return id;
    }

    /**
     * Returns the set of transitions belonging to the P-Invariant, i.e. all
     * transitions with a factor greater than 0.
     */
    public Set<Place> places() {
        return places.keySet();
    }

    /**
     * Returns a vector of the transition factors in this P-Invariant, over all
     * transitions of the associated Petri net, sorted by their ID. Transitions
     * not occurring in the P-Invariant have factor 0.
     *
     * @param sortedTransitions The sorted transitions.
     * @return Returns a list of the transition factors.
     * @see #asVector()
     */
    public List<Integer> asVector(List<Place> sortedPlaces) {
        if (placesVector == null) {
            List<Integer> ret = new ArrayList<>(sortedPlaces.size());

            for (Place place : sortedPlaces) {
                ret.add(factor(place));
            }
            placesVector = Collections.unmodifiableList(ret);
        }
        return placesVector;
    }

    /**
     * Returns a vector of the transition factors in this P-Invariant, over all
     * transitions of the associated Petri net, sorted by their ID. Transitions
     * not occurring in the P-Invariant have factor 0.
     *
     * @return Returns a list of transition factors.
     * @see #asVector(List)
     */
    public List<Integer> asVector() {
        if (placesVector == null) {
            // Extract Petri net information and sort all transitions.

            Iterator<Place> it = places.keySet().iterator();
            List<Place> sortedPlaces;
            PetriNet petriNet;
            if (it.hasNext()) {
                petriNet = it.next().container();
                sortedPlaces = new ArrayList<>(petriNet.places());
                Collections.sort(sortedPlaces);
                placesVector = asVector(sortedPlaces);
            } else {
                placesVector = Collections.unmodifiableList(new ArrayList<Integer>());
            }
        }

        return placesVector;
    }

    /**
     * Returns the factor associated with a transition in the P-Invariant.
     *
     * @param transition The transition.
     * @return The factor. <code>0</code> if the transition does not occur in
     * the P-Invariant.
     */
    public int factor(Place place) {
        Integer ret = places.get(place);
        return ret == null ? 0 : ret;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ ");
        ret.append(id);
        ret.append(": ");

        boolean first = true;
        for (Map.Entry<Place, Integer> entry : places.entrySet()) {
            if (first) {
                first = false;
            } else {
                ret.append(", ");
            }
            if (entry.getValue() != 1) {
                ret.append(entry.getValue());
                ret.append(" * ");
            }
            ret.append("T");
            ret.append(entry.getKey().id());
        }
        if (places.size() > 0) {
            ret.append(" ");
        }
        ret.append("]");
        return ret.toString();
    }

    public boolean add(Place o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Place> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        return places().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return places().containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return places().isEmpty();
    }

    @Override
    public Iterator<Place> iterator() {
        return places().iterator();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return places().size();
    }

    @Override
    public Object[] toArray() {
        return places().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return places().toArray(a);
    }
}
