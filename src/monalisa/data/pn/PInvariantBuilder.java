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
 * <p>A builder class for {@link PInvariant}s.
 * Builder classes have a {@link PetriNet} associated with them to allow the
 * link between transition identifiers and actual transitions in the net.</p>
 * <p>The builder class supports a fluent interface, meaning it can be used as
 * follows:</p>
 * <pre>PInvariantBuilder builder = new PInvariantBuilder();
 *{@link PInvariant} inv = builder.setId(5)
 *                        .add(1, 4)
 *                        .add(2, 3)
 *                        .build();</pre>
 * </pre>
 * @author Konrad Rudolph
 */
public final class PInvariantBuilder {
    private final PetriNetFacade petriNet;
    private Map<Place, Integer> places;
    private int id;
    
    /**
     * Create a new builder for the given Petri net and puts it in a ready
     * state.
     * @param petriNet A Petri net.
     * @see #clear()
     * @see #isEmpty()
     */
    public PInvariantBuilder(PetriNetFacade petriNet) {
        this.petriNet = petriNet;
        clear();
    }
    
    /**
     * Set the identifier of the P-invariant.
     * Since the builder is agnostic of other existing invariants, it does not
     * check whether the identifier is really unique. This is the
     * responsibility of the user of this class.
     * @param id The (unique) identifier of the P-invariant.
     * @return Returns a reference to this builder instance.
     */
    public PInvariantBuilder setId(int id) {
        this.id = id;
        return this;
    }
    
    /**
     * Adds a transition, identified by a unique identifier, to the
     * P-invariant. If the factor of the transition is <code>0</code>, it is
     * not added to the P-invariant. Negative factors are invalid but are
     * <em>not</em> checked for.
     * @param transitionId The unique identifier of the transition.
     * @param factor The factor of the transition in the P-invariant vector.
     * @return Returns a reference to this builder instance.
     * @throws InvalidIdException Thrown when the transition identifier does
     *          not exist in the associated Petri net.
     */
    public PInvariantBuilder add(int placeId, int factor) {
        if (factor == 0)
            return this;
        
        Place place = petriNet.findPlace(placeId);
        if (place == null)
            throw new InvalidIdException(placeId);
        places.put(place, factor);
        return this;
    }
    
    /**
     * Adds a transition to the P-invariant. If the factor of the transition is
     * <code>0</code>, it is not added to the P-invariant. Negative factors are
     * invalid but are <em>not</em> checked for.
     * @param transition The transition.
     * @param factor The factor of the transition in the P-invariant vector.
     * @return Returns a reference to this builder instance.
     * @throws InvalidIdException Thrown when the transition identifier does
     *          not exist in the associated Petri net.
     * @see #add(int, int) add(int transitionId, int factor)
     */
    public PInvariantBuilder add(Place place, int factor) {
        return add(place.id(), factor);
    }
    
    /**
     * Returns whether the builder is currently empty, i.e. wheter it contains
     * no transition information. It ignores whether an ID for the P-invariant
     * has already been set.
     */
    public boolean isEmpty() {
        return places.isEmpty();
    }

    /**
     * Creates and returns a new P-invariant based on the information collected
     * in the builder. Subsequent calls to this method will create new
     * instances.
     * Subsequent changes to this builder instance are <em>not</em> carried
     * over to the P-invariant.
     * @return Returns the newly created {@link PInvariant} instance.
     */
    public PInvariant build() {
        return new PInvariant(id, new HashMap<>(places));
    }
    
    /**
     * Clears the builder instance. After a call to this method its status is
     * as just after construction, and {@link #isEmpty()} will return
     * <code>true</code>.
     */
    public void clear() {
        places = new HashMap<>();
        id = -1;
    }

    /**
     * Return number of transitions in a T Invariant
     * @return
     */
    public int getSize() {
        return places.size();
    }

    /**
     * Creates and returns a new P-invariant based on the information collected
     * in the builder. Clear the builder afterwards so that it can be reused to
     * build a new P-invariant.
     * Calling this code is equivalent to the following:
     * <pre>PInvariant invariant = builder.build();
     *builder.clear();</pre>
     * @return Returns the newly created {@link PInvariant} instance.
     * @see #build()
     * @see #clear()
     */
    public PInvariant buildAndClear() {
        PInvariant ret = build();
        clear();
        return ret;
    }
}
