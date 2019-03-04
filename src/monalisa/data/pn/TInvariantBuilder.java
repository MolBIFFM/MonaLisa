/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.data.pn;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>A builder class for {@link TInvariant}s.
 * Builder classes have a {@link PetriNet} associated with them to allow the
 * link between transition identifiers and actual transitions in the net.</p>
 * <p>The builder class supports a fluent interface, meaning it can be used as
 * follows:</p>
 * <pre>TInvariantBuilder builder = new TInvariantBuilder();
 *{@link TInvariant} inv = builder.setId(5)
 *                        .add(1, 4)
 *                        .add(2, 3)
 *                        .build();</pre>
 * </pre>
 * @author Konrad Rudolph
 */
public final class TInvariantBuilder {
    private final PetriNetFacade petriNet;
    private Map<Transition, Integer> transitions;
    private int id;
    
    /**
     * Create a new builder for the given Petri net and puts it in a ready
     * state.
     * @param petriNet A Petri net.
     * @see #clear()
     * @see #isEmpty()
     */
    public TInvariantBuilder(PetriNetFacade petriNet) {
        this.petriNet = petriNet;
        clear();
    }
    
    /**
     * Set the identifier of the T-invariant.
     * Since the builder is agnostic of other existing invariants, it does not
     * check whether the identifier is really unique. This is the
     * responsibility of the user of this class.
     * @param id The (unique) identifier of the T-invariant.
     * @return Returns a reference to this builder instance.
     */
    public TInvariantBuilder setId(int id) {
        this.id = id;
        return this;
    }
    
    /**
     * Adds a transition, identified by a unique identifier, to the
     * T-invariant. If the factor of the transition is <code>0</code>, it is
     * not added to the T-invariant. Negative factors are invalid but are
     * <em>not</em> checked for.
     * @param transitionId The unique identifier of the transition.
     * @param factor The factor of the transition in the T-invariant vector.
     * @return Returns a reference to this builder instance.
     * @throws InvalidIdException Thrown when the transition identifier does
     *          not exist in the associated Petri net.
     */
    public TInvariantBuilder add(int transitionId, int factor) {
        if (factor == 0)
            return this;
        
        Transition transition = petriNet.findTransition(transitionId);
        if (transition == null)
            throw new InvalidIdException(transitionId);
        transitions.put(transition, factor);
        return this;
    }
    
    /**
     * Adds a transition to the T-invariant. If the factor of the transition is
     * <code>0</code>, it is not added to the T-invariant. Negative factors are
     * invalid but are <em>not</em> checked for.
     * @param transition The transition.
     * @param factor The factor of the transition in the T-invariant vector.
     * @return Returns a reference to this builder instance.
     * @throws InvalidIdException Thrown when the transition identifier does
     *          not exist in the associated Petri net.
     * @see #add(int, int) add(int transitionId, int factor)
     */
    public TInvariantBuilder add(Transition transition, int factor) {
        return add(transition.id(), factor);
    }
    
    /**
     * Returns whether the builder is currently empty, i.e. wheter it contains
     * no transition information. It ignores whether an ID for the T-invariant
     * has already been set.
     */
    public boolean isEmpty() {
        return transitions.isEmpty();
    }

    /**
     * Creates and returns a new T-invariant based on the information collected
     * in the builder. Subsequent calls to this method will create new
     * instances.
     * Subsequent changes to this builder instance are <em>not</em> carried
     * over to the T-invariant.
     * @return Returns the newly created {@link TInvariant} instance.
     */
    public TInvariant build() {
        return new TInvariant(id, new HashMap<>(transitions));
    }
    
    /**
     * Clears the builder instance. After a call to this method its status is
     * as just after construction, and {@link #isEmpty()} will return
     * <code>true</code>.
     */
    public void clear() {
        transitions = new HashMap<>();
        id = -1;
    }

    /**
     * Return number of transitions in a T Invariant
     * @return
     */
    public int getSize() {
        return transitions.size();
    }

    /**
     * Creates and returns a new T-invariant based on the information collected
     * in the builder. Clear the builder afterwards so that it can be reused to
     * build a new T-invariant.
     * Calling this code is equivalent to the following:
     * <pre>TInvariant invariant = builder.build();
     *builder.clear();</pre>
     * @return Returns the newly created {@link TInvariant} instance.
     * @see #build()
     * @see #clear()
     */
    public TInvariant buildAndClear() {
        TInvariant ret = build();
        clear();
        return ret;
    }
}
