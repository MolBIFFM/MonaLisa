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

public class MInvariant implements Serializable, Set<Transition> {

    private static final long serialVersionUID = -9192304162641653823L;

    private final int id;
    private final Map<Transition, Integer> transitions;
    transient private List<Integer> transitionsVector;

    /**
     * <p>
     * Creates a new M-invariant with an identifier and a list of weighted
     * transitions. The weight on each transition is its factor in the M-
     * invariant vector.</p>
     * <p>
     * <strong>Note:</strong> {@code transitions} must not contain transitions
     * with factor <code>0</code>!</p>
     * <p>
     * It is advised not to use this constructor directly. Instead, use the
     * {@link MInvariantBuilder} class to create instances of M-invariants.</p>
     *
     * @param id The identifier of the M-invariant. It should be unique for each
     * Petri net.
     * @param transitions A mapping of transitions and weights.
     */
    public MInvariant(int id, Map<Transition, Integer> transitions) {
        if (transitions == null) {
            throw new NullPointerException("transitions");
        }
        this.id = id;
        this.transitions = Collections.unmodifiableMap(transitions);
    }

    /**
     * Returns the numeric identifier of the M-invariant.
     */
    public int id() {
        return id;
    }

    /**
     * Returns the set of transitions belonging to the M-invariant, i.e. all
     * transitions with a factor greater than 0.
     */
    public Set<Transition> transitions() {
        return transitions.keySet();
    }

    /**
     * Returns a vector of the transition factors in this M-invariant, over all
     * transitions of the associated Petri net, sorted by their ID. Transitions
     * not occurring in the M-invariant have factor 0.
     *
     * @param sortedTransitions The sorted transitions.
     * @return Returns a list of the transition factors.
     * @see #asVector()
     */
    public List<Integer> asVector(List<Transition> sortedTransitions) {
        if (transitionsVector == null) {
            List<Integer> ret = new ArrayList<>(sortedTransitions.size());

            for (Transition transition : sortedTransitions) {
                ret.add(factor(transition));
            }
            transitionsVector = Collections.unmodifiableList(ret);
        }
        return transitionsVector;
    }

    /**
     * Returns a vector of the transition factors in this M-invariant, over all
     * transitions of the associated Petri net, sorted by their ID. Transitions
     * not occurring in the T-invariant have factor 0.
     *
     * @return Returns a list of transition factors.
     * @see #asVector(List)
     */
    public List<Integer> asVector() {
        if (transitionsVector == null) {
            // Extract Petri net information and sort all transitions.

            Iterator<Transition> it = transitions.keySet().iterator();
            List<Transition> sortedTransitions;
            PetriNet petriNet;
            if (it.hasNext()) {
                petriNet = it.next().container();
                sortedTransitions = new ArrayList<>(petriNet.transitions());
                Collections.sort(sortedTransitions);
                transitionsVector = asVector(sortedTransitions);
            } else {
                transitionsVector = Collections.unmodifiableList(new ArrayList<Integer>());
            }
        }

        return transitionsVector;
    }

    /**
     * Returns the factor associated with a transition in the M-invariant.
     *
     * @param transition The transition.
     * @return The factor. <code>0</code> if the transition does not occur in
     * the M-invariant.
     */
    public int factor(Transition transition) {
        Integer ret = transitions.get(transition);
        return ret == null ? 0 : ret;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ ");
        ret.append(id);
        ret.append(": ");

        boolean first = true;
        for (Map.Entry<Transition, Integer> entry : transitions.entrySet()) {
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
        if (transitions.size() > 0) {
            ret.append(" ");
        }
        ret.append("]");
        return ret.toString();
    }

    @Override
    public boolean add(Transition o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Transition> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        return transitions().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return transitions().containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return transitions().isEmpty();
    }

    @Override
    public Iterator<Transition> iterator() {
        return transitions().iterator();
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
        return transitions().size();
    }

    @Override
    public Object[] toArray() {
        return transitions().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return transitions().toArray(a);
    }
}
