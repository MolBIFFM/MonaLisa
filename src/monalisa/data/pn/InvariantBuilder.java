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

import java.util.HashMap;
import java.util.Map;

public final class InvariantBuilder {
    private final PetriNetFacade petriNet;
    private Map<Transition, Integer> transitions;
    private Map<Place, Integer> places;
    private int id;
    private final String InvType;
    
    /**
     * Create a new builder for the given Petri net and puts it in a ready
     * state.
     * @param petriNet A Petri net.
     * @see #clear()
     * @see #isEmpty()
     */
    public InvariantBuilder(PetriNetFacade petriNet, String InvType) {
        this.petriNet = petriNet;
        this.InvType = InvType;
        clear();
    }
    
    /**
     * Set the identifier of the Invariant.
     * Since the builder is agnostic of other existing invariants, it does not
     * check whether the identifier is really unique. This is the
     * responsibility of the user of this class.
     * @param id The (unique) identifier of the Invariant.
     * @return Returns a reference to this builder instance.
     */
    public InvariantBuilder setId(int id) {
        this.id = id;
        return this;
    }
    
    /**
     * Adds a transition or place, identified by a unique identifier, to the
     * Invariant. If the factor of the transition is <code>0</code>, it is
     * not added to the Invariant. Negative factors are invalid but are
     * <em>not</em> checked for.
     * @param ObjectId The unique identifier of the Object.
     * @param factor The factor of the transition or place in the Invariant vector.
     * @return Returns a reference to this builder instance.
     * @throws InvalidIdException Thrown when the transition identifier does
     *          not exist in the associated Petri net.
     */
    public InvariantBuilder add(int ObjectId, int factor) {
        if (factor == 0)
            return this;
        if(InvType.equals("PI")){
            Place place = petriNet.findPlace(ObjectId);
            if (place == null)
                throw new InvalidIdException(ObjectId);
            places.put(place, factor);
            return this;
        }else{
            Transition transition = petriNet.findTransition(ObjectId);
            if (transition == null)
                throw new InvalidIdException(ObjectId);
            transitions.put(transition, factor);
            return this;
        }
    }
    
    /**
     * Adds a transition or place to the Invariant. If the factor of the transition is
     * <code>0</code>, it is not added to the Invariant. Negative factors are
     * invalid but are <em>not</em> checked for.
     * @param transition The transition.
     * @param factor The factor of the transition in the Invariant vector.
     * @return Returns a reference to this builder instance.
     * @throws InvalidIdException Thrown when the transition identifier does
     *          not exist in the associated Petri net.
     * @see #add(int, int) add(int transitionId, int factor)
     */
    public InvariantBuilder add(Transition transition, int factor) {
        return add(transition.id(), factor);
    }
    
    public InvariantBuilder add(Place place, int factor) {
        return add(place.id(), factor);
    }
    
    /**
     * Returns whether the builder is currently empty, i.e. wheter it contains
     * no transition or place information. It ignores whether an ID for the Invariant
     * has already been set.
     */
    public boolean isEmpty() {
        if(InvType.equals("PI")){
            return places.isEmpty();
        }else{
            return transitions.isEmpty();
        }
    }

    /**
     * Creates and returns a new Invariant based on the information collected
     * in the builder. Subsequent calls to this method will create new
     * instances.
     * Subsequent changes to this builder instance are <em>not</em> carried
     * over to the Invariant.
     * @return Returns the newly created Invariant instance.
     */
    public <T> T build() {
        if(InvType.equals("MI")){
            return (T)new MInvariant(id, new HashMap<>(transitions));
        }
        else if(InvType.equals("PI")){
            return (T)new PInvariant(id, new HashMap<>(places));
        }
        else{
            return (T)new TInvariant(id, new HashMap<>(transitions));
        }
    }
    
    /**
     * Clears the builder instance. After a call to this method its status is
     * as just after construction, and {@link #isEmpty()} will return
     * <code>true</code>.
     */
    public void clear() {
        if(InvType.equals("PI"))
            places = new HashMap<>();
        else{
        transitions = new HashMap<>();
        }
        id = -1;
    }

    /**
     * Return number of transitions or places in a Invariant
     * @return
     */
    public int getSize() {
        if(InvType.equals("PI"))
            return places.size();
        else{
        return transitions.size();
        }
    }

    /**
     * Creates and returns a new Invariant based on the information collected
     * in the builder. Clear the builder afterwards so that it can be reused to
     * build a new Invariant.
     * @return Returns the newly created Invariant instance.
     * @see #build()
     * @see #clear()
     */
    public <T> T buildAndClear() {
        if(InvType.equals("MI")){
            MInvariant ret = build();
            clear();
            return (T)ret;
        }
        else if(InvType.equals("PI")){
            PInvariant ret = build();
            clear();
            return (T)ret;
        }
        else{
            TInvariant ret = build();
            clear();
            return (T)ret;
        }
    }
}
